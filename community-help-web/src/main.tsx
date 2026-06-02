import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import "bootstrap/dist/css/bootstrap.css";
import "bootstrap/dist/js/bootstrap.bundle.js";
import "bootstrap-icons/font/bootstrap-icons.css";
import "./index.css";
import App from "./App.tsx";
import { AppContextProvider } from "./context/AppContextProvider.tsx";
import "leaflet/dist/leaflet.css";
import "./index.css";

createRoot(document.getElementById("root")!).render(
    <StrictMode>
        <BrowserRouter
            future={{
                v7_startTransition: true,
                v7_relativeSplatPath: true,
            }}
        >
            {/* AppContextProvider envuelve toda la app para que cualquier
                componente pueda acceder al estado global con useAppContext() */}
            <AppContextProvider>
                {/* ToastContainer renderiza las notificaciones toast.
                    position: esquina donde aparecen
                    autoClose: ms hasta que desaparecen solos */}
                <ToastContainer position="top-right" autoClose={3000} />
                <App />
            </AppContextProvider>
        </BrowserRouter>
    </StrictMode>
);