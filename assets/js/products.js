/**
 * products.js - Products & Inventory Catalogue Module (#view-products)
 */
window.HiperProducts = (() => {
  "use strict";

  const { state, $, $$ } = window.HiperApi;

  function renderProducts() {
    const body = $("#products-body");
    if (!body) return;
    body.replaceChildren();

    const query = $(`[data-search="products"]`)?.value.trim().toLowerCase() || "";
    const items = (state.products || []).filter((item) =>
      !query || Object.values(item).some((value) => String(value ?? "").toLowerCase().includes(query))
    );

    for (const item of items) {
      const row = document.createElement("tr");

      const tdName = document.createElement("td");
      tdName.innerHTML = `<span class="cell-title">${item.name || "Unnamed"}</span><span class="cell-subtitle">${item.description || ""}</span>`;
      row.append(tdName);

      const tdCode = document.createElement("td");
      tdCode.textContent = [item.code, item.barcode].filter(Boolean).join(" · ") || "-";
      row.append(tdCode);

      const tdShop = document.createElement("td");
      tdShop.textContent = item.at_shop || "Main Shop";
      row.append(tdShop);

      const tdStock = document.createElement("td");
      if (!item.service && Number(item.quantity) <= 5) {
        tdStock.innerHTML = `<span class="stock-badge low">${item.quantity || 0}</span>`;
      } else {
        tdStock.innerHTML = `<span class="stock-badge">${item.service ? "Service" : (item.quantity || 0)}</span>`;
      }
      row.append(tdStock);

      const tdPrice = document.createElement("td");
      tdPrice.textContent = "$" + (Number(item.price) || 0).toFixed(2);
      row.append(tdPrice);

      const tdCost = document.createElement("td");
      tdCost.textContent = "$" + (Number(item.cost) || 0).toFixed(2);
      row.append(tdCost);

      const tdServ = document.createElement("td");
      tdServ.textContent = item.service ? "Yes" : "No";
      row.append(tdServ);

      const tdAction = document.createElement("td");
      if (window.HiperAuth && window.HiperAuth.isManager()) {
        const btn = document.createElement("button");
        btn.className = "text-button";
        btn.textContent = "Edit";
        btn.addEventListener("click", () => {
          if (window.HiperApp) window.HiperApp.openRecordDialog("products", item);
        });
        tdAction.append(btn);
      }
      row.append(tdAction);

      body.append(row);
    }

    if (!items.length) {
      const emptyRow = document.createElement("tr");
      emptyRow.innerHTML = `<td colspan="8" class="empty-state">No products match this search.</td>`;
      body.append(emptyRow);
    }
  }

  return {
    renderProducts
  };
})();
