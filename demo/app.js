import {
  mealTypes,
  categories,
  todayKey,
  shiftDate,
  weekDates,
  validAmount,
  filterRecipes,
  generateGroceries,
  fillWeek,
  nutrition,
  quantity,
} from "./core.js";
import { sampleState } from "./data.js";

const storageKey = "mealplanner-portfolio-v1";
let state = sampleState();
let storageUnavailable = false;
try {
  const stored = JSON.parse(localStorage.getItem(storageKey));
  if (
    stored?.version === 1 &&
    ["recipes", "plans", "groceries"].every((key) => Array.isArray(stored[key]))
  )
    state = stored;
} catch {
  storageUnavailable = true;
}
let screen = "home",
  selectedDate = todayKey(),
  query = "",
  category = "All",
  favorites = false,
  weekView = false,
  detailId = null,
  detailServings = 1;
const app = document.querySelector("#app");
const dialog = document.querySelector("#dialog");
let returnFocus;
const esc = (value) =>
  String(value ?? "").replace(
    /[&<>"']/g,
    (char) =>
      ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[
        char
      ],
  );
const dateLabel = (
  date,
  options = { weekday: "long", month: "short", day: "numeric" },
) => new Date(`${date}T12:00:00`).toLocaleDateString("en", options);
const paths = {
  home: '<path d="m3 10 9-7 9 7v10H3z"/><path d="M9 20v-7h6v7"/>',
  plan: '<rect x="3" y="5" width="18" height="16" rx="3"/><path d="M7 3v4m10-4v4M3 11h18m-13 5h2m4 0h2"/>',
  recipes:
    '<path d="M12 5c-3-3-7-3-10-1v15c3-2 7-2 10 1 3-3 7-3 10-1V4c-3-2-7-2-10 1zm0 0v15"/>',
  grocery:
    '<path d="m2 3 3 1 3 12h11l3-9H6"/><circle cx="9" cy="21" r="1"/><circle cx="18" cy="21" r="1"/>',
  settings:
    '<path d="M4 7h16M4 17h16"/><circle cx="9" cy="7" r="3"/><circle cx="15" cy="17" r="3"/>',
  plus: '<path d="M12 5v14M5 12h14"/>',
  back: '<path d="m14 5-7 7 7 7"/>',
  next: '<path d="m10 5 7 7-7 7"/>',
  heart: '<path d="M20 5c-3-3-6-1-8 1C6-1-2 6 4 12l8 8 8-8c2-2 3-5 0-7z"/>',
  search: '<circle cx="10" cy="10" r="6"/><path d="m15 15 6 6"/>',
  clock: '<circle cx="12" cy="12" r="9"/><path d="M12 6v6l4 2"/>',
  check: '<path d="m5 12 4 4L20 5"/>',
  close: '<path d="m6 6 12 12M6 18 18 6"/>',
  edit: '<path d="m15 4 5 5-11 11-6 1 1-6zM13 6l5 5"/>',
  trash: '<path d="M3 6h18M9 6V3h6v3M5 6l1 15h12l1-15M10 10v7m4-7v7"/>',
  spark: '<path d="m12 2 3 7 7 3-7 3-3 7-3-7-7-3 7-3z"/>',
};
const icon = (name) =>
  `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${paths[name] || paths.recipes}</svg>`;
function art(kind, large = false) {
  const colors = {
    salad: ["#dce8cf", "#e85737"],
    toast: ["#eee2c5", "#d5a44c"],
    salmon: ["#f0ddcd", "#eb997b"],
    berries: ["#e8dfe9", "#7f507b"],
  };
  const [bg, accent] = colors[kind] || colors.salad;
  return `<svg class="food-art ${large ? "large" : ""}" viewBox="0 0 160 140" aria-hidden="true"><rect width="160" height="140" rx="20" fill="${bg}"/><ellipse cx="80" cy="83" rx="56" ry="40" fill="#b4bda5" opacity=".25"/><ellipse cx="80" cy="74" rx="57" ry="43" fill="#faf7ed"/><ellipse cx="80" cy="74" rx="46" ry="33" fill="#e1dfcc"/>${kind === "toast" ? '<path d="M42 48q-5-20 19-15h38q23-5 20 15l-5 52H47z" fill="#ab753f"/><path d="M49 49q-3-15 14-10h33q18-5 17 10l-4 43H53z" fill="#cda66a"/><path d="m58 53 45 30m-42-39 44 30m-46-5 31 22" stroke="#70964a" stroke-width="10"/><ellipse cx="80" cy="61" rx="22" ry="19" fill="#fffbed"/><circle cx="81" cy="61" r="10" fill="#e6b94a"/>' : kind === "berries" ? '<ellipse cx="80" cy="73" rx="39" ry="28" fill="#f7f4e9"/><g fill="#79547e"><circle cx="60" cy="59" r="10"/><circle cx="85" cy="80" r="11"/><circle cx="100" cy="58" r="9"/></g><g fill="#c6525c"><circle cx="76" cy="52" r="10"/><circle cx="58" cy="81" r="8"/><circle cx="102" cy="81" r="10"/></g><g fill="#caac70"><circle cx="89" cy="59" r="4"/><circle cx="69" cy="73" r="4"/><circle cx="85" cy="96" r="4"/></g>' : `<g fill="#71964e"><ellipse cx="56" cy="56" rx="17" ry="9" transform="rotate(-35 56 56)"/><ellipse cx="98" cy="52" rx="19" ry="10" transform="rotate(25 98 52)"/><ellipse cx="109" cy="80" rx="18" ry="10" transform="rotate(-30 109 80)"/><ellipse cx="63" cy="93" rx="21" ry="10" transform="rotate(20 63 93)"/><ellipse cx="48" cy="77" rx="12" ry="18"/></g><path d="m65 52 30 8 9 27-29 5-14-24z" fill="${kind === "salmon" ? accent : "#dab58b"}"/><path d="m72 57 21 9m-25-2 28 12m-24 0 24 10" stroke="#fcdfb9" stroke-width="3"/><g fill="${accent}"><circle cx="47" cy="61" r="7"/><circle cx="102" cy="96" r="7"/><circle cx="114" cy="63" r="6"/></g>`}<path d="m123 28 9-12m-6 19 14-4" stroke="#245840" stroke-width="2" opacity=".5"/></svg>`;
}
function save() {
  try {
    localStorage.setItem(storageKey, JSON.stringify(state));
  } catch {
    storageUnavailable = true;
    toast("Browser storage is unavailable. Changes last for this visit only.");
  }
}
let toastTimer;
function toast(message) {
  const el = document.querySelector("#toast");
  el.textContent = message;
  el.classList.add("visible");
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => el.classList.remove("visible"), 3800);
}
function go(destination) {
  screen = destination;
  detailId = null;
  render();
}
function header(title, subtitle = "", action = "") {
  return `<header class="app-header"><div>${subtitle ? `<p class="eyebrow">${subtitle}</p>` : ""}<h2>${title}</h2></div>${action || '<img src="./leaf.svg" width="38" height="38" alt="MealPlanner">'}</header>`;
}
function recipeCard(recipe) {
  return `<article class="recipe-card"><button class="recipe-open" data-action="detail" data-id="${recipe.id}">${art(recipe.art)}<span class="recipe-copy"><span class="tag">${esc(recipe.category)}</span><strong>${esc(recipe.name)}</strong><span class="meta">${icon("clock")} ${recipe.prep + recipe.cook} min <span>·</span> ${quantity(recipe.calories)} kcal</span></span></button><button class="icon-button heart ${recipe.favorite ? "selected" : ""}" aria-label="${recipe.favorite ? "Unfavorite" : "Favorite"} ${esc(recipe.name)}" data-action="favorite" data-id="${recipe.id}">${icon("heart")}</button></article>`;
}
function home() {
  const today = todayKey(),
    meals = state.plans.filter((p) => p.date === today),
    totals = nutrition(state, today);
  return (
    header(
      "Good food, planned.",
      dateLabel(today),
      `<button class="avatar" data-action="nav" data-to="settings" aria-label="Open settings">MP</button>`,
    ) +
    `<section class="hero"><div><span class="eyebrow">MAKE ROOM FOR YOUR WEEK</span><h3>Less wondering.<br>More cooking.</h3><p>Your next good meal<br>starts with a little plan.</p><button class="primary" data-action="nav" data-to="plan">Plan my meals ${icon("next")}</button></div>${art("salad", true)}</section>
    <div class="section-title"><h3>Today at a glance</h3><span class="muted">${meals.length} meals planned</span></div><section class="nutrition-card"><div class="nutrition-heading"><span><strong>${quantity(totals.calories)}</strong> <span class="muted">/ ${state.target} kcal</span></span><span class="tag">Planned</span></div><progress value="${totals.calories}" max="${state.target}" aria-label="Planned calories"></progress><div class="macro-grid">${["protein", "carbs", "fat"].map((k) => `<div><strong>${quantity(totals[k])}<small>g</small></strong><span>${k[0].toUpperCase() + k.slice(1)}</span></div>`).join("")}</div><p class="fine-print">Sample nutrition values are illustrative.</p></section>
    <div class="section-title"><h3>On the menu</h3><button class="text-button" data-action="nav" data-to="plan">View plan ${icon("next")}</button></div>${meals.length ? meals.map((p) => mealRow(p)).join("") : '<p class="empty">A fresh start. Open Plan to add your first meal.</p>'}
    <div class="section-title"><h3>From your recipe book</h3><button class="text-button" data-action="nav" data-to="recipes">See all</button></div>${state.recipes.slice(0, 2).map(recipeCard).join("")}`
  );
}
function mealRow(plan, removable = false) {
  const recipe = state.recipes.find((r) => r.id === plan.recipeId);
  if (!recipe) return "";
  return `<div class="meal-row"><button class="meal-main" data-action="detail" data-id="${recipe.id}"><span class="meal-dot ${plan.type.toLowerCase()}">${icon(plan.type === "Breakfast" ? "spark" : "recipes")}</span><span><span class="eyebrow">${plan.type}</span><strong>${esc(recipe.name)}</strong><small>${plan.servings} serving${plan.servings === 1 ? "" : "s"} · ${quantity(recipe.calories * plan.servings)} kcal</small></span></button>${removable ? `<button class="icon-button" data-action="remove-meal" data-id="${esc(plan.id)}" aria-label="Remove ${esc(recipe.name)}">${icon("close")}</button>` : ""}</div>`;
}
function plan() {
  const dates = weekDates(selectedDate);
  return (
    header(
      "Your week, sorted.",
      "MEAL PLAN",
      `<button class="icon-button outlined" data-action="week-view" aria-label="${weekView ? "Show day view" : "Show week view"}">${icon(weekView ? "recipes" : "plan")}</button>`,
    ) +
    `<div class="week-heading"><button class="icon-button" data-action="previous" aria-label="Previous week">${icon("back")}</button><button class="text-button" data-action="today">${dateLabel(dates[0], { month: "short", day: "numeric" })} – ${dateLabel(dates[6], { month: "short", day: "numeric" })}</button><button class="icon-button" data-action="next" aria-label="Next week">${icon("next")}</button></div><div class="week-strip">${dates.map((d) => `<button class="day ${d === selectedDate ? "active" : ""}" data-action="date" data-date="${d}" aria-label="${dateLabel(d)}" aria-pressed="${d === selectedDate}"><span>${dateLabel(d, { weekday: "short" })}</span><strong>${new Date(`${d}T12:00:00`).getDate()}</strong><i class="${state.plans.some((p) => p.date === d) ? "has-meals" : ""}"></i></button>`).join("")}</div><div class="plan-tools"><button class="secondary" data-action="fill">${icon("spark")} Fill empty slots</button><button class="secondary" data-action="generate">${icon("grocery")} Groceries</button></div>
    <p class="fine-print">Auto-fill keeps existing meals. Suggestions do not apply dietary or allergy restrictions.</p>
    ${
      weekView
        ? dates
            .map(
              (d) =>
                `<button class="week-summary" data-action="date" data-date="${d}"><strong>${dateLabel(d, { weekday: "long", day: "numeric" })}</strong><span>${state.plans.filter((p) => p.date === d).length} meals planned ${icon("next")}</span></button>`,
            )
            .join("")
        : `<div class="section-title"><h3>${dateLabel(selectedDate, { weekday: "long" })}</h3><span class="muted">${quantity(nutrition(state, selectedDate).calories)} kcal planned</span></div>${mealTypes
            .map(
              (type) =>
                `<section class="meal-section"><div class="section-title"><h4>${type}</h4><button class="icon-button small-button" data-action="add-meal" data-type="${type}" aria-label="Add ${type.toLowerCase()}">${icon("plus")}</button></div>${
                  state.plans
                    .filter((p) => p.date === selectedDate && p.type === type)
                    .map((p) => mealRow(p, true))
                    .join("") ||
                  `<button class="empty-slot" data-action="add-meal" data-type="${type}">${icon("plus")} Choose a recipe</button>`
                }</section>`,
            )
            .join("")}`
    }`
  );
}
function recipes() {
  const results = filterRecipes(state.recipes, query, category, favorites);
  return (
    header(
      "Your recipe book.",
      "SAVE WHAT YOU LOVE",
      `<button class="icon-button outlined ${favorites ? "selected" : ""}" data-action="favorites" aria-label="Show favorites" aria-pressed="${favorites}">${icon("heart")}</button>`,
    ) +
    `<label class="search">${icon("search")}<input id="search" type="search" placeholder="Search recipes or tags" aria-label="Search recipes" value="${esc(query)}"></label><div class="chips">${categories.map((c) => `<button data-action="category" data-category="${c}" class="chip ${category === c ? "active" : ""}" aria-pressed="${category === c}">${c}</button>`).join("")}</div><div class="section-title"><span class="muted">${results.length} recipe${results.length === 1 ? "" : "s"}${favorites ? " · Favorites" : ""}</span><button class="text-button" data-action="new-recipe">${icon("plus")} New recipe</button></div><div id="recipe-results">${results.map(recipeCard).join("") || '<div class="empty"><h3>No matching recipes</h3><p>Try another search or category, or add a recipe of your own.</p><button class="secondary" data-action="clear-filters">Clear filters</button></div>'}</div>`
  );
}
function detail() {
  const recipe = state.recipes.find((r) => r.id === detailId);
  if (!recipe) {
    screen = "recipes";
    return recipes();
  }
  return `<header class="detail-toolbar"><button class="icon-button" data-action="back" aria-label="Back to recipes">${icon("back")}</button><span>RECIPE BOOK</span><div><button class="icon-button ${recipe.favorite ? "selected" : ""}" data-action="favorite" data-id="${recipe.id}" aria-label="Toggle favorite">${icon("heart")}</button><button class="icon-button" data-action="edit-recipe" data-id="${recipe.id}" aria-label="Edit recipe">${icon("edit")}</button></div></header><div class="recipe-hero">${art(recipe.art, true)}<span class="tag">${esc(recipe.category)}</span><h2>${esc(recipe.name)}</h2><p>${esc(recipe.description)}</p><span class="meta">${icon("clock")} ${recipe.prep + recipe.cook} min · Makes ${recipe.servings} servings</span></div><section class="serving-bar"><strong>Cooking for</strong><div><button class="icon-button" data-action="servings-down" aria-label="Decrease servings" ${detailServings <= 1 ? "disabled" : ""}>−</button><span>${detailServings}</span><button class="icon-button" data-action="servings-up" aria-label="Increase servings" ${detailServings >= 1000 ? "disabled" : ""}>+</button></div></section><div class="section-title"><h3>Nutrition</h3><span class="muted">Total for ${detailServings} servings</span></div><div class="detail-macros">${["calories", "protein", "carbs", "fat"].map((k) => `<div><strong>${quantity(recipe[k] * detailServings)}</strong><span>${k === "calories" ? "kcal" : k + " (g)"}</span></div>`).join("")}</div><p class="fine-print">Illustrative sample values. Verify your own ingredients and portions.</p><div class="section-title"><h3>Ingredients</h3><span class="muted">Scaled for ${detailServings}</span></div><ul class="ingredients">${recipe.ingredients.map((item) => `<li><span>${esc(item.name)}</span><strong>${quantity((item.quantity * detailServings) / recipe.servings)} ${esc(item.unit)}</strong></li>`).join("")}</ul><div class="section-title"><h3>Let's make it</h3></div><ol class="instructions">${recipe.instructions
    .split("\n")
    .filter(Boolean)
    .map((line) => `<li>${esc(line)}</li>`)
    .join(
      "",
    )}</ol><button class="primary full" data-action="plan-recipe" data-id="${recipe.id}">${icon("plan")} Add to meal plan</button><button class="text-button danger full" data-action="delete-recipe" data-id="${recipe.id}">${icon("trash")} Delete recipe</button>`;
}
function grocery() {
  const checked = state.groceries.filter((i) => i.checked).length;
  return (
    header(
      "One list. All set.",
      "GROCERIES",
      `<button class="icon-button outlined" data-action="add-grocery" aria-label="Add grocery item">${icon("plus")}</button>`,
    ) +
    `<section class="grocery-summary"><div><strong>${state.groceries.length - checked}</strong><span>items to pick up</span></div><span class="tag">${checked} checked</span></section><div class="plan-tools"><button class="secondary" data-action="generate">${icon("plan")} From this week</button><button class="secondary" data-action="export">Export list ↓</button></div><p class="fine-print">Generated for ${dateLabel(weekDates(selectedDate)[0], { month: "short", day: "numeric" })}. Regenerating keeps manual items.</p>${state.groceries.length ? `<div class="grocery-list">${state.groceries.map((item) => `<div class="grocery-item ${item.checked ? "checked" : ""}"><label><input type="checkbox" data-grocery="${esc(item.id)}" ${item.checked ? "checked" : ""}><span><strong>${esc(item.name)}</strong><small>${quantity(item.quantity)} ${esc(item.unit)}${item.manual ? " · Added by you" : ""}</small></span></label><button class="icon-button" data-action="delete-grocery" data-id="${esc(item.id)}" aria-label="Delete ${esc(item.name)}">${icon("close")}</button></div>`).join("")}</div><button class="text-button danger" data-action="clear-checked" ${checked ? "" : "disabled"}>Clear checked items</button>` : `<div class="empty illustrated">${icon("grocery")}<h3>A clean shopping list</h3><p>Generate ingredients from your meal plan, or add something you need.</p><button class="primary" data-action="add-grocery">${icon("plus")} Add an item</button></div>`}`
  );
}
function settings() {
  return (
    header("Make it yours.", "SETTINGS") +
    `<section class="settings-section"><h3>Appearance</h3><label class="toggle"><span><strong>Dark mode</strong><small>A quieter view for evening planning.</small></span><input type="checkbox" id="dark-mode" role="switch" ${state.dark ? "checked" : ""}></label></section><section class="settings-section"><h3>Personal target</h3><form id="target-form"><label class="field">Daily calorie target<span class="input-with-unit"><input name="target" aria-label="Daily calorie target" type="number" min="1" max="100000" step="1" required value="${state.target}"><span>kcal</span></span></label><button class="secondary" type="submit">Save target</button></form><p class="fine-print">A personal reference only. This app does not calculate dietary needs or provide medical advice.</p></section><section class="settings-section"><h3>About this demo</h3><p>A browser adaptation of the Android Studio project. Try recipes, meal planning and grocery lists with synthetic sample data.</p><dl><div><dt>Storage</dt><dd>${storageUnavailable ? "This visit only" : "This browser only"}</dd></div><div><dt>Android sync</dt><dd>Not connected</dd></div><div><dt>Notifications</dt><dd>Android app only</dd></div></dl><a class="text-link" href="https://github.com/luigi043/MealPlanner" target="_blank" rel="noreferrer">View the Kotlin Android project ↗</a><a class="text-link" href="./gallery.html">Browse native & demo screenshots ↗</a><a class="text-link" href="./downloads/mealplanner-debug.apk" download>Download debug APK ↓</a></section><section class="settings-section"><h3>Start fresh</h3><p>Restore the example recipes and today's sample meals. This removes changes in this browser.</p><button class="secondary danger" data-action="reset">Reset demo data</button></section>`
  );
}
function render(keepScroll = false) {
  const scroll = app.querySelector("main")?.scrollTop || 0;
  document.querySelector("#phone").classList.toggle("dark", state.dark);
  const content =
    detailId !== null
      ? detail()
      : ({ home, plan, recipes, grocery, settings }[screen] || home)();
  app.innerHTML = `<main tabindex="-1" aria-label="${detailId !== null ? "Recipe details" : screen}">${content}<div class="bottom-spacer"></div></main>${detailId === null ? `<nav class="bottom-nav" aria-label="Main navigation">${["home", "plan", "recipes", "grocery", "settings"].map((key) => `<button data-action="nav" data-to="${key}" ${screen === key ? 'aria-current="page"' : ""}>${icon(key)}<span>${{ home: "Home", plan: "Plan", recipes: "Recipes", grocery: "Grocery", settings: "Settings" }[key]}</span></button>`).join("")}</nav>` : ""}`;
  if (keepScroll) app.querySelector("main").scrollTop = scroll;
}
function openDialog(title, body, formId, submitText = "Save") {
  returnFocus = document.activeElement;
  dialog.innerHTML = `<form id="${formId}"><div class="dialog-heading"><h3>${title}</h3><button type="button" class="icon-button" data-action="close-dialog" aria-label="Close dialog">${icon("close")}</button></div><div class="dialog-body">${body}<p id="form-error" role="alert" class="form-error"></p></div><footer class="dialog-footer"><button type="button" class="secondary" data-action="close-dialog">Cancel</button><button type="submit" class="primary">${submitText}</button></footer></form>`;
  dialog.showModal();
  positionDialog();
  dialog.querySelector("input, select, textarea")?.focus();
}
function positionDialog() {
  const rect = document.querySelector("#phone").getBoundingClientRect();
  Object.assign(dialog.style, {
    position: "fixed",
    left: `${rect.left + rect.width / 2}px`,
    top: `${rect.top + rect.height / 2}px`,
    right: "auto",
    bottom: "auto",
    margin: "0",
    transform: "translate(-50%, -50%)",
    width: `${Math.min(360, rect.width - 30)}px`,
    maxHeight: `${rect.height - 40}px`,
  });
  dialog.querySelector("form").style.maxHeight = `${rect.height - 44}px`;
}
window.addEventListener("resize", () => {
  if (dialog.open) positionDialog();
});
function closeDialog() {
  dialog.close();
  if (returnFocus?.isConnected) returnFocus.focus();
  else app.querySelector("h2, main")?.focus();
}
const field = (label, name, value = "", attrs = "") =>
  `<label class="field">${label}<input name="${name}" aria-label="${esc(label)}" value="${esc(value)}" ${attrs}></label>`;
function mealDialog(type = "Dinner", preselected) {
  openDialog(
    "Add a meal",
    `${field("Date", "date", selectedDate, 'type="date" required')}<label class="field">Meal<select name="type" aria-label="Meal">${mealTypes.map((t) => `<option ${type === t ? "selected" : ""}>${t}</option>`).join("")}</select></label><label class="field">Recipe<select name="recipeId" aria-label="Recipe" required>${state.recipes.length ? state.recipes.map((r) => `<option value="${r.id}" ${preselected === r.id ? "selected" : ""}>${esc(r.name)}</option>`).join("") : '<option value="">Add a recipe first</option>'}</select></label>${field("Servings", "servings", 1, 'type="number" min="1" max="1000" step="1" required')}`,
    "meal-form",
    "Add to plan",
  );
}
function recipeDialog(id) {
  const r = state.recipes.find((r) => r.id === id);
  openDialog(
    r ? "Edit recipe" : "New recipe",
    `<input type="hidden" name="id" value="${r?.id || ""}">${field("Recipe name", "name", r?.name, 'required maxlength="100"')}${field("Description", "description", r?.description, 'maxlength="500"')}<label class="field">Category<select name="category" aria-label="Category">${categories
      .filter((c) => c !== "All")
      .map(
        (c) => `<option ${r?.category === c ? "selected" : ""}>${c}</option>`,
      )
      .join(
        "",
      )}</select></label><div class="form-row">${field("Prep (min)", "prep", r?.prep ?? 0, 'type="number" min="0" max="10000" step="1" required')}${field("Cook (min)", "cook", r?.cook ?? 0, 'type="number" min="0" max="10000" step="1" required')}</div>${field("Recipe yield (servings)", "servings", r?.servings ?? 2, 'type="number" min="1" max="1000" step="1" required')}<h4>Nutrition per serving</h4><p class="fine-print">Use values you have verified. Enter 0 if unknown.</p><div class="form-row">${field("Calories (kcal)", "calories", r?.calories ?? 0, 'type="number" min="0" step="any" required')}${field("Protein (g)", "protein", r?.protein ?? 0, 'type="number" min="0" step="any" required')}</div><div class="form-row">${field("Carbs (g)", "carbs", r?.carbs ?? 0, 'type="number" min="0" step="any" required')}${field("Fat (g)", "fat", r?.fat ?? 0, 'type="number" min="0" step="any" required')}</div><label class="field">Ingredients for the whole recipe<textarea name="ingredients" aria-label="Ingredients for the whole recipe" rows="4" required placeholder="200 g Rice&#10;2 piece Carrot">${esc(r?.ingredients.map((i) => `${i.quantity} ${i.unit} ${i.name}`).join("\n") || "")}</textarea><small>One per line: quantity, unit, then name. Example: 200 g Rice</small></label><label class="field">Instructions<textarea name="instructions" aria-label="Instructions" rows="4" required>${esc(r?.instructions || "")}</textarea><small>One step per line.</small></label>${field("Tags (comma separated)", "tags", r?.tags)}`,
    "recipe-form",
    r ? "Save changes" : "Save recipe",
  );
}
document.addEventListener("click", (event) => {
  const button = event.target.closest("[data-action]");
  if (!button || button.disabled) return;
  const { action, id, to, type } = button.dataset;
  const recipe = state.recipes.find((r) => r.id === Number(id));
  if (action === "nav") return go(to);
  if (action === "close-dialog") return closeDialog();
  if (action === "detail") {
    detailId = Number(id);
    detailServings = recipe.servings;
    render();
    return;
  }
  if (action === "back") {
    detailId = null;
    screen = "recipes";
    render();
    return;
  }
  if (action === "favorite") {
    recipe.favorite = !recipe.favorite;
    save();
    render(true);
    return;
  }
  if (action === "servings-down" || action === "servings-up") {
    detailServings = Math.max(
      1,
      Math.min(1000, detailServings + (action === "servings-up" ? 1 : -1)),
    );
    render(true);
    return;
  }
  if (action === "previous" || action === "next")
    selectedDate = shiftDate(selectedDate, action === "next" ? 7 : -7);
  if (action === "today") selectedDate = todayKey();
  if (action === "week-view") weekView = !weekView;
  if (action === "date") {
    selectedDate = button.dataset.date;
    weekView = false;
  }
  if (action === "category") category = button.dataset.category;
  if (action === "favorites") favorites = !favorites;
  if (action === "clear-filters") {
    category = "All";
    favorites = false;
    query = "";
  }
  if (action === "new-recipe" || action === "edit-recipe")
    return recipeDialog(Number(id));
  if (action === "add-meal" || action === "plan-recipe")
    return mealDialog(type, Number(id));
  if (action === "remove-meal") {
    state.plans = state.plans.filter((p) => p.id !== id);
    save();
    toast("Meal removed from plan.");
  }
  if (action === "fill") {
    const before = state.plans.length;
    state.plans = fillWeek(state, weekDates(selectedDate));
    save();
    toast(
      `${state.plans.length - before} empty meal slots filled. Existing meals kept.`,
    );
  }
  if (action === "generate") {
    state.groceries = generateGroceries(state, weekDates(selectedDate));
    save();
    toast("Grocery list updated. Manual items kept.");
    screen = "grocery";
  }
  if (action === "add-grocery")
    return openDialog(
      "Add an item",
      `${field("Item name", "name", "", 'required maxlength="100"')}<div class="form-row">${field("Quantity", "quantity", 1, 'type="number" min="0.01" step="any" required')}${field("Unit", "unit", "piece", 'required maxlength="25"')}</div>`,
      "grocery-form",
      "Add item",
    );
  if (action === "delete-grocery") {
    state.groceries = state.groceries.filter((i) => i.id !== id);
    save();
  }
  if (action === "clear-checked") {
    state.groceries = state.groceries.filter((i) => !i.checked);
    save();
    toast("Checked items cleared.");
  }
  if (action === "export") {
    const text = state.groceries
      .map(
        (i) =>
          `${i.checked ? "[x]" : "[ ]"} ${i.name}: ${quantity(i.quantity)} ${i.unit}`,
      )
      .join("\n");
    const url = URL.createObjectURL(
      new Blob(["MealPlanner grocery list\n\n" + text], { type: "text/plain" }),
    );
    const link = document.createElement("a");
    link.href = url;
    link.download = "mealplanner-groceries.txt";
    link.click();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
    toast("Grocery list exported.");
    return;
  }
  if (action === "reset")
    return openDialog(
      "Reset demo data?",
      "<p>This removes your browser changes and restores the sample recipes and meals.</p>",
      "reset-form",
      "Reset demo",
    );
  if (action === "delete-recipe")
    return openDialog(
      "Delete recipe?",
      `<input type="hidden" name="id" value="${id}"><p>Delete ${esc(recipe.name)} and its planned meals? This cannot be undone.</p>`,
      "delete-form",
      "Delete recipe",
    );
  render(true);
});
document.addEventListener("input", (event) => {
  if (event.target.id === "search") {
    const position = event.target.selectionStart;
    query = event.target.value;
    render(true);
    const input = document.querySelector("#search");
    input.focus();
    input.setSelectionRange(position, position);
  }
});
document.addEventListener("change", (event) => {
  if (event.target.id === "dark-mode") {
    state.dark = event.target.checked;
    save();
    render(true);
  }
  if (event.target.dataset.grocery) {
    const item = state.groceries.find(
      (i) => i.id === event.target.dataset.grocery,
    );
    item.checked = event.target.checked;
    save();
    render(true);
  }
});
document.addEventListener("submit", (event) => {
  event.preventDefault();
  const form = event.target,
    formId = form.getAttribute("id"),
    values = Object.fromEntries(new FormData(form));
  const error = (message) => {
    form.querySelector("#form-error").textContent = message;
  };
  if (formId === "target-form") {
    if (validAmount(values.target) && Number.isInteger(Number(values.target))) {
      state.target = Number(values.target);
      save();
      toast("Personal target saved.");
      render(true);
    }
    return;
  }
  if (formId === "meal-form") {
    if (
      !values.date ||
      !state.recipes.some((r) => r.id === Number(values.recipeId)) ||
      !Number.isInteger(Number(values.servings)) ||
      Number(values.servings) < 1 ||
      Number(values.servings) > 1000
    )
      return error("Choose a recipe, date and valid serving count.");
    state.plans.push({
      id: crypto.randomUUID(),
      date: values.date,
      type: values.type,
      recipeId: Number(values.recipeId),
      servings: Number(values.servings),
    });
    selectedDate = values.date;
    screen = "plan";
    detailId = null;
    toast("Meal added to your plan.");
  }
  if (formId === "grocery-form") {
    if (
      !values.name.trim() ||
      !values.unit.trim() ||
      !validAmount(values.quantity)
    )
      return error("Enter an item name, unit and a positive quantity.");
    state.groceries.push({
      id: crypto.randomUUID(),
      name: values.name.trim(),
      unit: values.unit.trim(),
      quantity: Number(values.quantity),
      checked: false,
      manual: true,
    });
    toast("Item added.");
  }
  if (formId === "recipe-form") {
    if (!values.name.trim() || !values.instructions.trim())
      return error("Recipe name and instructions are required.");
    for (const key of ["calories", "protein", "carbs", "fat", "prep", "cook"])
      if (!validAmount(values[key], true))
        return error("Use finite, non-negative numbers.");
    const ingredients = values.ingredients
      .split("\n")
      .filter((line) => line.trim())
      .map((line) => {
        const match = line.trim().match(/^(\S+)\s+(\S+)\s+(.+)$/);
        return match && validAmount(match[1])
          ? {
              quantity: Number(match[1]),
              unit: match[2],
              name: match[3].trim(),
            }
          : null;
      });
    if (!ingredients.length || ingredients.some((i) => !i))
      return error(
        "Each ingredient needs a positive quantity, unit and name. Example: 200 g Rice",
      );
    const old = state.recipes.find((r) => r.id === Number(values.id));
    const recipe = {
      id: old?.id || Date.now(),
      name: values.name.trim(),
      description: values.description.trim(),
      category: values.category,
      instructions: values.instructions.trim(),
      tags: values.tags.trim(),
      ingredients,
      favorite: old?.favorite || false,
      art: old?.art || "salad",
    };
    ["calories", "protein", "carbs", "fat", "prep", "cook", "servings"].forEach(
      (key) => (recipe[key] = Number(values[key])),
    );
    if (old)
      state.recipes = state.recipes.map((r) => (r.id === old.id ? recipe : r));
    else state.recipes.push(recipe);
    detailId = recipe.id;
    detailServings = recipe.servings;
    toast(old ? "Recipe updated." : "Recipe saved.");
  }
  if (formId === "reset-form") {
    state = sampleState();
    selectedDate = todayKey();
    screen = "home";
    detailId = null;
    query = "";
    category = "All";
    favorites = false;
    toast("Sample data restored.");
  }
  if (formId === "delete-form") {
    const id = Number(values.id);
    state.recipes = state.recipes.filter((r) => r.id !== id);
    state.plans = state.plans.filter((p) => p.recipeId !== id);
    detailId = null;
    screen = "recipes";
    toast("Recipe and planned meals removed.");
  }
  save();
  closeDialog();
  render();
});
dialog.addEventListener("click", (event) => {
  if (event.target === dialog) closeDialog();
});
render();
