package com.example.proyectofinal.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectofinal.R
import com.example.proyectofinal.data.SavingFrequency
import com.example.proyectofinal.data.SavingItem
import com.example.proyectofinal.ui.theme.MiFuenteGoogle

@Composable
fun SavingDetailScreen(
    item: SavingItem,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = item.country,
                        fontFamily = MiFuenteGoogle,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (item.cities.isNotEmpty()) {
                        Text(
                            text = item.cities,
                            fontFamily = MiFuenteGoogle,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    if (item.photoUrl.isNotEmpty()) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_user_avatar),
                            contentDescription = "Foto del apartado",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    Text("MONTO AHORRADO", fontFamily = MiFuenteGoogle, fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                    Text(
                        text = formatCurrency(item.value),
                        fontFamily = MiFuenteGoogle,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (item.progreso && item.meta > 0f) {
                        Spacer(modifier = Modifier.height(20.dp))
                        val progress = (item.value / item.meta).coerceIn(0f, 1f)
                        val percentage = (progress * 100).toInt()

                        Text(
                            text = "Meta: ${formatCurrency(item.meta)} ($percentage%)",
                            fontFamily = MiFuenteGoogle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val recommendation = calculateContributionText(item)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = recommendation,
                                fontFamily = MiFuenteGoogle,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("FECHA DE REGISTRO", fontFamily = MiFuenteGoogle, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = item.date.ifEmpty { item.year }, fontFamily = MiFuenteGoogle, color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 16.sp)
                        
                        if (item.progreso && item.fechaFinal.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("FECHA FINAL META", fontFamily = MiFuenteGoogle, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = item.fechaFinal, fontFamily = MiFuenteGoogle, color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("DESCRIPCIÓN (OPCIONAL)", fontFamily = MiFuenteGoogle, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 80.dp)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = item.description.ifEmpty { "Sin descripción" },
                                fontFamily = MiFuenteGoogle,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

fun calculateContributionText(item: SavingItem): String {
    val remaining = (item.meta - item.value).coerceAtLeast(0f)
    if (remaining <= 0f) return "¡Felicidades! Has alcanzado la meta de este apartado."

    val divisor = when (item.frecuencia) {
        SavingFrequency.SEMANAL -> 12f
        SavingFrequency.QUINCENAL -> 6f
        SavingFrequency.MENSUAL -> 3f
    }

    val perPeriod = remaining / divisor
    val freqName = when (item.frecuencia) {
        SavingFrequency.SEMANAL -> "semana"
        SavingFrequency.QUINCENAL -> "quincena"
        SavingFrequency.MENSUAL -> "mes"
    }

    return "Faltan ${formatCurrency(remaining)} para tu meta. Te recomendamos aportar ${formatCurrency(perPeriod)} por $freqName."
}
