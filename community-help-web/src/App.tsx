import { Routes, Route, Navigate } from "react-router-dom";
import Home from "./pages/Home";
import Login from "./pages/auth/Login";
import EmailVerify from "./pages/auth/EmailVerify";
import ResetPassword from "./pages/auth/ResetPassword";
import OAuth2Callback from "./pages/auth/OAuth2Callback";

/**
 * Componente raíz que define todas las rutas de la aplicación.
 * Cada <Route> mapea una URL a un componente de página.
 * El path="*" captura cualquier ruta no definida y redirige al home.
 */
function App() {
    return (
        <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<Login />} />
            <Route path="/email-verify" element={<EmailVerify />} />
            <Route path="/reset-password" element={<ResetPassword />} />
            <Route path="/oauth2/callback" element={<OAuth2Callback />} />
            {/* Cualquier ruta desconocida redirige al home */}
            <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
    );
}

export default App;