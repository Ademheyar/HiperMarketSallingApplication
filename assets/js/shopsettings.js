/**
 * shopsettings.js - Manager Shop Settings & Access Control Module (#view-shop-settings)
 */
window.HiperShopSettings = (() => {
  "use strict";

  const { state, $, showToast } = window.HiperApi;

  function renderExpenses() {
    const body = $("#expenses-body");
    if (!body) return;
    body.replaceChildren();
    for (const e of state.expenses) {
      const row = document.createElement("tr");
      row.innerHTML = `<td>${e.id}</td><td>${e.title}</td><td>$${e.amount.toFixed(2)}</td><td>${e.category}</td><td>${e.date}</td><td>${e.ref}</td><td>${e.description}</td>`;
      body.append(row);
    }
  }

  function renderMatrix() {
    const body = $("#matrix-body");
    if (!body) return;
    body.replaceChildren();
    state.matrixPermissions.forEach((perm, idx) => {
      const row = document.createElement("tr");
      const tdPerm = document.createElement("td");
      tdPerm.textContent = perm;
      const tdLvl = document.createElement("td");
      tdLvl.textContent = `Level ${state.matrixLevels[idx] || 2}`;
      const tdBtn = document.createElement("td");
      const btn = document.createElement("button");
      btn.className = "button button-secondary button-small";
      btn.textContent = "🧮 Calculator Getter";
      btn.addEventListener("click", () => {
        if (window.HiperGetValue) {
          window.HiperGetValue.openKeypadDialog(`Change Access Level of ${perm}`, state.matrixLevels[idx], (val) => {
            state.matrixLevels[idx] = parseInt(val) || 0;
            renderMatrix();
          });
        }
      });
      tdBtn.append(btn);
      row.append(tdPerm, tdLvl, tdBtn);
      body.append(row);
    });
  }

  function renderWorkers() {
    const body = $("#workers-body");
    if (!body) return;
    body.replaceChildren();
    state.workers.forEach((w) => {
      const row = document.createElement("tr");
      row.innerHTML = `<td>${w.id}</td><td>${w.fullName}</td><td>${w.userName}</td><td>${w.role}</td><td>${w.shopName}</td><td>Level ${w.accessLevel}</td>`;
      body.append(row);
    });
  }

  function renderOthers() {
    const catList = $("#category-tree-list");
    if (catList) {
      catList.replaceChildren();
      ["MANS > ACCESSORIES > BAG > BACKPACK", "MANS > SHOES > CASUAL > SNEAKERS", "WOMANS > DRESS > CASUAL DRESS", "KIDS > BOYS > SHOES > SNEAKERS"].forEach(c => {
        const li = document.createElement("li");
        li.textContent = "📁 " + c;
        catList.append(li);
      });
    }
    const slipList = $("#slip-layout-list");
    if (slipList) {
      slipList.replaceChildren();
      ["1. LOGO", "2. Information", "3. Phone_No", "4. Receipt_no", "5. Date", "6. Seller", "7. Customer", "8. Item", "9. Total", "10. Rules"].forEach(s => {
        const li = document.createElement("li");
        li.textContent = "🧾 " + s;
        slipList.append(li);
      });
    }
  }

  function bindShopSettingsEvents() {
    ["info", "expenses", "workers", "others"].forEach(tab => {
      $(`#tab-shop-${tab}`)?.addEventListener("click", () => {
        ["info", "expenses", "workers", "others"].forEach(t => {
          $(`#tab-shop-${t}`)?.classList.toggle("active", t === tab);
          $(`#panel-shop-${t}`)?.classList.toggle("hidden", t !== tab);
        });
      });
    });

    $("#si-save-btn")?.addEventListener("click", () => {
      showToast("Shop Info saved successfully.");
    });
  }

  return {
    renderExpenses,
    renderMatrix,
    renderWorkers,
    renderOthers,
    bindShopSettingsEvents
  };
})();
