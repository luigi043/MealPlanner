package com.mealplanner.presentation.ui.dashboard

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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseOutCubic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToMealPlan: () -> Unit,
    onNavigateToRecipes: () -> Unit,
    onNavigateToGrocery: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Good ${getGreeting()} 👋",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Meal Planner",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            Icons.Outlined.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingState(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Date header
                item {
                    DateHeader(state.today)
                }

                // Calorie overview card
                item {
                    NutritionOverviewCard(
                        nutritionSummary = state.nutritionSummary,
                        goals = state.userGoals,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // Quick actions
                item {
                    QuickActionsRow(
                        onPlanMeals = onNavigateToMealPlan,
                        onBrowseRecipes = onNavigateToRecipes,
                        onViewGrocery = onNavigateToGrocery,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                // Today's meals
                item {
                    SectionHeader(
                        title = "Today's Meals",
                        action = "View All",
                        onActionClick = onNavigateToMealPlan,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                if (state.todayMeals.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Outlined.RestaurantMenu,
                            title = "No meals planned",
                            message = "Start planning your meals for today!",
                            action = {
                                FilledTonalButton(onClick = onNavigateToMealPlan) {
                                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Plan Meals")
                                }
                            },
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(state.todayMeals, key = { it.id }) { meal ->
                        TodayMealItem(
                            mealPlan = meal,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }

                // Weekly overview
                item {
                    state.weeklySummary?.let { weekly ->
                        SectionHeader(
                            title = "This Week",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        WeeklyCalorieChart(
                            weeklySummary = weekly,
                            goals = state.userGoals,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                // Streak card
                item {
                    if (state.streakDays > 0) {
                        StreakCard(
                            streakDays = state.streakDays,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DateHeader(today: LocalDate) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.CalendarToday,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun NutritionOverviewCard(
    nutritionSummary: NutritionSummary?,
    goals: UserGoals,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Nutrition Today",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CalorieRing(
                    consumed = nutritionSummary?.totalCalories ?: 0.0,
                    target = goals.dailyCalorieTarget.toDouble(),
                    modifier = Modifier.size(100.dp)
                )
                Spacer(Modifier.width(20.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NutritionBar(
                        label = "Protein",
                        value = nutritionSummary?.totalProtein ?: 0.0,
                        target = goals.dailyProteinTarget.toDouble(),
                        color = Color(0xFF0077B6),
                        unit = "g"
                    )
                    NutritionBar(
                        label = "Carbs",
                        value = nutritionSummary?.totalCarbs ?: 0.0,
                        target = goals.dailyCarbsTarget.toDouble(),
                        color = Color(0xFFE76F51),
                        unit = "g"
                    )
                    NutritionBar(
                        label = "Fat",
                        value = nutritionSummary?.totalFat ?: 0.0,
                        target = goals.dailyFatTarget.toDouble(),
                        color = Color(0xFF6A4C93),
                        unit = "g"
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            val remaining = (goals.dailyCalorieTarget - (nutritionSummary?.totalCalories ?: 0.0)).coerceAtLeast(0.0)
            Text(
                text = if (remaining > 0) "🎯 ${remaining.toInt()} kcal remaining today"
                       else "✅ Daily calorie goal reached!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickActionsRow(
    onPlanMeals: () -> Unit,
    onBrowseRecipes: () -> Unit,
    onViewGrocery: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionButton(
            icon = Icons.Outlined.DateRange,
            label = "Plan",
            color = GreenPrimary,
            onClick = onPlanMeals,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            icon = Icons.Outlined.MenuBook,
            label = "Recipes",
            color = Color(0xFF0077B6),
            onClick = onBrowseRecipes,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            icon = Icons.Outlined.ShoppingCart,
            label = "Grocery",
            color = OrangeAccent,
            onClick = onViewGrocery,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun TodayMealItem(mealPlan: MealPlan, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MealTypeBadge(mealPlan.mealType)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(mealPlan.recipeName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${(mealPlan.recipeCalories * mealPlan.servings).toInt()} kcal • ${mealPlan.servings} serving(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WeeklyCalorieChart(
    weeklySummary: WeeklyNutritionSummary,
    goals: UserGoals,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val maxCalories = weeklySummary.dailySummaries.maxOfOrNull { it.totalCalories }
                ?.coerceAtLeast(goals.dailyCalorieTarget.toDouble()) ?: goals.dailyCalorieTarget.toDouble()

            Row(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklySummary.dailySummaries.forEach { day ->
                    val barHeight = if (maxCalories > 0) (day.totalCalories / maxCalories).toFloat() else 0f
                    val isToday = day.date == LocalDate.now()
                    val dayName = day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            val animHeight by animateFloatAsState(
                                targetValue = barHeight,
                                animationSpec = tween(800, easing = EaseOutCubic),
                                label = "barHeight"
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .fillMaxHeight(animHeight.coerceAtLeast(0.03f))
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(
                                        if (isToday) GreenPrimary
                                        else if (barHeight > 0f) GreenSecondary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = dayName.take(1),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isToday) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Avg: ${weeklySummary.avgCalories.toInt()} kcal/day",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Goal: ${goals.dailyCalorieTarget} kcal",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun StreakCard(streakDays: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔥", fontSize = 32.sp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "$streakDays Day Streak!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF856404)
                )
                Text(
                    text = "Keep planning your meals consistently!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF856404).copy(alpha = 0.7f)
                )
            }
        }
    }
}

private fun getGreeting(): String {
    val hour = java.time.LocalTime.now().hour
    return when {
        hour < 12 -> "morning"
        hour < 17 -> "afternoon"
        else -> "evening"
    }
}