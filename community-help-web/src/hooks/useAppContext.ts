import { useContext } from "react";
import { AppContext } from "../context/AppContext";

/**
 * Hook para acceder al contexto global de la app.
 *
 * Uso en cualquier componente:
 *   const { userData, logout } = useAppContext();
 *
 * Es preferible a importar AppContext directamente porque:
 * - Encapsula el useContext
 * - Si en el futuro cambia la implementación, solo se toca aquí
 * - Permite añadir validaciones (ej: lanzar error si se usa fuera del Provider)
 */
export const useAppContext = () => {
    const context = useContext(AppContext);
    if (!context) {
        throw new Error("useAppContext must be used within AppContextProvider");
    }
    return context;
};