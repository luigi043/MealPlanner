package com.mealplanner.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.*
import com.mealplanner.presentation.ui.dashboard.DashboardScreen
import com.mealplanner.presentation.ui.grocery.GroceryScreen
import com.mealplanner.presentation.ui.mealplan.MealPlanScreen
import com.mealplanner.presentation.ui.recipes.*
import com.mealplanner.presentation.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object MealPlan : Screen("meal_plan")
    object Recipes : Screen("recipes")
    object RecipeDetail : Screen("recipe/{recipeId}") {
        fun createRoute(recipeId: Long) = "recipe/$recipeId"
    }
    object AddEditRecipe : Screen("add_edit_recipe?recipeId={recipeId}") {
        fun createRoute(recipeId: Long? = null) =
            if (recipeId != null) "add_edit_recipe?recipeId=$recipeId" else "add_edit_recipe"
    }
    object Grocery : Screen("grocery")
    object Settings : Screen("settings")
}

@Composable
fun MealPlannerNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                initialOffsetX = { it / 4 }, animationSpec = tween(300)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                targetOffsetX = { -it / 4 }, animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                initialOffsetX = { -it / 4 }, animationSpec = tween(300)
            )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                targetOffsetX = { it / 4 }, animationSpec = tween(300)
            )
        }
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToMealPlan = { navController.navigate(Screen.MealPlan.route) },
                onNavigateToRecipes = { navController.navigate(Screen.Recipes.route) },
                onNavigateToGrocery = { navController.navigate(Screen.Grocery.route) }
            )
        }

        composable(Screen.MealPlan.route) {
            MealPlanScreen(
                onNavigateToRecipes = { navController.navigate(Screen.Recipes.route) }
            )
        }

        composable(Screen.Recipes.route) {
            RecipesScreen(
                onRecipeClick = { id -> navController.navigate(Screen.RecipeDetail.createRoute(id)) },
                onAddRecipe = { navController.navigate(Screen.AddEditRecipe.createRoute()) }
            )
        }

        composable(
            route = Screen.RecipeDetail.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: return@composable
            RecipeDetailScreen(
                recipeId = recipeId,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Screen.AddEditRecipe.createRoute(recipeId)) }
            )
        }

        composable(
            route = Screen.AddEditRecipe.route,
            arguments = listOf(navArgument("recipeId") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("recipeId")?.takeIf { it != -1L }
            AddEditRecipeScreen(
                recipeId = recipeId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Grocery.route) {
            GroceryScreen()
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}