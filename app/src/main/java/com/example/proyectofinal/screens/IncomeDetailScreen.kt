package com.example.proyectofinal.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectofinal.R
import com.example.proyectofinal.data.IncomeItem
import com.example.proyectofinal.ui.theme.MiFuenteGoogle

@Composable
fun IncomeDetailScreen(
    item: IncomeItem,
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = item.name,
                        fontFamily = MiFuenteGoogle,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("MONTO", fontFamily = MiFuenteGoogle, fontSize = 18.sp, color = MaterialTheme.colorScheme.onPrimary)
                    Text(
                        text = formatCurrency(item.value),
                        fontFamily = MiFuenteGoogle,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("FECHA", fontFamily = MiFuenteGoogle, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        Text(text = item.date, fontFamily = MiFuenteGoogle, color = MaterialTheme.colorScheme.onPrimary, fontSize = 18.sp)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text("DESCRIPCION", fontFamily = MiFuenteGoogle, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = item.description.ifEmpty { "Sin descripción" },
                                fontFamily = MiFuenteGoogle,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
