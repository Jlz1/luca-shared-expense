package com.example.luca.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.luca.ui.theme.UIAccentYellow
import com.example.luca.ui.theme.UIBlack
import com.example.luca.ui.theme.UIWhite

@Composable
fun AvatarSelectionOverlay(
    currentSelection: String,
    onAvatarSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // 1. Buat daftar seed avatar manual (misal 16 pilihan)
    // Ini menggantikan AvatarUtils.avatars
    val availableAvatars = remember {
        (1..16).map { "avatar_$it" }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(UIWhite, shape = RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select an Avatar",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = UIBlack
                )

                Spacer(modifier = Modifier.height(20.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4), // 4 Kolom
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.height(300.dp) // Scrollable
                ) {
                    // 2. Loop list String seed tadi
                    items(availableAvatars) { seedName ->
                        val isSelected = seedName == currentSelection

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) UIAccentYellow else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    onAvatarSelected(seedName) // Kirim string seed
                                    onDismiss()
                                }
                        ) {
                            // 3. Gunakan AsyncImage untuk load dari DiceBear
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data("https://api.dicebear.com/9.x/avataaars/png?seed=$seedName")
                                    .crossfade(true)
                                    .build(),
                                contentDescription = seedName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                // Gunakan resource sistem Android untuk placeholder agar aman dari error R class
                                placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                                error = painterResource(android.R.drawable.ic_menu_report_image)
                            )
                        }
                    }
                }
            }
        }
    }
}