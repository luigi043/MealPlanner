import test from "node:test";
import assert from "node:assert/strict";
import { sampleState } from "../data.js";
import {
  todayKey,
  weekDates,
  shiftDate,
  fillWeek,
  generateGroceries,
  filterRecipes,
  validAmount,
  nutrition,
} from "../core.js";
test("weeks cross month and year boundaries with local dates", () => {
  assert.deepEqual(weekDates("2026-01-01"), [
    "2025-12-29",
    "2025-12-30",
    "2025-12-31",
    "2026-01-01",
    "2026-01-02",
    "2026-01-03",
    "2026-01-04",
  ]);
  assert.equal(shiftDate("2026-03-29", 1), "2026-03-30");
});
test("auto-fill preserves meals and is idempotent", () => {
  const state = sampleState();
  const first = state.plans[0];
  state.plans = fillWeek(state, weekDates(todayKey()));
  assert.equal(state.plans.length, 21);
  assert.ok(state.plans.includes(first));
  assert.equal(fillWeek(state, weekDates(todayKey())).length, 21);
});
test("grocery quantities scale by recipe yield and manual items survive regeneration", () => {
  const state = sampleState();
  state.plans = [{ recipeId: 1, date: todayKey(), servings: 1 }];
  const manual = {
    id: "manual",
    name: "Soap",
    unit: "piece",
    quantity: 1,
    manual: true,
    checked: true,
  };
  state.groceries = [manual];
  state.groceries = generateGroceries(state, weekDates(todayKey()));
  assert.equal(
    state.groceries.find((i) => i.name === "Chicken breast").quantity,
    100,
  );
  assert.ok(state.groceries.includes(manual));
  assert.deepEqual(
    generateGroceries(state, weekDates(todayKey())),
    state.groceries,
  );
});
test("filters compose and whitespace is ignored", () => {
  const state = sampleState();
  assert.deepEqual(
    filterRecipes(state.recipes, " chicken ", "Lunch", true).map((r) => r.id),
    [1],
  );
  assert.equal(
    filterRecipes(state.recipes, "chicken", "Breakfast", true).length,
    0,
  );
});
test("non-finite, empty and non-positive amounts are rejected", () => {
  for (const value of ["NaN", "Infinity", "-1", "", " ", "0"])
    assert.equal(validAmount(value), false);
  assert.equal(validAmount("0", true), true);
  assert.equal(validAmount("0.5"), true);
});
test("nutrition is per serving, not per batch", () => {
  const state = sampleState();
  state.plans = [{ recipeId: 1, date: todayKey(), servings: 3 }];
  assert.equal(nutrition(state, todayKey()).calories, 960);
  assert.equal(nutrition(state, todayKey()).protein, 126);
});
