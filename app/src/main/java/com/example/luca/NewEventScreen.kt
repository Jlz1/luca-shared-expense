package com.example.luca


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.ui.theme.*

@Composable
fun NewEventScreen() {
    Scaffold(
        topBar = {
            HeaderSection()
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 100.dp) // Space for bottom buttons
            ) {
                // Event Card Section
                EventCard()

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar Section
                SearchBarModify(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp,vertical = 12.dp)
                        .height(50.dp),
                    placeholder = "Search",
                    onSearchQueryChange = { query ->
                        // Handle search query change
                        println("Search: $query")
                    },
                    readOnly = false
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Empty State Message
                EmptyStateMessage()
            }

            // Bottom Action Area (Fixed at bottom)
            BottomActionAreaNew()
        }
    }
}

// Event Card Component
@Composable
fun EventCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Background Image Card
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(UIAccentYellow) // Placeholder color (use image in real implementation)
        ) {
            // Placeholder for background image
            // In real implementation, use AsyncImage or Image with painterResource
            // For now, using colored background
        }

        // Exit Button (Top Left - Separated)
        IconButton(
            onClick = { /* Handle exit */ },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .size(31.dp)
                .background(UIWhite, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Exit",
                tint = UIBlack,
                modifier = Modifier.size(20.dp)
            )
        }

        // Edit & Delete Buttons (Top Right)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(25.dp)
        ) {
            // Edit Button
            IconButton(
                onClick = { /* Handle edit */ },
                modifier = Modifier
                    .size(31.dp)
                    .background(UIWhite, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = UIBlack,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Delete Button
            IconButton(
                onClick = { /* Handle delete */ },
                modifier = Modifier
                    .size(31.dp)
                    .background(UIWhite, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = UIAccentRed,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Event Info at Bottom
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
                        text = "Snorkeling Trip",
                        style = AppFont.Bold,
                        fontSize = 18.sp,
                        color = UIBlack,
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
                        Icon(
                            painter = painterResource(id = R.drawable.ic_location_pin),
                            contentDescription = "Location",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Thousand Islands, Indonesia",
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
                // Profile Icons Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-8).dp)
                ) {
                    // Placeholder for profile icons
                    repeat(4) { index ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    when (index) {
                                        0 -> Color(0xFFE74C3C)
                                        1 -> Color(0xFF3498DB)
                                        2 -> Color(0xFF2ECC71)
                                        else -> Color(0xFFF39C12)
                                    }
                                )
                        )
                    }
                }

                // Date with White Background
                Surface(
                    color = UIWhite,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "December 20, 2025",
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

// Empty State Message Component
@Composable
fun EmptyStateMessage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // "Oops." Text
        Text(
            text = "Oops.",
            style = AppFont.Bold,
            fontSize = 32.sp,
            color = UIBlack,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // "You haven't made any activities." Text
        Text(
            text = "You haven't made any activities.",
            style = AppFont.Regular,
            fontSize = 16.sp,
            color = UIDarkGrey,
            textAlign = TextAlign.Center
        )
    }
}

// Bottom Action Area Component
@Composable
fun BottomActionAreaNew(
    onAddActivityClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        // FAB with Hint (Bottom Right)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Hint Text and Arrow
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp, end = 8.dp)
            ) {
                Text(
                    text = "Click here to add one!",
                    style = AppFont.Regular,
                    fontSize = 16.sp,
                    color = UIDarkGrey
                )
                Spacer(modifier = Modifier.width(4.dp))
                // Curved arrow pointing down-right (using text emoji as placeholder)
                Text(
                    text = "↷",
                    fontSize = 40.sp,
                    color = UIDarkGrey
                )
            }

            // Add FAB
            FloatingActionButton(
                onClick = onAddActivityClick,
                containerColor = UIAccentYellow,
                contentColor = UIBlack,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Activity",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun StevenPreview(){
    LucaTheme {
        NewEventScreen()
    }
}
