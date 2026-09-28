package com.example.proyectofinal.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectofinal.R
import com.example.proyectofinal.data.EntryType
import com.example.proyectofinal.data.SavingFrequency
import com.example.proyectofinal.ui.theme.MiFuenteGoogle
import com.example.proyectofinal.utils.periodForMonth
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(
    type: EntryType,
    existingApartados: List<String> = emptyList(),
    existingSubApartados: List<String> = emptyList(),
    onBack: () -> Unit,
    onSave: (name: String, amount: Float, extra: String, category: String, subCategory: String, description: String) -> Unit,
    onSaveSaving: (name: String, amount: Float, date: String, category: String, subCategory: String, description: String, photoUrl: String, progreso: Boolean, meta: Float, fechaFinal: String, frecuencia: SavingFrequency) -> Unit = { _, _, _, _, _, _, _, _, _, _, _ -> },
    onSaveDebt: (name: String, amount: Float, date: String, period: String, concepto: String, description: String, progreso: Boolean, fechaFinal: String, frecuencia: SavingFrequency) -> Unit = { _, _, _, _, _, _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val today = remember { Calendar.getInstance() }
    val todayDay = today.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
    val todayMonth = (today.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
    val todayYear = today.get(Calendar.YEAR).toString()
    val todayBimester = periodForMonth(today.get(Calendar.MONTH) + 1, 2).orEmpty()

    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var day by remember { mutableStateOf(todayDay) }
    var month by remember { mutableStateOf(todayMonth) }
    var year by remember { mutableStateOf(todayYear) }
    
    var category by remember {
        mutableStateOf(if (type == EntryType.EXPENSE) todayBimester else "")
    }
    var isAddingOtherCategory by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    var subCategory by remember { mutableStateOf("") } 
    var isAddingOtherSubCategory by remember { mutableStateOf(false) }
    var newSubCategoryName by remember { mutableStateOf("") }

    // Saving & Debt specific fields
    var photoUrl by remember { mutableStateOf("") }
    var progreso by remember { mutableStateOf(false) }
    var metaStr by remember { mutableStateOf("") }
    var endDay by remember { mutableStateOf(todayDay) }
    var endMonth by remember { mutableStateOf(todayMonth) }
    var endYear by remember { mutableStateOf(todayYear) }
    var frecuencia by remember { mutableStateOf(SavingFrequency.SEMANAL) }

    val backgroundColor = when (type) {
        EntryType.INCOME -> Color(0xFF513F8B)
        EntryType.SAVING -> Color(0xFFE1D9FF)
        EntryType.EXPENSE -> Color(0xFFFFD1D1)
        EntryType.DEBT -> Color(0xFFFFCDD2)
    }
    
    val contentColor = if (type == EntryType.INCOME) Color.White else Color.Black

    Scaffold(modifier = modifier.fillMaxSize().imePadding()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                }
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = backgroundColor)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (type) {
                        EntryType.INCOME -> IncomeForm(
                            name = name, onNameChange = { name = it },
                            amount = amount, onAmountChange = { if (it.all { c -> c.isDigit() || c == '.' }) amount = it },
                            day = day, onDayChange = { day = it },
                            month = month, onMonthChange = { month = it },
                            year = year, onYearChange = { year = it },
                            color = contentColor
                        )
                        EntryType.SAVING -> SavingForm(
                            apartado = category, 
                            onApartadoChange = { 
                                if(it == "AGREGAR OTRO...") {
                                    isAddingOtherCategory = true
                                    category = "OTRO"
                                } else {
                                    isAddingOtherCategory = false
                                    category = it
                                }
                            },
                            isAddingOtherApartado = isAddingOtherCategory,
                            newApartadoName = newCategoryName,
                            onOtherApartadoChange = { newCategoryName = it },
                            subApartado = subCategory,
                            onSubApartadoChange = {
                                if(it == "AGREGAR OTRO...") {
                                    isAddingOtherSubCategory = true
                                    subCategory = "OTRO"
                                } else {
                                    isAddingOtherSubCategory = false
                                    subCategory = it
                                }
                            },
                            isAddingOtherSub = isAddingOtherSubCategory,
                            newSubName = newSubCategoryName,
                            onOtherSubChange = { newSubCategoryName = it },
                            amount = amount, onAmountChange = { if (it.all { c -> c.isDigit() || c == '.' }) amount = it },
                            day = day, onDayChange = { day = it },
                            month = month, onMonthChange = { month = it },
                            year = year, onYearChange = { year = it },
                            photoUrl = photoUrl, onPhotoUrlChange = { photoUrl = it },
                            progreso = progreso, onProgresoChange = { progreso = it },
                            metaStr = metaStr, onMetaStrChange = { if (it.all { c -> c.isDigit() || c == '.' }) metaStr = it },
                            endDay = endDay, onEndDayChange = { endDay = it },
                            endMonth = endMonth, onEndMonthChange = { endMonth = it },
                            endYear = endYear, onEndYearChange = { endYear = it },
                            frecuencia = frecuencia, onFrecuenciaChange = { frecuencia = it },
                            existingApartados = existingApartados,
                            existingSubApartados = existingSubApartados,
                            color = contentColor
                        )
                        EntryType.EXPENSE -> ExpenseForm(
                            periodo = category, onPeriodoChange = { category = it },
                            concepto = subCategory, onConceptoChange = { subCategory = it },
                            amount = amount, onAmountChange = { if (it.all { c -> c.isDigit() || c == '.' }) amount = it },
                            day = day, onDayChange = { day = it },
                            month = month, onMonthChange = { month = it },
                            year = year, onYearChange = { year = it },
                            color = contentColor
                        )
                        EntryType.DEBT -> DebtForm(
                            periodo = category, onPeriodoChange = { category = it },
                            concepto = subCategory, onConceptoChange = { subCategory = it },
                            amount = amount, onAmountChange = { if (it.all { c -> c.isDigit() || c == '.' }) amount = it },
                            day = day, onDayChange = { day = it },
                            month = month, onMonthChange = { month = it },
                            year = year, onYearChange = { year = it },
                            progreso = progreso, onProgresoChange = { progreso = it },
                            endDay = endDay, onEndDayChange = { endDay = it },
                            endMonth = endMonth, onEndMonthChange = { endMonth = it },
                            endYear = endYear, onEndYearChange = { endYear = it },
                            frecuencia = frecuencia, onFrecuenciaChange = { frecuencia = it },
                            color = contentColor
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Descripción (Opcional)", color = contentColor, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = MiFuenteGoogle)
                        Spacer(modifier = Modifier.height(12.dp))
                        TextField(
                            value = description,
                            onValueChange = { description = it },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = TextStyle(fontSize = 16.sp, fontFamily = MiFuenteGoogle),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(28.dp))
                    
                    Button(
                        onClick = { 
                            val amt = amount.toFloatOrNull() ?: 0f
                            val finalDate = "$day/$month/$year"
                            val finalCategory = if(isAddingOtherCategory) newCategoryName else category
                            val finalSub = if(isAddingOtherSubCategory) newSubCategoryName else subCategory
                            val finalName = name.ifEmpty { if(type == EntryType.INCOME) "INGRESO" else "REGISTRO" }
                            
                            if (type == EntryType.SAVING) {
                                val metaVal = metaStr.toFloatOrNull() ?: 0f
                                val finalEndDate = "$endDay/$endMonth/$endYear"
                                onSaveSaving(finalName, amt, finalDate, finalCategory, finalSub, description, photoUrl, progreso, metaVal, finalEndDate, frecuencia)
                                onSave(finalName, amt, finalDate, finalCategory, finalSub, description)
                            } else if (type == EntryType.DEBT) {
                                val finalEndDate = "$endDay/$endMonth/$endYear"
                                onSaveDebt(finalName, amt, finalDate, finalCategory, finalSub, description, progreso, finalEndDate, frecuencia)
                                onSave(finalName, amt, finalDate, finalCategory, finalSub, description)
                            } else {
                                onSave(finalName, amt, finalDate, finalCategory, finalSub, description)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = contentColor.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        Text("GUARDAR", color = contentColor, fontWeight = FontWeight.Bold, fontFamily = MiFuenteGoogle)
                    }
                }
            }
        }
    }
}

@Composable
fun IncomeForm(
    name: String, onNameChange: (String) -> Unit,
    amount: String, onAmountChange: (String) -> Unit,
    day: String, onDayChange: (String) -> Unit,
    month: String, onMonthChange: (String) -> Unit,
    year: String, onYearChange: (String) -> Unit,
    color: Color
) {
    Text("NUEVO INGRESO", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = color, fontFamily = MiFuenteGoogle)
    Spacer(modifier = Modifier.height(24.dp))

    Text("Nombre del Ingreso *", color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), fontFamily = MiFuenteGoogle)
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        placeholder = { Text("Ej. Nómina, Bono...", color = Color.Gray, fontFamily = MiFuenteGoogle) },
        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        textStyle = TextStyle(fontSize = 16.sp, fontFamily = MiFuenteGoogle),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        ),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(16.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("MONTO", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color, fontFamily = MiFuenteGoogle)
        Spacer(modifier = Modifier.width(4.dp))
        Text("*", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color, fontFamily = MiFuenteGoogle)
    }
    Spacer(modifier = Modifier.height(8.dp))
    EntryAmountField(value = amount, onValueChange = onAmountChange)
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Text("Fecha *", color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), fontFamily = MiFuenteGoogle)
    Spacer(modifier = Modifier.height(8.dp))
    DatePickerSection(day, month, year, onDayChange, onMonthChange, onYearChange)
}

@Composable
fun SavingForm(
    apartado: String, onApartadoChange: (String) -> Unit,
    isAddingOtherApartado: Boolean, newApartadoName: String, onOtherApartadoChange: (String) -> Unit,
    subApartado: String, onSubApartadoChange: (String) -> Unit,
    isAddingOtherSub: Boolean, newSubName: String, onOtherSubChange: (String) -> Unit,
    amount: String, onAmountChange: (String) -> Unit,
    day: String, onDayChange: (String) -> Unit,
    month: String, onMonthChange: (String) -> Unit,
    year: String, onYearChange: (String) -> Unit,
    photoUrl: String, onPhotoUrlChange: (String) -> Unit,
    progreso: Boolean, onProgresoChange: (Boolean) -> Unit,
    metaStr: String, onMetaStrChange: (String) -> Unit,
    endDay: String, onEndDayChange: (String) -> Unit,
    endMonth: String, onEndMonthChange: (String) -> Unit,
    endYear: String, onEndYearChange: (String) -> Unit,
    frecuencia: SavingFrequency, onFrecuenciaChange: (SavingFrequency) -> Unit,
    existingApartados: List<String>,
    existingSubApartados: List<String>,
    color: Color
) {
    var showFreqMenu by remember { mutableStateOf(false) }

    Text("NUEVO AHORRO", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = color, fontFamily = MiFuenteGoogle)
    Spacer(modifier = Modifier.height(24.dp))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPhotoUrlChange("https://picsum.photos/200") }
            .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.DarkGray)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = if (photoUrl.isEmpty()) "Agregar foto al apartado (Opcional)" else "Foto de apartado adjunta ✓",
            fontFamily = MiFuenteGoogle,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray
        )
    }

    Spacer(modifier = Modifier.height(20.dp))
    
    Text("Apartado *", color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), fontFamily = MiFuenteGoogle)
    Spacer(modifier = Modifier.height(8.dp))
    CustomDropdown(
        value = apartado, 
        options = existingApartados + "AGREGAR OTRO...", 
        onValueChange = onApartadoChange, 
        modifier = Modifier.fillMaxWidth()
    )
    
    if (isAddingOtherApartado) {
        Spacer(modifier = Modifier.height(12.dp))
        TextField(
            value = newApartadoName,
            onValueChange = onOtherApartadoChange,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(8.dp),
            textStyle = TextStyle(fontSize = 16.sp, fontFamily = MiFuenteGoogle),
            placeholder = { Text("Escribe nuevo apartado...", fontSize = 16.sp, fontFamily = MiFuenteGoogle) },
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
        )
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Text("Sub-Apartado (Opcional)", color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), fontFamily = MiFuenteGoogle)
    Spacer(modifier = Modifier.height(8.dp))
    CustomDropdown(
        value = subApartado,
        options = existingSubApartados + "AGREGAR OTRO...",
        onValueChange = onSubApartadoChange,
        modifier = Modifier.fillMaxWidth()
    )
    
    if (isAddingOtherSub) {
        Spacer(modifier = Modifier.height(12.dp))
        TextField(
            value = newSubName,
            onValueChange = onOtherSubChange,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(8.dp),
            textStyle = TextStyle(fontSize = 16.sp, fontFamily = MiFuenteGoogle),
            placeholder = { Text("Escribe nuevo sub-apartado...", fontSize = 16.sp, fontFamily = MiFuenteGoogle) },
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
        )
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Text("Fecha de Registro *", color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), fontFamily = MiFuenteGoogle)
    Spacer(modifier = Modifier.height(8.dp))
    DatePickerSection(day, month, year, onDayChange, onMonthChange, onYearChange)

    Spacer(modifier = Modifier.height(16.dp))

    Text("Monto / Cantidad inicial *", color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), fontFamily = MiFuenteGoogle)
    Spacer(modifier = Modifier.height(8.dp))
    EntryAmountField(value = amount, onValueChange = onAmountChange)

    Spacer(modifier = Modifier.height(24.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Medir Progreso / Meta (Opcional)", fontFamily = MiFuenteGoogle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text("Establece un monto final y periodo de ahorro", fontFamily = MiFuenteGoogle, fontSize = 11.sp, color = Color.DarkGray)
        }
        Switch(
            checked = progreso,
            onCheckedChange = onProgresoChange
        )
    }

    if (progreso) {
        Spacer(modifier = Modifier.height(20.dp))
        
        Text("Meta Total a Ahorrar ($) *", color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), fontFamily = MiFuenteGoogle)
        Spacer(modifier = Modifier.height(8.dp))
        EntryAmountField(value = metaStr, onValueChange = onMetaStrChange)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Fecha Final / Límite *", color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), fontFamily = MiFuenteGoogle)
        Spacer(modifier = Modifier.height(8.dp))
        DatePickerSection(endDay, endMonth, endYear, onEndDayChange, onEndMonthChange, onEndYearChange)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Frecuencia de Cálculo *", color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), fontFamily = MiFuenteGoogle)
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { showFreqMenu = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
            ) {
                Text(
                    text = when (frecuencia) {
                        SavingFrequency.SEMANAL -> "Semanal"
                        SavingFrequency.QUINCENAL -> "Quincenal"
                        SavingFrequency.MENSUAL -> "Mensual"
                    },
                    fontFamily = MiFuenteGoogle,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }

            DropdownMenu(expanded = showFreqMenu, onDismissRequest = { showFreqMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Semanal", fontFamily = MiFuenteGoogle) },
                    onClick = {
                        onFrecuenciaChange(SavingFrequency.SEMANAL)
                        showFreqMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Quincenal", fontFamily = MiFuenteGoogle) },
                    onClick = {
                        onFrecuenciaChange(SavingFrequency.QUINCENAL)
                        showFreqMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Mensual", fontFamily = MiFuenteGoogle) },
                    onClick = {
                        onFrecuenciaChange(SavingFrequency.MENSUAL)
                        showFreqMenu = false
                    }
                )
            }
        }
    }
}

@Composable
fun ExpenseForm(
    periodo: String, onPeriodoChange: (String) -> Unit,
    concepto: String, onConceptoChange: (String) -> Unit,
    amount: String, onAmountChange: (String) -> Unit,
    day: String, onDayChange: (String) -> Unit,
    month: String, onMonthChange: (String) -> Unit,
    year: String, onYearChange: (String) -> Unit,
    color: Color
) {
    Text("NUEVO GASTO", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = color, fontFamily = MiFuenteGoogle)
    Spacer(modifier = Modifier.height(24.dp))
    
    val periods = listOf("ENE-FEB", "MAR-ABR", "MAY-JUN", "JUL-AGO", "SEP-OCT", "NOV-DIC")
    Text("Periodo *", color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), fontFamily = MiFuenteGoogle)
    Spacer(modifier = Modifier.height(8.dp))
    CustomDropdown(value = if (periodo.isEmpty()) "ENE-FEB" else periodo, options = periods, onValueChange = onPeriodoChange, modifier = Modifier.fillMaxWidth())
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Text("Concepto / Nombre *", color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), fontFamily = MiFuenteGoogle)
    Spacer(modifier = Modifier.height(8.dp))
    TextField(
        value = concepto,
        onValueChange = onConceptoChange,
        modifier = Modifier.fillMaxWidth().height(60.dp),
        shape = RoundedCornerShape(8.dp),
        textStyle = TextStyle(fontSize = 16.sp, fontFamily = MiFuenteGoogle),
        placeholder = { Text("¿En qué gastaste?", fontSize = 16.sp, fontFamily = MiFuenteGoogle) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White, 
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
    
    Spacer(modifier = Modifier.height(16.dp))

    Text("Fecha *", color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), fontFamily = MiFuenteGoogle)
    Spacer(modifier = Modifier.height(8.dp))
    DatePickerSection(day, month, year, onDayChange, onMonthChange, onYearChange)

    Spacer(modifier = Modifier.height(16.dp))
    
    Text("Monto *", color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), fontFamily = MiFuenteGoogle)
    Spacer(modifier = Modifier.height(8.dp))
    EntryAmountField(value = amount, onValueChange = onAmountChange)
}

@Composable
fun DebtForm(
    periodo: String, onPeriodoChange: (String) -> Unit,
    concepto: String, onConceptoChange: (String) -> Unit,
    amount: String, onAmountChange: (String) -> Unit,
    day: String, onDayChange: (String) -> Unit,
    month: String, onMonthChange: (String) -> Unit,
    year: String, onYearChange: (String) -> Unit,
    progreso: Boolean, onProgresoChange: (Boolean) -> Unit,
    endDay: String, onEndDayChange: (String) -> Unit,
    endMonth: String, onEndMonthChange: (String) -> Unit,
    endYear: String, onEndYearChange: (String) -> Unit,
    frecuencia: SavingFrequency, onFrecuenciaChange: (SavingFrequency) -> Unit,
    color: Color
) {
    var showFreqMenu by remember { mutableStateOf(false) }

    Text("NUEVA DEUDA", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = color, fontFamily = MiFuenteGoogle)
    Spacer(modifier = Modifier.height(24.dp))
    
    val periods = listOf("ENE-FEB", "MAR-ABR", "MAY-JUN", "JUL-AGO", "SEP-OCT", "NOV-DIC")
    Text("Periodo (opcional)", color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), fontFamily = MiFuenteGoogle)
    Spacer(modifier = Modifier.height(8.dp))
    CustomDropdown(
        value = periodo,
        options = listOf("SIN PERIODO") + periods,
        onValueChange = { onPeriodoChange(if (it == "SIN PERIODO") "" else it) },
        modifier = Modifier.fillMaxWidth()
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Text("Concepto / Nombre *", color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), fontFamily = MiFuenteGoogle)
    Spacer(modifier = Modifier.height(8.dp))
    TextField(
        value = concepto,
        onValueChange = onConceptoChange,
        modifier = Modifier.fillMaxWidth().height(60.dp),
        shape = RoundedCornerShape(8.dp),
        textStyle = TextStyle(fontSize = 16.sp, fontFamily = MiFuenteGoogle),
        placeholder = { Text("Ej. Tarjeta de crédito", fontSize = 16.sp, fontFamily = MiFuenteGoogle) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White, 
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
    
    Spacer(modifier = Modifier.height(16.dp))

    Text("Fecha de Registro *", color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), fontFamily = MiFuenteGoogle)
    Spacer(modifier = Modifier.height(8.dp))
    DatePickerSection(day, month, year, onDayChange, onMonthChange, onYearChange)

    Spacer(modifier = Modifier.height(16.dp))
    
    Text("Monto *", color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), fontFamily = MiFuenteGoogle)
    Spacer(modifier = Modifier.height(8.dp))
    EntryAmountField(value = amount, onValueChange = onAmountChange)

    Spacer(modifier = Modifier.height(24.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Medir Fecha Límite / Sugerencia de Pago (Opcional)", fontFamily = MiFuenteGoogle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text("Establece fecha de pago límite y frecuencia", fontFamily = MiFuenteGoogle, fontSize = 11.sp, color = Color.DarkGray)
        }
        Switch(
            checked = progreso,
            onCheckedChange = onProgresoChange
        )
    }

    if (progreso) {
        Spacer(modifier = Modifier.height(20.dp))

        Text("Fecha de Pago Límite *", color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), fontFamily = MiFuenteGoogle)
        Spacer(modifier = Modifier.height(8.dp))
        DatePickerSection(endDay, endMonth, endYear, onEndDayChange, onEndMonthChange, onEndYearChange)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Frecuencia de Sugerencia *", color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), fontFamily = MiFuenteGoogle)
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { showFreqMenu = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
            ) {
                Text(
                    text = when (frecuencia) {
                        SavingFrequency.SEMANAL -> "Semanal"
                        SavingFrequency.QUINCENAL -> "Quincenal"
                        SavingFrequency.MENSUAL -> "Mensual"
                    },
                    fontFamily = MiFuenteGoogle,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }

            DropdownMenu(expanded = showFreqMenu, onDismissRequest = { showFreqMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Semanal", fontFamily = MiFuenteGoogle) },
                    onClick = {
                        onFrecuenciaChange(SavingFrequency.SEMANAL)
                        showFreqMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Quincenal", fontFamily = MiFuenteGoogle) },
                    onClick = {
                        onFrecuenciaChange(SavingFrequency.QUINCENAL)
                        showFreqMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Mensual", fontFamily = MiFuenteGoogle) },
                    onClick = {
                        onFrecuenciaChange(SavingFrequency.MENSUAL)
                        showFreqMenu = false
                    }
                )
            }
        }
    }
}

@Composable
fun DatePickerSection(
    day: String, month: String, year: String,
    onDayChange: (String) -> Unit, onMonthChange: (String) -> Unit, onYearChange: (String) -> Unit
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CustomDropdown(value = day, options = (1..31).map { it.toString().padStart(2, '0') }, onValueChange = onDayChange, modifier = Modifier.weight(1f))
        CustomDropdown(value = month, options = (1..12).map { it.toString().padStart(2, '0') }, onValueChange = onMonthChange, modifier = Modifier.weight(1f))
        CustomDropdown(value = year, options = (currentYear - 10..currentYear + 10).map { it.toString() }, onValueChange = onYearChange, modifier = Modifier.weight(1.5f))
    }
}

@Composable
fun EntryAmountField(value: String, onValueChange: (String) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().height(60.dp).background(Color.White, RoundedCornerShape(8.dp)).padding(horizontal = 16.dp)
    ) {
        Text("$", fontSize = 24.sp, color = Color.Gray, fontWeight = FontWeight.Bold, fontFamily = MiFuenteGoogle)
        Spacer(modifier = Modifier.width(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("0.00", color = Color.LightGray, fontSize = 28.sp, fontFamily = MiFuenteGoogle) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black, fontFamily = MiFuenteGoogle),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )
    }
}

@Composable
fun CustomDropdown(value: String, options: List<String>, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(text = if(value.isEmpty() || value == "OTRO") "Seleccionar..." else value, color = Color.Black, fontSize = 16.sp, fontFamily = MiFuenteGoogle)
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.align(Alignment.CenterEnd),
                tint = Color.Gray
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontFamily = MiFuenteGoogle) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
