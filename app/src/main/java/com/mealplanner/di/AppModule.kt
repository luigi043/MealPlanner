package com.mealplanner.di

import android.content.Context
import androidx.room.Room
import com.mealplanner.data.local.MealPlannerDatabase
import com.mealplanner.data.local.dao.*
import com.mealplanner.data.repository.*
import com.mealplanner.domain.usecase.*
import com.mealplanner.util.DatabaseSeeder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MealPlannerDatabase =
        Room.databaseBuilder(
            context,
            MealPlannerDatabase::class.java,
            MealPlannerDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()

    @Provides fun provideRecipeDao(db: MealPlannerDatabase): RecipeDao = db.recipeDao()
    @Provides fun provideIngredientDao(db: MealPlannerDatabase): IngredientDao = db.ingredientDao()
    @Provides fun provideMealPlanDao(db: MealPlannerDatabase): MealPlanDao = db.mealPlanDao()
    @Provides fun provideGroceryDao(db: MealPlannerDatabase): GroceryDao = db.groceryDao()

    @Provides
    @Singleton
    fun provideRecipeRepository(
        recipeDao: RecipeDao,
        ingredientDao: IngredientDao
    ): RecipeRepository = RecipeRepository(recipeDao, ingredientDao)

    @Provides
    @Singleton
    fun provideMealPlanRepository(mealPlanDao: MealPlanDao): MealPlanRepository =
        MealPlanRepository(mealPlanDao)

    @Provides
    @Singleton
    fun provideGroceryRepository(groceryDao: GroceryDao): GroceryRepository =
        GroceryRepository(groceryDao)

    @Provides
    fun provideGenerateGroceryListUseCase(
        mealPlanRepo: MealPlanRepository,
        recipeRepo: RecipeRepository,
        groceryRepo: GroceryRepository
    ): GenerateGroceryListUseCase = GenerateGroceryListUseCase(mealPlanRepo, recipeRepo, groceryRepo)

    @Provides
    fun provideGetNutritionSummaryUseCase(
        mealPlanRepo: MealPlanRepository
    ): GetNutritionSummaryUseCase = GetNutritionSummaryUseCase(mealPlanRepo)

    @Provides
    fun provideGetSmartSuggestionsUseCase(
        recipeRepo: RecipeRepository,
        mealPlanRepo: MealPlanRepository
    ): GetSmartSuggestionsUseCase = GetSmartSuggestionsUseCase(recipeRepo, mealPlanRepo)

    @Provides
    fun provideAutoGenerateMealPlanUseCase(
        recipeRepo: RecipeRepository,
        mealPlanRepo: MealPlanRepository
    ): AutoGenerateMealPlanUseCase = AutoGenerateMealPlanUseCase(recipeRepo, mealPlanRepo)

    @Provides
    @Singleton
    fun provideDatabaseSeeder(
        recipeRepository: RecipeRepository
    ): DatabaseSeeder = DatabaseSeeder(recipeRepository)
}
