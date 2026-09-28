package com.example.proyectofinal.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONArray
import org.json.JSONObject
import java.text.DateFormat
import java.util.Date
import java.util.Locale

data class AppNotification(
    val title: String,
    val message: String,
    val timestamp: Long
) {
    fun formattedDate(): String =
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())
            .format(Date(timestamp))
}

object NotificationHelper {
    private const val CHANNEL_ID = "maximo_finanzas_channel"
    private const val CHANNEL_NAME = "Maximo Finanzas Notificaciones"
    private const val PREFERENCES_NAME = "app_notifications"
    private const val MAX_STORED_NOTIFICATIONS = 50

    private fun preferences(context: Context) =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private fun currentUserKey(): String =
        FirebaseAuth.getInstance().currentUser?.uid ?: "guest"

    @Synchronized
    private fun storeNotification(context: Context, notification: AppNotification) {
        val prefs = preferences(context)
        val notifications = try {
            JSONArray(prefs.getString(currentUserKey(), "[]"))
        } catch (error: org.json.JSONException) {
            throw IllegalStateException("No se pudo leer el historial de notificaciones.", error)
        }
        val updated = JSONArray()
        val firstIndex = (notifications.length() - MAX_STORED_NOTIFICATIONS + 1).coerceAtLeast(0)
        for (index in firstIndex until notifications.length()) {
            updated.put(notifications.getJSONObject(index))
        }
        updated.put(
            JSONObject()
                .put("title", notification.title)
                .put("message", notification.message)
                .put("timestamp", notification.timestamp)
        )
        prefs.edit().putString(currentUserKey(), updated.toString()).apply()
    }

    @Synchronized
    fun getNotifications(context: Context): List<AppNotification> {
        val raw = preferences(context).getString(currentUserKey(), "[]") ?: "[]"
        val notifications = JSONArray(raw)
        return (0 until notifications.length()).map { index ->
            val item = notifications.getJSONObject(index)
            AppNotification(
                title = item.getString("title"),
                message = item.getString("message"),
                timestamp = item.getLong("timestamp")
            )
        }.reversed()
    }

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de ahorro y tarjetas bancarias"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        createChannel(context)
        storeNotification(context, AppNotification(title, message, System.currentTimeMillis()))
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }

    fun showBelvoCardLinkedNotification(context: Context, cardAlias: String) {
        showNotification(
            context = context,
            title = "¡Tarjeta Vinculada con Éxito!",
            message = "Se ha conectado tu tarjeta '$cardAlias' a través de Belvo."
        )
    }

    fun showMotivationalSavingNotification(context: Context) {
        showNotification(
            context = context,
            title = "💡 Consejo de Ahorro Diario",
            message = "¡Cada pequeña moneda cuenta! Recuerda apartar un poco hoy para alcanzar tus metas."
        )
    }
}
