package com.example.fitnessapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitnessapp.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()

    var age by remember { mutableStateOf(profile.age.toString()) }
    var weight by remember { mutableStateOf(profile.weight.toString()) }
    var height by remember { mutableStateOf(profile.height.toString()) }
    var caloriesGoal by remember { mutableStateOf(profile.dailyCaloriesGoal.toString()) }
    var stepsGoal by remember { mutableStateOf(profile.dailyStepsGoal.toString()) }
    var selectedGender by remember { mutableStateOf(profile.gender) }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Nagłówek z przyciskiem powrotu
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Powrót",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Profil",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Ikona profilu
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(100.dp),
                color = Color(0xFF2C2C2E),
                shape = RoundedCornerShape(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "👤",
                        fontSize = 48.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Płeć - Dropdown
        Text(
            text = "Płeć",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.LightGray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedGender,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF32D74B),
                    unfocusedBorderColor = Color(0xFF3A3A3C),
                    focusedContainerColor = Color(0xFF2C2C2E),
                    unfocusedContainerColor = Color(0xFF2C2C2E)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF2C2C2E))
            ) {
                DropdownMenuItem(
                    text = { Text("Mężczyzna", color = Color.White) },
                    onClick = {
                        selectedGender = "Mężczyzna"
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Kobieta", color = Color.White) },
                    onClick = {
                        selectedGender = "Kobieta"
                        expanded = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Wiek
        ProfileInputField(
            label = "Wiek",
            value = age,
            onValueChange = { age = it },
            placeholder = "np. 25",
            suffix = "lat"
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Waga
        ProfileInputField(
            label = "Waga",
            value = weight,
            onValueChange = { weight = it },
            placeholder = "np. 70",
            suffix = "kg"
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Wzrost
        ProfileInputField(
            label = "Wzrost",
            value = height,
            onValueChange = { height = it },
            placeholder = "np. 175",
            suffix = "cm"
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Separator
        Text(
            text = "Cele dzienne",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Cel kalorii
        ProfileInputField(
            label = "Cel kalorii",
            value = caloriesGoal,
            onValueChange = { caloriesGoal = it },
            placeholder = "np. 500",
            suffix = "kcal"
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Cel kroków
        ProfileInputField(
            label = "Cel kroków",
            value = stepsGoal,
            onValueChange = { stepsGoal = it },
            placeholder = "np. 6000",
            suffix = "kroków"
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Przycisk zapisz
        Button(
            onClick = {
                // Walidacja i zapisanie danych
                val ageInt = age.toIntOrNull() ?: 0
                val weightFloat = weight.toFloatOrNull() ?: 0f
                val heightInt = height.toIntOrNull() ?: 0
                val caloriesGoalInt = caloriesGoal.toIntOrNull() ?: 500
                val stepsGoalInt = stepsGoal.toIntOrNull() ?: 6000

                if (ageInt > 0 && weightFloat > 0 && heightInt > 0) {
                    viewModel.updateProfile(
                        gender = selectedGender,
                        age = ageInt,
                        weight = weightFloat,
                        height = heightInt,
                        caloriesGoal = caloriesGoalInt,
                        stepsGoal = stepsGoalInt
                    )
                    onNavigateBack()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF32D74B),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Zapisz",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Info o BMI
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2C2C2E)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "ℹ️ Informacja",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Te dane są używane do dokładnego obliczania spalonych kalorii podczas aktywności fizycznej.",
                    fontSize = 12.sp,
                    color = Color.LightGray,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun ProfileInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    suffix: String
) {
    Column {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.LightGray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.Gray) },
            suffix = { Text(suffix, color = Color.LightGray) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF32D74B),
                unfocusedBorderColor = Color(0xFF3A3A3C),
                focusedContainerColor = Color(0xFF2C2C2E),
                unfocusedContainerColor = Color(0xFF2C2C2E),
                cursorColor = Color(0xFF32D74B)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
    }
}