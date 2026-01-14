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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.luca.ui.theme.AppFont
import com.example.luca.ui.theme.LucaTheme
import com.example.luca.ui.theme.UIGrey
import com.example.luca.ui.theme.UIWhite
import java.text.NumberFormat
import java.util.Locale

/**
 * Data class to represent a receipt item
 * @param quantity The quantity of the item
 * @param itemName The name of the item
 * @param price The price of the item
 * @param members List of colors representing members (empty list means no members to display)
 */
data class ReceiptItem(
    val quantity: Int,
    val itemName: String,
    val price: Long,
    val members: List<Color> = emptyList()
)

/**
 * Single receipt row composable
 * Displays quantity, item name, price, and members (if any)
 */
@Composable
fun ReceiptRow(
    item: ReceiptItem,
    avatarSize: Dp = 36.dp,
    horizontalPadding: Dp = 16.dp,
    verticalPadding: Dp = 12.dp
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
    ) {
        // Top Row: Quantity x Item Name | Price
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${item.quantity}x ${item.itemName}",
                style = AppFont.Bold,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = formatRupiah(item.price),
                style = AppFont.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )
        }

        // Bottom Row: Member Avatars (only show if members list is not empty)
        if (item.members.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy((-8).dp)
            ) {
                item.members.forEach { memberColor ->
                    Surface(
                        modifier = Modifier
                            .size(avatarSize)
                            .padding(4.dp),
                        shape = CircleShape,
                        color = memberColor
                    ) {}
                }
            }
        }
    }
}

/**
 * Main receipt list composable
 * Displays a list of receipt items with horizontal dividers
 *
 * @param items List of receipt items to display
 * @param modifier Modifier for the LazyColumn
 * @param avatarSize Size of member avatar circles
 * @param horizontalPadding Horizontal padding for each item row
 * @param verticalPadding Vertical padding for each item row
 * @param maxHeight Optional max height for the list (scrollable if exceeded)
 * @param dividerPadding Horizontal padding for dividers
 * @param dividerThickness Thickness of divider lines
 * @param fontSize Font size for item name and price
 * @param spacerHeight Height of spacer between item name and avatars
 */
@Composable
fun ReceiptList(
    items: List<ReceiptItem>,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 36.dp,
    horizontalPadding: Dp = 16.dp,
    verticalPadding: Dp = 12.dp,
    maxHeight: Dp? = null,
    dividerPadding: Dp = 16.dp,
    dividerThickness: Dp = 1.dp,
    fontSize: Int = 16,
    spacerHeight: Dp = 8.dp
) {
    val listModifier = if (maxHeight != null) {
        modifier
            .fillMaxWidth()
            .background(Color.White)
            .height(maxHeight)
    } else {
        modifier
            .fillMaxWidth()
            .background(Color.White)
    }

    LazyColumn(modifier = listModifier) {
        items(items, key = { it.itemName }) { item ->
            ReceiptRow(
                item = item,
                avatarSize = avatarSize,
                horizontalPadding = horizontalPadding,
                verticalPadding = verticalPadding
            )

            // Add divider after each item except the last
            if (items.indexOf(item) < items.size - 1) {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dividerPadding),
                    color = UIGrey,
                    thickness = dividerThickness
                )
            }
        }
    }
}

/**
 * Format price to Indonesian Rupiah format
 */
fun formatRupiah(price: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
    return formatter.format(price)
}

@Preview
@Composable
fun ReceiptListPreview() {
    LucaTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(UIWhite)
        ) {
            Column {

                Text(
                    text = "Default Receipt List",
                    modifier = Modifier.padding(16.dp),
                    style = AppFont.Bold
                )
                // ini kalau default copas aja teman teman dari sini
                ReceiptList(
                    items = listOf(
                        ReceiptItem(
                            quantity = 1,
                            itemName = "Gurame Bakar Kecap",
                            price = 120000,
                            members = listOf(
                                Color(0xFFC44536),
                                Color(0xFF26A69A),
                                Color(0xFFFFA726)
                            )
                        ),
                        ReceiptItem(
                            quantity = 3,
                            itemName = "Nasi Putih",
                            price = 30000,
                            members = listOf(
                                Color(0xFFC44536),
                                Color(0xFF26A69A),
                                Color(0xFFFFA726)
                            )
                        )
                    ),
                    modifier = Modifier.padding(vertical = 16.dp)

                    // sampai ini
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Example 2: Custom size with maxHeight
                Text(
                    text = "Custom Size with MaxHeight (200dp)",
                    modifier = Modifier.padding(16.dp),
                    style = AppFont.Bold
                )
                // Ini kalau kalian mau menggunakan custom size receiptnya
                //copas dari sini
                ReceiptList(
                    items = listOf(
                        ReceiptItem(
                            quantity = 1,
                            itemName = "Gurame Bakar Kecap",
                            price = 120000,
                            members = listOf(
                                Color(0xFFC44536),
                                Color(0xFF26A69A),
                                Color(0xFFFFA726)
                            )
                        ),
                        ReceiptItem(
                            quantity = 3,
                            itemName = "Nasi Putih",
                            price = 30000,
                            members = listOf(
                                Color(0xFFC44536),
                                Color(0xFF26A69A),
                                Color(0xFFFFA726)
                            )
                        ),
                        ReceiptItem(
                            quantity = 2,
                            itemName = "Tumis Kangkung",
                            price = 50000,
                            members = listOf(
                                Color(0xFFC44536),
                                Color(0xFF26A69A)
                            )
                        ),
                        ReceiptItem(
                            quantity = 1,
                            itemName = "Chocolate Milkshake",
                            price = 27000,
                            members = listOf(Color(0xFF26A69A))
                        ),
                        ReceiptItem(
                            quantity = 2,
                            itemName = "Es Teh Manis",
                            price = 32000,
                            members = listOf(
                                Color(0xFFC44536),
                                Color(0xFFFFA726)
                            )
                        )
                    ),

                    //costum size receiptnya
                    modifier = Modifier.padding(vertical = 60.dp, horizontal = 60.dp),
                    maxHeight = 200.dp,
                    avatarSize = 32.dp,
                    horizontalPadding = 5.dp,
                    verticalPadding = 5.dp
                )
                // sampai sini
            }
        }
    }
}