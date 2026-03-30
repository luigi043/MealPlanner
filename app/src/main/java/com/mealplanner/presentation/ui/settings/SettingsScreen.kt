package com.mealplanner.presentation.ui.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mealplanner.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Settings", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile/Goal section
            SettingsCard(title = "My Goal", icon = Icons.Outlined.TrackChanges) {
                Text("Planning Goal", style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PlanningGoal.entries.forEach { goal ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (state.userGoals.planningGoal == goal) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(0.3f)
                                )
                                .clickable { viewModel.updatePlanningGoal(goal) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.userGoals.planningGoal == goal,
                                onClick = { viewModel.updatePlanningGoal(goal) }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(goal.displayName, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Nutrition targets
            SettingsCard(title = "Daily Nutrition Targets", icon = Icons.Outlined.MonitorHeart) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NutritionTargetField(
                        label = "Calories", value = state.userGoals.dailyCalorieTarget.toString(),
                        onValueChange = viewModel::updateCalorieTarget, unit = "kcal",
                        modifier = Modifier.weight(1f)
                    )
                    NutritionTargetField(
                        label = "Protein", value = state.userGoals.dailyProteinTarget.toString(),
                        onValueChange = viewModel::updateProteinTarget, unit = "g",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NutritionTargetField(
                        label = "Carbs", value = state.userGoals.dailyCarbsTarget.toString(),
                        onValueChange = viewModel::updateCarbsTarget, unit = "g",
                        modifier = Modifier.weight(1f)
                    )
                    NutritionTargetField(
                        label = "Fat", value = state.userGoals.dailyFatTarget.toString(),
                        onValueChange = viewModel::updateFatTarget, unit = "g",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Preferences
            SettingsCard(title = "Preferences", icon = Icons.Outlined.Tune) {
                SettingsToggle(
                    label = "Meal Reminders",
                    description = "Get notified about meal times",
                    icon = Icons.Outlined.Notifications,
                    checked = state.notificationsEnabled,
                    onCheckedChange = viewModel::toggleNotifications
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                SettingsToggle(
                    label = "Dark Mode",
                    description = "Switch between light and dark theme",
                    icon = Icons.Outlined.DarkMode,
                    checked = state.darkModeEnabled,
                    onCheckedChange = viewModel::toggleDarkMode
                )
            }

            // App info
            SettingsCard(title = "About", icon = Icons.Outlined.Info) {
                InfoRow("Version", "1.0.0")
                InfoRow("Build", "Production")
                InfoRow("Recipe Storage", "Room Database (local)")
                InfoRow("Architecture", "MVVM + Clean Architecture")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            content()
        }
    }
}

@Composable
private fun NutritionTargetField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    unit: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        suffix = { Text(unit, style = MaterialTheme.typography.labelSmall) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun SettingsToggle(
    label: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(description, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}
