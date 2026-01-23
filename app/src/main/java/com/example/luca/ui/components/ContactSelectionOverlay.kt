package com.example.luca.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.model.Contact
import com.example.luca.ui.theme.*
import com.example.luca.util.AvatarUtils

@Composable
fun ContactSelectionOverlay(
    availableContacts: List<Contact>,
    selectedContacts: List<Contact>,
    onDismiss: () -> Unit,
    onConfirm: (List<Contact>) -> Unit,
    onAddNewContact: () -> Unit
) {
    var currentSelection by remember { mutableStateOf(selectedContacts) }

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

            // Tombol Add New Contact
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAddNewContact() }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(UIAccentYellow),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, null, tint = UIBlack)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("Add New Contact", style = AppFont.SemiBold, color = UIBlack)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = UIGrey)

            // List Kontak
            LazyColumn(modifier = Modifier.weight(1f)) {
                if (availableContacts.isEmpty()) {
                    item {
                        Text("No contacts found.", style = AppFont.Regular, color = UIDarkGrey, modifier = Modifier.padding(vertical = 12.dp))
                    }
                }

                items(availableContacts) { contact ->
                    val isSelected = currentSelection.any { it.id == contact.id }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                currentSelection = if (isSelected) {
                                    currentSelection.filter { it.id != contact.id }
                                } else {
                                    currentSelection + contact
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = AvatarUtils.getAvatarResId(contact.avatarName)),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(contact.name, style = AppFont.Medium, color = UIBlack, modifier = Modifier.weight(1f))

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