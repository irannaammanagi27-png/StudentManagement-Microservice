
const BASE_URL = "";
const TOKEN_KEY = "edumanage_token";

// ---------------- TOKEN MANAGEMENT ----------------

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function saveToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
}

export function getUserFromToken() {
  const token = getToken();

  if (!token) return null;

  try {
    const payload = token.split(".")[1];
    const decoded = JSON.parse(
      atob(payload.replace(/-/g, "+").replace(/_/g, "/"))
    );

    return {
      email: decoded.sub || decoded.email,
      role: decoded.role,
      userId: decoded.userId,
      studentId: decoded.studentId,
    };
  } catch (error) {
    console.error("Unable to decode JWT:", error);
    return null;
  }
}

// ---------------- COMMON API REQUEST ----------------

async function request(endpoint, options = {}) {
  const token = getToken();

  const headers = {
    ...(options.body !== undefined
      ? { "Content-Type": "application/json" }
      : {}),
    ...(options.headers || {}),
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  const responseText = await response.text();

  let result = null;

  if (responseText) {
    try {
      result = JSON.parse(responseText);
    } catch {
      result = responseText;
    }
  }

  if (response.status === 401) {
    window.dispatchEvent(new Event("auth-expired"));
  }

  if (!response.ok) {
    const message =
      typeof result === "string"
        ? result
        : result?.message ||
          result?.error ||
          `Request failed: ${response.status}`;

    throw new Error(message);
  }

  return result;
}

// ---------------- AUTH API ----------------

export const authAPI = {
  async login(email, password) {
    const result = await request("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    });

    const token =
      result?.token ||
      result?.accessToken ||
      result?.jwt ||
      result?.access_token;

    if (!token) {
      throw new Error("Login response does not contain a JWT token.");
    }

    saveToken(token);

    const user = getUserFromToken();

    if (!user?.role) {
      clearToken();
      throw new Error("JWT does not contain a user role.");
    }

    return user;
  },

  me() {
    return request("/api/auth/me");
  },

  logout() {
    clearToken();
  },
};

// ---------------- RESOURCE API FACTORY ----------------

function createResource(endpoint) {
  return {
    getAll(params = "") {
      const query = params
        ? params.startsWith("?")
          ? params
          : `?${params}`
        : "";

      return request(`${endpoint}${query}`);
    },

    getById(id) {
      return request(`${endpoint}/${id}`);
    },

    getByStudentId(studentId) {
      return request(`${endpoint}/student/${studentId}`);
    },

    create(data) {
      return request(endpoint, {
        method: "POST",
        body: JSON.stringify(data),
      });
    },

    update(id, data) {
      return request(`${endpoint}/${id}`, {
        method: "PUT",
        body: JSON.stringify(data),
      });
    },

    remove(id) {
      return request(`${endpoint}/${id}`, {
        method: "DELETE",
      });
    },
  };
}

// ---------------- STUDENT API ----------------

export const studentAPI = createResource("/api/students");

// ---------------- COURSE API ----------------

export const courseAPI = createResource("/api/courses");

// ---------------- MARKS API ----------------

export const marksAPI = createResource("/api/marks");

// ---------------- ATTENDANCE API ----------------

export const attendanceAPI = createResource("/api/attendance");

// ---------------- ATTENDANCE SUMMARY API ----------------

export const attendanceSummaryAPI = {
  getPercentage(studentId) {
    return request(
      `/api/attendance/student/${studentId}/percentage`
    );
  },
};
// ---------------- AI ASSISTANT API ----------------

export const aiAPI = {
  chat(message) {
    return request("/api/ai/chat", {
      method: "POST",
      body: JSON.stringify({ message }),
    });
  },
};