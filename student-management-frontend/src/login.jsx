
import React, { useState } from "react";
import { GraduationCap, Lock, Mail } from "lucide-react";
import { authAPI } from "./api.js";

function Login({ onLogin }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");
    setLoading(true);

    try {
      const user = await authAPI.login(email, password);
      onLogin(user);
    } catch (err) {
      setError(err.message || "Invalid email or password.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.page}>
      <form onSubmit={handleSubmit} style={styles.card}>
        <div style={styles.logo}>
          <GraduationCap size={32} />
        </div>

        <h1 style={styles.title}>EduManage</h1>
        <p style={styles.subtitle}>Sign in to your account</p>

        <label style={styles.label}>Email address</label>
        <div style={styles.inputWrap}>
          <Mail size={18} />
          <input
            style={styles.input}
            type="email"
            placeholder="Enter your email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>

        <label style={styles.label}>Password</label>
        <div style={styles.inputWrap}>
          <Lock size={18} />
          <input
            style={styles.input}
            type="password"
            placeholder="Enter your password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>

        {error && <p style={styles.error}>{error}</p>}

        <button type="submit" style={styles.button} disabled={loading}>
          {loading ? "Signing in..." : "Sign In"}
        </button>

        <p style={styles.footer}>
          Student Management System
        </p>
      </form>
    </div>
  );
}

const styles = {
  page: {
    minHeight: "100vh",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    background: "#f5f3ff",
    padding: "20px",
  },
  card: {
    width: "100%",
    maxWidth: "410px",
    padding: "36px",
    background: "#fff",
    borderRadius: "18px",
    boxShadow: "0 12px 40px rgba(30, 20, 80, 0.10)",
  },
  logo: {
    width: "58px",
    height: "58px",
    borderRadius: "16px",
    background: "#6c5ce7",
    color: "#fff",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    margin: "0 auto 16px",
  },
  title: {
    textAlign: "center",
    margin: "0",
    color: "#272343",
  },
  subtitle: {
    textAlign: "center",
    color: "#777",
    marginBottom: "28px",
  },
  label: {
    display: "block",
    margin: "16px 0 7px",
    fontWeight: "600",
    color: "#333",
  },
  inputWrap: {
    display: "flex",
    alignItems: "center",
    gap: "10px",
    border: "1px solid #ddd",
    borderRadius: "9px",
    padding: "0 12px",
    color: "#777",
  },
  input: {
    width: "100%",
    padding: "13px 0",
    border: "none",
    outline: "none",
    fontSize: "14px",
  },
  button: {
    width: "100%",
    padding: "14px",
    marginTop: "24px",
    border: "none",
    borderRadius: "9px",
    background: "#6c5ce7",
    color: "#fff",
    fontWeight: "600",
    cursor: "pointer",
  },
  error: {
    color: "#d63031",
    fontSize: "13px",
  },
  footer: {
    textAlign: "center",
    color: "#888",
    fontSize: "12px",
    marginTop: "22px",
  },
};

export default Login;
