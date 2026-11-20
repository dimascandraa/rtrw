(function () {
  const isDirectWebDirectory = /\/web(?:\/|$)/.test(window.location.pathname);
  const apiBaseUrl = new URL(
    isDirectWebDirectory ? "../backend/public/api/v1" : "/api/v1",
    window.location.href
  )
    .toString()
    .replace(/\/+$/, "");

  class ApiError extends Error {
    constructor(message, status, code) {
      super(message);
      this.name = "ApiError";
      this.status = status;
      this.code = code;
    }
  }

  async function request(path, options = {}) {
    const headers = { Accept: "application/json" };
    const controller = new AbortController();
    const timeoutMs = Number.isFinite(options.timeoutMs) && options.timeoutMs > 0
      ? options.timeoutMs
      : 15000;
    const timeoutSeconds = Math.ceil(timeoutMs / 1000);
    let timedOut = false;
    const requestOptions = {
      method: options.method || "GET",
      credentials: "same-origin",
      cache: "no-store",
      signal: controller.signal,
      headers
    };
    const timeoutId = window.setTimeout(() => {
      timedOut = true;
      controller.abort();
    }, timeoutMs);

    if (options.body !== undefined) {
      headers["Content-Type"] = "application/json";
      requestOptions.body = JSON.stringify(options.body);
    }

    let response;
    let responseText;
    try {
      response = await fetch(`${apiBaseUrl}${path}`, requestOptions);
      responseText = await response.text();
    } catch (error) {
      if (timedOut) {
        throw new Error(`API tidak merespons dalam ${timeoutSeconds} detik. Periksa backend dan koneksi database.`);
      }
      if (error instanceof TypeError) {
        throw new Error("Tidak dapat terhubung ke API. Jalankan server PHP dan periksa konfigurasi database.");
      }
      throw error;
    } finally {
      window.clearTimeout(timeoutId);
    }

    let payload;
    try {
      payload = JSON.parse(responseText);
    } catch (error) {
      throw new Error("API mengirim respons yang tidak valid. Periksa konfigurasi server.");
    }

    if (!response.ok) {
      const apiError = payload && payload.error ? payload.error : {};
      throw new ApiError(
        apiError.message || "Permintaan ke API gagal.",
        response.status,
        apiError.code || "api_error"
      );
    }

    if (!payload || !Object.prototype.hasOwnProperty.call(payload, "data")) {
      throw new Error("Format respons API tidak sesuai.");
    }

    return payload.data;
  }

  window.RtrwApi = { request, ApiError };
})();
