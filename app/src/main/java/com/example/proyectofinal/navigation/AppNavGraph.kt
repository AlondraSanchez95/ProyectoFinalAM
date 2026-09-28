package com.example.proyectofinal.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.proyectofinal.data.EntryType
import com.example.proyectofinal.screens.*
import com.example.proyectofinal.viewmodel.FinanceViewModel

private fun navigateToTab(
    navController: androidx.navigation.NavHostController,
    route: String
) {
    navController.navigate(route) {
        popUpTo("home") { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppNavGraph(viewModel: FinanceViewModel) {
    val navController = rememberNavController()
    val startDestination = if (viewModel.currentUser == null) "login" else "home"
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomNavigation = viewModel.currentUser != null &&
        currentRoute !in setOf("login", "register", "forgot_password")

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomNavigation) {
                androidx.compose.foundation.layout.Box(Modifier.navigationBarsPadding()) {
                    BottomNavBar(
                        onInfoClick = { navigateToTab(navController, "info") },
                        onHomeClick = { navigateToTab(navController, "home") },
                        onCardsClick = { navigateToTab(navController, "cards") },
                        onProfileClick = { navigateToTab(navController, "profile") }
                    )
                }
            }
        }
    ) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = androidx.compose.ui.Modifier.padding(contentPadding),
            enterTransition = {
                slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            }
        ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { user ->
                    viewModel.updateCurrentUser(user)
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToForgotPassword = { navController.navigate("forgot_password") }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { user ->
                    viewModel.updateCurrentUser(user)
                    navController.navigate("home") {
                        popUpTo("register") { inclusive = true }
                        popUpTo("login") { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }
        composable("forgot_password") {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("home") {
            val user = viewModel.currentUser
            if (user != null) {
                if (!viewModel.financialDataReady) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (viewModel.financialDataLoading) {
                            CircularProgressIndicator()
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(viewModel.financialDataLoadError ?: "No se pudieron cargar tus datos financieros.")
                                Button(onClick = { viewModel.retryLoadFinancialData() }) {
                                    Text("Reintentar")
                                    }
                            }
                        }
                    }
                } else {
                    val data = viewModel.financialDataMap[user.id] ?: com.example.proyectofinal.data.UserFinancialData()
                    HomeScreen(
                        user = user,
                        data = data,
                        bankCards = viewModel.bankCards,
                        filterMode = viewModel.filterMode,
                        selectedPeriod = viewModel.selectedPeriod,
                        onViewEntries = { type ->
                            navController.navigate("financial_entries/${type.name}")
                        },
                        onFilterModeChange = { viewModel.changeFilterMode(it) },
                        onPeriodChange = { viewModel.changePeriod(it) },
                        onAddClick = { type ->
                            navController.navigate("add_entry/${type.name}")
                        },
                        onIncomeDetail = { item ->
                            viewModel.selectIncome(item)
                            navController.navigate("income_detail")
                        },
                        onSavingDetail = { item ->
                            viewModel.selectSaving(item)
                            navController.navigate("saving_detail")
                        },
                        onExpenseDetail = { item ->
                            viewModel.selectExpense(item)
                            navController.navigate("expense_detail")
                        },
                        onDebtDetail = { item ->
                            viewModel.selectDebt(item)
                            navController.navigate("debt_detail")
                        },
                        onProfileClick = { navController.navigate("profile") },
                        onInfoClick = { navController.navigate("info") },
                        onCardsClick = { navController.navigate("cards") }
                    )
                }

            }
        }
        composable("profile") {
            val user = viewModel.currentUser
            if (user != null) {
                ProfileScreen(
                    user = user,
                    onSaveProfile = { updatedUser, onResult ->
                        viewModel.updateFullProfile(updatedUser, onResult)
                    },
                    onLogout = {
                        viewModel.logout()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable("info") {
            InfoScreen(onBack = { navController.popBackStack() })
        }
        composable("cards") {
            CardsScreen(
                cards = viewModel.bankCards,
                transactions = viewModel.cardTransactions,
                belvoLoading = viewModel.belvoLoading,
                belvoError = viewModel.belvoError,
                onAddCardClick = { navController.navigate("add_card") },
                onDeleteCard = { viewModel.deleteCard(it) },
                onSyncBelvo = {
                    viewModel.requestBelvoAccessToken { _ ->
                        navController.navigate("belvo_webview")
                    }
                },
                onLinkTransaction = { tx, targetType, targetCat ->
                    viewModel.linkTransactionToSection(tx, targetType, targetCat)
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("add_card") {
            AddCardScreen(
                onSaveCard = { card ->
                    viewModel.addCard(card)
                    navController.popBackStack()
                },
                isLoading = viewModel.belvoLoading,
                belvoError = viewModel.belvoError,
                onStartBelvoSync = {
                    viewModel.requestBelvoAccessToken { _ ->
                        navController.navigate("belvo_webview")
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("belvo_webview") {
            BelvoWebViewScreen(
                accessToken = viewModel.belvoAccessToken.orEmpty(),
                isSyncing = viewModel.belvoLoading,
                syncError = viewModel.belvoError,
                onLinkSuccess = { linkId ->
                    viewModel.syncBelvoLinkId(linkId) {
                        navController.popBackStack()
                        navController.popBackStack()
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("income_detail") {
            val item = viewModel.selectedIncomeItem
            if (item != null) {
                IncomeDetailScreen(
                    item = item,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable("saving_detail") {
            val item = viewModel.selectedSavingItem
            if (item != null) {
                SavingDetailScreen(
                    item = item,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable("expense_detail") {
            val item = viewModel.selectedExpenseItem
            if (item != null) {
                ExpenseDetailScreen(
                    item = item,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable("debt_detail") {
            val item = viewModel.selectedDebtItem
            if (item != null) {
                DebtDetailScreen(
                    item = item,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable(
            route = "financial_entries/{type}",
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val typeName = backStackEntry.arguments?.getString("type") ?: EntryType.INCOME.name
            val type = EntryType.valueOf(typeName)
            val user = viewModel.currentUser
            val data = user?.let { viewModel.financialDataMap[it.id] }
            if (data != null) {
                FinancialEntriesScreen(
                    type = type,
                    data = data,
                    onBack = { navController.popBackStack() },
                    onIncomeClick = { item ->
                        viewModel.selectIncome(item)
                        navController.navigate("income_detail")
                    },
                    onSavingClick = { item ->
                        viewModel.selectSaving(item)
                        navController.navigate("saving_detail")
                    },
                    onExpenseClick = { item ->
                        viewModel.selectExpense(item)
                        navController.navigate("expense_detail")
                    },
                    onDebtClick = { item ->
                        viewModel.selectDebt(item)
                        navController.navigate("debt_detail")
                    },
                    onUpdateIncome = { viewModel.updateIncomeEntry(it) },
                    onUpdateSaving = { viewModel.updateSavingEntry(it) },
                    onUpdateExpense = { viewModel.updateExpenseEntry(it) },
                    onUpdateDebt = { viewModel.updateDebtEntry(it) },
                    onDeleteIncome = { viewModel.deleteIncomeEntry(it) },
                    onDeleteSaving = { viewModel.deleteSavingEntry(it) },
                    onDeleteExpense = { viewModel.deleteExpenseEntry(it) },
                    onDeleteDebt = { viewModel.deleteDebtEntry(it) }
                )
            }
        }
        composable(
            route = "add_entry/{type}",
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val typeStr = backStackEntry.arguments?.getString("type") ?: EntryType.INCOME.name
            val entryType = try { EntryType.valueOf(typeStr) } catch (e: Exception) { EntryType.INCOME }
            val user = viewModel.currentUser
            val userData = user?.let { viewModel.financialDataMap[it.id] }
            val existingApartados = userData?.savingItems?.map { it.country }?.distinct() ?: emptyList()

            AddEntryScreen(
                type = entryType,
                existingApartados = existingApartados,
                onBack = { navController.popBackStack() },
                onSave = { name, amount, extra, category, subCategory, desc ->
                    if (entryType != EntryType.SAVING && entryType != EntryType.DEBT) {
                        viewModel.addEntry(entryType, name, amount, extra, category, subCategory, desc)
                        navController.popBackStack()
                    }
                },
                onSaveSaving = { _, amount, date, category, subCategory, desc, photoUrl, progreso, meta, fechaFinal, frecuencia ->
                    viewModel.addSavingEntry(amount, date, category, subCategory, desc, photoUrl, progreso, meta, fechaFinal, frecuencia)
                    navController.popBackStack()
                },
                onSaveDebt = { _, amount, date, period, concepto, desc, progreso, fechaFinal, frecuencia ->
                    viewModel.addDebtEntry(amount, date, period, concepto, desc, progreso, fechaFinal, frecuencia)
                    navController.popBackStack()
                }
            )
        }
    }
    }
}
