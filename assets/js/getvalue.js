/**
 * getvalue.js - Keypad Calculator Modal / GetValueDialog
 */
window.HiperGetValue = (() => {
  "use strict";

  const { $, $$ } = window.HiperApi;

  let keypadTarget = null;

  function openKeypadDialog(title, initialVal, onConfirm) {
    if ($("#keypad-title")) $("#keypad-title").textContent = title;
    if ($("#keypad-input")) $("#keypad-input").value = String(initialVal || "0");
    keypadTarget = onConfirm;
    const dialog = $("#keypad-dialog");
    if (dialog && !dialog.open) dialog.showModal();
  }

  function bindKeypadEvents() {
    $$(".keypad-btn").forEach(btn => {
      btn.addEventListener("click", () => {
        const key = btn.dataset.key;
        const input = $("#keypad-input");
        if (!input) return;
        if (key === "clear") {
          input.value = "0";
        } else {
          if (input.value === "0") input.value = key;
          else input.value += key;
        }
      });
    });

    $("#keypad-confirm-btn")?.addEventListener("click", () => {
      if (typeof keypadTarget === "function") {
        const input = $("#keypad-input");
        keypadTarget(input ? input.value : "0");
      }
      $("#keypad-dialog")?.close();
    });
  }

  return {
    openKeypadDialog,
    bindKeypadEvents
  };
})();
