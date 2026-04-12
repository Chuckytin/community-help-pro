import { useEffect, useRef } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { toast } from "react-toastify";
import { useAppContext } from "../../hooks/useAppContext";

/**
 * Página de callback OAuth2.
 * El backend redirige aquí tras autenticar con Google:
 *   http://localhost:5173/oauth2/callback?token=eyJ...
 *
 * Esta página:
 * 1. Lee el token de la URL
 * 2. Lo guarda en el contexto (igual que haría el login normal)
 * 3. Carga los datos del usuario
 * 4. Redirige al home
 */
const OAuth2Callback = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const { setToken, setIsLoggedIn, getUserDataWithToken } = useAppContext();
    
    /**
     * useRef evita que el efecto se ejecute dos veces en StrictMode.
     * En desarrollo React monta/desmonta/remonta los componentes para
     * detectar efectos secundarios — el ref persiste entre montajes.
     */
    const handled = useRef(false);

    useEffect(() => {
        if (handled.current) return;
        handled.current = true;

        const token = searchParams.get("token");

        if (!token) {
            toast.error("Error al iniciar sesión con Google.");
            navigate("/login");
            return;
        }

        const handleOAuth2Login = async () => {
            try {
                setToken(token);
                setIsLoggedIn(true);
                await getUserDataWithToken(token);
                toast.success("¡Bienvenido!");
                navigate("/");
            } catch {
                toast.error("Error al cargar el perfil.");
                navigate("/login");
            }
        };

        handleOAuth2Login();
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return (
        <div className="min-vh-100 d-flex justify-content-center align-items-center"
            style={{ background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)" }}>
            <div className="text-center text-white">
                <div className="spinner-border mb-3" role="status" />
                <p className="fw-medium">Iniciando sesión...</p>
            </div>
        </div>
    );
};

export default OAuth2Callback;