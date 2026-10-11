/**
 * documents.js - Manage Documents Module (#view-sales / Doc.py - 100% Matching DocumentsScreen.kt)
 */
window.HiperDocuments = (() => {
  "use strict";

  const { state, $, $$ } = window.HiperApi;

  function populateUserDropdown() {
    const userSelect = $("#filter-user-id");
    if (!userSelect) return;
    const currentVal = userSelect.value;
    const uniqueUsers = Array.from(
      new Set(
        (state.sales || []).map(s => s.user_id || s.Seller_id || "admin").filter(Boolean)
      )
    );
    if (!uniqueUsers.includes("admin")) uniqueUsers.unshift("admin");

    userSelect.innerHTML = `<option value="">All Users</option>` +
      uniqueUsers.map(u => `<option value="${u}">${u}</option>`).join("");
    userSelect.value = currentVal;
  }

  function performDocSearch() {
    const userIdVal = $("#filter-user-id")?.value.trim() || "";
    const targetSeller = userIdVal || "All Sellers";
    if (!state.searchedTabs.includes(targetSeller)) {
      state.searchedTabs.push(targetSeller);
    }
    state.activeSellerTab = targetSeller;
    renderNotebookTabs();
    renderSales();
  }

  function renderNotebookTabs() {
    populateUserDropdown();

    const container = $("#seller-notebook-tabs");
    if (!container) return;
    container.replaceChildren();

    state.searchedTabs.forEach(tabName => {
      const btn = document.createElement("button");
      btn.className = `notebook-tab ${state.activeSellerTab === tabName ? "active" : ""}`;
      btn.innerHTML = `${tabName === "All Sellers" ? "👥" : "👤"} ${tabName}`;
      btn.addEventListener("click", () => {
        state.activeSellerTab = tabName;
        if ($("#filter-user-id")) $("#filter-user-id").value = tabName === "All Sellers" ? "" : tabName;
        renderNotebookTabs();
        renderSales();
      });

      if (tabName !== "All Sellers") {
        const close = document.createElement("span");
        close.className = "notebook-tab-close";
        close.textContent = "✕";
        close.addEventListener("click", (e) => {
          e.stopPropagation();
          state.searchedTabs = state.searchedTabs.filter(t => t !== tabName);
          if (state.activeSellerTab === tabName) state.activeSellerTab = "All Sellers";
          renderNotebookTabs();
          renderSales();
        });
        btn.append(close);
      }
      container.append(btn);
    });

    // Update Sub-Notebook tab titles with active seller name!
    if ($("#tab-doc-list")) $("#tab-doc-list").textContent = `📄 List Docs (${state.activeSellerTab})`;
    if ($("#tab-doc-report")) $("#tab-doc-report").textContent = `📊 Report (${state.activeSellerTab})`;
  }

  function renderSales() {
    const body = $("#sales-body");
    if (!body) return;
    body.replaceChildren();

    const docId = $("#filter-doc-id")?.value.trim() || "";
    const docType = $("#filter-doc-type")?.value.trim() || "";
    const docBarcode = $("#filter-doc-barcode")?.value.trim() || "";
    const extBarcode = $("#filter-ext-barcode")?.value.trim() || "";
    const customerId = $("#filter-customer-id")?.value.trim() || "";
    const filterUserId = $("#filter-user-id")?.value.trim() || "";

    const docs = (state.sales || []).filter(item => {
      const activeSeller = state.activeSellerTab !== "All Sellers" ? state.activeSellerTab : filterUserId;
      const matchesSeller = !activeSeller ||
        String(item.user_id || "").toLowerCase() === activeSeller.toLowerCase() ||
        String(item.Seller_id || "").toLowerCase() === activeSeller.toLowerCase();

      const matchesId = !docId || String(item.id || "").includes(docId);
      const matchesType = !docType || String(item.type || "").toLowerCase().includes(docType.toLowerCase());
      const matchesBarcode = !docBarcode || String(item.doc_barcode || "").toLowerCase().includes(docBarcode.toLowerCase());
      const matchesExtBarcode = !extBarcode || String(item.extension_barcode || "").toLowerCase().includes(extBarcode.toLowerCase());
      const matchesCustomer = !customerId || String(item.customer_id || "").toLowerCase().includes(customerId.toLowerCase());

      return matchesSeller && matchesId && matchesType && matchesBarcode && matchesExtBarcode && matchesCustomer;
    });

    for (const item of docs) {
      const row = document.createElement("tr");

      row.innerHTML = `
        <td>${item.id}</td>
        <td><span class="cell-title">${item.doc_barcode || "DOC"}</span><span class="cell-subtitle">${item.type || "Sale"}</span></td>
        <td>${item.extension_barcode || "-"}</td>
        <td>${item.user_id || "admin"}</td>
        <td>${item.customer_id || "-"}</td>
        <td>${item.type || "Sale"}</td>
        <td>${item.item ? "Cart Items" : "Items"}</td>
        <td>${item.qty || 1}</td>
        <td>$${(Number(item.price) || 0).toFixed(2)}</td>
        <td>$${(Number(item.discount) || 0).toFixed(2)}</td>
        <td>$${(Number(item.tax) || 0).toFixed(2)}</td>
        <td>${item.payments || "Cash"}</td>
        <td>${item.doc_created_date || "-"}</td>
        <td>${item.doc_expire_date || "-"}</td>
      `;

      row.addEventListener("click", () => {
        $$("#sales-body tr").forEach(r => r.style.background = "");
        row.style.background = "rgba(21, 101, 192, 0.35)";
        state.selectedDocInTable = item;
      });

      body.append(row);
    }

    const totalDocCount = docs.length;
    const totalItemCount = docs.reduce((sum, d) => sum + (parseFloat(d.qty) || 1), 0);
    const totalGrandTotal = docs.reduce((sum, d) => sum + (parseFloat(d.price) || 0), 0);

    const summaryHeader = $("#doc-summary-header");
    if (summaryHeader) {
      summaryHeader.textContent = `TOTAL DOC : ${totalDocCount} ITEMS : ${totalItemCount} COUNTED Grand Total : $${totalGrandTotal.toFixed(2)}`;
    }

    if ($("#rep-gross")) $("#rep-gross").textContent = "$" + totalGrandTotal.toFixed(2);
    if ($("#rep-count")) $("#rep-count").textContent = String(totalDocCount);
    if ($("#rep-profit")) $("#rep-profit").textContent = "$" + (totalGrandTotal * 0.35).toFixed(2);

    if (!docs.length) {
      const emptyRow = document.createElement("tr");
      emptyRow.innerHTML = `<td colspan="14" class="empty-state">No documents recorded for ${state.activeSellerTab}.</td>`;
      body.append(emptyRow);
    }
  }

  function bindDocEvents() {
    $("#filter-user-id")?.addEventListener("change", (e) => {
      const selected = e.target.value || "All Sellers";
      if (!state.searchedTabs.includes(selected)) {
        state.searchedTabs.push(selected);
      }
      state.activeSellerTab = selected;
      renderNotebookTabs();
      renderSales();
    });

    $("#btn-doc-search")?.addEventListener("click", performDocSearch);

    $("#btn-doc-view")?.addEventListener("click", () => {
      if (state.selectedDocInTable && window.HiperPos) {
        window.HiperPos.showReceipt(state.selectedDocInTable);
      } else if (!state.selectedDocInTable) {
        if (window.HiperApi) window.HiperApi.showToast("Select a document row from the table first.", true);
      }
    });

    $("#btn-doc-delete")?.addEventListener("click", () => {
      if (state.selectedDocInTable) {
        state.sales = (state.sales || []).filter(s => s.id !== state.selectedDocInTable.id);
        state.selectedDocInTable = null;
        renderSales();
        if (window.HiperApi) window.HiperApi.showToast("Document deleted.");
      } else {
        if (window.HiperApi) window.HiperApi.showToast("Select a document row to delete.", true);
      }
    });

    $("#btn-doc-undo")?.addEventListener("click", () => {
      if (window.HiperApp) window.HiperApp.refreshWorkspace();
    });

    $("#btn-doc-getdate")?.addEventListener("click", () => {
      const today = new Date().toISOString().split("T")[0];
      if ($("#filter-date-from")) $("#filter-date-from").value = today;
      if ($("#filter-date-to")) $("#filter-date-to").value = today;
      renderSales();
    });

    $("#tab-doc-list")?.addEventListener("click", () => {
      $("#tab-doc-list")?.classList.add("active");
      $("#tab-doc-report")?.classList.remove("active");
      $("#doc-list-view")?.classList.remove("hidden");
      $("#doc-report-view")?.classList.add("hidden");
    });

    $("#tab-doc-report")?.addEventListener("click", () => {
      $("#tab-doc-report")?.classList.add("active");
      $("#tab-doc-list")?.classList.remove("active");
      $("#doc-report-view")?.classList.remove("hidden");
      $("#doc-list-view")?.classList.add("hidden");
    });
  }

  return {
    populateUserDropdown,
    performDocSearch,
    renderNotebookTabs,
    renderSales,
    bindDocEvents
  };
})();
