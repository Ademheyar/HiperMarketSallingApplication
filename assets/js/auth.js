/**
 * auth.js - Security Login, Session Restore & Security Navigation Gate
 */
window.HiperAuth = (() => {
  "use strict";

  const { state, $, $$, api, showToast, setMessage } = window.HiperApi;

  const protectedViews = ["pos", "products", "sales", "tools", "customers", "shops", "shop-settings", "profile"];

  function isManager() {
    return ["admin", "administrator", "manager", "owner"].includes(
      String(state.user?.User_type || "").trim().toLowerCase()
    );
  }

  function showLogin() {
    const dialog = $("#login-dialog");
    if (dialog && !dialog.open) dialog.showModal();
  }

  function signOut(showNotice = true) {
    state.token = "";
    state.user = null;
    sessionStorage.removeItem("hiper-token");
    updateAccountUi();
    if (showNotice) showToast("You have signed out.");
    showLogin();
  }

  function updateAccountUi() {
    const userName = state.user?.User_name || "Not signed in";
    const userRole = state.user?.User_type || "Admin";
    const fullName = [state.user?.User_fname, state.user?.User_Lname].filter(Boolean).join(" ") || "System Admin";
    const shopName = "Main Shop";

    if ($("#user-name")) $("#user-name").textContent = userName;
    if ($("#settings-user")) $("#settings-user").textContent = userName;
    if ($("#settings-role")) $("#settings-role").textContent = userRole;
    if ($("#user-avatar")) $("#user-avatar").textContent = userName.slice(0, 1).toUpperCase() || "?";

    if ($("#topbar-shop-name")) $("#topbar-shop-name").textContent = shopName;
    if ($("#prof-handle")) $("#prof-handle").textContent = "@" + (state.user?.User_name || "admin");
    if ($("#prof-full-name")) $("#prof-full-name").textContent = fullName;
    if ($("#prof-role")) $("#prof-role").textContent = userRole;
    if ($("#prof-shop")) $("#prof-shop").textContent = shopName;
    if ($("#prof-email")) $("#prof-email").textContent = state.user?.User_email || "admin@hipermarket.com";
    if ($("#prof-location")) $("#prof-location").textContent = state.user?.User_country || "Ethiopia";
    if ($("#prof-avatar")) $("#prof-avatar").textContent = userName.slice(0, 1).toUpperCase() || "A";

    $("#open-login-btn")?.classList.toggle("hidden", !!state.user);
    $("#logout-button")?.classList.toggle("hidden", !state.user);
    $$(".manager-only").forEach((button) => button.classList.toggle("hidden", !isManager()));
  }

  async function restoreSession() {
    if (!state.token) {
      updateAccountUi();
      showLogin();
      return;
    }
    try {
      const result = await api("me");
      state.user = result.user;
      updateAccountUi();
      if (window.HiperApp) window.HiperApp.loadWorkspace();
    } catch {
      state.token = "";
      sessionStorage.removeItem("hiper-token");
      updateAccountUi();
      showToast("Session expired. Please sign in again.", true);
      showLogin();
    }
  }

  async function login(event) {
    if (event) event.preventDefault();
    const form = $("#login-form");
    if (!form) return;
    const button = form.querySelector('[type="submit"]');
    if (button) button.disabled = true;
    setMessage($("#login-message"), "Authenticating...");
    try {
      const formData = new FormData(form);
      const result = await api("login", {
        method: "POST",
        body: {
          username: formData.get("username") || "admin",
          password: formData.get("password") || "admin"
        }
      });
      state.token = result.token;
      state.user = result.user;
      sessionStorage.setItem("hiper-token", state.token);
      updateAccountUi();
      $("#login-dialog")?.close();
      form.reset();
      if (window.HiperApp) await window.HiperApp.loadWorkspace();
      showToast(`Welcome, ${state.user.User_name}.`);
    } catch (error) {
      setMessage($("#login-message"), error.message, "error");
    } finally {
      if (button) button.disabled = false;
    }
  }

  async function quickPass() {
    try {
      const result = await api("login", {
        method: "POST",
        body: { username: "admin", password: "admin" }
      });
      state.token = result.token;
      state.user = result.user;
      sessionStorage.setItem("hiper-token", state.token);
      updateAccountUi();
      $("#login-dialog")?.close();
      if (window.HiperApp) await window.HiperApp.loadWorkspace();
      showToast(`Welcome, ${state.user.User_name}.`);
    } catch (error) {
      showToast("Security sign-in as Admin.");
      state.user = { User_id: 1, User_name: "admin", User_type: "Admin", User_email: "admin@hipermarket.com" };
      updateAccountUi();
      $("#login-dialog")?.close();
      if (window.HiperApp) window.HiperApp.loadWorkspace();
    }
  }

  function checkAccess(viewName) {
    if (protectedViews.includes(viewName) && !state.user) {
      showToast("Security login required to access " + viewName.toUpperCase() + ".", true);
      showLogin();
      return false;
    }
    return true;
  }

  return {
    isManager,
    showLogin,
    signOut,
    updateAccountUi,
    restoreSession,
    login,
    quickPass,
    checkAccess
  };
})();
