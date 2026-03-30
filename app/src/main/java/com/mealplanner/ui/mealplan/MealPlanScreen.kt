package com.mealplanner.presentation.ui.mealplan

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mealplanner.domain.model.*
import com.mealplanner.presentation.ui.components.*
import com.mealplanner.presentation.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanScreen(
    onNavigateToRecipes: () -> Unit,
    viewModel: MealPlanViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddMealDialog by remember { mutableStateOf<MealType?>(null) }
    var showSnackbar by remember { mutableStateOf(false) }

    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            kotlinx.coroutines.delay(2000)
            showSnackbar = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Meal Planner", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleView() }) {
                        Icon(
                            if (state.showWeekView) Icons.Outlined.CalendarViewDay else Icons.Outlined.CalendarViewWeek,
                            contentDescription = "Toggle view",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = {
                        viewModel.autoGenerateWeekPlan()
                        showSnackbar = true
                    }) {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = "Auto-generate",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Week strip calendar
            WeekStripCalendar(
                selectedDate = state.selectedDate,
                weekStart = state.weekStart,
                datesWithPlans = state.datesWithPlans,
                onDateSelected = viewModel::selectDate
            )

            // Generate grocery list CTA
            Surface(color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Plan ready? Generate grocery list",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FilledTonalButton(
                        onClick = { viewModel.generateGroceryList() },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Outlined.ShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Generate", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            if (state.isGenerating) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Day meals
            LazyColumn(
                contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(MealType.values().sortedBy { it.order }) { mealType ->
                    MealTypeSection(
                        mealType = mealType,
                        meals = state.dayMeals[mealType] ?: emptyList(),
                        onAddMeal = {
                            viewModel.loadSmartSuggestions(mealType)
                            showAddMealDialog = mealType
                        },
                        onRemoveMeal = viewModel::removeMeal
                    )
                }
            }
        }
    }

    showAddMealDialog?.let { mealType ->
        AddMealDialog(
            mealType = mealType,
            recipes = state.availableRecipes,
            suggestions = state.suggestions,
            onDismiss = { showAddMealDialog = null },
            onAdd = { recipe, servings ->
                viewModel.addMeal(state.selectedDate, mealType, recipe, servings)
                showAddMealDialog = null
            }
        )
    }

    if (showSnackbar) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Snackbar(modifier = Modifier.padding(16.dp)) {
                Text("✨ Week meal plan generated!")
            }
        }
    }
}

@Composable
private fun WeekStripCalendar(
    selectedDate: LocalDate,
    weekStart: LocalDate,
    datesWithPlans: List<LocalDate>,
    onDateSelected: (LocalDate) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(bottom = 12.dp)) {
            Text(
                text = weekStart.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                (0..6).forEach { offset ->
                    val date = weekStart.plusDays(offset.toLong())
                    val isSelected = date == selectedDate
                    val isToday = date == LocalDate.now()
                    val hasPlan = date in datesWithPlans

                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                                else Color.Transparent
                            )
                            .clickable { onDateSelected(date) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(1),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else Color.Transparent,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(
                                    if (hasPlan) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                    else Color.Transparent,
                                    CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MealTypeSection(
    mealType: MealType,
    meals: List<MealPlan>,
    onAddMeal: () -> Unit,
    onRemoveMeal: (MealPlan) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(mealType.emoji, fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(mealType.displayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    if (meals.isNotEmpty()) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${meals.sumOf { (it.recipeCalories * it.servings).toInt() }} kcal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                FilledTonalIconButton(onClick = onAddMeal, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Add, contentDescription = "Add meal", modifier = Modifier.size(16.dp))
                }
            }

            if (meals.isEmpty()) {
                Text(
                    "No ${mealType.displayName.lowercase()} planned",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                Spacer(Modifier.height(8.dp))
                meals.forEach { meal ->
                    MealItem(
                        mealPlan = meal,
                        onRemove = { onRemoveMeal(meal) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MealItem(mealPlan: MealPlan, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.4f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(mealPlan.recipeName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                "${(mealPlan.recipeCalories * mealPlan.servings).toInt()} kcal • ${mealPlan.servings} serving(s)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Filled.Close, contentDescription = "Remove",
                modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
        }
    }
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun AddMealDialog(
    mealType: MealType,
    recipes: List<Recipe>,
    suggestions: List<SmartSuggestion>,
    onDismiss: () -> Unit,
    onAdd: (Recipe, Int) -> Unit
) {
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
    var servings by remember { mutableStateOf("1") }
    var searchQuery by remember { mutableStateOf("") }
    var showSuggestions by remember { mutableStateOf(true) }

    val filteredRecipes = if (searchQuery.isBlank()) recipes
    else recipes.filter { it.name.contains(searchQuery, ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to ${mealType.displayName}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Search
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it; showSuggestions = false },
                    placeholder = { Text("Search recipes...") },
                    leadingIcon = { Icon(Icons.Outlined.Search, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Smart suggestions
                if (showSuggestions && suggestions.isNotEmpty()) {
                    Text("✨ Smart Suggestions", style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    suggestions.take(3).forEach { suggestion ->
                        SuggestionItem(
                            suggestion = suggestion,
                            isSelected = selectedRecipe?.id == suggestion.recipe.id,
                            onClick = { selectedRecipe = suggestion.recipe }
                        )
                    }
                    Divider()
                }

                // Recipe list
                Text("All Recipes", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Column(
                    modifier = Modifier.heightIn(max = 220.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    filteredRecipes.take(20).forEach { recipe ->
                        RecipeSelectItem(
                            recipe = recipe,
                            isSelected = selectedRecipe?.id == recipe.id,
                            onClick = { selectedRecipe = recipe }
                        )
                    }
                }

                // Servings
                selectedRecipe?.let {
                    Divider()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Servings:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        OutlinedTextField(
                            value = servings,
                            onValueChange = { if (it.toIntOrNull() != null || it.isEmpty()) servings = it },
                            modifier = Modifier.width(80.dp),
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedRecipe?.let { onAdd(it, servings.toIntOrNull() ?: 1) } },
                enabled = selectedRecipe != null
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun SuggestionItem(suggestion: SmartSuggestion, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant.copy(0.4f)
        ),
        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(suggestion.recipe.name, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold)
                Text(suggestion.reason, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary)
            }
            Text("${suggestion.recipe.calories.toInt()} kcal",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RecipeSelectItem(recipe: Recipe, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(recipe.name, style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
        Text("${recipe.calories.toInt()} kcal",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}