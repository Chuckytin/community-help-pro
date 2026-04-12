import { Link, useNavigate } from "react-router-dom";
import { useState } from "react";
import { toast } from "react-toastify";
import axios from "axios";
import { useAppContext } from "../../hooks/useAppContext";

const Login = () => {
  const [isCreateAccount, setIsCreateAccount] = useState(false);
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);

  const { backendURL, setIsLoggedIn, setToken, getUserDataWithToken } =
    useAppContext();
  const navigate = useNavigate();

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      if (isCreateAccount) {
        const { data } = await axios.post(`${backendURL}/auth/register`, {
          name,
          email,
          password,
        });
        setToken(data.token);
        setIsLoggedIn(true);
        await getUserDataWithToken(data.token);
        toast.success("Cuenta creada. Revisa tu email para verificarla.");
        navigate("/email-verify");
      } else {
        const { data } = await axios.post(`${backendURL}/auth/login`, {
          email,
          password,
        });
        setToken(data.token);
        setIsLoggedIn(true);
        await getUserDataWithToken(data.token);
        navigate("/");
      }
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        if (error.response?.data?.code === "EMAIL_NOT_VERIFIED") {
          toast.error("Email no verificado. Revisa tu bandeja de entrada.");
          navigate("/email-verify");
        } else {
          toast.error(error.response?.data?.message || "Ha ocurrido un error.");
        }
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
      {/* Burbujas decorativas */}
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

      {/* Logo */}
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

      {/* Card */}
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
        <h2
          className="text-center mb-4"
          style={{
            background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
            WebkitBackgroundClip: "text",
            WebkitTextFillColor: "transparent",
          }}
        >
          {isCreateAccount ? "Crear cuenta" : "Bienvenido"}
        </h2>

        <form onSubmit={onSubmit}>
          {isCreateAccount && (
            <div className="mb-3">
              <label
                className="form-label"
                style={{ color: "#4a5568", fontWeight: 500 }}
              >
                Nombre completo
              </label>
              <input
                type="text"
                className="form-control py-2"
                style={{ borderRadius: 8, border: "1px solid #e2e8f0" }}
                placeholder="Ana García"
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
              />
            </div>
          )}

          <div className="mb-3">
            <label
              className="form-label"
              style={{ color: "#4a5568", fontWeight: 500 }}
            >
              Email
            </label>
            <input
              type="email"
              className="form-control py-2"
              style={{ borderRadius: 8, border: "1px solid #e2e8f0" }}
              placeholder="ana@ejemplo.com"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>

          <div className="mb-3">
            <label
              className="form-label"
              style={{ color: "#4a5568", fontWeight: 500 }}
            >
              Contraseña
            </label>
            <input
              type="password"
              className="form-control py-2"
              style={{ borderRadius: 8, border: "1px solid #e2e8f0" }}
              placeholder="••••••••"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>

          {!isCreateAccount && (
            <div className="d-flex justify-content-end mb-3">
              <Link
                to="/reset-password"
                className="text-decoration-none small"
                style={{ color: "#667eea" }}
              >
                ¿Olvidaste tu contraseña?
              </Link>
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="btn w-100 py-2"
            style={{
              background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
              color: "white",
              border: "none",
              borderRadius: 8,
              fontWeight: 600,
              boxShadow: "0 4px 6px rgba(102,126,234,0.3)",
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
            {loading
              ? "Procesando..."
              : isCreateAccount
                ? "Crear cuenta"
                : "Entrar"}
          </button>
        </form>

        {/* ── Separador ── */}
        <div className="d-flex align-items-center my-3">
          <hr className="grow m-0" />
          <span className="px-2 text-muted small">o continúa con</span>
          <hr className="grow m-0" />
        </div>

        {/* ── Botón Google ── */}
        <a
          href="http://localhost:8080/oauth2/authorization/google"
          className="btn w-100 py-2 d-flex align-items-center justify-content-center gap-2"
          style={{
            border: "1px solid #e2e8f0",
            borderRadius: 8,
            background: "white",
            color: "#4a5568",
            fontWeight: 500,
            textDecoration: "none",
            transition: "all 0.3s ease",
          }}
          onMouseEnter={(e: React.MouseEvent<HTMLAnchorElement>) =>
            (e.currentTarget.style.background = "#f8f9fa")
          }
          onMouseLeave={(e: React.MouseEvent<HTMLAnchorElement>) =>
            (e.currentTarget.style.background = "white")
          }
        >
          <svg width="18" height="18" viewBox="0 0 48 48">
            <path
              fill="#EA4335"
              d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"
            />
            <path
              fill="#4285F4"
              d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"
            />
            <path
              fill="#FBBC05"
              d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"
            />
            <path
              fill="#34A853"
              d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.18 1.48-4.97 2.31-8.16 2.31-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"
            />
            <path fill="none" d="M0 0h48v48H0z" />
          </svg>
          Continuar con Google
        </a>

        {/* ── Toggle login/registro ── */}
        <div className="text-center mt-3">
          <p className="mb-0" style={{ color: "#4a5568" }}>
            {isCreateAccount ? (
              <>
                ¿Ya tienes cuenta?{" "}
                <span
                  onClick={() => setIsCreateAccount(false)}
                  style={{
                    color: "#667eea",
                    cursor: "pointer",
                    fontWeight: 500,
                    textDecoration: "underline",
                  }}
                >
                  Inicia sesión
                </span>
              </>
            ) : (
              <>
                ¿No tienes cuenta?{" "}
                <span
                  onClick={() => setIsCreateAccount(true)}
                  style={{
                    color: "#667eea",
                    cursor: "pointer",
                    fontWeight: 500,
                    textDecoration: "underline",
                  }}
                >
                  Regístrate
                </span>
              </>
            )}
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;
