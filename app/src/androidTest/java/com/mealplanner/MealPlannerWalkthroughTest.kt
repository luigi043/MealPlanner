package com.mealplanner

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import android.graphics.Bitmap
import java.io.File
import org.junit.Rule
import org.junit.Test

/** Real emulator screenshots. Run only on a test emulator with synthetic app data. */
class MealPlannerWalkthroughTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    private fun shot(name: String) {
        rule.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val folder = File(instrumentation.targetContext.getExternalFilesDir(null), "verification")
        folder.mkdirs()
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        File(folder, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    @Test fun walkthrough() {
        rule.waitUntil(30000) { rule.onAllNodesWithText("Home").fetchSemanticsNodes().isNotEmpty() }
        shot("01-dashboard")
        rule.onAllNodesWithText("Recipes", useUnmergedTree = true).onLast().performClick()
        shot("02-recipes")
        rule.onNode(hasSetTextAction()).performTextInput("Grilled")
        rule.waitUntil(30000) { rule.onAllNodesWithText("Grilled Chicken Salad").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Grilled Chicken Salad").performClick()
        rule.waitUntil(30000) { rule.onAllNodesWithText("Grilled Chicken Salad").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithContentDescription("Edit").assertExists()
        shot("03-recipe-detail")
        rule.onNodeWithContentDescription("Edit").performClick()
        rule.waitForIdle()
        rule.waitUntil(30000) { rule.onAllNodesWithText("Grilled Chicken Salad").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Edit Recipe").assertExists()
        shot("04-edit-recipe")
        rule.onNodeWithContentDescription("Back").performClick()
        rule.onNodeWithContentDescription("Back").performClick()
        rule.onNodeWithContentDescription("Clear").performClick()
        rule.onNodeWithText("Add Recipe", useUnmergedTree = true).performClick()
        rule.onNodeWithText("New Recipe").assertExists()
        shot("05-new-recipe")
        rule.onNodeWithText("Save", substring = false).performClick()
        rule.onNodeWithText("Recipe name is required").assertExists()
        shot("06-recipe-validation")
        rule.onNodeWithText("Add", substring = false).performScrollTo().performClick()
        rule.onNodeWithText("Add Ingredient").assertExists()
        shot("07-add-ingredient")
        rule.onNodeWithText("Ingredient Name").performTextInput("Synthetic rice")
        rule.onNodeWithText("Qty").performTextInput("200")
        rule.onNodeWithText("Unit", substring = false).performTextInput("g")
        rule.onAllNodesWithText("Add", substring = false).onLast().performClick()
        rule.onNodeWithText("Recipe Name *").performScrollTo().performTextInput("Synthetic rice recipe")
        rule.onNodeWithText("Calories *").performScrollTo().performTextInput("0")
        rule.onNodeWithText("Instructions *").performScrollTo().performTextInput("Follow the package instructions.")
        rule.onNodeWithText("Save", substring = false).performClick()
        rule.waitUntil(30000) { rule.onAllNodesWithText("Synthetic rice recipe").fetchSemanticsNodes().isNotEmpty() }
        shot("07b-recipe-saved")
        rule.onNodeWithText("Plan", useUnmergedTree = true).performClick()
        shot("08-meal-plan-day")
        rule.onNodeWithContentDescription("Auto-generate").performClick()
        rule.waitUntil(30000) { rule.onAllNodesWithText("Remove", substring = false).fetchSemanticsNodes().isNotEmpty() || rule.onAllNodesWithContentDescription("Remove").fetchSemanticsNodes().isNotEmpty() }
        shot("09-meal-plan-filled")
        rule.onNodeWithContentDescription("Toggle view").performClick()
        shot("10-meal-plan-week")
        rule.onNodeWithContentDescription("Toggle view").performClick()
        rule.onAllNodesWithContentDescription("Add meal")[0].performClick()
        shot("11-add-meal")
        rule.onNodeWithText("Cancel").performClick()
        rule.onNodeWithText("Generate", substring = false).performClick()
        rule.onNodeWithText("Grocery", useUnmergedTree = true).performClick()
        rule.waitUntil(30000) { rule.onAllNodesWithText("Grocery List").fetchSemanticsNodes().isNotEmpty() }
        shot("12-groceries")
        rule.onNodeWithContentDescription("Add item").performClick()
        shot("13-add-grocery")
        rule.onNodeWithText("Item Name").performTextInput("Synthetic shopping item")
        rule.onNodeWithText("Qty").performTextInput("2")
        rule.onNodeWithText("Add", substring = false).performClick()
        rule.onNodeWithText("Settings", useUnmergedTree = true).performClick()
        shot("14-settings-goals")
        rule.onNodeWithText("Dark Mode").performScrollTo()
        rule.onAllNodes(isToggleable()).onLast().performClick()
        shot("15-settings-preferences")
        rule.onNodeWithText("About").performScrollTo()
        shot("16-settings-about")
    }
}
