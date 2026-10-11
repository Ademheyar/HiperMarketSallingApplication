/**
 * customers.js - Users & Customers Module (#view-customers)
 */
window.HiperCustomers = (() => {
  "use strict";

  const { state, $ } = window.HiperApi;

  function renderUsers() {
    const body = $("#users-body");
    if (!body) return;
    body.replaceChildren();

    const query = $(`[data-search="users"]`)?.value.trim().toLowerCase() || "";
    const items = (state.users || []).filter((item) =>
      !query || Object.values(item).some((value) => String(value ?? "").toLowerCase().includes(query))
    );

    for (const item of items) {
      const row = document.createElement("tr");
      const fullName = [item.User_fname, item.User_Lname].filter(Boolean).join(" ") || "User";
      row.innerHTML = `
        <td><span class="cell-title">${fullName}</span><span class="cell-subtitle">${item.User_address || ""}</span></td>
        <td>${item.User_name || "-"}</td>
        <td>${item.User_phone_num || "-"}</td>
        <td>${item.User_email || "-"}</td>
        <td>${item.User_type || "Worker"}</td>
        <td>${item.User_country || "Global"}</td>
        <td><button class="text-button">Edit</button></td>
      `;
      body.append(row);
    }

    if (!items.length) {
      const emptyRow = document.createElement("tr");
      emptyRow.innerHTML = `<td colspan="7" class="empty-state">No users match this search.</td>`;
      body.append(emptyRow);
    }
  }

  return {
    renderUsers
  };
})();
