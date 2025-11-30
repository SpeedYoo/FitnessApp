package com.example.fitnessapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.fitnessapp.ui.components.FitnessTextField
import com.example.fitnessapp.ui.components.InfoCard
import com.example.fitnessapp.ui.components.PrimaryButton
import com.example.fitnessapp.ui.components.ScreenHeader
import com.example.fitnessapp.ui.theme.*
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
            .background(BackgroundBlack)
            .padding(Dimensions.paddingLarge)
            .verticalScroll(rememberScrollState())
    ) {
        // Nagłówek
        ScreenHeader(
            title = "Profil",
            onNavigateBack = onNavigateBack
        )

        Spacer(modifier = Modifier.height(Dimensions.spacingXLarge))

        // Ikona profilu
        ProfileAvatar()

        Spacer(modifier = Modifier.height(Dimensions.spacingXLarge))

        // Płeć - Dropdown
        GenderDropdown(
            selectedGender = selectedGender,
            expanded = expanded,
            onExpandedChange = { expanded = it },
            onGenderSelected = {
                selectedGender = it
                expanded = false
            }
        )

        Spacer(modifier = Modifier.height(Dimensions.spacingLarge))

        // Wiek
        FitnessTextField(
            label = "Wiek",
            value = age,
            onValueChange = { age = it },
            placeholder = "np. 25",
            suffix = "lat"
        )

        Spacer(modifier = Modifier.height(Dimensions.spacingLarge))

        // Waga
        FitnessTextField(
            label = "Waga",
            value = weight,
            onValueChange = { weight = it },
            placeholder = "np. 70",
            suffix = "kg"
        )

        Spacer(modifier = Modifier.height(Dimensions.spacingLarge))

        // Wzrost
        FitnessTextField(
            label = "Wzrost",
            value = height,
            onValueChange = { height = it },
            placeholder = "np. 175",
            suffix = "cm"
        )

        Spacer(modifier = Modifier.height(Dimensions.spacingXLarge))

        // Separator
        Text(
            text = "Cele dzienne",
            style = FitnessTextStyles.screenTitle.copy(fontSize = 20.sp),
            color = TextWhite,
            modifier = Modifier.padding(bottom = Dimensions.spacingLarge)
        )

        // Cel kalorii
        FitnessTextField(
            label = "Cel kalorii",
            value = caloriesGoal,
            onValueChange = { caloriesGoal = it },
            placeholder = "np. 500",
            suffix = "kcal"
        )

        Spacer(modifier = Modifier.height(Dimensions.spacingLarge))

        // Cel kroków
        FitnessTextField(
            label = "Cel kroków",
            value = stepsGoal,
            onValueChange = { stepsGoal = it },
            placeholder = "np. 6000",
            suffix = "kroków"
        )

        Spacer(modifier = Modifier.height(Dimensions.spacingXLarge))

        // Przycisk zapisz
        PrimaryButton(
            text = "Zapisz",
            onClick = {
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
            }
        )

        Spacer(modifier = Modifier.height(Dimensions.spacingLarge))

        // Info
        InfoCard(
            title = "ℹ️ Informacja",
            description = "Te dane są używane do dokładnego obliczania spalonych kalorii podczas aktywności fizycznej."
        )

        Spacer(modifier = Modifier.height(Dimensions.spacingLarge))
    }
}

@Composable
private fun ProfileAvatar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.spacingXLarge),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(Dimensions.avatarSizeXLarge),
            color = SurfaceDark,
            shape = RoundedCornerShape(50)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "👤",
                    fontSize = 48.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenderDropdown(
    selectedGender: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onGenderSelected: (String) -> Unit
) {
    Column {
        Text(
            text = "Płeć",
            style = FitnessTextStyles.cardTitle,
            color = TextLightGray,
            modifier = Modifier.padding(bottom = Dimensions.spacingSmall)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = onExpandedChange
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
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = FitnessGreen,
                    unfocusedBorderColor = SurfaceDarkSecondary,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                ),
                shape = RoundedCornerShape(Dimensions.inputFieldCornerRadius)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) },
                modifier = Modifier.background(SurfaceDark)
            ) {
                DropdownMenuItem(
                    text = { Text("Mężczyzna", color = TextWhite) },
                    onClick = { onGenderSelected("Mężczyzna") }
                )
                DropdownMenuItem(
                    text = { Text("Kobieta", color = TextWhite) },
                    onClick = { onGenderSelected("Kobieta") }
                )
            }
        }
    }
}