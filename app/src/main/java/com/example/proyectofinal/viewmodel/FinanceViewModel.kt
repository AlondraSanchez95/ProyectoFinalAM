package com.example.proyectofinal.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectofinal.data.*
import com.example.proyectofinal.utils.monthAbbreviation
import com.example.proyectofinal.utils.monthNumberFromDate
import com.example.proyectofinal.utils.periodForMonth
import com.example.proyectofinal.utils.yearFromDate
import com.example.proyectofinal.utils.NotificationHelper
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.launch
import java.util.Calendar

enum class FilterMode {
    MONTH, BIMESTER, TRIMESTER, SEMESTER
}

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val financialDataSaveMutex = Mutex()
    private var financialDataLoadedUserId: String? = null

    var financialDataReady by mutableStateOf(false)
        private set

    var financialDataLoading by mutableStateOf(false)
        private set

    var financialDataLoadError by mutableStateOf<String?>(null)
        private set

    var currentUser by mutableStateOf<User?>(null)
        private set

    init {
        val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            val user = User(
                id = firebaseUser.uid,
                correo = firebaseUser.email ?: "",
                nombre = firebaseUser.displayName ?: "Usuario"
            )
            updateCurrentUser(user)
            FirebaseRepository.getUserProfile(firebaseUser.uid) { savedProfile ->
                if (currentUser?.id == firebaseUser.uid && savedProfile != null) {
                    updateCurrentUser(savedProfile)
                }
            }
        }
    }

    var financialDataMap by mutableStateOf(mutableMapOf<String, UserFinancialData>())
        private set

    var selectedIncomeItem by mutableStateOf<IncomeItem?>(null)
        private set

    var selectedSavingItem by mutableStateOf<SavingItem?>(null)
        private set

    var selectedExpenseItem by mutableStateOf<ExpenseItem?>(null)
        private set

    var selectedDebtItem by mutableStateOf<DebtItem?>(null)
        private set

    var bankCards by mutableStateOf<List<BankCard>>(emptyList())
        private set

    var cardTransactions by mutableStateOf<List<CardTransaction>>(emptyList())
        private set

    var belvoAccessToken by mutableStateOf<String?>(null)
        private set

    var belvoLoading by mutableStateOf(false)
        private set

    var belvoError by mutableStateOf<String?>(null)
        private set

    var filterMode by mutableStateOf(FilterMode.BIMESTER)
        private set

    var selectedPeriod by mutableStateOf("TODOS")
        private set

    fun requestBelvoAccessToken(onTokenReady: (String) -> Unit) {
        viewModelScope.launch {
            belvoLoading = true
            belvoError = null
            try {
                val token = BelvoRepository.requestAccessToken()

                if (!token.isNullOrBlank()) {
                    belvoAccessToken = token
                    belvoLoading = false
                    onTokenReady(token)
                } else {
                    throw Exception("El token devuelto por el servidor está vacío.")
                }
            } catch (e: Exception) {
                belvoLoading = false
                belvoError = e.localizedMessage ?: "No se pudo obtener el acceso a Belvo. Verifica tu conexión o servidor."
                android.util.Log.e("BelvoError", "Error al solicitar el access token de Belvo", e)
            }
        }
    }

    fun syncBelvoLinkId(linkId: String, onSynced: () -> Unit) {
        viewModelScope.launch {
            belvoLoading = true
            belvoError = null
            try {
                val accounts = BelvoRepository.fetchAccounts(linkId)
                val txs = BelvoRepository.fetchTransactions(linkId)
                bankCards = bankCards + accounts
                cardTransactions = cardTransactions + txs
                persistFinancialData()
                belvoLoading = false

                accounts.forEach { card ->
                    NotificationHelper.showBelvoCardLinkedNotification(getApplication(), card.alias)
                }
                onSynced()
            } catch (e: Exception) {
                belvoLoading = false
                belvoError = e.localizedMessage ?: "Error al sincronizar cuentas de Belvo"
            }
        }
    }

    fun triggerMotivationalNotification() {
        NotificationHelper.showMotivationalSavingNotification(getApplication())
    }

    fun changeFilterMode(mode: FilterMode) {
        filterMode = mode
        selectedPeriod = "TODOS"
    }

    fun changePeriod(period: String) {
        selectedPeriod = period
    }

    fun updateCurrentUser(user: User) {
        val isDifferentUser = currentUser?.id != user.id
        currentUser = user
        if (isDifferentUser) {
            financialDataLoadedUserId = null
            financialDataReady = false
            financialDataLoadError = null
            bankCards = emptyList()
            cardTransactions = emptyList()
        }
        if (!financialDataMap.containsKey(user.id)) {
            financialDataMap = financialDataMap.toMutableMap().apply {
                put(
                    user.id,
                    UserFinancialData(
                        totalIncomes = 0f,
                        incomeItems = emptyList(),
                        totalSavings = 0f,
                        savingItems = emptyList(),
                        totalExpenses = 0f,
                        expenseItems = emptyList(),
                        pieChartData = emptyList(),
                        totalDebts = 0f,
                        debtItems = emptyList()
                    )
                )
            }
        }
        if (financialDataLoadedUserId == user.id) return

        loadFinancialData(user)
    }

    fun retryLoadFinancialData() {
        currentUser?.let(::loadFinancialData)
    }

    private fun loadFinancialData(user: User) {
        financialDataLoading = true
        financialDataLoadError = null
        FirebaseRepository.loadFinancialData(user.id) { data, cards, transactions, error ->
            if (currentUser?.id != user.id) return@loadFinancialData
            if (error != null) {
                android.util.Log.e("FinanceData", "No se pudieron cargar los datos de Firestore para el usuario ${user.id}", error)
                financialDataLoading = false
                financialDataLoadError = error.localizedMessage ?: "No se pudieron cargar tus datos financieros."
                return@loadFinancialData
            }
            financialDataMap = financialDataMap.toMutableMap().apply {
                put(user.id, data ?: UserFinancialData())
            }
            bankCards = cards
            cardTransactions = transactions
            financialDataLoadedUserId = user.id
            financialDataReady = true
            financialDataLoading = false
        }
    }

    fun logout() {
        FirebaseRepository.logout()
        currentUser = null
        financialDataLoadedUserId = null
        financialDataReady = false
        financialDataLoading = false
        financialDataLoadError = null
        bankCards = emptyList()
        cardTransactions = emptyList()
    }

    fun updateProfile(newName: String, newEmail: String) {
        val user = currentUser ?: return
        currentUser = user.copy(nombre = newName, correo = newEmail)
    }

    fun updateFullProfile(updatedUser: User, onResult: (User?, String?) -> Unit) {
        val activeUser = currentUser
        if (activeUser == null || activeUser.id != updatedUser.id) {
            onResult(null, "No se pudo validar el usuario actual.")
            return
        }
        viewModelScope.launch {
            try {
                val savedUser = FirebaseRepository.saveUserProfile(getApplication(), updatedUser)
                if (currentUser?.id == savedUser.id) {
                    currentUser = savedUser
                }
                onResult(savedUser, null)
            } catch (error: Exception) {
                android.util.Log.e("UserProfile", "No se pudo guardar el perfil en Firestore", error)
                onResult(null, error.localizedMessage ?: "No se pudo guardar el perfil.")
            }
        }
    }

    fun selectIncome(item: IncomeItem) {
        selectedIncomeItem = item
    }

    fun selectSaving(item: SavingItem) {
        selectedSavingItem = item
    }

    fun selectExpense(item: ExpenseItem) {
        selectedExpenseItem = item
    }

    fun selectDebt(item: DebtItem) {
        selectedDebtItem = item
    }

    fun updateIncomeEntry(item: IncomeItem) {
        val user = currentUser ?: return
        val currentData = financialDataMap[user.id] ?: return
        val items = currentData.incomeItems.map { if (it.id == item.id) item else it }
        updateFinancialData(user.id, currentData.copy(
            totalIncomes = items.sumOf { it.value.toDouble() }.toFloat(),
            incomeItems = items
        ))
        if (selectedIncomeItem?.id == item.id) selectedIncomeItem = item
    }

    fun deleteIncomeEntry(id: String) {
        val user = currentUser ?: return
        val currentData = financialDataMap[user.id] ?: return
        val items = currentData.incomeItems.filterNot { it.id == id }
        updateFinancialData(user.id, currentData.copy(
            totalIncomes = items.sumOf { it.value.toDouble() }.toFloat(),
            incomeItems = items
        ))
        if (selectedIncomeItem?.id == id) selectedIncomeItem = null
    }

    fun updateSavingEntry(item: SavingItem) {
        val user = currentUser ?: return
        val currentData = financialDataMap[user.id] ?: return
        val items = currentData.savingItems.map { if (it.id == item.id) item else it }
        updateFinancialData(user.id, currentData.copy(
            totalSavings = items.sumOf { it.value.toDouble() }.toFloat(),
            savingItems = items
        ))
        if (selectedSavingItem?.id == item.id) selectedSavingItem = item
    }

    fun deleteSavingEntry(id: String) {
        val user = currentUser ?: return
        val currentData = financialDataMap[user.id] ?: return
        val items = currentData.savingItems.filterNot { it.id == id }
        updateFinancialData(user.id, currentData.copy(
            totalSavings = items.sumOf { it.value.toDouble() }.toFloat(),
            savingItems = items
        ))
        if (selectedSavingItem?.id == id) selectedSavingItem = null
    }

    fun updateExpenseEntry(item: ExpenseItem) {
        val user = currentUser ?: return
        val currentData = financialDataMap[user.id] ?: return
        val items = currentData.expenseItems.map { if (it.id == item.id) item else it }
        updateFinancialData(user.id, currentData.copy(
            totalExpenses = items.sumOf { it.amount.toDouble() }.toFloat(),
            expenseItems = items,
            pieChartData = buildExpensePieChart(items, currentData.pieChartData)
        ))
        if (selectedExpenseItem?.id == item.id) selectedExpenseItem = item
    }

    fun deleteExpenseEntry(id: String) {
        val user = currentUser ?: return
        val currentData = financialDataMap[user.id] ?: return
        val items = currentData.expenseItems.filterNot { it.id == id }
        updateFinancialData(user.id, currentData.copy(
            totalExpenses = items.sumOf { it.amount.toDouble() }.toFloat(),
            expenseItems = items,
            pieChartData = buildExpensePieChart(items, currentData.pieChartData)
        ))
        if (selectedExpenseItem?.id == id) selectedExpenseItem = null
    }

    fun updateDebtEntry(item: DebtItem) {
        val user = currentUser ?: return
        val currentData = financialDataMap[user.id] ?: return
        val items = currentData.debtItems.map { if (it.id == item.id) item else it }
        updateFinancialData(user.id, currentData.copy(
            totalDebts = items.sumOf { it.amount.toDouble() }.toFloat(),
            debtItems = items
        ))
        if (selectedDebtItem?.id == item.id) selectedDebtItem = item
    }

    fun deleteDebtEntry(id: String) {
        val user = currentUser ?: return
        val currentData = financialDataMap[user.id] ?: return
        val items = currentData.debtItems.filterNot { it.id == id }
        updateFinancialData(user.id, currentData.copy(
            totalDebts = items.sumOf { it.amount.toDouble() }.toFloat(),
            debtItems = items
        ))
        if (selectedDebtItem?.id == id) selectedDebtItem = null
    }

    private fun updateFinancialData(userId: String, data: UserFinancialData) {
        financialDataMap = financialDataMap.toMutableMap().apply {
            put(userId, data)
        }
        persistFinancialData(userId, data)
    }

    private fun buildExpensePieChart(
        expenses: List<ExpenseItem>,
        previousSlices: List<PieChartSlice>
    ): List<PieChartSlice> {
        val oldColors = previousSlices.associate { it.label.lowercase() to it.color }
        return expenses.groupBy { it.label }.map { (label, entries) ->
            PieChartSlice(
                value = entries.sumOf { it.amount.toDouble() }.toFloat(),
                color = oldColors[label.lowercase()] ?: Color(0xFF513F8B),
                label = label
            )
        }
    }

    fun linkTransactionToSection(
        transaction: CardTransaction,
        targetType: EntryType,
        targetCategory: String = ""
    ) {
        val user = currentUser ?: return
        val currentData = financialDataMap[user.id] ?: return
        val monthNumber = monthNumberFromDate(transaction.fecha)
            ?: Calendar.getInstance().get(Calendar.MONTH) + 1
        val currentBimester = periodForMonth(monthNumber, 2) ?: "ENE-FEB"

        val updatedData = when (targetType) {
            EntryType.INCOME -> {
                val newIncome = IncomeItem(
                    name = transaction.concepto,
                    month = monthAbbreviation(monthNumber) ?: "NUEVO",
                    value = transaction.monto,
                    color = Color(0xFF4CAF50),
                    date = transaction.fecha,
                    description = "Vinculado de tarjeta (${transaction.cardAlias})"
                )
                currentData.copy(
                    totalIncomes = currentData.totalIncomes + transaction.monto,
                    incomeItems = currentData.incomeItems + newIncome
                )
            }
            EntryType.SAVING -> {
                val cat = targetCategory.ifEmpty { transaction.categoria.ifEmpty { "APARTADO " + transaction.cardAlias } }
                val newSaving = SavingItem(
                    year = yearFromDate(transaction.fecha) ?: Calendar.getInstance().get(Calendar.YEAR).toString(),
                    country = cat,
                    cities = transaction.concepto,
                    color = Color(0xFF03A9F4),
                    value = transaction.monto,
                    description = "Vinculado de tarjeta (${transaction.cardAlias})",
                    date = transaction.fecha
                )
                currentData.copy(
                    totalSavings = currentData.totalSavings + transaction.monto,
                    savingItems = currentData.savingItems + newSaving
                )
            }
            EntryType.EXPENSE -> {
                val newExpense = ExpenseItem(
                    label = transaction.concepto,
                    amount = transaction.monto,
                    period = currentBimester,
                    description = "Vinculado de tarjeta (${transaction.cardAlias})",
                    date = transaction.fecha
                )
                val newPieSlice = PieChartSlice(transaction.monto, Color((0..0xFFFFFF).random() or 0xFF000000.toInt()), transaction.concepto)
                currentData.copy(
                    totalExpenses = currentData.totalExpenses + transaction.monto,
                    expenseItems = currentData.expenseItems + newExpense,
                    pieChartData = currentData.pieChartData + newPieSlice
                )
            }
            EntryType.DEBT -> {
                val newDebt = DebtItem(
                    label = transaction.concepto,
                    amount = transaction.monto,
                    period = currentBimester,
                    description = "Vinculado de tarjeta (${transaction.cardAlias})",
                    date = transaction.fecha
                )
                currentData.copy(
                    totalDebts = currentData.totalDebts + transaction.monto,
                    debtItems = currentData.debtItems + newDebt
                )
            }
        }

        financialDataMap = financialDataMap.toMutableMap().apply {
            put(user.id, updatedData)
        }
        persistFinancialData(user.id, updatedData)
    }

    fun addSavingEntry(
        amount: Float,
        date: String,
        category: String,
        subCategory: String,
        description: String,
        photoUrl: String,
        progreso: Boolean,
        meta: Float,
        fechaFinal: String,
        frecuencia: SavingFrequency
    ) {
        val user = currentUser ?: return
        val currentData = financialDataMap[user.id] ?: return

        val finalSub = if (subCategory == "OTRO" || subCategory.isEmpty()) "" else subCategory
        val newSaving = SavingItem(
            year = yearFromDate(date) ?: Calendar.getInstance().get(Calendar.YEAR).toString(),
            country = category,
            cities = finalSub,
            color = Color(0xFF03A9F4),
            value = amount,
            description = description,
            date = date,
            photoUrl = photoUrl,
            progreso = progreso,
            meta = meta,
            fechaFinal = fechaFinal,
            frecuencia = frecuencia
        )

        val updatedData = currentData.copy(
            totalSavings = currentData.totalSavings + amount,
            savingItems = currentData.savingItems + newSaving
        )

        financialDataMap = financialDataMap.toMutableMap().apply {
            put(user.id, updatedData)
        }
        persistFinancialData(user.id, updatedData)
    }

    fun addDebtEntry(
        amount: Float,
        date: String,
        period: String,
        concepto: String,
        description: String,
        progreso: Boolean,
        fechaFinal: String,
        frecuencia: SavingFrequency
    ) {
        val user = currentUser ?: return
        val currentData = financialDataMap[user.id] ?: return
        val finalPeriod = period.trim()
        val finalConcepto = concepto.ifEmpty { "Deuda" }

        val newDebtItems = currentData.debtItems + DebtItem(
            label = finalConcepto,
            amount = amount,
            period = finalPeriod,
            description = description,
            date = date,
            progreso = progreso,
            fechaFinal = fechaFinal,
            frecuencia = frecuencia
        )

        val updatedData = currentData.copy(
            totalDebts = currentData.totalDebts + amount,
            debtItems = newDebtItems
        )

        financialDataMap = financialDataMap.toMutableMap().apply {
            put(user.id, updatedData)
        }
        persistFinancialData(user.id, updatedData)
    }

    fun addCard(card: BankCard) {
        bankCards = bankCards + card
        persistFinancialData()
    }

    fun deleteCard(cardId: String) {
        bankCards = bankCards.filter { it.id != cardId }
        persistFinancialData()
    }

    fun addEntry(
        type: EntryType,
        name: String,
        amount: Float,
        extra: String,
        category: String,
        subCategory: String,
        description: String
    ) {
        val user = currentUser ?: return
        val currentData = financialDataMap[user.id] ?: return
        
        val updatedData = when (type) {
            EntryType.INCOME -> {
                val monthAbr = monthNumberFromDate(extra)?.let(::monthAbbreviation) ?: "NUEVO"
                val newIncomeItems = currentData.incomeItems + IncomeItem(
                    name = name,
                    month = monthAbr,
                    value = amount,
                    color = Color(0xFF4CAF50),
                    date = extra,
                    description = description
                )
                
                currentData.copy(
                    totalIncomes = currentData.totalIncomes + amount,
                    incomeItems = newIncomeItems
                )
            }
            EntryType.SAVING -> {
                val finalSub = if (subCategory == "OTRO" || subCategory.isEmpty()) "" else subCategory
                val newSavingItems = currentData.savingItems + SavingItem(
                    year = yearFromDate(extra) ?: Calendar.getInstance().get(Calendar.YEAR).toString(),
                    country = category,
                    cities = finalSub,
                    color = Color(0xFF03A9F4),
                    value = amount,
                    description = description,
                    date = extra
                )
                
                currentData.copy(
                    totalSavings = currentData.totalSavings + amount,
                    savingItems = newSavingItems
                )
            }
            EntryType.EXPENSE -> {
                val monthNumber = monthNumberFromDate(extra)
                val period = monthNumber?.let { periodForMonth(it, 2) } ?: category.ifEmpty { "ENE-FEB" }
                val finalConcepto = subCategory.ifEmpty { name }
                val newExpenseItems = currentData.expenseItems + ExpenseItem(
                    label = finalConcepto,
                    amount = amount,
                    period = period,
                    description = description,
                    date = extra
                )
                
                val existingSliceIndex = currentData.pieChartData.indexOfFirst { it.label.equals(finalConcepto, ignoreCase = true) }
                val newPieChartData = if (existingSliceIndex != -1) {
                    currentData.pieChartData.mapIndexed { index, slice ->
                        if (index == existingSliceIndex) slice.copy(value = slice.value + amount) else slice
                    }
                } else {
                    currentData.pieChartData + PieChartSlice(amount, Color((0..0xFFFFFF).random() or 0xFF000000.toInt()), finalConcepto)
                }
                
                currentData.copy(
                    totalExpenses = currentData.totalExpenses + amount,
                    expenseItems = newExpenseItems,
                    pieChartData = newPieChartData
                )
            }
            EntryType.DEBT -> {
                val finalConcepto = subCategory.ifEmpty { name }
                val newDebtItems = currentData.debtItems + DebtItem(
                    label = finalConcepto,
                    amount = amount,
                    period = category.trim(),
                    description = description,
                    date = extra
                )
                
                currentData.copy(
                    totalDebts = currentData.totalDebts + amount,
                    debtItems = newDebtItems
                )
            }
        }
        
        financialDataMap = financialDataMap.toMutableMap().apply {
            put(user.id, updatedData)
        }
        persistFinancialData(user.id, updatedData)
    }

    private fun persistFinancialData(
        userId: String? = currentUser?.id,
        data: UserFinancialData? = null
    ) {
        val resolvedUserId = userId ?: return
        val resolvedData = data ?: financialDataMap[resolvedUserId] ?: return
        if (financialDataLoadedUserId != resolvedUserId) return
        val cardsSnapshot = bankCards
        val transactionsSnapshot = cardTransactions

        viewModelScope.launch {
            financialDataSaveMutex.withLock {
                try {
                    FirebaseRepository.saveFinancialData(
                        userId = resolvedUserId,
                        data = resolvedData,
                        cards = cardsSnapshot,
                        transactions = transactionsSnapshot
                    )
                } catch (error: Exception) {
                    android.util.Log.e("FinanceData", "No se pudieron guardar los datos de Firestore para el usuario $resolvedUserId", error)
                }
            }
        }
    }
}
