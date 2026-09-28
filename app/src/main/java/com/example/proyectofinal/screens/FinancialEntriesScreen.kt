package com.example.proyectofinal.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectofinal.data.DebtItem
import com.example.proyectofinal.data.EntryType
import com.example.proyectofinal.data.ExpenseItem
import com.example.proyectofinal.data.IncomeItem
import com.example.proyectofinal.data.SavingItem
import com.example.proyectofinal.data.SavingFrequency
import com.example.proyectofinal.data.UserFinancialData
import com.example.proyectofinal.ui.theme.MiFuenteGoogle
import com.example.proyectofinal.utils.monthAbbreviation
import com.example.proyectofinal.utils.monthNumberFromDate
import com.example.proyectofinal.utils.periodForMonth
import com.example.proyectofinal.utils.yearFromDate
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialEntriesScreen(
    type: EntryType,
    data: UserFinancialData,
    onBack: () -> Unit,
    onIncomeClick: (IncomeItem) -> Unit,
    onSavingClick: (SavingItem) -> Unit,
    onExpenseClick: (ExpenseItem) -> Unit,
    onDebtClick: (DebtItem) -> Unit,
    onUpdateIncome: (IncomeItem) -> Unit,
    onUpdateSaving: (SavingItem) -> Unit,
    onUpdateExpense: (ExpenseItem) -> Unit,
    onUpdateDebt: (DebtItem) -> Unit,
    onDeleteIncome: (String) -> Unit,
    onDeleteSaving: (String) -> Unit,
    onDeleteExpense: (String) -> Unit,
    onDeleteDebt: (String) -> Unit
) {
    var editingEntry by remember { mutableStateOf<EditableEntry?>(null) }
    var deletingEntry by remember { mutableStateOf<PendingDeletion?>(null) }
    val title = when (type) {
        EntryType.INCOME -> "Ingresos"
        EntryType.SAVING -> "Ahorros"
        EntryType.EXPENSE -> "Gastos"
        EntryType.DEBT -> "Deudas"
    }
    val accent = when (type) {
        EntryType.INCOME -> Color(0xFF388E3C)
        EntryType.SAVING -> Color(0xFF1976D2)
        EntryType.EXPENSE -> Color(0xFFE56A43)
        EntryType.DEBT -> Color(0xFFC62828)
    }
    val icon = when (type) {
        EntryType.INCOME -> Icons.Default.AccountBalanceWallet
        EntryType.SAVING -> Icons.Default.Savings
        EntryType.EXPENSE -> Icons.Default.ShoppingBag
        EntryType.DEBT -> Icons.Default.CreditCard
    }
    val count: Int
    val total: Float
    when (type) {
        EntryType.INCOME -> {
            count = data.incomeItems.size
            total = data.totalIncomes
        }
        EntryType.SAVING -> {
            count = data.savingItems.size
            total = data.totalSavings
        }
        EntryType.EXPENSE -> {
            count = data.expenseItems.size
            total = data.totalExpenses
        }
        EntryType.DEBT -> {
            count = data.debtItems.size
            total = data.totalDebts
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Desglose de $title", fontFamily = MiFuenteGoogle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SummaryCard(title, count, total, accent, icon)
            }
            when (type) {
                EntryType.INCOME -> {
                    if (data.incomeItems.isEmpty()) item { EmptyEntriesMessage(title) }
                    items(data.incomeItems.asReversed(), key = { it.id }) { entry ->
                        EntryCard(
                            title = entry.name,
                            subtitle = "Mes: ${entry.month}",
                            date = entry.date,
                            amount = entry.value,
                            description = entry.description,
                            accent = accent,
                            icon = icon,
                            onClick = { onIncomeClick(entry) },
                            onEdit = {
                                editingEntry = EditableEntry(
                                    type, entry.id, entry.name, entry.value.toString(), entry.date,
                                    entry.month, "", entry.description
                                )
                            },
                            onDelete = {
                                deletingEntry = PendingDeletion(entry.name) { onDeleteIncome(entry.id) }
                            }
                        )
                    }
                }
                EntryType.SAVING -> {
                    if (data.savingItems.isEmpty()) item { EmptyEntriesMessage(title) }
                    items(data.savingItems.asReversed(), key = { it.id }) { entry ->
                        EntryCard(
                            title = entry.country,
                            subtitle = listOf(entry.cities, entry.year).filter { it.isNotBlank() }.joinToString(" • "),
                            date = entry.date,
                            amount = entry.value,
                            description = entry.description,
                            accent = accent,
                            icon = icon,
                            onClick = { onSavingClick(entry) },
                            onEdit = {
                                editingEntry = EditableEntry(
                                    type, entry.id, entry.country, entry.value.toString(), entry.date,
                                    entry.year, entry.cities, entry.description,
                                    photoUrl = entry.photoUrl,
                                    progreso = entry.progreso,
                                    meta = entry.meta.toString(),
                                    fechaFinal = entry.fechaFinal,
                                    frecuencia = entry.frecuencia
                                )
                            },
                            onDelete = {
                                deletingEntry = PendingDeletion(entry.country) { onDeleteSaving(entry.id) }
                            }
                        )
                    }
                }
                EntryType.EXPENSE -> {
                    if (data.expenseItems.isEmpty()) item { EmptyEntriesMessage(title) }
                    items(data.expenseItems.asReversed(), key = { it.id }) { entry ->
                        EntryCard(
                            title = entry.label,
                            subtitle = "Periodo: ${entry.period}",
                            date = entry.date,
                            amount = entry.amount,
                            description = entry.description,
                            accent = accent,
                            icon = icon,
                            onClick = { onExpenseClick(entry) },
                            onEdit = {
                                editingEntry = EditableEntry(
                                    type, entry.id, entry.label, entry.amount.toString(), entry.date,
                                    entry.period, "", entry.description
                                )
                            },
                            onDelete = {
                                deletingEntry = PendingDeletion(entry.label) { onDeleteExpense(entry.id) }
                            }
                        )
                    }
                }
                EntryType.DEBT -> {
                    if (data.debtItems.isEmpty()) item { EmptyEntriesMessage(title) }
                    items(data.debtItems.asReversed(), key = { it.id }) { entry ->
                        EntryCard(
                            title = entry.label,
                            subtitle = "Periodo: ${entry.period}",
                            date = entry.date,
                            amount = entry.amount,
                            description = entry.description,
                            accent = accent,
                            icon = icon,
                            onClick = { onDebtClick(entry) },
                            onEdit = {
                                editingEntry = EditableEntry(
                                    type, entry.id, entry.label, entry.amount.toString(), entry.date,
                                    entry.period, "", entry.description,
                                    progreso = entry.progreso,
                                    fechaFinal = entry.fechaFinal,
                                    frecuencia = entry.frecuencia
                                )
                            },
                            onDelete = {
                                deletingEntry = PendingDeletion(entry.label) { onDeleteDebt(entry.id) }
                            }
                        )
                    }
                }

            }
        }
    }

    editingEntry?.let { entry ->
        EditEntryDialog(
            entry = entry,
            onDismiss = { editingEntry = null },
            onSave = { updated ->
                val monthNumber = monthNumberFromDate(updated.date)
                when (updated.type) {
                    EntryType.INCOME -> onUpdateIncome(
                        data.incomeItems.first { it.id == updated.id }.copy(
                            name = updated.title,
                            value = updated.amount.toFloat(),
                            date = updated.date,
                            month = monthNumber?.let(::monthAbbreviation) ?: updated.group,
                            description = updated.description
                        )
                    )
                    EntryType.SAVING -> onUpdateSaving(
                        data.savingItems.first { it.id == updated.id }.copy(
                            country = updated.title,
                            value = updated.amount.toFloat(),
                            date = updated.date,
                            year = yearFromDate(updated.date) ?: updated.group,
                            cities = updated.subGroup,
                            description = updated.description,
                            photoUrl = updated.photoUrl,
                            progreso = updated.progreso,
                            meta = updated.meta.toFloatOrNull() ?: 0f,
                            fechaFinal = updated.fechaFinal,
                            frecuencia = updated.frecuencia
                        )
                    )
                    EntryType.EXPENSE -> onUpdateExpense(
                        data.expenseItems.first { it.id == updated.id }.copy(
                            label = updated.title,
                            amount = updated.amount.toFloat(),
                            date = updated.date,
                            period = monthNumber?.let { periodForMonth(it, 2) } ?: updated.group,
                            description = updated.description
                        )
                    )
                    EntryType.DEBT -> onUpdateDebt(
                        data.debtItems.first { it.id == updated.id }.copy(
                            label = updated.title,
                            amount = updated.amount.toFloat(),
                            date = updated.date,
                            period = updated.group,
                            description = updated.description,
                            progreso = updated.progreso,
                            fechaFinal = updated.fechaFinal,
                            frecuencia = updated.frecuencia
                        )
                    )
                }
                editingEntry = null
            }
        )
    }

    deletingEntry?.let { pending ->
        AlertDialog(
            onDismissRequest = { deletingEntry = null },
            title = { Text("¿Eliminar registro?", fontFamily = MiFuenteGoogle, fontWeight = FontWeight.Bold) },
            text = { Text("¿Seguro que quieres eliminar \"${pending.title}\"? Esta acción también actualizará Firestore.", fontFamily = MiFuenteGoogle) },
            confirmButton = {
                Button(onClick = {
                    pending.delete()
                    deletingEntry = null
                }) {
                    Text("Sí, eliminar", fontFamily = MiFuenteGoogle)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingEntry = null }) {
                    Text("Cancelar", fontFamily = MiFuenteGoogle)
                }
            }
        )
    }
}

@Composable
private fun SummaryCard(title: String, count: Int, total: Float, accent: Color, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = accent)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Total de ${title.lowercase()}", fontFamily = MiFuenteGoogle, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(formatCurrency(total), fontFamily = MiFuenteGoogle, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                Text("$count ${if (count == 1) "registro" else "registros"}", fontFamily = MiFuenteGoogle, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
            }
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(38.dp))
        }
    }
}

@Composable
private fun EmptyEntriesMessage(title: String) {
    Text(
        text = "Aún no hay registros de $title.",
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontFamily = MiFuenteGoogle
    )
}

@Composable
private fun EntryCard(
    title: String,
    subtitle: String,
    date: String,
    amount: Float,
    description: String,
    accent: Color,
    icon: ImageVector,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, accent.copy(alpha = 0.18f), RoundedCornerShape(18.dp)),
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(accent.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(23.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontFamily = MiFuenteGoogle, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                if (subtitle.isNotBlank()) {
                    Text(subtitle, fontFamily = MiFuenteGoogle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (date.isNotBlank()) {
                    Surface(color = accent.copy(alpha = 0.10f), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            "Fecha: $date",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontFamily = MiFuenteGoogle,
                            fontSize = 10.sp,
                            color = accent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                if (description.isNotBlank()) {
                    Text(description, fontFamily = MiFuenteGoogle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                formatCurrency(amount),
                fontFamily = MiFuenteGoogle,
                fontWeight = FontWeight.ExtraBold,
                color = accent,
                fontSize = 14.sp
            )
            Column {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar $title", tint = accent, modifier = Modifier.size(19.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar $title", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(19.dp))
                }
            }
        }
    }
}

private data class EditableEntry(
    val type: EntryType,
    val id: String,
    val title: String,
    val amount: String,
    val date: String,
    val group: String,
    val subGroup: String,
    val description: String,
    val photoUrl: String = "",
    val progreso: Boolean = false,
    val meta: String = "",
    val fechaFinal: String = "",
    val frecuencia: SavingFrequency = SavingFrequency.SEMANAL
)

private data class PendingDeletion(
    val title: String,
    val delete: () -> Unit
)

@Composable
private fun EditEntryDialog(
    entry: EditableEntry,
    onDismiss: () -> Unit,
    onSave: (EditableEntry) -> Unit
) {
    var title by remember(entry.id) { mutableStateOf(entry.title) }
    var amount by remember(entry.id) { mutableStateOf(entry.amount) }
    var date by remember(entry.id) { mutableStateOf(entry.date) }
    var group by remember(entry.id) { mutableStateOf(entry.group) }
    var subGroup by remember(entry.id) { mutableStateOf(entry.subGroup) }
    var description by remember(entry.id) { mutableStateOf(entry.description) }
    var photoUrl by remember(entry.id) { mutableStateOf(entry.photoUrl) }
    var progreso by remember(entry.id) { mutableStateOf(entry.progreso) }
    var meta by remember(entry.id) { mutableStateOf(entry.meta) }
    var fechaFinal by remember(entry.id) { mutableStateOf(entry.fechaFinal) }
    var frecuencia by remember(entry.id) { mutableStateOf(entry.frecuencia) }
    var showFrequencyMenu by remember { mutableStateOf(false) }
    val parsedAmount = amount.toFloatOrNull()
    val groupLabel = when (entry.type) {
        EntryType.INCOME -> "Mes"
        EntryType.SAVING -> "Año"
        EntryType.EXPENSE, EntryType.DEBT -> "Periodo"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar registro", fontFamily = MiFuenteGoogle, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (entry.type == EntryType.SAVING) "Apartado" else "Concepto") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Monto") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Fecha (dd/MM/aaaa)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = group,
                    onValueChange = { group = it },
                    label = { Text(groupLabel) },
                    singleLine = true
                )
                if (entry.type == EntryType.SAVING) {
                    OutlinedTextField(
                        value = subGroup,
                        onValueChange = { subGroup = it },
                        label = { Text("Subapartado") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = photoUrl,
                        onValueChange = { photoUrl = it },
                        label = { Text("Foto (URL)") },
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") }
                )
                if (entry.type == EntryType.SAVING || entry.type == EntryType.DEBT) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (entry.type == EntryType.SAVING) "Medir meta" else "Recordar fecha de pago",
                            fontFamily = MiFuenteGoogle
                        )
                        Switch(checked = progreso, onCheckedChange = { progreso = it })
                    }
                    if (progreso) {
                        if (entry.type == EntryType.SAVING) {
                            OutlinedTextField(
                                value = meta,
                                onValueChange = { meta = it },
                                label = { Text("Meta de ahorro") },
                                singleLine = true
                            )
                        }
                        OutlinedTextField(
                            value = fechaFinal,
                            onValueChange = { fechaFinal = it },
                            label = { Text("Fecha límite (dd/MM/aaaa)") },
                            singleLine = true
                        )
                        Box {
                            OutlinedButton(onClick = { showFrequencyMenu = true }) {
                                Text("Frecuencia: ${frecuencia.name.lowercase().replaceFirstChar { it.uppercase() }}")
                            }
                            DropdownMenu(
                                expanded = showFrequencyMenu,
                                onDismissRequest = { showFrequencyMenu = false }
                            ) {
                                SavingFrequency.values().forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                        onClick = {
                                            frecuencia = option
                                            showFrequencyMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank() && parsedAmount != null && parsedAmount >= 0f,
                onClick = {
                    parsedAmount?.let {
                        onSave(entry.copy(
                            title = title.trim(),
                            amount = it.toString(),
                            date = date.trim(),
                            group = group.trim(),
                            subGroup = subGroup.trim(),
                            description = description.trim(),
                            photoUrl = photoUrl.trim(),
                            progreso = progreso,
                            meta = meta,
                            fechaFinal = fechaFinal.trim(),
                            frecuencia = frecuencia
                        ))
                    }
                }
            ) {
                Text("Guardar cambios", fontFamily = MiFuenteGoogle)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", fontFamily = MiFuenteGoogle)
            }
        }
    )
}
