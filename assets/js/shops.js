/**
 * shops.js - Shops & Stores Module (#view-shops)
 */
window.HiperShops = (() => {
  "use strict";

  const { state, $ } = window.HiperApi;

  function renderShops() {
    const grid = $("#shops-grid");
    if (!grid) return;
    grid.replaceChildren();

    const query = $(`[data-search="shops"]`)?.value.trim().toLowerCase() || "";
    const items = (state.shops || []).filter((item) =>
      !query || Object.values(item).some((value) => String(value ?? "").toLowerCase().includes(query))
    );

    for (const item of items) {
      const card = document.createElement("article");
      card.className = "panel shop-card";
      card.innerHTML = `
        <span class="shop-icon">🏬</span>
        <div>
          <h3>${item.Shop_name || "Main Shop"}</h3>
          <p class="muted">${item.Shop_brand_name || "BELLEMA FASHION"}</p>
          <p class="muted">${[item.Shop_location, item.Shop_country].filter(Boolean).join(" · ") || "Ethiopia"}</p>
        </div>
      `;
      grid.append(card);
    }

    if (!items.length) {
      const empty = document.createElement("p");
      empty.className = "panel empty-state";
      empty.textContent = "No shops match this search.";
      grid.append(empty);
    }
  }

  return {
    renderShops
  };
})();
