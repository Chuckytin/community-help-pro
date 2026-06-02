import { useNavigate } from "react-router-dom";
import { useRef, useEffect, useState } from "react";
import { useAppContext } from "../hooks/useAppContext";

/**
 * Barra de navegación superior.
 * Misma estética que el proyecto de referencia: fondo blanco, sombra,
 * avatar circular con dropdown al hacer clic.
 */
const Menubar = () => {
    const navigate = useNavigate();
    const { userData, isLoggedIn, logout } = useAppContext();
    const [dropdownOpen, setDropdownOpen] = useState(false);
    const dropdownRef = useRef<HTMLDivElement>(null);

    /**
     * Cierra el dropdown si el usuario hace clic fuera de él.
     * Se añade el listener al montar y se elimina al desmontar para no acumular listeners.
     */
    useEffect(() => {
        const handleClickOutside = (e: MouseEvent) => {
            if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
                setDropdownOpen(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    const handleLogout = () => {
        logout();
        navigate("/login");
    };

    return (
        <nav className="navbar bg-white px-4 py-3 d-flex justify-content-between align-items-center shadow-sm">

            {/* Logo + nombre — navega al home al hacer clic */}
            <div
                className="d-flex align-items-center gap-2"
                onClick={() => navigate("/")}
                style={{ cursor: "pointer", transition: "all 0.3s ease-in-out" }}
                onMouseEnter={(e) => {
                    e.currentTarget.style.transform = "scale(1.05)";
                    (e.currentTarget.querySelector("span") as HTMLElement).style.textShadow =
                        "0 0 8px rgba(102, 126, 234, 0.3)";
                }}
                onMouseLeave={(e) => {
                    e.currentTarget.style.transform = "scale(1)";
                    (e.currentTarget.querySelector("span") as HTMLElement).style.textShadow = "none";
                }}
            >
                <span style={{ fontSize: "28px" }}>🤝</span>
                <span
                    className="fw-bold fs-4"
                    style={{
                        background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
                        WebkitBackgroundClip: "text",
                        WebkitTextFillColor: "transparent",
                        transition: "all 0.3s ease-in-out",
                        display: "inline-block"
                    }}
                >
                    Community Help
                </span>
            </div>

            {/* Avatar con dropdown o botón de login */}
            {isLoggedIn && userData ? (
                <div className="position-relative" ref={dropdownRef}>
                    {/* Avatar circular con la inicial del nombre */}
                    <div
                        className="bg-dark text-white rounded-circle d-flex justify-content-center align-items-center"
                        style={{ width: 40, height: 40, cursor: "pointer", userSelect: "none" }}
                        onClick={() => setDropdownOpen(prev => !prev)}
                    >
                        {userData.name[0].toUpperCase()}
                    </div>

                    {/* Dropdown — visible cuando dropdownOpen es true */}
                    {dropdownOpen && (
                        <div
                            className="position-absolute shadow bg-white rounded p-2"
                            style={{ top: "50px", right: 0, zIndex: 100, minWidth: "160px" }}
                        >
                            {/* Nombre y email del usuario */}
                            <div className="px-2 py-1 border-bottom mb-1">
                                <div className="fw-medium small">{userData.name}</div>
                                <div className="text-muted" style={{ fontSize: "0.75rem" }}>
                                    {userData.email}
                                </div>
                            </div>

                            {/* Verificar email — solo si no está verificado */}
                            {!userData.emailVerified && (
                                <div
                                    className="dropdown-item py-1 px-2 small"
                                    style={{ cursor: "pointer" }}
                                    onClick={() => { navigate("/email-verify"); setDropdownOpen(false); }}
                                >
                                    ✉️ Verificar email
                                </div>
                            )}

                            <div className="dropdown-item py-1 px-2 small" style={{ cursor: "pointer" }}
                                onClick={() => { navigate("/profile"); setDropdownOpen(false); }}>
                                👤 Mi perfil
                            </div>

                            <div className="dropdown-item py-1 px-2 small" style={{ cursor: "pointer" }}
                                onClick={() => { navigate("/profile/volunteer"); setDropdownOpen(false); }}>
                                🙋 Perfil de voluntario
                            </div>

                            <div className="dropdown-item py-1 px-2 small" style={{ cursor: "pointer" }}
                                onClick={() => { navigate("/chat"); setDropdownOpen(false); }}>
                                💬 Mis conversaciones
                            </div>

                            <div
                                className="dropdown-item py-1 px-2 text-danger small"
                                style={{ cursor: "pointer" }}
                                onClick={handleLogout}
                            >
                                → Cerrar sesión
                            </div>
                        </div>
                    )}
                </div>
            ) : (
                <button
                    className="btn rounded-pill px-3 py-2 d-flex align-items-center"
                    onClick={() => navigate("/login")}
                    style={{
                        background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
                        color: "white",
                        border: "none",
                        boxShadow: "0 2px 8px rgba(102, 126, 234, 0.3)",
                        transition: "all 0.3s ease"
                    }}
                    onMouseOver={(e) => (e.currentTarget.style.transform = "translateX(4px)")}
                    onMouseOut={(e) => (e.currentTarget.style.transform = "translateX(0)")}
                >
                    Login <i className="bi bi-arrow-right ms-2"></i>
                </button>
            )}
        </nav>
    );
};

export default Menubar;