import { useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import axios from "axios";
import { toast } from "react-toastify";
import { useAppContext } from "../../hooks/useAppContext";

/**
 * Flujo de recuperación de contraseña en 3 pasos con la misma
 * estética que el proyecto de referencia.
 */
const ResetPassword = () => {
  const [step, setStep] = useState<"email" | "otp" | "password">("email");
  const [email, setEmail] = useState("");
  const [otp, setOtp] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const inputRef = useRef<HTMLInputElement[]>([]);
  const { backendURL } = useAppContext();
  const navigate = useNavigate();

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement>,
    index: number,
  ) => {
    const value = e.target.value.replace(/\D/, "");
    e.target.value = value;
    if (value && index < 5) inputRef.current[index + 1]?.focus();
  };

  const handleKeyDown = (
    e: React.KeyboardEvent<HTMLInputElement>,
    index: number,
  ) => {
    if (e.key === "Backspace" && !e.currentTarget.value && index > 0) {
      inputRef.current[index - 1]?.focus();
    }
  };

  const handlePaste = (e: React.ClipboardEvent) => {
    e.preventDefault();
    const paste = e.clipboardData
      .getData("text")
      .replace(/\D/g, "")
      .slice(0, 6)
      .split("");
    paste.forEach((digit, i) => {
      if (inputRef.current[i]) inputRef.current[i].value = digit;
    });
    inputRef.current[Math.min(paste.length, 5)]?.focus();
  };

  const onSubmitEmail = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await axios.post(`${backendURL}/auth/forgot-password`, { email });
      toast.success("Código de recuperación enviado.");
      setStep("otp");
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        toast.error(
          error.response?.data?.message || "Error al enviar el código.",
        );
      } else {
        toast.error("Error al enviar el código.");
      }
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOtp = () => {
    const code = inputRef.current.map((i) => i?.value || "").join("");
    if (code.length !== 6) {
      toast.error("Introduce los 6 dígitos.");
      return;
    }
    setOtp(code);
    setStep("password");
  };

  const onSubmitNewPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      // El backend espera { email, code, newPassword } — no { email, otp, newPassword }
      await axios.post(`${backendURL}/auth/reset-password`, {
        email,
        code: otp, // ← renombrado de otp a code
        newPassword,
      });
      toast.success("Contraseña actualizada correctamente.");
      navigate("/login");
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        toast.error(
          error.response?.data?.message || "Error al actualizar la contraseña.",
        );
      } else {
        toast.error("Error al actualizar la contraseña.");
      }
    } finally {
      setLoading(false);
    }
  };

  // Estilos reutilizables
  const cardStyle: React.CSSProperties = {
    zIndex: 1,
    width: "100%",
    maxWidth: "450px",
    padding: "2.5rem",
    backgroundColor: "rgba(255,255,255,0.97)",
    borderRadius: "16px",
    boxShadow: "0 12px 30px rgba(0,0,0,0.15)",
    backdropFilter: "blur(4px)",
    border: "1px solid rgba(255,255,255,0.2)",
  };
  const btnStyle: React.CSSProperties = {
    background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
    color: "white",
    border: "none",
    borderRadius: 8,
    fontWeight: 600,
    boxShadow: "0 4px 6px rgba(102,126,234,0.3)",
    transition: "all 0.3s ease",
  };
  const inputStyle: React.CSSProperties = {
    borderRadius: 8,
    border: "1px solid #e2e8f0",
    transition: "all 0.3s ease",
  };

  return (
    <div
      className="position-relative min-vh-100 d-flex flex-column justify-content-center align-items-center"
      style={{
        background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
        overflow: "hidden",
      }}
    >
      <div
        style={{
          position: "absolute",
          top: "-10%",
          right: "-10%",
          width: "500px",
          height: "500px",
          borderRadius: "50%",
          background:
            "radial-gradient(circle, rgba(255,255,255,0.9) 0%, rgba(255,255,255,0) 70%)",
          opacity: 0.9,
          animation: "moveBubble 15s infinite ease-in-out",
        }}
      />
      <div
        style={{
          position: "absolute",
          bottom: "-15%",
          left: "-15%",
          width: "600px",
          height: "600px",
          borderRadius: "50%",
          background:
            "radial-gradient(circle, rgba(255,255,255,0.8) 0%, rgba(255,255,255,0) 70%)",
          opacity: 0.8,
          animation: "moveBubble 12s infinite ease-in-out",
        }}
      />

      <Link
        to="/"
        className="d-flex align-items-center gap-2 text-decoration-none"
        style={{ position: "absolute", top: 30, left: 40, zIndex: 2 }}
      >
        <span style={{ fontSize: "28px" }}>🤝</span>
        <span
          className="fw-bold fs-4"
          style={{
            background: "linear-gradient(135deg, #ffffff 0%, #e0e7ff 100%)",
            WebkitBackgroundClip: "text",
            WebkitTextFillColor: "transparent",
          }}
        >
          Community Help
        </span>
      </Link>

      {/* Paso 1: Email */}
      {step === "email" && (
        <div className="position-relative" style={cardStyle}>
          <h2
            className="text-center mb-2"
            style={{
              background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
              WebkitBackgroundClip: "text",
              WebkitTextFillColor: "transparent",
            }}
          >
            Recuperar contraseña
          </h2>
          <p className="text-center text-muted mb-4">
            Introduce tu email y te enviamos un código
          </p>
          <form onSubmit={onSubmitEmail}>
            <div className="mb-4">
              <label
                className="form-label"
                style={{ color: "#4a5568", fontWeight: 500 }}
              >
                Email
              </label>
              <input
                type="email"
                className="form-control py-2"
                style={inputStyle}
                placeholder="ana@ejemplo.com"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>
            <button
              type="submit"
              disabled={loading}
              className="btn w-100 py-2"
              style={btnStyle}
              onMouseEnter={(e) =>
                (e.currentTarget.style.transform = "translateY(-2px)")
              }
              onMouseLeave={(e) =>
                (e.currentTarget.style.transform = "translateY(0)")
              }
            >
              {loading && (
                <span className="spinner-border spinner-border-sm me-2" />
              )}
              {loading ? "Enviando..." : "Enviar código"}
            </button>
          </form>
          <div className="text-center mt-4">
            <Link to="/login" style={{ color: "#667eea", fontWeight: 500 }}>
              ← Volver al login
            </Link>
          </div>
        </div>
      )}

      {/* Paso 2: OTP */}
      {step === "otp" && (
        <div className="position-relative" style={cardStyle}>
          <div className="text-center mb-4">
            <div style={{ fontSize: "60px", lineHeight: 1 }} className="mb-3">
              🛡️
            </div>
            <h2
              className="fw-bold mb-2"
              style={{
                background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
                WebkitBackgroundClip: "text",
                WebkitTextFillColor: "transparent",
              }}
            >
              Introduce el código
            </h2>
            <p className="text-muted">
              Enviado a{" "}
              <span className="fw-semibold" style={{ color: "#4a5568" }}>
                {email}
              </span>
            </p>
          </div>
          <div
            className="d-flex justify-content-between gap-3 mb-4"
            onPaste={handlePaste}
          >
            {[...Array(6)].map((_, i) => (
              <input
                key={i}
                type="text"
                maxLength={1}
                className="form-control text-center py-3 fs-4"
                style={{
                  borderRadius: 12,
                  border: "1px solid #e2e8f0",
                  fontWeight: 600,
                  color: "#667eea",
                  height: 60,
                  boxShadow: "0 2px 6px rgba(102,126,234,0.1)",
                }}
                ref={(el) => {
                  if (el) inputRef.current[i] = el;
                }}
                onChange={(e) => handleChange(e, i)}
                onKeyDown={(e) => handleKeyDown(e, i)}
                onFocus={(e) => {
                  e.target.style.borderColor = "#667eea";
                  e.target.style.boxShadow = "0 0 0 3px rgba(102,126,234,0.2)";
                }}
                onBlur={(e) => {
                  e.target.style.borderColor = "#e2e8f0";
                  e.target.style.boxShadow = "0 2px 6px rgba(102,126,234,0.1)";
                }}
              />
            ))}
          </div>
          <button
            onClick={handleVerifyOtp}
            className="btn w-100 py-3 mb-3"
            style={{ ...btnStyle, borderRadius: 12 }}
            onMouseEnter={(e) =>
              (e.currentTarget.style.transform = "translateY(-2px)")
            }
            onMouseLeave={(e) =>
              (e.currentTarget.style.transform = "translateY(0)")
            }
          >
            Verificar código
          </button>
          <div className="text-center">
            <span
              onClick={() => setStep("email")}
              style={{
                color: "#667eea",
                cursor: "pointer",
                fontWeight: 500,
                textDecoration: "underline",
              }}
            >
              Reenviar código
            </span>
          </div>
        </div>
      )}

      {/* Paso 3: Nueva contraseña */}
      {step === "password" && (
        <div className="position-relative" style={cardStyle}>
          <h2
            className="text-center mb-2"
            style={{
              background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
              WebkitBackgroundClip: "text",
              WebkitTextFillColor: "transparent",
            }}
          >
            Nueva contraseña
          </h2>
          <p className="text-center text-muted mb-4">
            Elige una contraseña segura
          </p>
          <form onSubmit={onSubmitNewPassword}>
            <div className="mb-4">
              <label
                className="form-label"
                style={{ color: "#4a5568", fontWeight: 500 }}
              >
                Nueva contraseña
              </label>
              <input
                type="password"
                className="form-control py-2"
                style={inputStyle}
                placeholder="••••••••"
                required
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
              />
            </div>
            <button
              type="submit"
              disabled={loading}
              className="btn w-100 py-2"
              style={btnStyle}
              onMouseEnter={(e) =>
                (e.currentTarget.style.transform = "translateY(-2px)")
              }
              onMouseLeave={(e) =>
                (e.currentTarget.style.transform = "translateY(0)")
              }
            >
              {loading && (
                <span className="spinner-border spinner-border-sm me-2" />
              )}
              {loading ? "Actualizando..." : "Actualizar contraseña"}
            </button>
          </form>
        </div>
      )}
    </div>
  );
};

export default ResetPassword;
