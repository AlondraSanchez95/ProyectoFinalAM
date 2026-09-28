package com.example.proyectofinal.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectofinal.data.User
import com.example.proyectofinal.ui.theme.MiFuenteGoogle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User,
    onSaveProfile: (User, (User?, String?) -> Unit) -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var nombre by remember(user.id) { mutableStateOf(user.nombre) }
    var apellidoPaterno by remember(user.id) { mutableStateOf(user.apellidoPaterno) }
    var apellidoMaterno by remember(user.id) { mutableStateOf(user.apellidoMaterno) }
    var correo by remember(user.id) { mutableStateOf(user.correo) }
    var numero by remember(user.id) { mutableStateOf(user.numero) }
    var fechaNacimiento by remember(user.id) { mutableStateOf(user.fechaNacimiento) }
    var sexo by remember(user.id) { mutableStateOf(normalizeSex(user.sexo)) }
    var avatarUrl by remember(user.id) { mutableStateOf(user.avatarUrl) }
    var isSavedMessageVisible by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showSexMenu by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                avatarUrl = uri.toString()
                errorMessage = null
                isSavedMessageVisible = false
            } catch (error: SecurityException) {
                errorMessage = error.localizedMessage ?: "No se pudo acceder a la foto seleccionada."
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Información y Perfil", fontFamily = MiFuenteGoogle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Box(contentAlignment = Alignment.BottomEnd) {
                    ProfileAvatar(user = user, avatarUrl = avatarUrl)
                    FilledIconButton(
                        onClick = { photoPicker.launch(arrayOf("image/*")) },
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Elegir foto de perfil")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = { photoPicker.launch(arrayOf("image/*")) }) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Elegir foto", fontFamily = MiFuenteGoogle)
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Datos de Registro",
                            fontFamily = MiFuenteGoogle,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre(s)", fontFamily = MiFuenteGoogle) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = apellidoPaterno,
                            onValueChange = { apellidoPaterno = it },
                            label = { Text("Apellido Paterno", fontFamily = MiFuenteGoogle) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = apellidoMaterno,
                            onValueChange = { apellidoMaterno = it },
                            label = { Text("Apellido Materno", fontFamily = MiFuenteGoogle) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = correo,
                            onValueChange = { correo = it },
                            label = { Text("Correo Electrónico", fontFamily = MiFuenteGoogle) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = numero,
                            onValueChange = { numero = it },
                            label = { Text("Teléfono", fontFamily = MiFuenteGoogle) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = fechaNacimiento,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Fecha de Nacimiento", fontFamily = MiFuenteGoogle) },
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = "Abrir calendario")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (fechaNacimiento.isBlank()) "Seleccionar fecha" else "Cambiar fecha",
                                fontFamily = MiFuenteGoogle
                            )
                        }

                        ExposedDropdownMenuBox(
                            expanded = showSexMenu,
                            onExpandedChange = { showSexMenu = it }
                        ) {
                            OutlinedTextField(
                                value = sexo,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Sexo", fontFamily = MiFuenteGoogle) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSexMenu) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = showSexMenu,
                                onDismissRequest = { showSexMenu = false }
                            ) {
                                sexOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option, fontFamily = MiFuenteGoogle) },
                                        onClick = {
                                            sexo = option
                                            showSexMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            errorMessage?.let { message ->
                item {
                    Text(
                        text = message,
                        fontFamily = MiFuenteGoogle,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (isSavedMessageVisible) {
                item {
                    Text(
                        text = "¡Perfil actualizado y guardado!",
                        fontFamily = MiFuenteGoogle,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        isSaving = true
                        errorMessage = null
                        isSavedMessageVisible = false
                        val updatedUser = user.copy(
                            nombre = nombre.trim(),
                            apellidoPaterno = apellidoPaterno.trim(),
                            apellidoMaterno = apellidoMaterno.trim(),
                            correo = correo.trim(),
                            numero = numero.trim(),
                            fechaNacimiento = fechaNacimiento,
                            sexo = sexo,
                            avatarUrl = avatarUrl
                        )
                        onSaveProfile(updatedUser) { savedUser, error ->
                            isSaving = false
                            if (savedUser != null) {
                                avatarUrl = savedUser.avatarUrl
                                isSavedMessageVisible = true
                            } else {
                                errorMessage = error ?: "No se pudo guardar el perfil."
                            }
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(
                            text = "Guardar Cambios",
                            fontFamily = MiFuenteGoogle,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cerrar Sesión",
                        fontFamily = MiFuenteGoogle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = parseDateMillis(fechaNacimiento)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            fechaNacimiento = formatDateMillis(millis)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar", fontFamily = MiFuenteGoogle)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", fontFamily = MiFuenteGoogle)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun ProfileAvatar(user: User, avatarUrl: String) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, key1 = avatarUrl) {
        value = withContext(Dispatchers.IO) { decodeProfileAvatar(context, avatarUrl) }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "Foto de perfil",
            modifier = Modifier
                .size(104.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )
    } else {
        Image(
            painter = painterResource(id = user.avatarRes),
            contentDescription = "Avatar de usuario",
            modifier = Modifier
                .size(104.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )
    }
}

private fun decodeProfileAvatar(context: android.content.Context, avatarUrl: String): Bitmap? {
    return try {
        if (avatarUrl.startsWith("data:image/")) {
            val bytes = Base64.decode(avatarUrl.substringAfter(','), Base64.DEFAULT)
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight)
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        } else if (avatarUrl.startsWith("content://")) {
            val uri = Uri.parse(avatarUrl)
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, bounds)
            } ?: return null
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight)
            }
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }
        } else {
            null
        }
    } catch (_: IOException) {
        null
    } catch (_: SecurityException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }
}

private fun sampleSizeFor(width: Int, height: Int): Int {
    var sampleSize = 1
    while (width / sampleSize > 512 || height / sampleSize > 512) {
        sampleSize *= 2
    }
    return sampleSize
}

private val sexOptions = listOf("Femenino", "Masculino", "Prefiero no decirlo")

private fun normalizeSex(value: String): String =
    if (value.equals("otros", ignoreCase = true)) "Prefiero no decirlo" else value

private fun parseDateMillis(date: String): Long? {
    if (date.isBlank()) return null
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
        isLenient = false
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return try {
        formatter.parse(date)?.time
    } catch (_: java.text.ParseException) {
        null
    }
}

private fun formatDateMillis(millis: Long): String {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = millis
    }
    return "%02d/%02d/%04d".format(
        Locale.getDefault(),
        calendar.get(Calendar.DAY_OF_MONTH),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.YEAR)
    )
}
