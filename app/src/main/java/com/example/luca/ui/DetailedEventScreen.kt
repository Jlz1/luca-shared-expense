package com.example.luca.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.luca.R
import com.example.luca.ui.theme.AppFont
import com.example.luca.ui.theme.LucaTheme
import com.example.luca.ui.theme.UIAccentRed
import com.example.luca.ui.theme.UIAccentYellow
import com.example.luca.ui.theme.UIBackground
import com.example.luca.ui.theme.UIBlack
import com.example.luca.ui.theme.UIDarkGrey
import com.example.luca.ui.theme.UIGrey
import com.example.luca.ui.theme.UIWhite
import com.example.luca.viewmodel.DeleteState
import com.example.luca.viewmodel.DetailedEventViewModel
import com.example.luca.viewmodel.UIActivityState
import com.example.luca.viewmodel.UIEventState

@Composable
fun DetailedEventScreen(
    eventId: String,
    viewModel: DetailedEventViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onNavigateToAddActivity: () -> Unit = {},
    onNavigateToEditEvent: (String) -> Unit = {},
    onNavigateToActivityDetail: (String) -> Unit = {}, // [BARU] Navigate ke NewActivityScreen2
    onNavigateToSummary: () -> Unit = {} // [BARU] Navigate ke SummaryScreen untuk split bill
) {
    // 1. Load Data
    LaunchedEffect(eventId) { viewModel.loadEventData(eventId) }

    // 2. Collect State
    val eventState by viewModel.uiEvent.collectAsState()
    val activitiesState by viewModel.uiActivities.collectAsState()
    val deleteState by viewModel.deleteState.collectAsState()

    // State lokal untuk dialog
    var showDeleteDialog by remember { mutableStateOf(false) }

    // 3. LOGIC FIX: Pantau perubahan deleteState di sini
    LaunchedEffect(deleteState) {
        if (deleteState is DeleteState.Success) {
            showDeleteDialog = false // Tutup dialog paksa
            onBackClick()            // Kembali ke halaman sebelumnya
            viewModel.resetDeleteState()
        }
    }

    // 4. Panggil UI Murni (DetailedEventContent)
    DetailedEventContent(
        eventState = eventState,
        activitiesState = activitiesState,
        onBackClick = onBackClick,
        onMenuClick = onMenuClick,
        onNavigateToAddActivity = onNavigateToAddActivity,
        onEditClick = { onNavigateToEditEvent(eventId) },
        onDeleteClick = { showDeleteDialog = true }, // Buka dialog saat diklik
        onActivityClick = onNavigateToActivityDetail
    )

    // 5. Tampilkan Dialog (Overlay)
    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onDismiss = {
                showDeleteDialog = false
                viewModel.resetDeleteState()
            },
            onConfirm = {
                viewModel.deleteEvent(eventId) // Panggil fungsi delete baru
            },
            isLoading = deleteState is DeleteState.Loading,
            errorMessage = (deleteState as? DeleteState.Error)?.message
        )
    }
}

// --- UI MURNI (Bisa di-Preview tanpa ViewModel) ---
@Composable
fun DetailedEventContent(
    eventState: UIEventState,
    activitiesState: List<UIActivityState>,
    onBackClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onNavigateToAddActivity: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onActivityClick: (String) -> Unit = {}
) {
    Column(modifier = Modifier
        .fillMaxSize()
        .background(UIBackground)
        .statusBarsPadding()) {
        // HEADER
        HeaderSection(
            currentState = HeaderState.HOME,
            onLeftIconClick = onMenuClick
        )

        // CONTENT
        Box(modifier = Modifier
            .weight(1f)
            .fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)) {
                    Spacer(Modifier.height(24.dp))
                    FigmaEventCard(
                        event = eventState,
                        onEditClick = onEditClick,
                        onDeleteClick = onDeleteClick,
                        onCloseClick = onBackClick
                    )
                    Spacer(Modifier.height(16.dp))
                    // Pastikan SearchBarModify tersedia atau ganti dengan Text sementara untuk preview
                    SearchBarModify(placeholder = "Search activities...", readOnly = false)
                    Spacer(Modifier.height(16.dp))

                    ActivitySection(
                        activities = activitiesState,
                        isEmpty = activitiesState.isEmpty(),
                        onNavigateToAddActivity = onNavigateToAddActivity,
                        onNavigateToActivityDetail = onActivityClick
                    )
                    Spacer(Modifier.height(120.dp))
                }
            }
            BottomActionArea(
                modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp),
                isEmpty = activitiesState.isEmpty(),
                onAddActivityClick = onNavigateToAddActivity,
                onSummaryClick = onNavigateToSummary
            )
        }
    }
}

@Composable
fun FigmaEventCard(
    event: UIEventState,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(horizontal = 0.dp, vertical = 8.dp)
    ) {
        // 1. Background Image Card
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(UIAccentYellow) // Fallback color
        ) {
            if (event.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = "Event Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Placeholder pattern or color if no image
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray))
            }
        }

        // 2. Exit Button (Top Left)
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier
                .padding(12.dp)
                .size(40.dp)
                .align(Alignment.TopStart)
        ) {
            // Gunakan resource drawable kamu, fallback ke Icon default jika error/belum ada
            // Image(painter = painterResource(id = R.drawable.ic_close_event), contentDescription = "Close")

            // UNTUK SEMENTARA SAYA PAKAI SURFACE + ICON AGAR AMAN JIKA DRAWABLE BELUM ADA
            Surface(
                shape = CircleShape,
                color = UIWhite.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close_event),
                    contentDescription = "Close",
                    modifier = Modifier.padding(4.dp),
                    tint = UIBlack
                )
            }
        }

        // 3. Edit & Delete Buttons (Top Right)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Edit Button
            IconButton(
                onClick = onEditClick,
                modifier = Modifier.size(40.dp)
            ) {
                // Image(painter = painterResource(id = R.drawable.ic_edit_event), contentDescription = "Edit")
                Surface(
                    shape = CircleShape,
                    color = UIWhite.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_edit_event),
                        contentDescription = "Edit",
                        modifier = Modifier.padding(4.dp),
                        tint = UIBlack
                    )
                }
            }

            // Delete Button
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(40.dp)
            ) {
                // Image(painter = painterResource(id = R.drawable.ic_delete_event), contentDescription = "Delete")
                Surface(
                    shape = CircleShape,
                    color = UIWhite.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete_event),
                        contentDescription = "Delete",
                        modifier = Modifier.padding(4.dp),
                        tint = Color.Red
                    )
                }
            }
        }

        // 4. Event Info at Bottom (Overlay)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Left side: Event Title and Location stacked
            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Event Title
                Surface(
                    color = UIWhite,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = event.title,
                        style = AppFont.Bold,
                        fontSize = 18.sp,
                        color = UIBlack,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // Location Info with White Background
                Surface(
                    color = UIWhite,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        // Gunakan resource drawable location pin kamu
                        // Icon(painter = painterResource(id = R.drawable.ic_location_pin), ...)

                        // Fallback icon
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = UIAccentYellow,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = event.location,
                            style = AppFont.Medium,
                            fontSize = 11.sp,
                            color = UIBlack
                        )
                    }
                }
            }

            // Right side: Profile Icons and Date stacked
            Column(
                modifier = Modifier.align(Alignment.BottomEnd),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Profile Icons Row (Dynamic)
                StackedAvatarRow(avatars = event.participantAvatars, itemSize = 34.dp)

                // Date with White Background
                Surface(
                    color = UIWhite,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = event.date,
                        style = AppFont.Medium,
                        fontSize = 11.sp,
                        color = UIBlack,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit, // Tidak butuh parameter String password lagi
    isLoading: Boolean,
    errorMessage: String? = null
) {
    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(UIWhite),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Judul
                Text(
                    text = "Delete Event",
                    style = AppFont.Bold,
                    fontSize = 20.sp,
                    color = UIAccentRed
                )

                Spacer(Modifier.height(16.dp))

                // Pesan Konfirmasi (Pengganti Input Password)
                Text(
                    text = "Are you sure you want to delete this event? This action cannot be undone.",
                    style = AppFont.Regular,
                    fontSize = 14.sp,
                    color = UIBlack,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                // Error Message (Opsional, misal gagal koneksi)
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = UIAccentRed,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Tombol Action
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Tombol Cancel
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = UIGrey),
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text("Cancel", color = UIBlack, style = AppFont.SemiBold)
                    }

                    // Tombol Delete
                    Button(
                        onClick = onConfirm, // Langsung panggil fungsi confirm
                        colors = ButtonDefaults.buttonColors(containerColor = UIAccentRed),
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = UIWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text("Delete", color = UIWhite, style = AppFont.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivitySection(activities: List<UIActivityState>, isEmpty: Boolean, onNavigateToAddActivity: () -> Unit, onNavigateToActivityDetail: (String) -> Unit) {
    if (isEmpty) EmptyStateMessage(onNavigateToAddActivity)
    else LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(activities) { ActivityItemCard(it, onNavigateToActivityDetail) }
    }
}

@Composable
fun ActivityItemCard(item: UIActivityState, onNavigateToActivityDetail: (String) -> Unit = {}) {
    Surface(shape = RoundedCornerShape(20.dp), color = UIWhite, shadowElevation = 1.dp, modifier = Modifier
        .fillMaxWidth()
        .height(80.dp)
        .clickable { onNavigateToActivityDetail(item.id) }) {
        Row(modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(item.iconColor.copy(0.3f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.ShoppingCart, null, tint = UIBlack, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = UIBlack, style = AppFont.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item.payer, fontSize = 12.sp, color = UIDarkGrey, style = AppFont.Medium)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(item.price, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = UIAccentRed, style = AppFont.Medium)
        }
    }
}

@Composable
fun SmallCircleButton(icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Surface(shape = CircleShape, color = UIWhite, modifier = modifier.size(36.dp)) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, null, modifier = Modifier.size(18.dp), tint = UIBlack) }
    }
}

@Composable
fun BottomActionArea(modifier: Modifier = Modifier, isEmpty: Boolean, onAddActivityClick: () -> Unit, onSummaryClick: () -> Unit = {}) {
    Box(modifier = modifier.fillMaxWidth()) {
        if (isEmpty) {
            Column(modifier = Modifier.align(Alignment.BottomEnd), horizontalAlignment = Alignment.End) {
                Text("Click here to add one! ↴", color = UIDarkGrey, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                FloatingAddButton(onClick = onAddActivityClick)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Summary Button (Calculate Settlement)
                Surface(
                    modifier = Modifier
                        .height(48.dp)
                        .clickable { onSummaryClick() },
                    shape = RoundedCornerShape(24.dp),
                    color = UIBlack,
                    shadowElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AttachMoney, contentDescription = null, tint = UIWhite, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Split Bill", color = UIWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }

                // Add Activity Button
                FloatingAddButton(onClick = onAddActivityClick)
            }
        }
    }
}

@Composable
fun FloatingAddButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(modifier = modifier
        .size(60.dp)
        .clickable { onClick() }, shape = CircleShape, color = UIAccentYellow, shadowElevation = 6.dp) {
        Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Add, null, tint = UIBlack, modifier = Modifier.size(32.dp)) }
    }
}

@Composable
fun EmptyStateMessage(onNavigateToAddActivity: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(top = 100.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Oops.", fontWeight = FontWeight.Bold, fontSize = 32.sp, color = UIDarkGrey)
        Spacer(Modifier.height(8.dp))
        Text("You haven't made any activities.", fontSize = 16.sp, color = UIDarkGrey)
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun DetailedEventScreenPreview_Populated() {
    // Dummy Data Event
    val dummyEvent = UIEventState(
        title = "Liburan ke Bali",
        location = "Kuta, Bali",
        date = "12 Dec 2025",
        imageUrl = "", // Kosongkan biar muncul placeholder abu-abu
        participantAvatars = listOf("avatar_1", "avatar_2", "avatar_3", "avatar_4", "avatar_5")
    )

    // Dummy Data Activities
    val dummyActivities = listOf(
        UIActivityState(
            id = "a1",
            title = "Sewa Papan Surfing",
            payer = "Paid by You",
            price = "Rp 150.000",
            iconColor = Color.Blue
        ),
        UIActivityState(
            id = "a2",
            title = "Makan Siang Nasi Padang",
            payer = "Paid by Jeremy",
            price = "Rp 45.000",
            iconColor = Color.Red
        ),
        UIActivityState(
            id = "a3",
            title = "Tiket Masuk GWK",
            payer = "Paid by Abel",
            price = "Rp 125.000",
            iconColor = Color.Green
        ),
        UIActivityState(
            id = "a4",
            title = "Tiket Masuk GWK",
            payer = "Paid by Abel",
            price = "Rp 125.000",
            iconColor = Color.Green
        ),
        UIActivityState(
            id = "a5",
            title = "Tiket Masuk GWK",
            payer = "Paid by Abel",
            price = "Rp 125.000",
            iconColor = Color.Green
        ),
        UIActivityState(
            id = "a6",
            title = "Tiket Masuk GWK",
            payer = "Paid by Abel",
            price = "Rp 125.000",
            iconColor = Color.Green
        )
    )

    LucaTheme {
        DetailedEventContent(
            eventState = dummyEvent,
            activitiesState = dummyActivities
        )
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun DetailedEventScreenPreview_Empty() {
    val dummyEvent = UIEventState(
        title = "Snorkeling Trip",
        location = "Pulau Seribu",
        date = "20 Jan 2026",
        imageUrl = "",
        participantAvatars = listOf("avatar_1", "avatar_2")
    )

    LucaTheme {
        DetailedEventContent(
            eventState = dummyEvent,
            activitiesState = emptyList() // List kosong untuk tes Empty State
        )
    }
}