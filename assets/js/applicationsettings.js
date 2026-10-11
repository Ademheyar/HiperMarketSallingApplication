/**
 * applicationsettings.js - App Preferences & Image Uploads Module (#view-settings)
 */
window.HiperAppSettings = (() => {
  "use strict";

  const { $, api, setMessage, showToast } = window.HiperApi;

  async function uploadImage(event) {
    const file = event.target.files?.[0];
    if (!file) return;
    const message = $("#upload-message");
    if (file.size > 5 * 1024 * 1024) {
      setMessage(message, "Images must be 5 MB or smaller.", "error");
      event.target.value = "";
      return;
    }
    setMessage(message, "Uploading image…");
    const form = new FormData();
    form.append("file", file);
    try {
      const result = await api("upload", { method: "POST", body: form });
      setMessage(message, `Uploaded. Save this image path in record: ${result.file.path}`, "success");
      showToast("Image uploaded successfully.");
    } catch (error) {
      setMessage(message, error.message, "error");
    } finally {
      event.target.value = "";
    }
  }

  function bindAppSettingsEvents() {
    $("#image-upload")?.addEventListener("change", uploadImage);
    $("#refresh-data")?.addEventListener("click", () => {
      if (window.HiperApp) window.HiperApp.refreshWorkspace();
    });
  }

  return {
    uploadImage,
    bindAppSettingsEvents
  };
})();
