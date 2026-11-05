package com.mealplanner

import com.mealplanner.data.local.dao.*
import com.mealplanner.data.local.entities.*
import com.mealplanner.data.local.toDomain
import com.mealplanner.data.repository.*
import com.mealplanner.domain.model.*
import com.mealplanner.domain.usecase.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.Proxy
import java.time.LocalDate

class PlanningRegressionTest {
    private val day = LocalDate.of(2026, 11, 5)
    private val recipe = RecipeEntity(1, "Test soup", instructions = "Combine", prepTimeMinutes = 5,
        cookTimeMinutes = 10, servings = 4, category = "DINNER", calories = 100.0,
        protein = 5.0, carbs = 10.0, fat = 2.0)
    private val ingredients = listOf(IngredientEntity(1, 1, "Rice", 400.0, "g"))
    private val plan = MealPlanEntity(1, day.toEpochDay(), "DINNER", 1, "Test soup", 100.0, 5.0, 10.0, 2.0, 2)

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T> dao(crossinline handle: (String, Array<out Any?>) -> Any?): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, args ->
            handle(method.name, args ?: emptyArray())
        } as T

    private fun recipes(): RecipeRepository = RecipeRepository(dao<RecipeDao> { name, _ ->
        when (name) {
            "getAllRecipes" -> flowOf(listOf(RecipeWithIngredients(recipe, ingredients)))
            "getRecipeById" -> RecipeWithIngredients(recipe, ingredients)
            else -> error("Unexpected recipe call $name")
        }
    }, dao<IngredientDao> { name, _ -> error("Unexpected ingredient call $name") })

    @Test fun `amount validation rejects non-finite negative and zero ingredient quantities`() {
        listOf("NaN", "Infinity", "-1", "", "abc").forEach { assertFalse(validAmount(it)) }
        assertTrue(validAmount("0"))
        assertFalse(validAmount("0", allowZero = false))
        assertTrue(validAmount("0.25", allowZero = false))
    }

    @Test fun `search category and favorites compose rather than override one another`() {
        val base = RecipeWithIngredients(recipe, ingredients).toDomain()
        val favorite = base.copy(id = 2, isFavorite = true)
        val breakfast = favorite.copy(id = 3, category = RecipeCategory.BREAKFAST)
        val results = filterRecipes(listOf(base, favorite, breakfast), " soup ", RecipeCategory.DINNER, true)
        assertEquals(listOf(2L), results.map { it.id })
    }

    @Test fun `grocery generation scales batch ingredients and preserves manual items through replacement API`() = runTest {
        var replaced: List<GroceryItemEntity>? = null
        val groceries = GroceryRepository(dao<GroceryDao> { name, args ->
            when (name) {
                "replaceGeneratedItems" -> { @Suppress("UNCHECKED_CAST")
                    replaced = args[0] as List<GroceryItemEntity>; Unit }
                else -> error("Must not clear manual groceries: $name")
            }
        })
        val plans = MealPlanRepository(dao<MealPlanDao> { _, _ -> flowOf(listOf(plan)) })
        GenerateGroceryListUseCase(plans, recipes(), groceries)(day, day)
        assertEquals(200.0, replaced!!.single().quantity, 0.001)
    }

    @Test fun `autofill skips occupied slots and repeated execution adds no duplicates`() = runTest {
        val stored = mutableListOf(plan)
        val plans = MealPlanRepository(dao<MealPlanDao> { name, args ->
            when (name) {
                "getMealPlansByDateRange" -> flowOf(stored.toList())
                "insertMealPlan" -> { stored.add(args[0] as MealPlanEntity); stored.size.toLong() }
                else -> error("Unexpected meal call $name")
            }
        })
        val fill = AutoGenerateMealPlanUseCase(recipes(), plans)
        fill(day, UserGoals())
        assertEquals(21, stored.size)
        assertTrue(stored.contains(plan))
        fill(day, UserGoals())
        assertEquals(21, stored.size)
    }

    @Test fun `daily nutrition multiplies per-serving values by planned servings`() = runTest {
        val plans = MealPlanRepository(dao<MealPlanDao> { _, _ -> flowOf(listOf(plan)) })
        GetNutritionSummaryUseCase(plans).getDailySummary(day).collect { summary ->
            assertEquals(200.0, summary.totalCalories, 0.001)
            assertEquals(10.0, summary.totalProtein, 0.001)
        }
        assertEquals(0.5, ingredientScale(2, 4), 0.001)
        assertTrue(ingredientScale(1, 0).isFinite())
    }
}
