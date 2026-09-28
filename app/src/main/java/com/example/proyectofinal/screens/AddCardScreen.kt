package com.example.proyectofinal.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectofinal.data.BankCard
import com.example.proyectofinal.ui.theme.MiFuenteGoogle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(
    onSaveCard: (BankCard) -> Unit,
    onStartBelvoSync: () -> Unit,
    isLoading: Boolean = false,
    belvoError: String? = null,
    onBack: () -> Unit
) {
    var alias by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("BBVA") }
    var cardNumber by remember { mutableStateOf("**** 8842") }
    var cardHolder by remember { mutableStateOf("") }
    var balanceStr by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar Tarjeta", fontFamily = MiFuenteGoogle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onStartBelvoSync,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.primary)
                } else {
                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar tarjeta", fontFamily = MiFuenteGoogle, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            belvoError?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    fontFamily = MiFuenteGoogle,
                    fontSize = 12.sp
                )
            }

            HorizontalDivider()

            OutlinedTextField(
                value = alias,
                onValueChange = { alias = it },
                label = { Text("Apodo / Alias (ej. Nómina Principal)", fontFamily = MiFuenteGoogle) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = bankName,
                onValueChange = { bankName = it },
                label = { Text("Banco (ej. BBVA, Banorte, Nu)", fontFamily = MiFuenteGoogle) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = cardNumber,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = { Text("Número / Últimos Dígitos (Solo lectura)", fontFamily = MiFuenteGoogle) },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
            OutlinedTextField(
                value = cardHolder,
                onValueChange = { cardHolder = it },
                label = { Text("Titular", fontFamily = MiFuenteGoogle) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = balanceStr,
                onValueChange = { balanceStr = it },
                label = { Text("Saldo inicial", fontFamily = MiFuenteGoogle) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val balance = balanceStr.toFloatOrNull() ?: 0f
                    val color = when(bankName.uppercase()) {
                        "BBVA" -> Color(0xFF1E88E5)
                        "BANORTE" -> Color(0xFFE53935)
                        "SANTANDER" -> Color(0xFFD32F2F)
                        "NU" -> Color(0xFF7B1FA2)
                        else -> Color(0xFF37474F)
                    }
                    val card = BankCard(
                        alias = alias.ifEmpty { "Mi Tarjeta" },
                        bankName = bankName,
                        cardNumber = cardNumber,
                        cardHolder = cardHolder.ifEmpty { "TITULAR" },
                        balance = balance,
                        cardColor = color
                    )
                    onSaveCard(card)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Guardar Tarjeta", fontFamily = MiFuenteGoogle, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
