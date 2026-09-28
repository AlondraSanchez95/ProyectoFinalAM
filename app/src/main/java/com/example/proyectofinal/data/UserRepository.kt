package com.example.proyectofinal.data

import androidx.compose.ui.graphics.Color
import com.example.proyectofinal.R

object UserRepository {
    val users = mapOf(
        "berenicecortesjjk1997@gmail.com" to User(id = "1", nombre = "BERENICE CORTES", correo = "berenicecortesjjk1997@gmail.com", avatarRes = R.drawable.ic_user_avatar),
        "juanbenitez@gmail.com" to User(id = "2", nombre = "JUAN BENITEZ", correo = "juanbenitez@gmail.com", avatarRes = R.drawable.ic_user_avatar),
        "karen.saldivar@example.com" to User(id = "3", nombre = "KAREN SALDIVAR", correo = "karen.saldivar@example.com", avatarRes = R.drawable.ic_user_avatar)
    )

    val credentials = mapOf(
        "berenicecortesjjk1997@gmail.com" to "Johnny1997@",
        "juanbenitez@gmail.com" to "Juan123",
        "karen.saldivar@example.com" to "Karen123"
    )

    val userDataMap = mapOf(
        "1" to UserFinancialData(
            totalIncomes = 12000f,
            incomeItems = listOf(
                IncomeItem(name = "NOMINA", month = "ENE", value = 4000f, color = Color(0xFF4CAF50), date = "15/01/2024"),
                IncomeItem(name = "BONO", month = "FEB", value = 3500f, color = Color(0xFF8BC34A), date = "10/02/2024"),
                IncomeItem(name = "FREELANCE", month = "MAR", value = 4500f, color = Color(0xFFCDDC39), date = "20/03/2024")
            ),
            totalSavings = 10000f,
            savingItems = listOf(
                SavingItem(year = "2025", country = "JAPAN", cities = "TOKYO, OSAKA", color = Color(0xFFFF5722), value = 10000f)
            ),
            totalExpenses = 8000f,
            expenseItems = listOf(
                ExpenseItem(label = "RENTA", amount = 5000f, period = "ENE-FEB"),
                ExpenseItem(label = "COMIDA", amount = 2000f, period = "ENE-FEB"),
                ExpenseItem(label = "TRANSPORTE", amount = 1000f, period = "ENE-FEB")
            ),
            pieChartData = listOf(
                PieChartSlice(5000f, Color(0xFFEF9A9A), "RENTA"),
                PieChartSlice(2000f, Color(0xFF81D4FA), "COMIDA"),
                PieChartSlice(1000f, Color(0xFFCE93D8), "TRANSPORTE")
            ),
            totalDebts = 3000f,
            debtItems = listOf(
                DebtItem(label = "TARJETA CREDITO", amount = 2000f, period = "ENE-FEB", description = "Visa BBVA", date = "05/01/2024", color = Color(0xFFE53935)),
                DebtItem(label = "PRESTAMO AUTO", amount = 1000f, period = "MAR-ABR", description = "Credito Bancomer", date = "12/03/2024", color = Color(0xFFD32F2F))
            )
        ),
        "2" to UserFinancialData(
            totalIncomes = 5000f,
            incomeItems = listOf(
                IncomeItem(name = "SUELDO", month = "ENE", value = 2000f, color = Color(0xFF2196F3), date = "01/01/2024"),
                IncomeItem(name = "VENTA", month = "FEB", value = 3000f, color = Color(0xFF3F51B5), date = "15/02/2024")
            ),
            totalSavings = 5000f,
            savingItems = listOf(
                SavingItem(year = "2026", country = "CANADA", cities = "TORONTO, VANCOUVER", color = Color(0xFF4CAF50), value = 5000f)
            ),
            totalExpenses = 12000f,
            expenseItems = listOf(
                ExpenseItem(label = "DEUDAS", amount = 8000f, period = "ENE-FEB"),
                ExpenseItem(label = "SERVICIOS", amount = 4000f, period = "ENE-FEB")
            ),
            pieChartData = listOf(
                PieChartSlice(8000f, Color(0xFFEF9A9A), "DEUDAS"),
                PieChartSlice(4000f, Color(0xFF81D4FA), "SERVICIOS")
            ),
            totalDebts = 4000f,
            debtItems = listOf(
                DebtItem(label = "HIPOTECA", amount = 4000f, period = "ENE-FEB", description = "Credito hipotecario", date = "01/01/2024", color = Color(0xFFC62828))
            )
        ),
        "3" to UserFinancialData(
            totalIncomes = 10750f,
            incomeItems = listOf(
                IncomeItem(name = "INGRESO 1", month = "ENE", value = 2500f, color = Color(0xFF2196F3), date = "05/01/2024"),
                IncomeItem(name = "INGRESO 2", month = "FEB", value = 2000f, color = Color(0xFFFF7043), date = "12/02/2024"),
                IncomeItem(name = "INGRESO 3", month = "MAR", value = 3000f, color = Color(0xFFEF5350), date = "18/03/2024"),
                IncomeItem(name = "INGRESO 4", month = "ABR", value = 1500f, color = Color(0xFF7E57C2), date = "25/04/2024"),
                IncomeItem(name = "INGRESO 5", month = "MAY", value = 1750f, color = Color(0xFF3949AB), date = "30/05/2024")
            ),
            totalSavings = 22000f,
            savingItems = listOf(
                SavingItem(year = "2027", country = "ESPANA", cities = "BARCELONA, MADRID, IBIZA", color = Color(0xFF03A9F4), value = 12000f),
                SavingItem(year = "2028", country = "MEXICO", cities = "CDMX, PUEBLA, MTY", color = Color(0xFF513F8B), value = 10000f)
            ),
            totalExpenses = 3800f,
            expenseItems = listOf(
                ExpenseItem(label = "MAQUILLAJE", amount = 1500f, period = "ENE-FEB"),
                ExpenseItem(label = "COMIDAS", amount = 850f, period = "ENE-FEB"),
                ExpenseItem(label = "GASOLINA", amount = 1450f, period = "ENE-FEB")
            ),
            pieChartData = listOf(
                PieChartSlice(1500f, Color(0xFFEF9A9A), "MAQUILLAJE"),
                PieChartSlice(850f, Color(0xFF81D4FA), "COMIDAS"),
                PieChartSlice(1450f, Color(0xFFCE93D8), "GASOLINA")
            ),
            totalDebts = 1500f,
            debtItems = listOf(
                DebtItem(label = "TARJETA LIVERPOOL", amount = 1500f, period = "ENE-FEB", description = "Compra ropa", date = "10/01/2024", color = Color(0xFFE53935))
            )
        )
    )
}
