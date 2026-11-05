package com.mealplanner.presentation.ui.recipes

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mealplanner.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecipeScreen(
    recipeId: Long?,
    onBack: () -> Unit,
    viewModel: AddEditRecipeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isEditing = recipeId != null

    LaunchedEffect(recipeId) {
        recipeId?.let { viewModel.loadRecipe(it) }
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onBack()
    }

    var showIngredientDialog by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditing) "Edit Recipe" else "New Recipe",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                actions = {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(end = 16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        TextButton(onClick = { viewModel.saveRecipe() }) {
                            Text("Save", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            state.errors["save"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (state.errors.isNotEmpty()) {
                Text("Please check the highlighted fields below.", color = MaterialTheme.colorScheme.error)
            }
            // Basic Info Card
            FormCard(title = "Basic Information") {
                FormField(
                    label = "Recipe Name *",
                    value = state.name,
                    onValueChange = viewModel::onNameChange,
                    placeholder = "e.g. Grilled Chicken Salad",
                    error = state.errors["name"]
                )
                FormField(
                    label = "Description",
                    value = state.description,
                    onValueChange = viewModel::onDescriptionChange,
                    placeholder = "Brief description...",
                    maxLines = 3
                )
                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = showCategoryMenu,
                    onExpandedChange = { showCategoryMenu = it }
                ) {
                    OutlinedTextField(
                        value = "${state.category.emoji} ${state.category.displayName}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { showCategoryMenu = false }
                    ) {
                        RecipeCategory.values().filter { it != RecipeCategory.ALL }.forEach { category ->
                            DropdownMenuItem(
                                text = { Text("${category.emoji} ${category.displayName}") },
                                onClick = {
                                    viewModel.onCategoryChange(category)
                                    showCategoryMenu = false
                                }
                            )
                        }
                    }
                }
                FormField(
                    label = "Tags (comma separated)",
                    value = state.tags,
                    onValueChange = viewModel::onTagsChange,
                    placeholder = "e.g. healthy, quick, low-calorie"
                )
            }

            // Timing & Servings Card
            FormCard(title = "Timing & Servings") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FormField(
                        label = "Prep Time (min)",
                        value = state.prepTime,
                        error = state.errors["prepTime"],
                        onValueChange = viewModel::onPrepTimeChange,
                        placeholder = "15",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                    FormField(
                        label = "Cook Time (min)",
                        value = state.cookTime,
                        error = state.errors["cookTime"],
                        onValueChange = viewModel::onCookTimeChange,
                        placeholder = "30",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                    FormField(
                        label = "Servings",
                        value = state.servings,
                        error = state.errors["servings"],
                        onValueChange = viewModel::onServingsChange,
                        placeholder = "2",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Nutrition Card
            FormCard(title = "Nutritional Values (per serving)") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FormField(
                        label = "Calories *",
                        value = state.calories,
                        onValueChange = viewModel::onCaloriesChange,
                        placeholder = "0",
                        keyboardType = KeyboardType.Decimal,
                        error = state.errors["calories"],
                        modifier = Modifier.weight(1f)
                    )
                    FormField(
                        label = "Protein (g)",
                        value = state.protein,
                        error = state.errors["protein"],
                        onValueChange = viewModel::onProteinChange,
                        placeholder = "0",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FormField(
                        label = "Carbs (g)",
                        value = state.carbs,
                        error = state.errors["carbs"],
                        onValueChange = viewModel::onCarbsChange,
                        placeholder = "0",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                    FormField(
                        label = "Fat (g)",
                        value = state.fat,
                        error = state.errors["fat"],
                        onValueChange = viewModel::onFatChange,
                        placeholder = "0",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                    FormField(
                        label = "Fiber (g)",
                        value = state.fiber,
                        error = state.errors["fiber"],
                        onValueChange = viewModel::onFiberChange,
                        placeholder = "0",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Ingredients Card
            FormCard(
                title = "Ingredients",
                trailingContent = {
                    FilledTonalButton(
                        onClick = { showIngredientDialog = true },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add", style = MaterialTheme.typography.labelMedium)
                    }
                }
            ) {
                state.errors["ingredients"]?.let { error ->
                    Text(error, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }
                if (state.ingredients.isEmpty()) {
                    Text(
                        "No ingredients added yet. Tap 'Add' to add ingredients.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    state.ingredients.forEach { ingredient ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("•", modifier = Modifier.padding(end = 8.dp))
                            Text(
                                "${ingredient.quantity} ${ingredient.unit} ${ingredient.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.removeIngredient(ingredient) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Remove",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // Instructions Card
            FormCard(title = "Instructions") {
                OutlinedTextField(
                    value = state.instructions,
                    onValueChange = viewModel::onInstructionsChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter step-by-step instructions (one step per line)...") },
                    label = { Text("Instructions *") },
                    minLines = 5,
                    maxLines = 15,
                    isError = state.errors.containsKey("instructions"),
                    supportingText = {
                        state.errors["instructions"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Save button
            Button(
                onClick = { viewModel.saveRecipe() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !state.isSaving
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (isEditing) "Update Recipe" else "Save Recipe",
                        style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showIngredientDialog) {
        AddIngredientDialog(
            onDismiss = { showIngredientDialog = false },
            onAdd = { name, quantity, unit ->
                viewModel.addIngredient(name, quantity, unit)
                showIngredientDialog = false
            }
        )
    }
}

@Composable
private fun FormCard(
    title: String,
    trailingContent: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                trailingContent?.invoke()
            }
            content()
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLines: Int = 1,
    error: String? = null,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        maxLines = maxLines,
        minLines = if (maxLines > 1) 2 else 1,
        isError = error != null,
        supportingText = { error?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun AddIngredientDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, quantity: String, unit: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    val commonUnits = listOf("g", "kg", "ml", "l", "cup", "tbsp", "tsp", "oz", "lb", "piece", "slice")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Ingredient") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Ingredient Name") },
                    placeholder = { Text("e.g. Chicken breast") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Qty") },
                        placeholder = { Text("100") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        placeholder = { Text("g") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                // Unit quick-select
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    commonUnits.forEach { u ->
                        FilterChip(
                            selected = unit == u,
                            onClick = { unit = u },
                            label = { Text(u, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank() && quantity.isNotBlank()) onAdd(name, quantity, unit) },
                enabled = name.isNotBlank() && unit.isNotBlank() && validAmount(quantity, allowZero = false)
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
