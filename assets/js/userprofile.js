/**
 * userprofile.js - User Profile Card Module (#view-profile)
 */
window.HiperUserProfile = (() => {
  "use strict";

  const { state, $, $$ } = window.HiperApi;

  function renderProfile() {
    const userName = state.user?.User_name || "admin";
    const userRole = state.user?.User_type || "Admin";
    const fullName = [state.user?.User_fname, state.user?.User_Lname].filter(Boolean).join(" ") || "System Admin";
    const shopName = "Main Shop";

    if ($("#prof-handle")) $("#prof-handle").textContent = "@" + userName;
    if ($("#prof-full-name")) $("#prof-full-name").textContent = fullName;
    if ($("#prof-role")) $("#prof-role").textContent = userRole;
    if ($("#prof-shop")) $("#prof-shop").textContent = shopName;
    if ($("#prof-email")) $("#prof-email").textContent = state.user?.User_email || "admin@hipermarket.com";
    if ($("#prof-location")) $("#prof-location").textContent = state.user?.User_country || "Ethiopia";
    if ($("#prof-avatar")) $("#prof-avatar").textContent = userName.slice(0, 1).toUpperCase() || "A";
  }

  function bindProfileEvents() {
    $("#prof-manage-btn")?.addEventListener("click", () => {
      if (window.HiperApp) window.HiperApp.navigate("manager");
    });

    $("#ptab-messages")?.addEventListener("click", () => {
      $$(".profile-tab.active").forEach(tab => tab.classList.remove("active"));
      $("#ptab-messages")?.classList.add("active");
      if ($("#prof-tab-content")) $("#prof-tab-content").innerHTML = `
        <div class="panel margin-top-sm">
          <p><strong>System Admin:</strong> Your daily POS shift report for Main Shop has been verified.</p>
          <p><strong>Warehouse Manager:</strong> New stock of barcode scanners has arrived.</p>
        </div>
      `;
    });

    $("#ptab-notif")?.addEventListener("click", () => {
      $$(".profile-tab.active").forEach(tab => tab.classList.remove("active"));
      $("#ptab-notif")?.classList.add("active");
      if ($("#prof-tab-content")) $("#prof-tab-content").innerHTML = `
        <div class="panel margin-top-sm">
          <p>🔔 <strong>Shift Open:</strong> POS Terminal shift opened successfully at Main Shop.</p>
          <p>🔔 <strong>Backup Complete:</strong> Local database backup synced to secure server storage.</p>
        </div>
      `;
    });

    $("#ptab-history")?.addEventListener("click", () => {
      $$(".profile-tab.active").forEach(tab => tab.classList.remove("active"));
      $("#ptab-history")?.classList.add("active");
      if ($("#prof-tab-content")) $("#prof-tab-content").innerHTML = `
        <div class="panel margin-top-sm">
          <p>🛒 <strong>Order #WEB-1052:</strong> $340.00 • Completed Today</p>
          <p>🛒 <strong>Order #WEB-1051:</strong> $85.50 • Completed Yesterday</p>
        </div>
      `;
    });
  }

  return {
    renderProfile,
    bindProfileEvents
  };
})();
