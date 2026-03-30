package com.mealplanner.presentation.ui.grocery

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mealplanner.domain.model.GroceryItem
import com.mealplanner.presentation.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryScreen(viewModel: GroceryViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var showClearMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Grocery List", style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        if (state.totalItems > 0) {
                            Text(
                                "${state.checkedItems}/${state.totalItems} items checked",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showClearMenu = true }) {
                            Icon(Icons.Outlined.MoreVert, contentDescription = "More",
                                tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        DropdownMenu(
                            expanded = showClearMenu,
                            onDismissRequest = { showClearMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Clear checked items") },
                                leadingIcon = { Icon(Icons.Outlined.CheckCircle, null) },
                                onClick = {
                                    viewModel.clearChecked()
                                    showClearMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear all items", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Outlined.DeleteSweep, null,
                                    tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    viewModel.clearAll()
                                    showClearMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add item",
                    tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingState(modifier = Modifier.padding(padding))
            state.totalItems == 0 -> EmptyState(
                icon = Icons.Outlined.ShoppingCart,
                title = "Your grocery list is empty",
                message = "Generate a list from your meal plan or add items manually",
                action = {
                    FilledTonalButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Item")
                    }
                },
                modifier = Modifier.fillMaxSize().padding(padding)
            )
            else -> {
                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    // Progress bar
                    if (state.totalItems > 0) {
                        LinearProgressIndicator(
                            progress = { state.checkedItems.toFloat() / state.totalItems.toFloat() },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 80.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        state.groupedItems.forEach { (category, items) ->
                            stickyHeader(key = "header_$category") {
                                CategoryHeader(category = category, count = items.size)
                            }
                            items(items, key = { it.id }) { item ->
                                GroceryItemRow(
                                    item = item,
                                    onToggle = { viewModel.toggleItem(item) },
                                    onDelete = { viewModel.deleteItem(item) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddGroceryItemDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, quantity, unit ->
                viewModel.addItem(name, quantity, unit)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun CategoryHeader(category: String, count: Int) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = getCategoryEmoji(category),
                fontSize = 16.sp
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = category,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "($count)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.weight(1f))
            Divider(modifier = Modifier.weight(2f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroceryItemRow(
    item: GroceryItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(12.dp))
                    .padding(end = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (item.isChecked)
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(if (item.isChecked) 0.dp else 1.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = item.isChecked,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (item.isChecked) FontWeight.Normal else FontWeight.Medium,
                        color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
                Text(
                    text = formatQuantity(item.quantity, item.unit),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.isManuallyAdded) {
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Filled.Edit, contentDescription = "Manual",
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
private fun AddGroceryItemDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Double, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("piece") }
    val commonUnits = listOf("piece", "g", "kg", "ml", "l", "cup", "tbsp", "tsp", "oz", "lb")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Item Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantity, onValueChange = { quantity = it },
                        label = { Text("Qty") }, modifier = Modifier.weight(1f), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = unit, onValueChange = { unit = it },
                        label = { Text("Unit") }, modifier = Modifier.weight(1f), singleLine = true
                    )
                }
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    commonUnits.forEach { u ->
                        FilterChip(
                            selected = unit == u, onClick = { unit = u },
                            label = { Text(u, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantity.toDoubleOrNull() ?: 1.0
                    if (name.isNotBlank()) onAdd(name, qty, unit)
                },
                enabled = name.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun formatQuantity(quantity: Double, unit: String): String {
    return if (quantity == quantity.toLong().toDouble()) "${quantity.toLong()} $unit"
    else "${"%.1f".format(quantity)} $unit"
}

private fun getCategoryEmoji(category: String): String = when (category) {
    "Meat & Fish" -> "🥩"
    "Dairy & Eggs" -> "🥚"
    "Grains & Bread" -> "🍞"
    "Fruits" -> "🍎"
    "Vegetables" -> "🥦"
    "Condiments & Oils" -> "🫙"
    "Baking & Sweets" -> "🍫"
    "Spices & Herbs" -> "🌿"
    "Plant Proteins" -> "🌱"
    "Nuts & Seeds" -> "🥜"
    else -> "🛒"
}