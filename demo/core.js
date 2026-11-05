export const mealTypes = ["Breakfast", "Lunch", "Dinner", "Snack"];
export const categories = ["All", ...mealTypes, "Vegetarian", "Quick & Easy"];
export const todayKey = () => {
  const date = new Date();
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
};
export function shiftDate(key, days) {
  const date = new Date(`${key}T12:00:00`);
  date.setDate(date.getDate() + days);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
}
export function weekDates(key) {
  const day = new Date(`${key}T12:00:00`).getDay();
  const monday = shiftDate(key, -((day + 6) % 7));
  return Array.from({ length: 7 }, (_, i) => shiftDate(monday, i));
}
export const validAmount = (value, zero = false) =>
  String(value).trim() !== "" &&
  Number.isFinite(Number(value)) &&
  (zero ? Number(value) >= 0 : Number(value) > 0);
export function filterRecipes(
  recipes,
  query = "",
  category = "All",
  favorites = false,
) {
  return recipes.filter(
    (r) =>
      (category === "All" || r.category === category) &&
      (!favorites || r.favorite) &&
      `${r.name} ${r.tags || ""}`
        .toLowerCase()
        .includes(query.trim().toLowerCase()),
  );
}
export function generateGroceries(state, dates) {
  const merged = new Map();
  state.plans
    .filter((p) => dates.includes(p.date))
    .forEach((plan) => {
      const recipe = state.recipes.find((r) => r.id === plan.recipeId);
      if (!recipe) return;
      recipe.ingredients.forEach((item) => {
        const key = `${item.name.trim().toLowerCase()}|${item.unit.trim().toLowerCase()}`;
        const quantity =
          (item.quantity * plan.servings) / Math.max(recipe.servings, 1);
        if (merged.has(key)) merged.get(key).quantity += quantity;
        else
          merged.set(key, {
            ...item,
            id: `generated-${key}`,
            quantity,
            checked: false,
            manual: false,
          });
      });
    });
  return [...state.groceries.filter((item) => item.manual), ...merged.values()];
}
export function fillWeek(state, dates) {
  const plans = [...state.plans];
  if (!state.recipes.length) return plans;
  dates.forEach((date, day) =>
    mealTypes.slice(0, 3).forEach((type, slot) => {
      if (plans.some((p) => p.date === date && p.type === type)) return;
      const matching = state.recipes.filter((r) => r.category === type);
      const pool = matching.length ? matching : state.recipes;
      const recipe = pool[(day + slot) % pool.length];
      plans.push({
        id: `${date}-${type}-${Date.now()}`,
        date,
        type,
        recipeId: recipe.id,
        servings: 1,
      });
    }),
  );
  return plans;
}
export function nutrition(state, date) {
  return state.plans
    .filter((p) => p.date === date)
    .reduce(
      (total, plan) => {
        const recipe = state.recipes.find((r) => r.id === plan.recipeId);
        if (recipe)
          ["calories", "protein", "carbs", "fat"].forEach(
            (key) => (total[key] += recipe[key] * plan.servings),
          );
        return total;
      },
      { calories: 0, protein: 0, carbs: 0, fat: 0 },
    );
}
export const quantity = (value) =>
  new Intl.NumberFormat("en", { maximumFractionDigits: 2 }).format(value);
