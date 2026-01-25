package com.example.luca.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.luca.ui.theme.*
import com.example.luca.util.AvatarUtils
import com.example.luca.viewmodel.DeleteState
import com.example.luca.viewmodel.DetailedEventViewModel
import com.example.luca.viewmodel.UIActivityState
import com.example.luca.viewmodel.UIEventState
import com.example.luca.ui.components.*

@Composable
fun DetailedEventScreen(
    eventId: String,
    viewModel: DetailedEventViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onNavigateToAddActivity: () -> Unit = {},
    onNavigateToEditEvent: (String) -> Unit = {}
) {
    LaunchedEffect(eventId) {
        viewModel.loadEventData(eventId)
    }

    val eventState by viewModel.uiEvent.collectAsState()
    val activitiesState by viewModel.uiActivities.collectAsState()
    val deleteState by viewModel.deleteState.collectAsState()
    val isEmpty = activitiesState.isEmpty()

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(deleteState) {
        if (deleteState is DeleteState.Success) {
            showDeleteDialog = false
            onBackClick() // Kembali ke Home setelah delete
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UIAccentYellow)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallCircleButton(Icons.Default.ArrowBack, Modifier.clickable { onBackClick() })
            Spacer(modifier = Modifier.weight(1f))
            SmallCircleButton(Icons.Default.MoreVert)
        }

        // Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(UIBackground)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // CARD EVENT UTAMA (DATA ASLI)
                    FigmaEventCard(
                        event = eventState,
                        onEditClick = { onNavigateToEditEvent(eventId) },
                        onDeleteClick = { showDeleteDialog = true },
                        onCloseClick = onBackClick // Tombol X menutup screen
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    FigmaSearchBar()
                    Spacer(modifier = Modifier.height(16.dp))

                    ActivitySection(
                        activities = activitiesState,
                        isEmpty = isEmpty,
                        onNavigateToAddActivity = onNavigateToAddActivity
                    )
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }

            if (!isEmpty) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Brush.verticalGradient(colors = listOf(Color.Transparent, UIBackground)))
                )
            }

            BottomActionArea(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                isEmpty = isEmpty,
                onAddActivityClick = onNavigateToAddActivity
            )
        }
    }

    if (showDeleteDialog) {
        val isLoading = deleteState is DeleteState.Loading
        val errorMsg = (deleteState as? DeleteState.Error)?.message

        PasswordConfirmationDialog(
            onDismiss = {
                showDeleteDialog = false
                viewModel.resetDeleteState()
            },
            onConfirm = { pass -> viewModel.deleteEventWithPassword(eventId, pass) },
            isLoading = isLoading,
            errorMessage = errorMsg
        )
    }
}

@Composable
fun FigmaEventCard(
    event: UIEventState,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onCloseClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = UIWhite),
        modifier = Modifier.fillMaxWidth().height(280.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Gambar Background
            if (event.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = "Event Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(200.dp).align(Alignment.TopCenter)
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp).background(Color.Gray).align(Alignment.TopCenter),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.DateRange, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                }
            }

            // Tombol Overlay (Close, Edit, Delete)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter)
            ) {
                // Tombol X (Close Screen)
                SmallCircleButton(Icons.Default.Close, Modifier.align(Alignment.TopStart).clickable { onCloseClick() })

                Row(modifier = Modifier.align(Alignment.TopEnd)) {
                    SmallCircleButton(Icons.Default.Edit, Modifier.clickable { onEditClick() })
                    Spacer(modifier = Modifier.width(12.dp))
                    SmallCircleButton(Icons.Default.Delete, Modifier.clickable { onDeleteClick() })
                }
            }

            // Footer (Title, Location, Date)
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(90.dp),
                shape = RoundedCornerShape(topStart = 40.dp),
                color = UIWhite
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp)) {
                    Column(
                        modifier = Modifier.align(Alignment.CenterStart),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(event.title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = UIBlack, style = AppFont.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = UIAccentYellow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(event.location, color = UIDarkGrey, fontSize = 12.sp, style = AppFont.Regular)
                        }
                    }
                    Text(
                        text = event.date,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = UIBlack,
                        style = AppFont.Bold,
                        modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 4.dp)
                    )
                }
            }

            // Avatar Participants (Floating)
            Row(
                modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-20).dp, y = (-70).dp),
                horizontalArrangement = Arrangement.spacedBy((-12).dp)
            ) {
                val displayAvatars = event.participantAvatars.take(3)
                val remainingCount = event.participantAvatars.size - 3

                displayAvatars.forEach { avatarName ->
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = AvatarUtils.getAvatarResId(avatarName)),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(36.dp).clip(CircleShape).border(2.dp, UIWhite, CircleShape).background(Color.LightGray)
                    )
                }

                if (remainingCount > 0) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).border(2.dp, UIWhite, CircleShape).background(Color(0xFF333333)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+$remainingCount", color = UIWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PasswordConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?
) {
    var password by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = UIWhite),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Delete Event", style = AppFont.Bold, fontSize = 20.sp, color = UIAccentRed)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Enter password to confirm deletion.", style = AppFont.Regular, color = UIDarkGrey, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(errorMessage, color = UIAccentRed, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = UIGrey), modifier = Modifier.weight(1f)) {
                        Text("Cancel", color = UIBlack)
                    }
                    Button(onClick = { onConfirm(password) }, colors = ButtonDefaults.buttonColors(containerColor = UIAccentRed), modifier = Modifier.weight(1f)) {
                        if (isLoading) CircularProgressIndicator(color = UIWhite, modifier = Modifier.size(20.dp)) else Text("Delete", color = UIWhite)
                    }
                }
            }
        }
    }
}

@Composable
fun ActivitySection(
    activities: List<UIActivityState>,
    isEmpty: Boolean,
    onNavigateToAddActivity: () -> Unit = {}
) {
    if (isEmpty) {
        EmptyStateMessage(onNavigateToAddActivity = onNavigateToAddActivity)
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(activities) { activity ->
                ActivityItemCard(item = activity)
            }
        }
    }
}

@Composable
fun ActivityItemCard(item: UIActivityState) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = UIWhite,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(item.iconColor.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ShoppingCart, null, tint = UIBlack, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = UIBlack, style = AppFont.Bold)
                Text(item.payer, fontSize = 12.sp, color = UIDarkGrey, style = AppFont.Medium)
            }
            Text(item.price, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = UIAccentRed, style = AppFont.Medium)
        }
    }
}

@Composable
fun BottomActionArea(
    modifier: Modifier = Modifier,
    isEmpty: Boolean,
    onAddActivityClick: () -> Unit = {}
) {
    Box(modifier = modifier.fillMaxWidth()) {
        if (isEmpty) {
            Column(
                modifier = Modifier.align(Alignment.BottomEnd),
                horizontalAlignment = Alignment.End
            ) {
                Text("Click here to add one! ↴", color = UIDarkGrey, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                FloatingAddButton(onClick = onAddActivityClick)
            }
        } else {
            Surface(
                modifier = Modifier.height(50.dp).width(160.dp).align(Alignment.Center),
                shape = RoundedCornerShape(30.dp),
                color = UIWhite,
                border = BorderStroke(2.dp, UIAccentYellow),
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Summarize", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = UIBlack)
                }
            }
            FloatingAddButton(modifier = Modifier.align(Alignment.CenterEnd), onClick = onAddActivityClick)
        }
    }
}

@Composable
fun FigmaSearchBar() {
    Surface(
        shape = RoundedCornerShape(25.dp),
        color = UIWhite,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().height(50.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
            Icon(Icons.Default.Search, null, tint = UIBlack, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("Search", color = UIDarkGrey, fontSize = 16.sp)
        }
    }
}

@Composable
fun SmallCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = UIWhite,
        modifier = modifier.size(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = UIBlack)
        }
    }
}

@Composable
fun FloatingAddButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier.size(60.dp).clickable { onClick() },
        shape = CircleShape,
        color = UIAccentYellow,
        shadowElevation = 6.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Add, null, tint = UIBlack, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun EmptyStateMessage(onNavigateToAddActivity: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Oops.", fontWeight = FontWeight.Bold, fontSize = 32.sp, color = UIDarkGrey)
        Spacer(modifier = Modifier.height(8.dp))
        Text("You haven't made any activities.", fontSize = 16.sp, color = UIDarkGrey)
    }
}