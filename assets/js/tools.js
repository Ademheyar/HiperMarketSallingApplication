/**
 * tools.js - Hardware Tools & Promotional Discounts Module (#view-tools)
 */
window.HiperTools = (() => {
  "use strict";

  const { state, $ } = window.HiperApi;

  function renderTools() {
    const body = $("#tools-body");
    if (!body) return;
    body.replaceChildren();
    const items = state.tools.length ? state.tools : [
      { name: "Thermal Printer 80mm", type: "Printer", status: "Active" },
      { name: "Barcode Scanner Wireless", type: "Scanner", status: "Active" }
    ];
    for (const t of items) {
      const row = document.createElement("tr");
      row.innerHTML = `<td>${t.name || t.tool_name}</td><td>${t.type || t.tool_type}</td><td>${t.status || "Ready"}</td><td><button class="text-button">Edit</button></td>`;
      body.append(row);
    }
  }

  function renderPromotions() {
    const body = $("#promotions-body");
    if (!body) return;
    body.replaceChildren();
    const items = state.promotions.length ? state.promotions : [
      { action_name: "Summer Sale", discount: "15%", status: "Active" },
      { action_name: "Clearance Promo", discount: "25%", status: "Active" }
    ];
    for (const p of items) {
      const row = document.createElement("tr");
      row.innerHTML = `<td>${p.action_name || p.name}</td><td>${p.discount || "10%"}</td><td>${p.status || "Active"}</td><td><button class="text-button">Edit</button></td>`;
      body.append(row);
    }
  }

  return {
    renderTools,
    renderPromotions
  };
})();
