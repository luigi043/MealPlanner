package com.mealplanner.presentation.ui.recipes

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mealplanner.domain.model.Ingredient
import com.mealplanner.domain.model.Recipe
import com.mealplanner.domain.model.ingredientScale
import com.mealplanner.presentation.ui.components.*
import com.mealplanner.presentation.ui.theme.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    viewModel: RecipeDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(recipeId) { viewModel.loadRecipe(recipeId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.recipe?.name ?: "", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (state.recipe?.isFavorite == true) Icons.Filled.Favorite
                                          else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (state.recipe?.isFavorite == true) Color(0xFFE63946)
                                   else MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingState(modifier = Modifier.padding(padding))
            state.recipe == null -> EmptyState(
                icon = Icons.Outlined.ErrorOutline,
                title = "Recipe not found",
                message = "This recipe may have been deleted",
                modifier = Modifier.padding(padding)
            )
            else -> {
                val recipe = state.recipe!!
                val scale = ingredientScale(state.scaledServings, recipe.servings)

                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                ) {
                    // Hero section
                    RecipeHero(recipe = recipe, modifier = Modifier.padding(16.dp))

                    // Serving adjuster
                    ServingAdjuster(
                        servings = state.scaledServings,
                        onDecrease = { viewModel.onServingsChange(state.scaledServings - 1) },
                        onIncrease = { viewModel.onServingsChange(state.scaledServings + 1) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    // Nutrition info
                    NutritionSection(recipe = recipe, scale = state.scaledServings.toDouble(), modifier = Modifier.padding(horizontal = 16.dp))

                    Spacer(Modifier.height(16.dp))

                    // Ingredients
                    IngredientsSection(
                        ingredients = recipe.ingredients,
                        scale = scale,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    // Instructions
                    InstructionsSection(
                        instructions = recipe.instructions,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Tags
                    if (recipe.tags.isNotEmpty()) {
                        TagsSection(tags = recipe.tags, modifier = Modifier.padding(16.dp))
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun RecipeHero(recipe: Recipe, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${recipe.category.emoji} ${recipe.category.displayName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (recipe.isFavorite) {
                    Text("⭐ Favorite", style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF856404))
                }
            }
            Spacer(Modifier.height(8.dp))
            if (recipe.description.isNotBlank()) {
                Text(
                    text = recipe.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(12.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                TimeChip(Icons.Outlined.Timer, "Prep", "${recipe.prepTimeMinutes} min")
                TimeChip(Icons.Outlined.LocalFireDepartment, "Cook", "${recipe.cookTimeMinutes} min")
                TimeChip(Icons.Outlined.People, "Serves", "${recipe.servings}")
            }
        }
    }
}

@Composable
private fun TimeChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary)
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ServingAdjuster(
    servings: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Adjust Servings", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledIconButton(
                    onClick = onDecrease,
                    modifier = Modifier.size(32.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) { Icon(Icons.Filled.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp)) }
                Text(
                    text = "$servings",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                FilledIconButton(
                    onClick = onIncrease,
                    modifier = Modifier.size(32.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Icon(Icons.Filled.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimary) }
            }
        }
    }
}

@Composable
private fun NutritionSection(recipe: Recipe, scale: Double, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text("Nutrition", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Total for ${"%.0f".format(scale)} servings. Values entered per serving.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MacroChip("Cal", recipe.calories * scale, "kcal", GreenPrimary, Modifier.weight(1f))
            MacroChip("Protein", recipe.protein * scale, "g", Color(0xFF0077B6), Modifier.weight(1f))
            MacroChip("Carbs", recipe.carbs * scale, "g", Color(0xFFE76F51), Modifier.weight(1f))
            MacroChip("Fat", recipe.fat * scale, "g", Color(0xFF6A4C93), Modifier.weight(1f))
        }
        if (recipe.fiber > 0) {
            Spacer(Modifier.height(8.dp))
            Text("Fiber: ${"%.1f".format(recipe.fiber * scale)}g",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun IngredientsSection(ingredients: List<Ingredient>, scale: Double, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Ingredients", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            ingredients.forEachIndexed { index, ingredient ->
                if (index > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(8.dp).background(GreenPrimary, CircleShape)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(ingredient.name, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        text = formatQuantity(ingredient.quantity * scale, ingredient.unit),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun InstructionsSection(instructions: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Instructions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            val steps = instructions.split("\n").filter { it.isNotBlank() }
            steps.forEachIndexed { index, step ->
                Row(modifier = Modifier.padding(vertical = 6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(step.trim(), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TagsSection(tags: List<String>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text("Tags", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            tags.forEach { tag ->
                SuggestionChip(
                    onClick = {},
                    label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
    }
}

private fun formatQuantity(quantity: Double, unit: String): String {
    val rounded = (quantity * 10).roundToInt() / 10.0
    return if (rounded == rounded.toLong().toDouble()) {
        "${rounded.toLong()} $unit"
    } else {
        "$rounded $unit"
    }
}
