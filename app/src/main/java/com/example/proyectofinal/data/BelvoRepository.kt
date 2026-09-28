package com.example.proyectofinal.data

import androidx.compose.ui.graphics.Color
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.HttpException

object BelvoRepository {
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    private suspend fun getBearerToken(): String {
        val user = auth.currentUser ?: throw Exception("Usuario no autenticado")
        val idTokenResult = user.getIdToken(true).await()
        val token = idTokenResult.token?.takeIf { it.isNotBlank() }
            ?: throw Exception("Token de Firebase no disponible")
        return "Bearer $token"
    }

    suspend fun requestAccessToken(): String = withContext(Dispatchers.IO) {
        try {
            val bearer = getBearerToken()
            val response = BelvoRetrofitClient.api.getAccessToken(bearer)
            val tokenString = response.token
            if (tokenString.isBlank()) {
                throw Exception("El servidor no devolvió un access_token válido.")
            }
            tokenString
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val msg = if (!errorBody.isNullOrBlank()) {
                "Error del servidor (${e.code()}): $errorBody"
            } else {
                "Error HTTP ${e.code()} al solicitar access token de Belvo"
            }
            throw Exception(msg, e)
        }
    }

    suspend fun fetchAccounts(linkId: String): List<BankCard> = withContext(Dispatchers.IO) {
        try {
            val bearer = getBearerToken()
            val accounts = BelvoRetrofitClient.api.getAccounts(bearer, BelvoLinkRequest(linkId))
            accounts.map { acc ->
                val typeStr = (acc.type ?: "").uppercase()
                val catStr = (acc.category ?: "").uppercase()
                val isCredit = typeStr.contains("CREDIT") || catStr.contains("CREDIT")

                val balanceVal = (acc.balance ?: 0.0).toFloat()
                val currentBal = (acc.currentBalance ?: acc.balance ?: 0.0).toFloat()
                val usedBal = (acc.usedBalance ?: 0.0).toFloat()

                BankCard(
                    id = acc.id,
                    alias = acc.name,
                    bankName = acc.category ?: "Banco Belvo",
                    cardNumber = "**** ${acc.id.takeLast(4).padStart(4, '0')}",
                    cardHolder = "TITULAR",
                    balance = currentBal,
                    cardColor = if (isCredit) Color(0xFF7B1FA2) else Color(0xFF1E88E5),
                    type = if (isCredit) "Crédito" else "Débito",
                    creditLimit = if (isCredit) balanceVal else 0f,
                    creditUsed = if (isCredit) usedBal else 0f
                )
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val msg = if (!errorBody.isNullOrBlank()) {
                "Error del servidor (${e.code()}): $errorBody"
            } else {
                "Error HTTP ${e.code()} al sincronizar cuentas de Belvo"
            }
            throw Exception(msg, e)
        }
    }

    suspend fun fetchTransactions(linkId: String): List<CardTransaction> = withContext(Dispatchers.IO) {
        try {
            val bearer = getBearerToken()
            val txs = BelvoRetrofitClient.api.getTransactions(bearer, BelvoLinkRequest(linkId))
            txs.map { tx ->
                val isIncome = (tx.type ?: "").equals("INCOME", ignoreCase = true) || tx.amount > 0
                CardTransaction(
                    id = tx.id,
                    cardId = tx.accountId ?: "1",
                    cardAlias = "Tarjeta Belvo",
                    concepto = tx.description ?: "Transacción",
                    monto = Math.abs(tx.amount).toFloat(),
                    tipo = if (isIncome) EntryType.INCOME else EntryType.EXPENSE,
                    fecha = tx.valueDate ?: "Hoy",
                    categoria = "GENERAL",
                    description = tx.description ?: ""
                )
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val msg = if (!errorBody.isNullOrBlank()) {
                "Error del servidor (${e.code()}): $errorBody"
            } else {
                "Error HTTP ${e.code()} al sincronizar transacciones de Belvo"
            }
            throw Exception(msg, e)
        }
    }
}
