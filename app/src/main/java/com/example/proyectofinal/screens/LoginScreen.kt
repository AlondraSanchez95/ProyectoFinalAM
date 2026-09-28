package com.example.proyectofinal.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectofinal.R
import com.example.proyectofinal.data.FirebaseRepository
import com.example.proyectofinal.data.User
import com.example.proyectofinal.data.UserRepository
import com.example.proyectofinal.ui.theme.MiFuenteGoogle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: (User) -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {}
) {
    val context = LocalContext.current
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var contrasenaVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var correoError by remember { mutableStateOf(false) }
    var contrasenaError by remember { mutableStateOf(false) }
    var mensajeErrorCorreo by remember { mutableStateOf("") }
    var mensajeErrorContrasena by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
                        onLoginSuccess(user)
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar(err ?: "Error al autenticar con Google")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            val apiEx = e as? ApiException
            scope.launch {
                snackbarHostState.showSnackbar("Error Google: ${e.localizedMessage} (Code: ${apiEx?.statusCode})")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Bienvenido",
                            fontFamily = MiFuenteGoogle,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = correo,
                            onValueChange = {
                                correo = it
                                if (correoError) correoError = false
                            },
                            label = { Text("Correo electrónico", fontFamily = MiFuenteGoogle) },
                            isError = correoError,
                            supportingText = {
                                if (correoError) {
                                    Text(text = mensajeErrorCorreo, color = MaterialTheme.colorScheme.error, fontFamily = MiFuenteGoogle)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = contrasena,
                            onValueChange = {
                                contrasena = it
                                if (contrasenaError) contrasenaError = false
                            },
                            label = { Text("Contraseña", fontFamily = MiFuenteGoogle) },
                            visualTransformation = if (contrasenaVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                val icon = if (contrasenaVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                IconButton(onClick = { contrasenaVisible = !contrasenaVisible }) {
                                    Icon(imageVector = icon, contentDescription = null)
                                }
                            },
                            isError = contrasenaError,
                            supportingText = {
                                if (contrasenaError) {
                                    Text(text = mensajeErrorContrasena, color = MaterialTheme.colorScheme.error, fontFamily = MiFuenteGoogle)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = "¿Olvidaste tu contraseña?",
                                fontFamily = MiFuenteGoogle,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { onNavigateToForgotPassword() }
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val emailLimpio = correo.trim()
                                correoError = false
                                contrasenaError = false
                                if (emailLimpio.isEmpty()) {
                                    correoError = true
                                    mensajeErrorCorreo = "El correo no puede estar vacío"
                                    return@Button
                                }
                                if (contrasena.isEmpty()) {
                                    contrasenaError = true
                                    mensajeErrorContrasena = "La contraseña no puede estar vacía"
                                    return@Button
                                }

                                isLoading = true
                                FirebaseRepository.loginWithEmail(emailLimpio, contrasena) { success, err, user ->
                                    isLoading = false
                                    if (success && user != null) {
                                        onLoginSuccess(user)
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(err ?: "Correo o contraseña incorrectos")
                                        }
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
                                Text(
                                    text = "Iniciar Sesión",
                                    fontFamily = MiFuenteGoogle,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f))
                            Text("O continúa con", fontFamily = MiFuenteGoogle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            HorizontalDivider(modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

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
                                text = "Continuar con Google",
                                fontFamily = MiFuenteGoogle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "¿No tienes una cuenta? Regístrate aquí",
                            fontFamily = MiFuenteGoogle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable { onNavigateToRegister() }
                        )
                    }
                }
            }
        }
    }
}
