package com.example.luca.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.luca.model.Contact
import com.example.luca.ui.theme.*

@Composable
fun ContactSelectionOverlay(
    currentUser: Contact,
    availableContacts: List<Contact>,
    selectedContacts: List<Contact>,
    onDismiss: () -> Unit,
    onConfirm: (List<Contact>) -> Unit,
    onAddNewContact: () -> Unit
) {
    var currentSelection by remember { mutableStateOf(selectedContacts.toMutableList()) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = UIWhite),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .fillMaxHeight(0.7f)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Add Participants", style = AppFont.Bold, fontSize = 20.sp, color = UIBlack)
            Spacer(modifier = Modifier.height(16.dp))

            // List Kontak
            LazyColumn(modifier = Modifier.weight(1f)) {
                if (availableContacts.isEmpty()) {
                    item {
                        Text(
                            "No contacts found.",
                            style = AppFont.Regular,
                            color = UIDarkGrey,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                }

                items(
                    availableContacts.filter { it.name.isNotBlank() },
                    key = { it.id + it.name }
                ) { contact ->
                    val isSelected = currentSelection.any { selectedContact ->
                        if (contact.id.isNotEmpty() && selectedContact.id.isNotEmpty()) {
                            selectedContact.id == contact.id
                        } else {
                            selectedContact.name == contact.name && selectedContact.avatarName == contact.avatarName
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isSelected) {
                                    // Remove dari selection
                                    currentSelection = currentSelection.filterNot { selectedContact ->
                                        if (contact.id.isNotEmpty() && selectedContact.id.isNotEmpty()) {
                                            selectedContact.id == contact.id
                                        } else {
                                            selectedContact.name == contact.name && selectedContact.avatarName == contact.avatarName
                                        }
                                    }.toMutableList()
                                } else {
                                    // Add ke selection
                                    currentSelection = (currentSelection + contact).toMutableList()
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // --- FIX: Gunakan AsyncImage untuk DiceBear ---
                        val avatarName = if (contact.avatarName.isNotBlank()) contact.avatarName else "avatar_1"

                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data("https://api.dicebear.com/9.x/avataaars/png?seed=$avatarName")
                                .crossfade(true)
                                .build(),
                            contentDescription = contact.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                            error = painterResource(android.R.drawable.ic_menu_report_image)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = contact.name,
                            style = AppFont.Medium,
                            color = UIBlack,
                            modifier = Modifier.weight(1f)
                        )

                        Icon(
                            imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = null,
                            tint = if (isSelected) UIAccentYellow else UIDarkGrey
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onConfirm(currentSelection) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = UIAccentYellow)
            ) {
                Text("Done", color = UIBlack, style = AppFont.Bold)
            }
        }
    }
}