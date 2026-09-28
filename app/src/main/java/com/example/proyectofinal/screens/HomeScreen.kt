package com.example.proyectofinal.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectofinal.R
import com.example.proyectofinal.data.*
import com.example.proyectofinal.ui.theme.MiFuenteGoogle
import com.example.proyectofinal.ui.theme.ProyectoFinalTheme
import com.example.proyectofinal.utils.AppNotification
import com.example.proyectofinal.utils.belongsToPeriod
import com.example.proyectofinal.utils.monthAbbreviation
import com.example.proyectofinal.utils.monthNumbersForPeriod
import com.example.proyectofinal.utils.monthNumberFromDate
import com.example.proyectofinal.utils.NotificationHelper
import com.example.proyectofinal.viewmodel.FilterMode
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    user: User,
    data: UserFinancialData,
    bankCards: List<BankCard> = emptyList(),
    filterMode: FilterMode = FilterMode.BIMESTER,
    selectedPeriod: String = "TODOS",
    onFilterModeChange: (FilterMode) -> Unit = {},
    onPeriodChange: (String) -> Unit = {},
    onAddClick: (EntryType) -> Unit,
    onIncomeDetail: (IncomeItem) -> Unit,
    onSavingDetail: (SavingItem) -> Unit,
    onExpenseDetail: (ExpenseItem) -> Unit,
    onDebtDetail: (DebtItem) -> Unit,
    onViewEntries: (EntryType) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onInfoClick: () -> Unit = {},
    onCardsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val filteredIncomes = data.incomeItems.filter { item ->
        belongsToPeriod(item.date, item.month, selectedPeriod)
    }
    val filteredExpenses = data.expenseItems.filter { item ->
        belongsToPeriod(item.date, item.period, selectedPeriod)
    }
    val filteredDebts = data.debtItems.filter { item ->
        belongsToPeriod(item.date, item.period, selectedPeriod)
    }
    val selectedMonths = monthNumbersForPeriod(selectedPeriod)
    val filteredSavings = data.savingItems.filter { item ->
        selectedPeriod == "TODOS" ||
            monthNumberFromDate(item.date)?.let { it in selectedMonths }
                ?: true
    }

    val filteredTotalIncomes = filteredIncomes.sumOf { it.value.toDouble() }.toFloat()
    val filteredTotalExpenses = filteredExpenses.sumOf { it.amount.toDouble() }.toFloat()
    val filteredTotalDebts = filteredDebts.sumOf { it.amount.toDouble() }.toFloat()
    val filteredTotalSavings = filteredSavings.sumOf { it.value.toDouble() }.toFloat()
    val context = LocalContext.current
    var showNotifications by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf(emptyList<AppNotification>()) }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(2.dp)) }
            item {
                HeaderSection(
                    user = user,
                    onNotificationsClick = {
                        notifications = NotificationHelper.getNotifications(context)
                        showNotifications = true
                    }
                )
            }
            item {
                FilterIconBar(
                    filterMode = filterMode,
                    selectedPeriod = selectedPeriod,
                    onFilterModeChange = onFilterModeChange,
                    onPeriodChange = onPeriodChange
                )
            }
            item {
                WalletCardsWidget(cards = bankCards, onCardsClick = onCardsClick)
            }
            item {
                DashboardSection(
                    title = "INGRESOS",
                    amount = formatCurrency(if (selectedPeriod == "TODOS") data.totalIncomes else filteredTotalIncomes),
                    buttonText = "AGREGAR INGRESO",
                    onAddClick = { onAddClick(EntryType.INCOME) },
                    onViewDetails = { onViewEntries(EntryType.INCOME) },
                    content = {
                        IncomeChart(
                            if (selectedPeriod == "TODOS") data.incomeItems else filteredIncomes,
                            onIncomeDetail,
                            selectedPeriod
                        )
                    }
                )
            }
            item {
                DashboardSection(
                    title = "AHORROS",
                    amount = formatCurrency(if (selectedPeriod == "TODOS") data.totalSavings else filteredTotalSavings),
                    buttonText = "AGREGAR AHORRO",
                    onAddClick = { onAddClick(EntryType.SAVING) },
                    onViewDetails = { onViewEntries(EntryType.SAVING) },
                    content = { SavingsList(filteredSavings, onSavingDetail) }
                )
            }
            item {
                DashboardSection(
                    title = "GASTOS",
                    amount = formatCurrency(if (selectedPeriod == "TODOS") data.totalExpenses else filteredTotalExpenses),
                    buttonText = "AGREGAR GASTO",
                    onAddClick = { onAddClick(EntryType.EXPENSE) },
                    onViewDetails = { onViewEntries(EntryType.EXPENSE) },
                    content = { ExpensesChart(if (selectedPeriod == "TODOS") data.expenseItems else filteredExpenses, data.pieChartData, onExpenseDetail) }
                )
            }
            item {
                DashboardSection(
                    title = "DEUDAS",
                    amount = formatCurrency(if (selectedPeriod == "TODOS") data.totalDebts else filteredTotalDebts),
                    buttonText = "AGREGAR DEUDA",
                    onAddClick = { onAddClick(EntryType.DEBT) },
                    onViewDetails = { onViewEntries(EntryType.DEBT) },
                    content = { DebtsChart(if (selectedPeriod == "TODOS") data.debtItems else filteredDebts, onDebtDetail) }
                )
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (showNotifications) {
        AlertDialog(
            onDismissRequest = { showNotifications = false },
            title = { Text("Notificaciones", fontFamily = MiFuenteGoogle, fontWeight = FontWeight.Bold) },
            text = {
                if (notifications.isEmpty()) {
                    Text("No hay notificaciones por ahora.", fontFamily = MiFuenteGoogle)
                } else {
                    Column(
                        modifier = Modifier.heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        notifications.forEach { notification ->
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = notification.title,
                                    fontFamily = MiFuenteGoogle,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(notification.message, fontFamily = MiFuenteGoogle, fontSize = 13.sp)
                                Text(
                                    notification.formattedDate(),
                                    fontFamily = MiFuenteGoogle,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotifications = false }) {
                    Text("Cerrar", fontFamily = MiFuenteGoogle)
                }
            }
        )
    }
}

@Composable
fun FilterIconBar(
    filterMode: FilterMode,
    selectedPeriod: String,
    onFilterModeChange: (FilterMode) -> Unit,
    onPeriodChange: (String) -> Unit
) {
    var showModeMenu by remember { mutableStateOf(false) }
    var showPeriodMenu by remember { mutableStateOf(false) }

    val modeLabels = mapOf(
        FilterMode.MONTH to "Mes",
        FilterMode.BIMESTER to "Bimestre",
        FilterMode.TRIMESTER to "Trimestre",
        FilterMode.SEMESTER to "Semestre"
    )

    val periodOptions = listOf("TODOS") + when (filterMode) {
        FilterMode.MONTH -> listOf("ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC")
        FilterMode.BIMESTER -> listOf("ENE-FEB", "MAR-ABR", "MAY-JUN", "JUL-AGO", "SEP-OCT", "NOV-DIC")
        FilterMode.TRIMESTER -> listOf("ENE-MAR", "ABR-JUN", "JUL-SEP", "OCT-DIC")
        FilterMode.SEMESTER -> listOf("ENE-JUN", "JUL-DIC")
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { showModeMenu = true },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                .size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filtrar por",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        DropdownMenu(expanded = showModeMenu, onDismissRequest = { showModeMenu = false }) {
            FilterMode.values().forEach { mode ->
                DropdownMenuItem(
                    text = { Text("Filtrar por: ${modeLabels[mode]}", fontFamily = MiFuenteGoogle) },
                    onClick = {
                        onFilterModeChange(mode)
                        showModeMenu = false
                    }
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            OutlinedButton(
                onClick = { showPeriodMenu = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "${modeLabels[filterMode]}: $selectedPeriod", fontFamily = MiFuenteGoogle, fontSize = 12.sp)
            }
            DropdownMenu(expanded = showPeriodMenu, onDismissRequest = { showPeriodMenu = false }) {
                periodOptions.forEach { period ->
                    DropdownMenuItem(
                        text = { Text(period, fontFamily = MiFuenteGoogle) },
                        onClick = {
                            onPeriodChange(period)
                            showPeriodMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WalletCardsWidget(cards: List<BankCard>, onCardsClick: () -> Unit) {
    if (cards.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCardsClick() },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MIS TARJETAS BANCARIAS",
                fontFamily = MiFuenteGoogle,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Ver todas (${cards.size}) >",
                fontFamily = MiFuenteGoogle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(cards.take(3)) { card ->
                Card(
                    modifier = Modifier
                        .width(260.dp)
                        .height(115.dp)
                        .clickable { onCardsClick() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = card.cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = card.alias,
                                fontFamily = MiFuenteGoogle,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Surface(
                                color = Color.White.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = card.bankName,
                                    fontFamily = MiFuenteGoogle,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = card.cardNumber,
                            fontFamily = MiFuenteGoogle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = card.cardHolder,
                                fontFamily = MiFuenteGoogle,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = formatCurrency(card.balance),
                                fontFamily = MiFuenteGoogle,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection(
    user: User,
    modifier: Modifier = Modifier,
    onNotificationsClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = user.avatarRes),
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.bienvenida),
                fontFamily = MiFuenteGoogle,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = user.name,
                fontFamily = MiFuenteGoogle,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        IconButton(onClick = onNotificationsClick) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notificaciones",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun DashboardSection(
    title: String,
    amount: String,
    buttonText: String,
    onAddClick: () -> Unit,
    onViewDetails: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var expanded by remember(title) { mutableStateOf(true) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(vertical = 28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = title,
                            fontFamily = MiFuenteGoogle,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = amount,
                            fontFamily = MiFuenteGoogle,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                AnimatedVisibility(visible = expanded) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        content()
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (onViewDetails != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onAddClick,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = buttonText,
                        fontFamily = MiFuenteGoogle,
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                TextButton(onClick = onViewDetails) {
                    Text("Ver desglose", fontFamily = MiFuenteGoogle, fontSize = 12.sp)
                }
            }
        } else {
            Button(
                onClick = onAddClick,
                modifier = Modifier.fillMaxWidth(0.65f).height(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = buttonText,
                    fontFamily = MiFuenteGoogle,
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun IncomeChart(
    items: List<IncomeItem>,
    onDetailClick: (IncomeItem) -> Unit,
    selectedPeriod: String = "TODOS"
) {
    var selectedMonthItems by remember { mutableStateOf<List<IncomeItem>?>(null) }
    val months = listOf("ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC")
    val visibleMonths = if (selectedPeriod == "TODOS") {
        months
    } else {
        val selectedMonthNumbers = monthNumbersForPeriod(selectedPeriod)
        months.filterIndexed { index, _ -> index + 1 in selectedMonthNumbers }
    }
    val groupedItems = items.groupBy { item ->
        monthNumberFromDate(item.date)?.let(::monthAbbreviation) ?: item.month.uppercase()
    }
    val monthTotals = groupedItems.mapValues { (_, monthItems) -> monthItems.sumOf { it.value.toDouble() }.toFloat() }
    val maxVal = monthTotals.values.maxOrNull()?.takeIf { it > 0f } ?: 1f

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        visibleMonths.forEach { month ->
            val monthItems = groupedItems[month] ?: emptyList()
            val total = monthTotals[month] ?: 0f
            val tint = Color(0xFF5B46A8).copy(alpha = 0.38f + 0.62f * (total / maxVal))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { if (monthItems.isNotEmpty()) selectedMonthItems = monthItems },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = month,
                    modifier = Modifier.width(36.dp),
                    fontFamily = MiFuenteGoogle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(if (total > 0f) (total / maxVal).coerceIn(0.06f, 1f) else 0f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(tint)
                    )
                }
                Text(
                    text = formatCurrency(total),
                    modifier = Modifier.width(78.dp),
                    fontFamily = MiFuenteGoogle,
                    fontSize = 11.sp,
                    fontWeight = if (total == maxVal && total > 0f) FontWeight.Bold else FontWeight.Medium,
                    color = if (total > 0f) tint else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Ver todos los ingresos de $month",
                    tint = if (monthItems.isNotEmpty()) tint else MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    selectedMonthItems?.let { monthItems ->
        val month = monthItems.firstOrNull()?.let { item ->
            monthNumberFromDate(item.date)?.let(::monthAbbreviation) ?: item.month.uppercase()
        }.orEmpty()
        AlertDialog(
            onDismissRequest = { selectedMonthItems = null },
            title = {
                Text("Ingresos de $month", fontFamily = MiFuenteGoogle, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Total del mes: ${formatCurrency(monthItems.sumOf { it.value.toDouble() }.toFloat())}",
                        fontFamily = MiFuenteGoogle,
                        fontWeight = FontWeight.SemiBold
                    )
                    monthItems.forEach { item ->
                        TextButton(
                            onClick = {
                                selectedMonthItems = null
                                onDetailClick(item)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    item.name,
                                    fontFamily = MiFuenteGoogle,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "${item.date} • ${formatCurrency(item.value)}",
                                    fontFamily = MiFuenteGoogle,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        HorizontalDivider()
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedMonthItems = null }) {
                    Text("Cerrar", fontFamily = MiFuenteGoogle)
                }
            }
        )
    }
}

@Composable
fun ChartBar(label: String, progress: Float, color: Color, value: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontFamily = MiFuenteGoogle,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(35.dp)
        )
        Box(
            modifier = Modifier
                .weight(progress.coerceAtLeast(0.01f))
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (progress > 0) color else MaterialTheme.colorScheme.surfaceVariant)
        )
        if (progress < 1f) {
            Spacer(modifier = Modifier.weight((1f - progress).coerceAtLeast(0.01f)))
        }
        
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = value,
            fontFamily = MiFuenteGoogle,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = ">",
            fontFamily = MiFuenteGoogle,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SavingsList(items: List<SavingItem>, onDetailClick: (SavingItem) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            .padding(16.dp)
    ) {
        if (items.isEmpty()) {
            Text(
                text = "Aún no se ingresan ahorros",
                fontFamily = MiFuenteGoogle,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        } else {
            items.forEachIndexed { index, item ->
                SavingsItem(item) { onDetailClick(item) }
                if (index < items.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun SavingsItem(item: SavingItem, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .border(2.dp, item.color, CircleShape)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(item.color.copy(alpha = 0.2f))
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            if (item.date.isNotEmpty()) {
                Text(
                    text = item.date,
                    fontFamily = MiFuenteGoogle,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = item.country,
                fontFamily = MiFuenteGoogle,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = item.cities,
                fontFamily = MiFuenteGoogle,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = formatCurrency(item.value),
            fontFamily = MiFuenteGoogle,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = ">",
            fontFamily = MiFuenteGoogle,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ExpensesChart(items: List<ExpenseItem>, pieData: List<PieChartSlice>, onDetailClick: (ExpenseItem) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        items.forEach { item ->
            ExpenseItem(item) { onDetailClick(item) }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        val totalsByLabel = items.groupBy { it.label }.mapValues { (_, entries) ->
            entries.sumOf { it.amount.toDouble() }.toFloat()
        }
        val filteredPie = totalsByLabel.map { (label, total) ->
            PieChartSlice(
                value = total,
                color = pieData.firstOrNull { it.label.equals(label, ignoreCase = true) }?.color
                    ?: MaterialTheme.colorScheme.primary,
                label = label
            )
        }
        val totalPie = filteredPie.sumOf { it.value.toDouble() }.toFloat()
        
        if (totalPie > 0) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val size = size.minDimension
                    var startAngle = 0f
                    filteredPie.forEach { slice ->
                        val sweep = (slice.value / totalPie) * 360f
                        drawArc(
                            color = slice.color,
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = true,
                            size = Size(size, size)
                        )
                        startAngle += sweep
                    }
                }
            }
        } else {
            Text(
                text = "No hay gastos en este periodo",
                fontFamily = MiFuenteGoogle,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ExpenseItem(item: ExpenseItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(20.dp).border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            if (item.date.isNotEmpty()) {
                Text(
                    text = item.date,
                    fontFamily = MiFuenteGoogle,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = item.label,
                fontFamily = MiFuenteGoogle,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = formatCurrency(item.amount),
            fontFamily = MiFuenteGoogle,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = ">",
            fontFamily = MiFuenteGoogle,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DebtsChart(items: List<DebtItem>, onDetailClick: (DebtItem) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        items.forEach { item ->
            DebtRowItem(item) { onDetailClick(item) }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (items.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .padding(horizontal = 8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val maxVal = items.maxOfOrNull { it.amount } ?: 1f
                    val points = items.mapIndexed { index, debt ->
                        val x = if (items.size > 1) index * (size.width / (items.size - 1)) else size.width / 2
                        val y = size.height - (debt.amount / maxVal) * (size.height - 30f) - 15f
                        androidx.compose.ui.geometry.Offset(x, y)
                    }
                    
                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = Color(0xFFE53935),
                            start = points[i],
                            end = points[i+1],
                            strokeWidth = 4f
                        )
                    }
                    points.forEach { pt ->
                        drawCircle(
                            color = Color(0xFFC62828),
                            radius = 6f,
                            center = pt
                        )
                    }
                }
            }
        } else {
            Text(
                text = "No hay deudas en este periodo",
                fontFamily = MiFuenteGoogle,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DebtRowItem(item: DebtItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(20.dp).border(1.dp, item.color, CircleShape), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(item.color))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            if (item.date.isNotEmpty()) {
                Text(text = item.date, fontFamily = MiFuenteGoogle, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = item.label, fontFamily = MiFuenteGoogle, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
        Text(text = formatCurrency(item.amount), fontFamily = MiFuenteGoogle, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = ">", fontFamily = MiFuenteGoogle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun BottomNavBar(
    onInfoClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onCardsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Far Left: Información ("i")
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                    .clickable { onInfoClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "i",
                    fontFamily = MiFuenteGoogle,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            // 2. Middle Left: Home (Inicio)
            IconButton(onClick = onHomeClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_home),
                    contentDescription = "Inicio",
                    modifier = Modifier.size(26.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // 3. Middle Right: Wallet / Tarjetas
            IconButton(onClick = onCardsClick) {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = "Tarjetas",
                    modifier = Modifier.size(26.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // 4. Far Right: Menu icon (3 lines) -> Editar Perfil
            Column(
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onProfileClick() },
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.5.dp)
                            .background(MaterialTheme.colorScheme.onSurface, shape = RoundedCornerShape(2.dp))
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val sampleUser = UserRepository.users["karen.saldivar@example.com"]!!
    val sampleData = UserRepository.userDataMap[sampleUser.id]!!
    ProyectoFinalTheme {
        HomeScreen(
            user = sampleUser, 
            data = sampleData, 
            onAddClick = {}, 
            onIncomeDetail = {},
            onSavingDetail = {},
            onExpenseDetail = {},
            onDebtDetail = {},
            onInfoClick = {},
            onCardsClick = {}
        )
    }
}

fun formatCurrency(amount: Float): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(amount.toDouble()).replace(".00", "")
}
