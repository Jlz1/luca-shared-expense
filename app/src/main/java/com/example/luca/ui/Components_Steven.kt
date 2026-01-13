package com.example.luca.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.ui.theme.AppFont
import com.example.luca.ui.theme.LucaTheme
import com.example.luca.ui.theme.UIDarkGrey
import com.example.luca.ui.theme.UIWhite


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarModify(
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    initialQuery: String = "", // Ganti dari searchQuery
    onSearchQueryChange: (String) -> Unit = {},
    onSearchClick: () -> Unit = {},
    readOnly: Boolean = true,
    enabled: Boolean = true,
    databaseLabel: String? = null
) {
    // STATE INTERNAL - Otomatis handle input
    var internalSearchQuery by remember { mutableStateOf(initialQuery) }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50.dp),
        color = UIWhite,
        shadowElevation = 2.dp,
        onClick = if (readOnly) { onSearchClick } else { {} }
    ) {
        // Database label disimpan internal saja, tidak ditampilkan di UI
        // Bisa digunakan untuk logging atau logic lainnya
        if (databaseLabel != null) {
            // Log atau logic internal bisa ditambahkan di sini
            // println("Searching in: $databaseLabel")
        }

        if (readOnly) {
                // Mode Read-Only: Hanya tampilan, tidak bisa diisi
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = UIDarkGrey,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = placeholder,
                        style = AppFont.Regular,
                        fontSize = 16.sp,
                        color = UIDarkGrey
                    )
                }
            } else {
                // Mode Editable: Bisa diisi text dengan STATE INTERNAL
                TextField(
                    value = internalSearchQuery,
                    onValueChange = { newQuery ->
                        internalSearchQuery = newQuery // Update state internal
                        onSearchQueryChange(newQuery) // Kirim ke callback
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = placeholder,
                            style = AppFont.Regular,
                            fontSize = 16.sp,
                            color = UIDarkGrey
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = UIDarkGrey,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    enabled = enabled,
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    textStyle = AppFont.Regular.copy(fontSize = 16.sp)
                )
            }
    }
}

@Preview
@Composable
fun ComponentsPreviewStv(){
    LucaTheme {
        Column(
            modifier = Modifier
                .background(UIWhite)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Contoh 2: Mode Editable tanpa database
            Text("2. Editable Mode (Tanpa Database):", fontWeight = FontWeight.Bold)
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

            // Contoh 3: Mode Editable dengan database label
            Text("3. Editable Mode (Dengan Database):", fontWeight = FontWeight.Bold)
            SearchBarModify(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp,vertical = 12.dp)
                    .height(50.dp),
                placeholder = "Search products...",
                onSearchQueryChange = { query ->
                    println("Searching in database: $query")
                },
                readOnly = false,
                databaseLabel = "Database: Products"
            )

        }
    }
}