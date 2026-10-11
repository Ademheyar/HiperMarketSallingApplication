/**
 * home.js - Overview / Home Feed Module (#view-overview)
 */
window.HiperHome = (() => {
  "use strict";

  const { $, api } = window.HiperApi;

  async function loadDashboard() {
    try {
      const { summary } = await api("dashboard");
      if ($("#metric-sales")) $("#metric-sales").textContent = "$" + (summary.sales_today.total || "0.00");
      if ($("#metric-sales-count")) $("#metric-sales-count").textContent = `${summary.sales_today.count || 0} transactions today`;
      if ($("#metric-products")) $("#metric-products").textContent = Number(summary.products || 0).toLocaleString();
      if ($("#metric-stock")) $("#metric-stock").textContent = Number(summary.low_stock || 0).toLocaleString();
      if ($("#metric-shops")) $("#metric-shops").textContent = Number(summary.shops || 1).toLocaleString();
    } catch {}
  }

  return {
    loadDashboard
  };
})();
