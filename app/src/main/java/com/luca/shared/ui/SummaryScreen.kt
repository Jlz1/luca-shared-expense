package com.luca.shared.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.luca.shared.model.Contact
import com.luca.shared.model.Settlement
import com.luca.shared.ui.theme.*
import com.luca.shared.viewmodel.SummaryViewModel
import com.luca.shared.viewmodel.UserConsumptionDetail
import com.luca.shared.viewmodel.ExpenseItemDetail
import java.util.Locale


enum class SummaryTab {
    SETTLEMENT, DETAILS
}

@Composable
fun SummaryScreen(
    eventId: String = "",
    viewModel: SummaryViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {}
) {
    // State Tab
    var currentTab by remember { mutableStateOf(SummaryTab.SETTLEMENT) }

    // Collect UI state from ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // Load settlements when screen is first displayed
    LaunchedEffect(eventId) {
        if (eventId.isNotEmpty()) {
            viewModel.loadAndCalculateSettlements(eventId)
        }
    }

    // Container Utama (Kuning)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UIAccentYellow)
            .statusBarsPadding()
    ) {
        // 1. Header
        HeaderSection(currentState = HeaderState.SUMMARY, onLeftIconClick = onBackClick)

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Content Box (Putih Rounded)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(UIBackground)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Event Title
                if (uiState.eventTitle.isNotEmpty()) {
                    Text(
                        text = uiState.eventTitle,
                        style = AppFont.Bold,
                        fontSize = 20.sp,
                        color = UIBlack,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // --- TAB SWITCHER ---
                CustomSegmentedControl(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // --- CONTENT BASED ON TAB ---
                Box(modifier = Modifier.weight(1f)) {
                    when {
                        uiState.isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = UIAccentYellow)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Calculating optimal settlements...",
                                        style = AppFont.Regular,
                                        color = UIDarkGrey
                                    )
                                }
                            }
                        }
                        uiState.errorMessage != null -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = uiState.errorMessage ?: "Unknown error",
                                        style = AppFont.Regular,
                                        color = Color.Red,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { viewModel.refresh() },
                                        colors = ButtonDefaults.buttonColors(containerColor = UIAccentYellow)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Retry", color = UIBlack)
                                    }
                                }
                            }
                        }
                        else -> {
                            when (currentTab) {
                                SummaryTab.SETTLEMENT -> {
                                    SettlementList(
                                        settlements = uiState.settlements,
                                        totalExpense = uiState.totalExpense,
                                        onTogglePaid = { settlement ->
                                            viewModel.toggleSettlementPaid(settlement.id)
                                        }
                                    )
                                }
                                SummaryTab.DETAILS -> {
                                    ConsumptionDetailsList(
                                        consumptionDetails = uiState.consumptionDetails,
                                        totalExpense = uiState.totalExpense
                                    )
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}

// --- KOMPONEN: CUSTOM TAB ---
@Composable
fun CustomSegmentedControl(
    currentTab: SummaryTab,
    onTabSelected: (SummaryTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(UIWhite)
            .border(1.dp, UIGrey.copy(alpha = 0.2f), RoundedCornerShape(25.dp))
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            SummaryTab.entries.forEach { tab ->
                val isSelected = currentTab == tab
                val textColor = if (isSelected) UIBlack else UIDarkGrey
                val weight = if (isSelected) FontWeight.Bold else FontWeight.Medium

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(4.dp)
                        .clip(RoundedCornerShape(21.dp))
                        .background(if (isSelected) UIAccentYellow else Color.Transparent)
                        .clickable { onTabSelected(tab) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (tab == SummaryTab.SETTLEMENT) "Settlement" else "Details",
                        color = textColor,
                        fontWeight = weight,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// --- KOMPONEN: SETTLEMENT LIST ---
@Composable
fun SettlementList(
    settlements: List<Settlement>,
    totalExpense: Long = 0L,
    onTogglePaid: (Settlement) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Total Expense Header
        if (totalExpense > 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = UIAccentYellow.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Expense",
                            style = AppFont.SemiBold,
                            fontSize = 14.sp,
                            color = UIBlack
                        )
                        Text(
                            text = "Rp${String.format(Locale.getDefault(), "%,.0f", totalExpense.toDouble())}",
                            style = AppFont.Bold,
                            fontSize = 18.sp,
                            color = UIBlack
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Settlements (${settlements.size})",
                style = AppFont.SemiBold,
                fontSize = 18.sp,
                color = UIBlack,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp, top = 8.dp)
            )
        }

        if (settlements.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No settlements needed.\nEveryone is settled up! 🎉",
                        style = AppFont.Regular,
                        fontSize = 16.sp,
                        color = UIDarkGrey,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(settlements) { item ->
                SettlementCard(item = item)
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "All calculations are optimized to minimize the number of transactions.",
                style = AppFont.Regular,
                fontSize = 12.sp,
                color = UIDarkGrey,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// --- KOMPONEN: KARTU SETTLEMENT ---
@Composable
fun SettlementCard(
    item: Settlement
) {
    val containerColor = if (item.isPaid) UIDarkGrey.copy(alpha = 0.1f) else UIWhite
    val borderColor = if (item.isPaid) Color.Transparent else UIAccentYellow.copy(alpha = 0.5f)

    // Create Contact objects from Settlement data for avatar display
    val fromContact = Contact(name = item.fromName, avatarName = item.fromAvatarName)
    val toContact = Contact(name = item.toName, avatarName = item.toAvatarName)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Kiri: Avatar Flow
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f).padding(end = 8.dp)
        ) {
            // From Avatar
            ParticipantAvatarItemSmall(fromContact)

            // Arrow
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(UIGrey.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Pays to",
                    tint = UIBlack,
                    modifier = Modifier.size(20.dp)
                )
            }

            // To Avatar
            ParticipantAvatarItemSmall(toContact)
        }

        // Kanan: Jumlah & Checkbox
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "Rp${String.format(Locale.getDefault(), "%,.0f", item.amount.toDouble())}",
                style = AppFont.Bold,
                fontSize = 16.sp,
                color = if (item.isPaid) UIDarkGrey else UIBlack,
                textDecoration = if (item.isPaid) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
            )

            Spacer(modifier = Modifier.height(4.dp))


        }
    }
}

// --- KOMPONEN: CONSUMPTION DETAILS LIST ---
@Composable
fun ConsumptionDetailsList(
    consumptionDetails: List<UserConsumptionDetail>,
    totalExpense: Long = 0L
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Total Expense Header
        if (totalExpense > 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = UIAccentYellow.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Expense",
                            style = AppFont.SemiBold,
                            fontSize = 14.sp,
                            color = UIBlack
                        )
                        Text(
                            text = "Rp${String.format(Locale.getDefault(), "%,.0f", totalExpense.toDouble())}",
                            style = AppFont.Bold,
                            fontSize = 18.sp,
                            color = UIBlack
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Consumption by User (${consumptionDetails.size})",
                style = AppFont.SemiBold,
                fontSize = 18.sp,
                color = UIBlack,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp, top = 8.dp)
            )
        }

        if (consumptionDetails.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No consumption data available.\nAdd activities and items first.",
                        style = AppFont.Regular,
                        fontSize = 16.sp,
                        color = UIDarkGrey,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(consumptionDetails) { userDetail ->
                UserConsumptionCard(userDetail = userDetail)
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "This shows what each person consumed, not who paid.",
                style = AppFont.Regular,
                fontSize = 12.sp,
                color = UIDarkGrey,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// --- KOMPONEN: USER CONSUMPTION CARD (EXPANDABLE) ---
@Composable
fun UserConsumptionCard(
    userDetail: UserConsumptionDetail
) {
    var isExpanded by remember { mutableStateOf(false) }

    // Create Contact for avatar display
    val userContact = Contact(name = userDetail.userName, avatarName = userDetail.avatarName)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = UIWhite),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row (Always visible)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Avatar + Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    ParticipantAvatarItemSmall(userContact)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = userDetail.userName,
                        style = AppFont.SemiBold,
                        fontSize = 16.sp,
                        color = UIBlack
                    )
                }

                // Right: Total Amount + Arrow
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rp${String.format(Locale.getDefault(), "%,.0f", userDetail.totalConsumption.toDouble())}",
                        style = AppFont.Bold,
                        fontSize = 16.sp,
                        color = UIBlack
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = UIBlack,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Expanded Content (Item List)
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = UIGrey.copy(alpha = 0.3f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Item Count
                Text(
                    text = "${userDetail.items.size} item${if (userDetail.items.size > 1) "s" else ""}",
                    style = AppFont.Regular,
                    fontSize = 12.sp,
                    color = UIDarkGrey,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // List of consumed items
                userDetail.items.forEach { item ->
                    ConsumptionItemRow(item = item)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

// --- KOMPONEN: CONSUMPTION ITEM ROW ---
@Composable
fun ConsumptionItemRow(
    item: ExpenseItemDetail
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(UIAccentYellow.copy(alpha = 0.1f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Item Details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.itemName,
                style = AppFont.SemiBold,
                fontSize = 14.sp,
                color = UIBlack
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${item.activityTitle} • ${item.quantity}x @ Rp${String.format(Locale.getDefault(), "%,.0f", item.price.toDouble())}",
                style = AppFont.Regular,
                fontSize = 11.sp,
                color = UIDarkGrey
            )
        }

        // Right: Split Amount
        Text(
            text = "Rp${String.format(Locale.getDefault(), "%,.0f", item.splitAmount.toDouble())}",
            style = AppFont.Bold,
            fontSize = 14.sp,
            color = UIBlack
        )
    }
}

// --- KOMPONEN: PLACEHOLDER SPREADSHEET ---
@Composable
fun SpreadsheetPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.TableChart,
            contentDescription = null,
            tint = UIAccentYellow,
            modifier = Modifier
                .size(80.dp)
                .padding(top = 40.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Detailed Transactions",
            style = AppFont.SemiBold,
            fontSize = 20.sp,
            color = UIBlack
        )
        Text(
            text = "A full spreadsheet view of every item and split will be available here.",
            style = AppFont.Regular,
            fontSize = 14.sp,
            color = UIDarkGrey,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Mock Table Visual
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(UIGrey.copy(alpha = 0.1f))
                .border(1.dp, UIGrey.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
        ) {
            // Garis-garis dummy spreadsheet
            Column {
                repeat(6) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .border(1.dp, UIGrey.copy(alpha = 0.1f))
                    )
                }
            }
            Text(
                text = "Spreadsheet View Placeholder",
                modifier = Modifier.align(Alignment.Center),
                color = UIDarkGrey.copy(alpha = 0.5f)
            )
        }
    }
}

// ========== PREVIEW FUNCTIONS ==========

@Preview(showBackground = true, name = "Settlement List with Data")
@Composable
fun SettlementListPreview() {
    val mockSettlements = listOf(
        Settlement(
            id = "1",
            fromName = "Alice",
            fromAvatarName = "avatar_1",
            toName = "Bob",
            toAvatarName = "avatar_2",
            amount = 150000L,
            isPaid = false
        ),
        Settlement(
            id = "2",
            fromName = "Charlie",
            fromAvatarName = "avatar_3",
            toName = "Bob",
            toAvatarName = "avatar_2",
            amount = 75000L,
            isPaid = true
        ),
        Settlement(
            id = "3",
            fromName = "Diana",
            fromAvatarName = "avatar_4",
            toName = "Alice",
            toAvatarName = "avatar_1",
            amount = 200000L,
            isPaid = false
        )
    )

    LucaTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(UIBackground)
        ) {
            SettlementList(
                settlements = mockSettlements,
                totalExpense = 500000L,
                onTogglePaid = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Empty Settlement List")
@Composable
fun EmptySettlementListPreview() {
    LucaTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(UIBackground)
        ) {
            SettlementList(
                settlements = emptyList(),
                totalExpense = 0L,
                onTogglePaid = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Settlement Card - Unpaid")
@Composable
fun SettlementCardUnpaidPreview() {
    val mockSettlement = Settlement(
        id = "1",
        fromName = "Alice Johnson",
        fromAvatarName = "avatar_1",
        toName = "Bob Smith",
        toAvatarName = "avatar_2",
        amount = 250000L,
        isPaid = false
    )

    LucaTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(UIBackground)
                .padding(20.dp)
        ) {
            SettlementCard(
                item = mockSettlement
            )
        }
    }
}

@Preview(showBackground = true, name = "Settlement Card - Paid")
@Composable
fun SettlementCardPaidPreview() {
    val mockSettlement = Settlement(
        id = "2",
        fromName = "Charlie Brown",
        fromAvatarName = "avatar_3",
        toName = "Diana Prince",
        toAvatarName = "avatar_4",
        amount = 125000L,
        isPaid = true
    )

    LucaTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(UIBackground)
                .padding(20.dp)
        ) {
            SettlementCard(
                item = mockSettlement
            )
        }
    }
}

@Preview(showBackground = true, name = "Tab Switcher - Settlement")
@Composable
fun CustomSegmentedControlSettlementPreview() {
    LucaTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(UIBackground)
                .padding(24.dp)
        ) {
            CustomSegmentedControl(
                currentTab = SummaryTab.SETTLEMENT,
                onTabSelected = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Tab Switcher - Details")
@Composable
fun CustomSegmentedControlDetailsPreview() {
    LucaTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(UIBackground)
                .padding(24.dp)
        ) {
            CustomSegmentedControl(
                currentTab = SummaryTab.DETAILS,
                onTabSelected = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Spreadsheet Placeholder")
@Composable
fun SpreadsheetPlaceholderPreview() {
    LucaTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(UIBackground)
        ) {
            SpreadsheetPlaceholder()
        }
    }
}

@Preview(showBackground = true, name = "Consumption Details List")
@Composable
fun ConsumptionDetailsListPreview() {
    val mockConsumptionDetails = listOf(
        UserConsumptionDetail(
            userName = "Alice",
            avatarName = "avatar_1",
            totalConsumption = 350000L,
            items = listOf(
                ExpenseItemDetail(
                    activityTitle = "Hotel Night 1",
                    itemName = "Deluxe Room",
                    price = 200000L,
                    quantity = 1,
                    splitAmount = 200000L
                ),
                ExpenseItemDetail(
                    activityTitle = "Dinner",
                    itemName = "Steak",
                    price = 150000L,
                    quantity = 1,
                    splitAmount = 150000L
                )
            )
        ),
        UserConsumptionDetail(
            userName = "Bob",
            avatarName = "avatar_2",
            totalConsumption = 125000L,
            items = listOf(
                ExpenseItemDetail(
                    activityTitle = "Lunch",
                    itemName = "Pasta",
                    price = 75000L,
                    quantity = 1,
                    splitAmount = 75000L
                ),
                ExpenseItemDetail(
                    activityTitle = "Coffee",
                    itemName = "Cappuccino",
                    price = 50000L,
                    quantity = 1,
                    splitAmount = 50000L
                )
            )
        )
    )

    LucaTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(UIBackground)
        ) {
            ConsumptionDetailsList(
                consumptionDetails = mockConsumptionDetails,
                totalExpense = 475000L
            )
        }
    }
}

@Preview(showBackground = true, name = "User Consumption Card - Collapsed")
@Composable
fun UserConsumptionCardCollapsedPreview() {
    val mockUser = UserConsumptionDetail(
        userName = "Charlie Brown",
        avatarName = "avatar_3",
        totalConsumption = 280000L,
        items = listOf(
            ExpenseItemDetail(
                activityTitle = "Transport",
                itemName = "Taxi",
                price = 80000L,
                quantity = 1,
                splitAmount = 40000L
            ),
            ExpenseItemDetail(
                activityTitle = "Hotel",
                itemName = "Twin Room",
                price = 480000L,
                quantity = 1,
                splitAmount = 240000L
            )
        )
    )

    LucaTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(UIBackground)
                .padding(20.dp)
        ) {
            UserConsumptionCard(userDetail = mockUser)
        }
    }
}

@Preview(showBackground = true, name = "Consumption Item Row")
@Composable
fun ConsumptionItemRowPreview() {
    val mockItem = ExpenseItemDetail(
        activityTitle = "Breakfast",
        itemName = "Croissant & Coffee",
        price = 45000L,
        quantity = 2,
        splitAmount = 90000L
    )

    LucaTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(UIBackground)
                .padding(20.dp)
        ) {
            ConsumptionItemRow(item = mockItem)
        }
    }
}

@Preview(showBackground = true, name = "Empty Consumption List")
@Composable
fun EmptyConsumptionDetailsListPreview() {
    LucaTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(UIBackground)
        ) {
            ConsumptionDetailsList(
                consumptionDetails = emptyList(),
                totalExpense = 0L
            )
        }
    }
}
