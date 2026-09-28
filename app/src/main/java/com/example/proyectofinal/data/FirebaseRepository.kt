package com.example.proyectofinal.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.ui.graphics.Color
import com.example.proyectofinal.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException

object FirebaseRepository {
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    val currentUserId: String?
        get() = auth.currentUser?.uid

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val webClientId = context.getString(R.string.default_web_client_id)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .requestProfile()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    fun firebaseAuthWithGoogle(idToken: String, onResult: (Boolean, String?, User?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val uid = firebaseUser?.uid ?: ""
                    val displayName = firebaseUser?.displayName ?: ""
                    val email = firebaseUser?.email ?: ""
                    val photoUrl = firebaseUser?.photoUrl?.toString() ?: ""

                    val user = User(
                        id = uid,
                        nombre = displayName,
                        correo = email,
                        avatarUrl = photoUrl
                    )

                    val userMap = mapOf(
                        "id" to uid,
                        "nombre" to user.nombre,
                        "correo" to user.correo,
                        "avatarUrl" to user.avatarUrl
                    )

                    val userDocument = db.collection("users").document(uid)
                    userDocument.get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                onResult(true, null, userFromDocument(document, uid))
                            } else {
                                userDocument.set(userMap)
                                    .addOnSuccessListener { onResult(true, null, user) }
                                    .addOnFailureListener { error ->
                                        onResult(
                                            false,
                                            "Error al guardar perfil de Google en Firestore: ${error.localizedMessage}",
                                            null
                                        )
                                    }
                            }
                        }
                        .addOnFailureListener { error ->
                            onResult(
                                false,
                                "Error al leer el perfil de Google en Firestore: ${error.localizedMessage}",
                                null
                            )
                        }
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Error en autenticación con Google", null)
                }
            }
    }

    fun registerUser(
        nombre: String,
        apellidoPaterno: String,
        apellidoMaterno: String,
        correo: String,
        numero: String,
        fechaNacimiento: String,
        sexo: String,
        contrasena: String,
        avatarUrl: String = "",
        onResult: (Boolean, String?, User?) -> Unit
    ) {
        if (correo.isEmpty()) {
            onResult(false, "El correo electrónico es obligatorio para el registro", null)
            return
        }

        auth.createUserWithEmailAndPassword(correo.trim(), contrasena)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""
                    val newUser = User(
                        id = uid,
                        nombre = nombre.trim(),
                        apellidoPaterno = apellidoPaterno.trim(),
                        apellidoMaterno = apellidoMaterno.trim(),
                        correo = correo.trim(),
                        numero = numero.trim(),
                        fechaNacimiento = fechaNacimiento,
                        sexo = sexo,
                        avatarUrl = avatarUrl
                    )

                    val userMap = mapOf(
                        "id" to uid,
                        "nombre" to newUser.nombre,
                        "apellidoPaterno" to newUser.apellidoPaterno,
                        "apellidoMaterno" to newUser.apellidoMaterno,
                        "correo" to newUser.correo,
                        "numero" to newUser.numero,
                        "fechaNacimiento" to newUser.fechaNacimiento,
                        "sexo" to newUser.sexo,
                        "avatarUrl" to newUser.avatarUrl
                    )

                    db.collection("users").document(uid).set(userMap)
                        .addOnSuccessListener {
                            onResult(true, null, newUser)
                        }
                        .addOnFailureListener { e: Exception ->
                            onResult(false, "Error al guardar en Firestore: ${e.localizedMessage}", null)
                        }
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Error al registrar usuario", null)
                }
            }
    }

    fun loginWithEmail(
        email: String,
        pass: String,
        onResult: (Boolean, String?, User?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email.trim(), pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""
                    getUserProfile(uid) { user ->
                        if (user != null) {
                            onResult(true, null, user)
                        } else {
                            auth.signOut()
                            onResult(false, "El usuario no se encuentra registrado en la base de datos. Por favor crea una cuenta.", null)
                        }
                    }
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Correo o contraseña incorrectos", null)
                }
            }
    }

    fun sendPasswordResetEmail(email: String, onResult: (Boolean, String?) -> Unit) {
        if (email.trim().isEmpty()) {
            onResult(false, "Ingrese su correo electrónico")
            return
        }
        auth.sendPasswordResetEmail(email.trim())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "Se ha enviado un correo para restablecer la contraseña")
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Error al enviar correo de recuperación")
                }
            }
    }

    fun getUserProfile(userId: String, onResult: (User?) -> Unit) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    onResult(userFromDocument(doc, userId))
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    private fun userFromDocument(
        document: com.google.firebase.firestore.DocumentSnapshot,
        userId: String
    ): User = User(
        id = document.getString("id") ?: userId,
        nombre = document.getString("nombre") ?: "",
        apellidoPaterno = document.getString("apellidoPaterno") ?: "",
        apellidoMaterno = document.getString("apellidoMaterno") ?: "",
        correo = document.getString("correo") ?: "",
        numero = document.getString("numero") ?: "",
        fechaNacimiento = document.getString("fechaNacimiento") ?: "",
        sexo = document.getString("sexo") ?: "",
        avatarUrl = document.getString("avatarUrl") ?: ""
    )

    suspend fun saveUserProfile(context: Context, user: User) = withContext(Dispatchers.IO) {
        val authenticatedUserId = auth.currentUser?.uid
            ?: throw IllegalStateException("Inicia sesión para guardar tu perfil.")
        if (user.id != authenticatedUserId) {
            throw SecurityException("No puedes modificar el perfil de otro usuario.")
        }

        val avatarValue = if (user.avatarUrl.startsWith("content://")) {
            encodeProfilePhoto(context, Uri.parse(user.avatarUrl))
        } else {
            user.avatarUrl
        }
        val profile = mapOf(
            "id" to user.id,
            "nombre" to user.nombre,
            "apellidoPaterno" to user.apellidoPaterno,
            "apellidoMaterno" to user.apellidoMaterno,
            "correo" to user.correo,
            "numero" to user.numero,
            "fechaNacimiento" to user.fechaNacimiento,
            "sexo" to user.sexo,
            "avatarUrl" to avatarValue
        )
        db.collection("users").document(authenticatedUserId)
            .set(profile, SetOptions.merge())
            .await()
        user.copy(avatarUrl = avatarValue)
    }

    private fun encodeProfilePhoto(context: Context, uri: Uri): String {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, bounds)
        } ?: throw IOException("No se pudo abrir la foto seleccionada.")
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw IOException("El archivo seleccionado no es una imagen válida.")
        }

        var sampleSize = 1
        while (bounds.outWidth / sampleSize > 512 || bounds.outHeight / sampleSize > 512) {
            sampleSize *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val bitmap = resolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, options)
        } ?: throw IOException("No se pudo leer la foto seleccionada.")

        return try {
            ByteArrayOutputStream().use { output ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 78, output)) {
                    throw IOException("No se pudo preparar la foto para guardarla.")
                }
                "data:image/jpeg;base64," + Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
            }
        } finally {
            bitmap.recycle()
        }
    }

    suspend fun saveFinancialData(
        userId: String,
        data: UserFinancialData,
        cards: List<BankCard>,
        transactions: List<CardTransaction>
    ) {
        if (userId.isEmpty()) {
            throw IllegalArgumentException("No se puede guardar información financiera sin un usuario.")
        }

        val map = hashMapOf<String, Any>(
            "totalIncomes" to data.totalIncomes,
            "incomeItems" to data.incomeItems.map { item ->
                hashMapOf(
                    "id" to item.id,
                    "name" to item.name,
                    "month" to item.month,
                    "value" to item.value,
                    "color" to item.color.value.toLong(),
                    "date" to item.date,
                    "description" to item.description
                )
            },
            "totalSavings" to data.totalSavings,
            "savingItems" to data.savingItems.map { item ->
                hashMapOf(
                    "id" to item.id,
                    "year" to item.year,
                    "country" to item.country,
                    "cities" to item.cities,
                    "color" to item.color.value.toLong(),
                    "value" to item.value,
                    "description" to item.description,
                    "date" to item.date,
                    "photoUrl" to item.photoUrl,
                    "progreso" to item.progreso,
                    "meta" to item.meta,
                    "fechaFinal" to item.fechaFinal,
                    "frecuencia" to item.frecuencia.name
                )
            },
            "totalExpenses" to data.totalExpenses,
            "expenseItems" to data.expenseItems.map { item ->
                hashMapOf(
                    "id" to item.id,
                    "label" to item.label,
                    "amount" to item.amount,
                    "period" to item.period,
                    "description" to item.description,
                    "date" to item.date
                )
            },
            "pieChartData" to data.pieChartData.map { slice ->
                hashMapOf(
                    "value" to slice.value,
                    "color" to slice.color.value.toLong(),
                    "label" to slice.label
                )
            },
            "totalDebts" to data.totalDebts,
            "debtItems" to data.debtItems.map { item ->
                hashMapOf(
                    "id" to item.id,
                    "label" to item.label,
                    "amount" to item.amount,
                    "period" to item.period,
                    "description" to item.description,
                    "date" to item.date,
                    "color" to item.color.value.toLong(),
                    "progreso" to item.progreso,
                    "fechaFinal" to item.fechaFinal,
                    "frecuencia" to item.frecuencia.name
                )
            },
            "bankCards" to cards.map { card ->
                hashMapOf(
                    "id" to card.id,
                    "alias" to card.alias,
                    "bankName" to card.bankName,
                    "cardNumber" to card.cardNumber,
                    "cardHolder" to card.cardHolder,
                    "balance" to card.balance,
                    "cardColor" to card.cardColor.value.toLong(),
                    "type" to card.type,
                    "creditLimit" to card.creditLimit,
                    "creditUsed" to card.creditUsed
                )
            },
            "cardTransactions" to transactions.map { tx ->
                hashMapOf(
                    "id" to tx.id,
                    "cardId" to tx.cardId,
                    "cardAlias" to tx.cardAlias,
                    "concepto" to tx.concepto,
                    "monto" to tx.monto,
                    "tipo" to tx.tipo.name,
                    "fecha" to tx.fecha,
                    "categoria" to tx.categoria,
                    "description" to tx.description
                )
            }
        )

        db.collection("users").document(userId).collection("financial").document("data")
            .set(map)
            .await()
    }

    fun loadFinancialData(
        userId: String,
        onResult: (UserFinancialData?, List<BankCard>, List<CardTransaction>, Exception?) -> Unit
    ) {
        if (userId.isEmpty()) {
            onResult(null, emptyList(), emptyList(), IllegalArgumentException("ID de usuario vacío"))
            return
        }

        db.collection("users").document(userId).collection("financial").document("data")
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val totalIncomes = (doc.get("totalIncomes") as? Number)?.toFloat() ?: 0f
                    val incomeItemsRaw = doc.get("incomeItems") as? List<*> ?: emptyList<Any>()
                    val incomeItems = incomeItemsRaw.mapNotNull { obj ->
                        val map = obj as? Map<*, *> ?: return@mapNotNull null
                        IncomeItem(
                            id = map["id"] as? String ?: java.util.UUID.randomUUID().toString(),
                            name = map["name"] as? String ?: "",
                            month = map["month"] as? String ?: "",
                            value = (map["value"] as? Number)?.toFloat() ?: 0f,
                            color = Color((map["color"] as? Number)?.toLong() ?: 0xFF4CAF50),
                            date = map["date"] as? String ?: "",
                            description = map["description"] as? String ?: ""
                        )
                    }

                    val totalSavings = (doc.get("totalSavings") as? Number)?.toFloat() ?: 0f
                    val savingItemsRaw = doc.get("savingItems") as? List<*> ?: emptyList<Any>()
                    val savingItems = savingItemsRaw.mapNotNull { obj ->
                        val map = obj as? Map<*, *> ?: return@mapNotNull null
                        val freqStr = map["frecuencia"] as? String ?: "SEMANAL"
                        val freq = try { SavingFrequency.valueOf(freqStr) } catch(e: Exception) { SavingFrequency.SEMANAL }
                        SavingItem(
                            id = map["id"] as? String ?: java.util.UUID.randomUUID().toString(),
                            year = map["year"] as? String ?: "2024",
                            country = map["country"] as? String ?: "",
                            cities = map["cities"] as? String ?: "",
                            color = Color((map["color"] as? Number)?.toLong() ?: 0xFF03A9F4),
                            value = (map["value"] as? Number)?.toFloat() ?: 0f,
                            description = map["description"] as? String ?: "",
                            date = map["date"] as? String ?: "",
                            photoUrl = map["photoUrl"] as? String ?: "",
                            progreso = map["progreso"] as? Boolean ?: false,
                            meta = (map["meta"] as? Number)?.toFloat() ?: 0f,
                            fechaFinal = map["fechaFinal"] as? String ?: "",
                            frecuencia = freq
                        )
                    }

                    val totalExpenses = (doc.get("totalExpenses") as? Number)?.toFloat() ?: 0f
                    val expenseItemsRaw = doc.get("expenseItems") as? List<*> ?: emptyList<Any>()
                    val expenseItems = expenseItemsRaw.mapNotNull { obj ->
                        val map = obj as? Map<*, *> ?: return@mapNotNull null
                        ExpenseItem(
                            id = map["id"] as? String ?: java.util.UUID.randomUUID().toString(),
                            label = map["label"] as? String ?: "",
                            amount = (map["amount"] as? Number)?.toFloat() ?: 0f,
                            period = map["period"] as? String ?: "ENE-FEB",
                            description = map["description"] as? String ?: "",
                            date = map["date"] as? String ?: ""
                        )
                    }

                    val pieChartRaw = doc.get("pieChartData") as? List<*> ?: emptyList<Any>()
                    val pieChartData = pieChartRaw.mapNotNull { obj ->
                        val map = obj as? Map<*, *> ?: return@mapNotNull null
                        PieChartSlice(
                            value = (map["value"] as? Number)?.toFloat() ?: 0f,
                            color = Color((map["color"] as? Number)?.toLong() ?: 0xFFEF9A9A),
                            label = map["label"] as? String ?: ""
                        )
                    }

                    val totalDebts = (doc.get("totalDebts") as? Number)?.toFloat() ?: 0f
                    val debtItemsRaw = doc.get("debtItems") as? List<*> ?: emptyList<Any>()
                    val debtItems = debtItemsRaw.mapNotNull { obj ->
                        val map = obj as? Map<*, *> ?: return@mapNotNull null
                        val freqStr = map["frecuencia"] as? String ?: "SEMANAL"
                        val freq = try { SavingFrequency.valueOf(freqStr) } catch(e: Exception) { SavingFrequency.SEMANAL }
                        DebtItem(
                            id = map["id"] as? String ?: java.util.UUID.randomUUID().toString(),
                            label = map["label"] as? String ?: "",
                            amount = (map["amount"] as? Number)?.toFloat() ?: 0f,
                            period = map["period"] as? String ?: "ENE-FEB",
                            description = map["description"] as? String ?: "",
                            date = map["date"] as? String ?: "",
                            color = Color((map["color"] as? Number)?.toLong() ?: 0xFFE53935),
                            progreso = map["progreso"] as? Boolean ?: false,
                            fechaFinal = map["fechaFinal"] as? String ?: "",
                            frecuencia = freq
                        )
                    }

                    val cardsRaw = doc.get("bankCards") as? List<*> ?: emptyList<Any>()
                    val bankCards = cardsRaw.mapNotNull { obj ->
                        val map = obj as? Map<*, *> ?: return@mapNotNull null
                        BankCard(
                            id = map["id"] as? String ?: java.util.UUID.randomUUID().toString(),
                            alias = map["alias"] as? String ?: "",
                            bankName = map["bankName"] as? String ?: "",
                            cardNumber = map["cardNumber"] as? String ?: "",
                            cardHolder = map["cardHolder"] as? String ?: "",
                            balance = (map["balance"] as? Number)?.toFloat() ?: 0f,
                            cardColor = Color((map["cardColor"] as? Number)?.toLong() ?: 0xFF1E88E5),
                            type = map["type"] as? String ?: "Débito",
                            creditLimit = (map["creditLimit"] as? Number)?.toFloat() ?: 0f,
                            creditUsed = (map["creditUsed"] as? Number)?.toFloat() ?: 0f
                        )
                    }

                    val txsRaw = doc.get("cardTransactions") as? List<*> ?: emptyList<Any>()
                    val cardTransactions = txsRaw.mapNotNull { obj ->
                        val map = obj as? Map<*, *> ?: return@mapNotNull null
                        val typeStr = map["tipo"] as? String ?: "EXPENSE"
                        val entryType = try { EntryType.valueOf(typeStr) } catch(e: Exception) { EntryType.EXPENSE }
                        CardTransaction(
                            id = map["id"] as? String ?: java.util.UUID.randomUUID().toString(),
                            cardId = map["cardId"] as? String ?: "",
                            cardAlias = map["cardAlias"] as? String ?: "",
                            concepto = map["concepto"] as? String ?: "",
                            monto = (map["monto"] as? Number)?.toFloat() ?: 0f,
                            tipo = entryType,
                            fecha = map["fecha"] as? String ?: "",
                            categoria = map["categoria"] as? String ?: "GENERAL",
                            description = map["description"] as? String ?: ""
                        )
                    }

                    val financialData = UserFinancialData(
                        totalIncomes = totalIncomes,
                        incomeItems = incomeItems,
                        totalSavings = totalSavings,
                        savingItems = savingItems,
                        totalExpenses = totalExpenses,
                        expenseItems = expenseItems,
                        pieChartData = pieChartData,
                        totalDebts = totalDebts,
                        debtItems = debtItems
                    )

                    onResult(financialData, bankCards, cardTransactions, null)
                } else {
                    onResult(null, emptyList(), emptyList(), null)
                }
            }
            .addOnFailureListener { error ->
                onResult(null, emptyList(), emptyList(), error)
            }
    }

    fun logout() {
        auth.signOut()
    }
}
