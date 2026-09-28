package com.example.proyectofinal.screens

import android.app.DatePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectofinal.R
import com.example.proyectofinal.data.FirebaseRepository
import com.example.proyectofinal.data.User
import com.example.proyectofinal.ui.theme.MiFuenteGoogle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: (User) -> Unit,
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    var nombre by remember { mutableStateOf("") }
    var apellidoPaterno by remember { mutableStateOf("") }
    var apellidoMaterno by remember { mutableStateOf("") }
    var correoONumero by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var sexo by remember { mutableStateOf("Femenino") }
    var contrasena by remember { mutableStateOf("") }
    var contrasenaVisible by remember { mutableStateOf(false) }
    var avatarUrl by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showGenderMenu by remember { mutableStateOf(false) }

    val sexoOptions = listOf("Femenino", "Masculino", "Prefiero no decirlo")

    // Calendar Picker Setup
    val calendar = Calendar.getInstance()
    val currYear = calendar.get(Calendar.YEAR)
    val currMonth = calendar.get(Calendar.MONTH)
    val currDay = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDay = selectedDay.toString().padStart(2, '0')
                val formattedMonth = (selectedMonth + 1).toString().padStart(2, '0')
                fechaNacimiento = "$formattedDay/$formattedMonth/$selectedYear"
            },
            currYear - 20,
            currMonth,
            currDay
        )
    }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                isLoading = true
                FirebaseRepository.firebaseAuthWithGoogle(idToken) { success, err, user ->
                    isLoading = false
                    if (success && user != null) {
                        onRegisterSuccess(user)
                    } else {
                        errorMessage = err ?: "Error en registro con Google"
                    }
                }
            }
        } catch (e: Exception) {
            val apiEx = e as? ApiException
            errorMessage = "Error Google: ${e.localizedMessage} (Code: ${apiEx?.statusCode})"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Cuenta", fontFamily = MiFuenteGoogle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackToLogin) {
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable {
                            avatarUrl = "https://picsum.photos/200"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = "Foto de perfil",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Foto (Opcional)",
                            fontFamily = MiFuenteGoogle,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre *", fontFamily = MiFuenteGoogle) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = apellidoPaterno,
                    onValueChange = { apellidoPaterno = it },
                    label = { Text("Apellido Paterno *", fontFamily = MiFuenteGoogle) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = apellidoMaterno,
                    onValueChange = { apellidoMaterno = it },
                    label = { Text("Apellido Materno (Opcional)", fontFamily = MiFuenteGoogle) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = correoONumero,
                    onValueChange = { correoONumero = it },
                    label = { Text("Correo o Teléfono *", fontFamily = MiFuenteGoogle) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                ) {
                    OutlinedTextField(
                        value = fechaNacimiento,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Fecha de Nacimiento *", fontFamily = MiFuenteGoogle) },
                        placeholder = { Text("Seleccionar fecha en calendario", fontFamily = MiFuenteGoogle) },
                        trailingIcon = {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Elegir Fecha")
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.primary,
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showGenderMenu = true }
                ) {
                    OutlinedTextField(
                        value = sexo,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Sexo *", fontFamily = MiFuenteGoogle) },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Elegir Sexo")
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    DropdownMenu(
                        expanded = showGenderMenu,
                        onDismissRequest = { showGenderMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        sexoOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, fontFamily = MiFuenteGoogle, fontSize = 14.sp) },
                                onClick = {
                                    sexo = option
                                    showGenderMenu = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = contrasena,
                    onValueChange = { contrasena = it },
                    label = { Text("Contraseña *", fontFamily = MiFuenteGoogle) },
                    visualTransformation = if (contrasenaVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (contrasenaVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { contrasenaVisible = !contrasenaVisible }) {
                            Icon(imageVector = icon, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            if (errorMessage.isNotEmpty()) {
                item {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontFamily = MiFuenteGoogle,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (nombre.trim().isEmpty() || apellidoPaterno.trim().isEmpty() || correoONumero.trim().isEmpty() || fechaNacimiento.trim().isEmpty() || contrasena.trim().isEmpty()) {
                            errorMessage = "Por favor complete todos los campos obligatorios (*)"
                            return@Button
                        }
                        isLoading = true
                        errorMessage = ""

                        val sexoGuardar = if (sexo == "Prefiero no decirlo") "otros" else sexo

                        FirebaseRepository.registerUser(
                            nombre = nombre,
                            apellidoPaterno = apellidoPaterno,
                            apellidoMaterno = apellidoMaterno,
                            correo = correoONumero,
                            numero = if (correoONumero.contains("@")) "" else correoONumero,
                            fechaNacimiento = fechaNacimiento,
                            sexo = sexoGuardar,
                            contrasena = contrasena,
                            avatarUrl = avatarUrl
                        ) { success, error, user ->
                            isLoading = false
                            if (success && user != null) {
                                onRegisterSuccess(user)
                            } else {
                                errorMessage = error ?: "Error al registrar"
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Registrarse", fontFamily = MiFuenteGoogle, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text("O regístrate con", fontFamily = MiFuenteGoogle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }
            }

            item {
                OutlinedButton(
                    onClick = {
                        val client = FirebaseRepository.getGoogleSignInClient(context)
                        googleLauncher.launch(client.signInIntent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Registrarse con Google",
                        fontFamily = MiFuenteGoogle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            item {
                TextButton(onClick = onBackToLogin) {
                    Text("¿Ya tienes una cuenta? Inicia sesión", fontFamily = MiFuenteGoogle, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
