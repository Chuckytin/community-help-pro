import { useEffect, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import axios from "axios";
import { toast } from "react-toastify";
import { useAppContext } from "../../hooks/useAppContext";

/**
 * Página de verificación de email con código OTP de 6 dígitos.
 * El usuario llega aquí tras registrarse — el backend ya envió el código al email.
 */
const EmailVerify = () => {
  const [loading, setLoading] = useState(false);
  const [userEmail, setUserEmail] = useState<string>("");
  const inputRef = useRef<HTMLInputElement[]>([]);

  const { backendURL, userData, isLoggedIn, getUserData } = useAppContext();
  const navigate = useNavigate();

  /** Captura el email cuando userData esté disponible tras el registro */
  useEffect(() => {
    if (userData?.email) setUserEmail(userData.email);
  }, [userData]);

  /** Si el email ya está verificado no tiene que estar aquí */
  useEffect(() => {
    if (isLoggedIn && userData?.emailVerified) navigate("/");
  }, [isLoggedIn, userData, navigate]);

  /**
   * Al escribir un dígito, filtra no-numéricos y avanza al siguiente input.
   */
  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement>,
    index: number,
  ) => {
    const value = e.target.value.replace(/\D/, "");
    e.target.value = value;
    if (value && index < 5) inputRef.current[index + 1]?.focus();
  };

  /**
   * Al pulsar Backspace en un input vacío, vuelve al anterior.
   */
  const handleKeyDown = (
    e: React.KeyboardEvent<HTMLInputElement>,
    index: number,
  ) => {
    if (e.key === "Backspace" && !e.currentTarget.value && index > 0) {
      inputRef.current[index - 1]?.focus();
    }
  };

  /**
   * Al pegar el código completo, lo distribuye en los 6 inputs.
   */
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

  /**
   * Recoge los 6 dígitos y los envía al backend junto con el email.
   * El backend espera { email, code } — ambos obligatorios.
   * Si el código es correcto, refresca userData y redirige al home.
   */
  const handleVerify = async () => {
    const otp = inputRef.current.map((i) => i?.value || "").join("");
    if (otp.length !== 6) {
      toast.error("Introduce los 6 dígitos del código.");
      return;
    }
    if (!userEmail) {
      toast.error("No se pudo obtener el email. Vuelve a iniciar sesión.");
      navigate("/login");
      return;
    }
    setLoading(true);
    try {
      await axios.post(`${backendURL}/auth/verify-email`, {
        email: userEmail,
        code: otp,
      });
      toast.success("Email verificado correctamente.");
      await getUserData();
      navigate("/");
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        toast.error(error.response?.data?.message || "Código incorrecto.");
      } else {
        toast.error("Ha ocurrido un error.");
      }
    } finally {
      setLoading(false);
    }
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

      <div
        className="position-relative"
        style={{
          zIndex: 1,
          width: "100%",
          maxWidth: "450px",
          padding: "2.5rem",
          backgroundColor: "rgba(255,255,255,0.97)",
          borderRadius: "16px",
          boxShadow: "0 12px 30px rgba(0,0,0,0.15)",
          backdropFilter: "blur(4px)",
          border: "1px solid rgba(255,255,255,0.2)",
        }}
      >
        <div className="text-center mb-4">
          <div style={{ fontSize: "60px", lineHeight: 1 }} className="mb-3">
            ✉️
          </div>
          <h2
            className="fw-bold mb-2"
            style={{
              background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
              WebkitBackgroundClip: "text",
              WebkitTextFillColor: "transparent",
            }}
          >
            Verifica tu email
          </h2>
          <p className="text-muted">
            Código enviado a{" "}
            <span className="fw-semibold">{userEmail || "tu email"}</span>
          </p>
        </div>

        {/* Inputs OTP */}
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
                transition: "all 0.3s ease",
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
          disabled={loading}
          onClick={handleVerify}
          className="btn w-100 py-3"
          style={{
            background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
            color: "white",
            border: "none",
            borderRadius: 12,
            fontWeight: 600,
            boxShadow: "0 4px 12px rgba(102,126,234,0.3)",
            transition: "all 0.3s ease",
          }}
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
          {loading ? "Verificando..." : "Verificar email"}
        </button>
      </div>
    </div>
  );
};

export default EmailVerify;
