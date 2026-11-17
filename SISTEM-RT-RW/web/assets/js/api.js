(function () {
  const apiBaseUrl = new URL("../backend/public/api/v1", window.location.href)
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
    const requestOptions = {
      method: options.method || "GET",
      credentials: "same-origin",
      cache: "no-store",
      headers
    };

    if (options.body !== undefined) {
      headers["Content-Type"] = "application/json";
      requestOptions.body = JSON.stringify(options.body);
    }

    let response;
    try {
      response = await fetch(`${apiBaseUrl}${path}`, requestOptions);
    } catch (error) {
      if (error instanceof TypeError) {
        throw new Error("Tidak dapat terhubung ke API. Jalankan server PHP dan periksa konfigurasi database.");
      }
      throw error;
    }

    const responseText = await response.text();
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
