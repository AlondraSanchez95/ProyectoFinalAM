package com.example.proyectofinal.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectofinal.data.BankCard
import com.example.proyectofinal.data.CardTransaction
import com.example.proyectofinal.data.EntryType
import com.example.proyectofinal.ui.theme.MiFuenteGoogle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsScreen(
    cards: List<BankCard>,
    transactions: List<CardTransaction> = emptyList(),
    onAddCardClick: () -> Unit,
    onDeleteCard: (String) -> Unit,
    belvoLoading: Boolean = false,
    belvoError: String? = null,
    onSyncBelvo: () -> Unit = {},
    onLinkTransaction: (CardTransaction, EntryType, String) -> Unit = { _, _, _ -> },
    onBack: () -> Unit
) {
    var selectedTx by remember { mutableStateOf<CardTransaction?>(null) }
    var targetType by remember { mutableStateOf(EntryType.SAVING) }
    var customCategory by remember { mutableStateOf("") }
    var isLinkSuccess by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Tarjetas y Movimientos", fontFamily = MiFuenteGoogle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    IconButton(onClick = onSyncBelvo) {
                        Icon(Icons.Default.Sync, contentDescription = "Actualizar tarjetas")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCardClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Tarjeta")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "TARJETAS VINCULADAS (${cards.size})",
                    fontFamily = MiFuenteGoogle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (belvoLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Conectando con Belvo...", fontFamily = MiFuenteGoogle, fontSize = 12.sp)
                    }
                }
            }

            belvoError?.let { message ->
                item {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        fontFamily = MiFuenteGoogle,
                        fontSize = 12.sp
                    )
                }
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(cards) { card ->
                        Card(
                            modifier = Modifier
                                .width(280.dp)
                                .height(140.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = card.cardColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = card.alias,
                                        fontFamily = MiFuenteGoogle,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    IconButton(onClick = { onDeleteCard(card.id) }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.White)
                                    }
                                }
                                Text(
                                    text = card.bankName,
                                    fontFamily = MiFuenteGoogle,
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = card.cardNumber,
                                    fontFamily = MiFuenteGoogle,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Tipo: ${card.type}",
                                            fontFamily = MiFuenteGoogle,
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                        Text(
                                            text = if (card.type == "Crédito") "Límite: ${formatCurrency(card.creditLimit)}" else "Saldo: ${formatCurrency(card.balance)}",
                                            fontFamily = MiFuenteGoogle,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    if (card.type == "Crédito") {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Text(
                                                text = "Utilizado: ${formatCurrency(card.creditUsed)}",
                                                fontFamily = MiFuenteGoogle,
                                                fontSize = 11.sp,
                                                color = Color.White.copy(alpha = 0.9f),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DESGLOSE DE MOVIMIENTOS",
                        fontFamily = MiFuenteGoogle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = onSyncBelvo) {
                        Text("Agregar tarjeta", fontFamily = MiFuenteGoogle, fontSize = 12.sp)
                    }
                }
            }

            if (transactions.isEmpty()) {
                item {
                    Text(
                        text = "No hay movimientos registrados en las tarjetas",
                        fontFamily = MiFuenteGoogle,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(transactions) { tx ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedTx = tx
                                isLinkSuccess = false
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = if (tx.tipo == EntryType.INCOME) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color(0xFFE53935).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (tx.tipo == EntryType.INCOME) "INGRESO" else "GASTO",
                                            fontFamily = MiFuenteGoogle,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (tx.tipo == EntryType.INCOME) Color(0xFF2E7D32) else Color(0xFFC62828),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = tx.categoria,
                                        fontFamily = MiFuenteGoogle,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = tx.concepto,
                                    fontFamily = MiFuenteGoogle,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Tarjeta: ${tx.cardAlias} • ${tx.fecha}",
                                    fontFamily = MiFuenteGoogle,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = formatCurrency(tx.monto),
                                    fontFamily = MiFuenteGoogle,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tx.tipo == EntryType.INCOME) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail & Linking Dialog
    selectedTx?.let { tx ->
        AlertDialog(
            onDismissRequest = { selectedTx = null },
            title = {
                Text(
                    text = tx.concepto,
                    fontFamily = MiFuenteGoogle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Tarjeta: ${tx.cardAlias}", fontFamily = MiFuenteGoogle, fontSize = 13.sp)
                    Text("Monto: ${formatCurrency(tx.monto)}", fontFamily = MiFuenteGoogle, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Fecha: ${tx.fecha}", fontFamily = MiFuenteGoogle, fontSize = 12.sp)
                    if (tx.description.isNotEmpty()) {
                        Text("Descripción: ${tx.description}", fontFamily = MiFuenteGoogle, fontSize = 12.sp)
                    }

                    HorizontalDivider()

                    Text(
                        text = "Vincular este movimiento a:",
                        fontFamily = MiFuenteGoogle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = targetType == EntryType.INCOME,
                            onClick = { targetType = EntryType.INCOME },
                            label = { Text("Ingreso", fontFamily = MiFuenteGoogle, fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = targetType == EntryType.SAVING,
                            onClick = { targetType = EntryType.SAVING },
                            label = { Text("Ahorro", fontFamily = MiFuenteGoogle, fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = targetType == EntryType.EXPENSE,
                            onClick = { targetType = EntryType.EXPENSE },
                            label = { Text("Gasto", fontFamily = MiFuenteGoogle, fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = targetType == EntryType.DEBT,
                            onClick = { targetType = EntryType.DEBT },
                            label = { Text("Deuda", fontFamily = MiFuenteGoogle, fontSize = 11.sp) }
                        )
                    }

                    if (targetType == EntryType.SAVING) {
                        OutlinedTextField(
                            value = customCategory,
                            onValueChange = { customCategory = it },
                            label = { Text("Nombre de Apartado de Ahorro", fontFamily = MiFuenteGoogle, fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (isLinkSuccess) {
                        Text(
                            text = "¡Movimiento vinculado con éxito!",
                            fontFamily = MiFuenteGoogle,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onLinkTransaction(tx, targetType, customCategory)
                        isLinkSuccess = true
                    }
                ) {
                    Text("Vincular Movimiento", fontFamily = MiFuenteGoogle)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedTx = null }) {
                    Text("Cerrar", fontFamily = MiFuenteGoogle)
                }
            }
        )
    }
}
