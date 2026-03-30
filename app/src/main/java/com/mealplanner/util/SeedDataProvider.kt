package com.mealplanner.util

import com.mealplanner.domain.model.*

object SeedDataProvider {

    fun getSampleRecipes(): List<Recipe> = listOf(
        Recipe(
            name = "Grilled Chicken Salad",
            description = "A fresh and healthy salad with grilled chicken, mixed greens, and a light lemon dressing.",
            ingredients = listOf(
                Ingredient(name = "Chicken breast", quantity = 200.0, unit = "g"),
                Ingredient(name = "Mixed salad greens", quantity = 100.0, unit = "g"),
                Ingredient(name = "Cherry tomatoes", quantity = 50.0, unit = "g"),
                Ingredient(name = "Cucumber", quantity = 50.0, unit = "g"),
                Ingredient(name = "Olive oil", quantity = 1.0, unit = "tbsp"),
                Ingredient(name = "Lemon juice", quantity = 2.0, unit = "tbsp"),
                Ingredient(name = "Salt", quantity = 1.0, unit = "tsp"),
                Ingredient(name = "Black pepper", quantity = 0.5, unit = "tsp")
            ),
            instructions = "1. Season chicken with salt, pepper, and olive oil.\n2. Grill chicken for 6-7 minutes per side until cooked through.\n3. Let chicken rest for 5 minutes, then slice.\n4. Combine salad greens, tomatoes, and cucumber in a bowl.\n5. Top with sliced chicken.\n6. Drizzle with lemon juice and olive oil.\n7. Serve immediately.",
            prepTimeMinutes = 10,
            cookTimeMinutes = 15,
            servings = 2,
            category = RecipeCategory.LUNCH,
            calories = 320.0,
            protein = 42.0,
            carbs = 8.0,
            fat = 12.0,
            fiber = 3.0,
            isFavorite = true,
            tags = listOf("healthy", "high-protein", "low-carb", "quick")
        ),
        Recipe(
            name = "Avocado Toast with Eggs",
            description = "Classic nutritious breakfast with creamy avocado and perfectly cooked eggs on whole grain toast.",
            ingredients = listOf(
                Ingredient(name = "Whole grain bread", quantity = 2.0, unit = "slice"),
                Ingredient(name = "Avocado", quantity = 1.0, unit = "piece"),
                Ingredient(name = "Eggs", quantity = 2.0, unit = "piece"),
                Ingredient(name = "Lemon juice", quantity = 1.0, unit = "tsp"),
                Ingredient(name = "Red pepper flakes", quantity = 0.25, unit = "tsp"),
                Ingredient(name = "Salt", quantity = 0.5, unit = "tsp")
            ),
            instructions = "1. Toast the bread until golden and crisp.\n2. Mash avocado with lemon juice and salt.\n3. Spread avocado evenly on toast.\n4. Fry or poach eggs to your preference.\n5. Place eggs on top of avocado toast.\n6. Sprinkle with red pepper flakes and extra salt if desired.",
            prepTimeMinutes = 5,
            cookTimeMinutes = 5,
            servings = 1,
            category = RecipeCategory.BREAKFAST,
            calories = 420.0,
            protein = 18.0,
            carbs = 32.0,
            fat = 24.0,
            fiber = 8.0,
            isFavorite = false,
            tags = listOf("breakfast", "quick", "vegetarian")
        ),
        Recipe(
            name = "Salmon with Quinoa",
            description = "Baked Atlantic salmon served with fluffy quinoa and roasted vegetables.",
            ingredients = listOf(
                Ingredient(name = "Salmon fillet", quantity = 200.0, unit = "g"),
                Ingredient(name = "Quinoa", quantity = 80.0, unit = "g"),
                Ingredient(name = "Broccoli florets", quantity = 150.0, unit = "g"),
                Ingredient(name = "Olive oil", quantity = 2.0, unit = "tbsp"),
                Ingredient(name = "Garlic", quantity = 2.0, unit = "piece"),
                Ingredient(name = "Lemon", quantity = 1.0, unit = "piece"),
                Ingredient(name = "Dill", quantity = 1.0, unit = "tbsp"),
                Ingredient(name = "Salt and pepper", quantity = 1.0, unit = "tsp")
            ),
            instructions = "1. Preheat oven to 200°C.\n2. Cook quinoa according to package instructions.\n3. Season salmon with salt, pepper, dill, and lemon juice.\n4. Toss broccoli with olive oil and minced garlic.\n5. Bake salmon and broccoli for 12-15 minutes.\n6. Serve salmon over quinoa with roasted broccoli.",
            prepTimeMinutes = 10,
            cookTimeMinutes = 20,
            servings = 2,
            category = RecipeCategory.DINNER,
            calories = 480.0,
            protein = 38.0,
            carbs = 35.0,
            fat = 16.0,
            fiber = 5.0,
            isFavorite = true,
            tags = listOf("healthy", "omega-3", "gluten-free", "high-protein")
        ),
        Recipe(
            name = "Greek Yogurt Parfait",
            description = "Layered Greek yogurt with fresh berries, honey, and granola for a perfect snack.",
            ingredients = listOf(
                Ingredient(name = "Greek yogurt", quantity = 150.0, unit = "g"),
                Ingredient(name = "Mixed berries", quantity = 100.0, unit = "g"),
                Ingredient(name = "Granola", quantity = 30.0, unit = "g"),
                Ingredient(name = "Honey", quantity = 1.0, unit = "tbsp"),
                Ingredient(name = "Chia seeds", quantity = 1.0, unit = "tsp")
            ),
            instructions = "1. Add Greek yogurt to a glass or bowl.\n2. Layer with mixed berries.\n3. Top with granola and chia seeds.\n4. Drizzle honey on top.\n5. Serve immediately or refrigerate for up to 2 hours.",
            prepTimeMinutes = 5,
            cookTimeMinutes = 0,
            servings = 1,
            category = RecipeCategory.SNACK,
            calories = 280.0,
            protein = 16.0,
            carbs = 38.0,
            fat = 6.0,
            fiber = 4.0,
            isFavorite = false,
            tags = listOf("snack", "high-protein", "quick", "no-cook")
        ),
        Recipe(
            name = "Vegetable Stir Fry",
            description = "Colorful Asian-inspired stir fry with tofu and a savory sauce.",
            ingredients = listOf(
                Ingredient(name = "Firm tofu", quantity = 200.0, unit = "g"),
                Ingredient(name = "Bell peppers", quantity = 150.0, unit = "g"),
                Ingredient(name = "Snap peas", quantity = 100.0, unit = "g"),
                Ingredient(name = "Carrots", quantity = 80.0, unit = "g"),
                Ingredient(name = "Soy sauce", quantity = 3.0, unit = "tbsp"),
                Ingredient(name = "Sesame oil", quantity = 1.0, unit = "tbsp"),
                Ingredient(name = "Ginger", quantity = 1.0, unit = "tsp"),
                Ingredient(name = "Garlic", quantity = 3.0, unit = "piece"),
                Ingredient(name = "Brown rice", quantity = 100.0, unit = "g")
            ),
            instructions = "1. Press tofu dry and cube it.\n2. Cook brown rice according to package.\n3. Heat oil in a wok over high heat.\n4. Fry tofu until golden, 5-6 minutes.\n5. Add vegetables and stir fry 4-5 minutes.\n6. Add garlic, ginger, and soy sauce.\n7. Toss everything together and serve over rice.",
            prepTimeMinutes = 15,
            cookTimeMinutes = 20,
            servings = 2,
            category = RecipeCategory.VEGAN,
            calories = 380.0,
            protein = 20.0,
            carbs = 52.0,
            fat = 10.0,
            fiber = 7.0,
            isFavorite = false,
            tags = listOf("vegan", "vegetarian", "asian", "meal-prep")
        ),
        Recipe(
            name = "Overnight Oats",
            description = "Creamy no-cook oats prepared the night before for a quick and nutritious breakfast.",
            ingredients = listOf(
                Ingredient(name = "Rolled oats", quantity = 80.0, unit = "g"),
                Ingredient(name = "Almond milk", quantity = 200.0, unit = "ml"),
                Ingredient(name = "Chia seeds", quantity = 1.0, unit = "tbsp"),
                Ingredient(name = "Banana", quantity = 1.0, unit = "piece"),
                Ingredient(name = "Peanut butter", quantity = 1.0, unit = "tbsp"),
                Ingredient(name = "Honey", quantity = 1.0, unit = "tsp"),
                Ingredient(name = "Cinnamon", quantity = 0.5, unit = "tsp")
            ),
            instructions = "1. Combine oats, almond milk, chia seeds, and cinnamon in a jar.\n2. Stir in honey and peanut butter.\n3. Cover and refrigerate overnight (at least 6 hours).\n4. In the morning, top with sliced banana.\n5. Add extra almond milk if too thick.",
            prepTimeMinutes = 5,
            cookTimeMinutes = 0,
            servings = 1,
            category = RecipeCategory.BREAKFAST,
            calories = 450.0,
            protein = 14.0,
            carbs = 68.0,
            fat = 14.0,
            fiber = 10.0,
            isFavorite = true,
            tags = listOf("breakfast", "meal-prep", "vegan", "no-cook")
        ),
        Recipe(
            name = "Chicken Burrito Bowl",
            description = "A hearty Mexican-inspired bowl with seasoned chicken, rice, beans, and fresh toppings.",
            ingredients = listOf(
                Ingredient(name = "Chicken thighs", quantity = 200.0, unit = "g"),
                Ingredient(name = "White rice", quantity = 80.0, unit = "g"),
                Ingredient(name = "Black beans", quantity = 100.0, unit = "g"),
                Ingredient(name = "Corn kernels", quantity = 60.0, unit = "g"),
                Ingredient(name = "Salsa", quantity = 50.0, unit = "g"),
                Ingredient(name = "Sour cream", quantity = 30.0, unit = "g"),
                Ingredient(name = "Lime", quantity = 1.0, unit = "piece"),
                Ingredient(name = "Cumin", quantity = 1.0, unit = "tsp"),
                Ingredient(name = "Paprika", quantity = 1.0, unit = "tsp")
            ),
            instructions = "1. Season chicken with cumin, paprika, salt, and pepper.\n2. Cook chicken in a pan until done, about 8 minutes per side.\n3. Let rest, then slice.\n4. Cook rice according to package instructions.\n5. Warm black beans with lime juice.\n6. Assemble bowl: rice, beans, chicken, corn.\n7. Top with salsa and sour cream.",
            prepTimeMinutes = 10,
            cookTimeMinutes = 25,
            servings = 2,
            category = RecipeCategory.LUNCH,
            calories = 520.0,
            protein = 36.0,
            carbs = 62.0,
            fat = 14.0,
            fiber = 8.0,
            isFavorite = false,
            tags = listOf("mexican", "meal-prep", "high-protein", "filling")
        ),
        Recipe(
            name = "Protein Smoothie",
            description = "A quick post-workout protein-packed smoothie with banana, almond butter, and protein powder.",
            ingredients = listOf(
                Ingredient(name = "Banana", quantity = 1.0, unit = "piece"),
                Ingredient(name = "Protein powder", quantity = 30.0, unit = "g"),
                Ingredient(name = "Almond milk", quantity = 250.0, unit = "ml"),
                Ingredient(name = "Almond butter", quantity = 1.0, unit = "tbsp"),
                Ingredient(name = "Ice cubes", quantity = 5.0, unit = "piece"),
                Ingredient(name = "Cocoa powder", quantity = 1.0, unit = "tbsp")
            ),
            instructions = "1. Add all ingredients to a blender.\n2. Blend until smooth and creamy.\n3. Adjust thickness by adding more almond milk if needed.\n4. Pour into a glass and serve immediately.",
            prepTimeMinutes = 5,
            cookTimeMinutes = 0,
            servings = 1,
            category = RecipeCategory.HIGH_PROTEIN,
            calories = 350.0,
            protein = 32.0,
            carbs = 30.0,
            fat = 10.0,
            fiber = 4.0,
            isFavorite = false,
            tags = listOf("smoothie", "post-workout", "high-protein", "quick")
        )
    )
}