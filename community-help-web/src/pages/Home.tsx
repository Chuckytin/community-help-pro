import Menubar from "../components/Menubar";
import { useAppContext } from "../hooks/useAppContext";

/**
 * Página principal. Misma estructura que el proyecto de referencia:
 * navbar arriba, contenido centrado, onda decorativa abajo.
 */
const Home = () => {
    const { userData, isLoggedIn } = useAppContext();

    return (
        <div className="d-flex flex-column min-vh-100" style={{ background: "#f8f9fa" }}>
            <Menubar />

            {/* Contenido central */}
            <div className="d-flex flex-column justify-content-center align-items-center grow py-5">
                <div className="text-center px-3" style={{ maxWidth: "600px" }}>

                    {/* Emoji grande como sustituto del logo */}
                    <div className="mb-4" style={{ fontSize: "80px", lineHeight: 1 }}>🤝</div>

                    <h5 className="fw-semibold" style={{ color: "#667eea" }}>
                        {isLoggedIn ? `Hola, ${userData?.name}` : "Bienvenido"}{" "}
                        <span role="img" aria-label="wave">👋</span>
                    </h5>

                    <h1 className="fw-bold display-5 mb-3" style={{
                        background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
                        WebkitBackgroundClip: "text",
                        WebkitTextFillColor: "transparent"
                    }}>
                        Community Help
                    </h1>

                    <p className="text-muted fs-5 mb-4">
                        Conectamos a personas que quieren ayudar con quienes lo necesitan.
                        Donaciones, voluntariado y ayuda vecinal en tu barrio.
                    </p>

                    {/* Badge de email no verificado */}
                    {isLoggedIn && userData && !userData.emailVerified && (
                        <div className="alert d-inline-flex align-items-center gap-2 mb-4 py-2 px-3"
                            style={{
                                background: "rgba(102, 126, 234, 0.1)",
                                border: "1px solid rgba(102, 126, 234, 0.3)",
                                borderRadius: "8px",
                                color: "#667eea",
                                fontSize: "0.9rem"
                            }}>
                            ⚠️ Verifica tu email para activar todas las funciones
                        </div>
                    )}

                    {/* Botón solo visible si no está logado */}
                    {!isLoggedIn && (
                        <button
                            className="btn rounded-pill px-4 py-2 fw-medium"
                            style={{
                                background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
                                color: "white",
                                border: "none",
                                boxShadow: "0 4px 12px rgba(102, 126, 234, 0.3)",
                                transition: "all 0.3s ease"
                            }}
                            onMouseOver={(e) => (e.currentTarget.style.transform = "translateY(-2px)")}
                            onMouseOut={(e) => (e.currentTarget.style.transform = "translateY(0)")}
                            onClick={() => window.location.href = "/login"}
                        >
                            Empezar →
                        </button>
                    )}
                </div>
            </div>

            {/* Onda decorativa inferior — igual que en el original */}
            <div style={{
                height: "100px",
                background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
                clipPath: "ellipse(100% 100% at 50% 100%)"
            }} />
        </div>
    );
};

export default Home;