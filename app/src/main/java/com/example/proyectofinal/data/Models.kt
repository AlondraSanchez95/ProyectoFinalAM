package com.example.proyectofinal.data

import androidx.compose.ui.graphics.Color
import com.example.proyectofinal.R

enum class EntryType {
    INCOME, SAVING, EXPENSE, DEBT
}

enum class SavingFrequency {
    SEMANAL, QUINCENAL, MENSUAL
}

data class User(
    val id: String = "",
    val nombre: String = "",
    val correo: String = "",
    val avatarRes: Int = R.drawable.ic_user_avatar,
    val apellidoPaterno: String = "",
    val apellidoMaterno: String = "",
    val numero: String = "",
    val fechaNacimiento: String = "",
    val sexo: String = "",
    val avatarUrl: String = ""
) {
    val name: String
        get() = when {
            nombre.isNotEmpty() && apellidoPaterno.isNotEmpty() -> "$nombre $apellidoPaterno".trim()
            nombre.isNotEmpty() -> nombre
            else -> "USUARIO"
        }
    val email: String
        get() = correo.ifEmpty { numero }
}

data class IncomeItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val month: String,
    val value: Float,
    val color: Color,
    val date: String = "",
    val description: String = ""
)

data class SavingItem(
    val year: String = "2024",
    val country: String = "", // Apartado / Categoría
    val cities: String = "", // Subapartado / Concepto
    val color: Color = Color(0xFF03A9F4),
    val value: Float = 0f, // Monto ahorrado actual
    val description: String = "",
    val date: String = "",
    val photoUrl: String = "", // Foto opcional del apartado
    val progreso: Boolean = false, // Toggle si mide progreso hacia una meta
    val meta: Float = 0f, // Meta total a ahorrar
    val fechaFinal: String = "", // Fecha límite
    val frecuencia: SavingFrequency = SavingFrequency.SEMANAL,
    val id: String = java.util.UUID.randomUUID().toString()
)

data class ExpenseItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val label: String,
    val amount: Float,
    val period: String = "ENE-FEB",
    val description: String = "",
    val date: String = ""
)

data class DebtItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val label: String,
    val amount: Float,
    val period: String = "ENE-FEB",
    val description: String = "",
    val date: String = "",
    val color: Color = Color(0xFFE53935),
    val progreso: Boolean = false,
    val fechaFinal: String = "",
    val frecuencia: SavingFrequency = SavingFrequency.SEMANAL
)

data class BankCard(
    val id: String = java.util.UUID.randomUUID().toString(),
    val alias: String,
    val bankName: String,
    val cardNumber: String,
    val cardHolder: String,
    val balance: Float,
    val cardColor: Color = Color(0xFF1E88E5),
    val type: String = "Débito",
    val creditLimit: Float = 0f,
    val creditUsed: Float = 0f
)

data class CardTransaction(
    val id: String = java.util.UUID.randomUUID().toString(),
    val cardId: String = "",
    val cardAlias: String,
    val concepto: String,
    val monto: Float,
    val tipo: EntryType = EntryType.EXPENSE,
    val fecha: String,
    val categoria: String = "GENERAL",
    val description: String = ""
)

data class PieChartSlice(
    val value: Float,
    val color: Color,
    val label: String = ""
)

data class UserFinancialData(
    val totalIncomes: Float = 0f,
    val incomeItems: List<IncomeItem> = emptyList(),
    val totalSavings: Float = 0f,
    val savingItems: List<SavingItem> = emptyList(),
    val totalExpenses: Float = 0f,
    val expenseItems: List<ExpenseItem> = emptyList(),
    val pieChartData: List<PieChartSlice> = emptyList(),
    val totalDebts: Float = 0f,
    val debtItems: List<DebtItem> = emptyList()
)
