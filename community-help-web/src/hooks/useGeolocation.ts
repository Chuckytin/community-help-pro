import { useEffect, useState } from "react";

interface GeolocationState {
    lat: number | null;
    lon: number | null;
    error: string | null;
    loading: boolean;
}

/**
 * Hook que obtiene la ubicación GPS del usuario.
 * - loading: true mientras el navegador está obteniendo la posición
 * - error: mensaje si el usuario denegó el permiso o falló
 * - lat/lon: coordenadas una vez obtenidas
 *
 * Se usa en el Home para centrar el mapa y buscar solicitudes cercanas.
 */
export const useGeolocation = () => {
    const [state, setState] = useState<GeolocationState>({
        lat: null,
        lon: null,
        error: null,
        loading: true,
    });

    useEffect(() => {
        if (!navigator.geolocation) {
            // eslint-disable-next-line react-hooks/set-state-in-effect
            setState(s => ({ ...s, loading: false, error: "Tu navegador no soporta geolocalización." }));
            return;
        }

        navigator.geolocation.getCurrentPosition(
            position => {
                setState({
                    lat: position.coords.latitude,
                    lon: position.coords.longitude,
                    error: null,
                    loading: false,
                });
            },
            () => {
                // Si el usuario deniega, usamos Gijón como fallback
                setState({
                    lat: 43.5322,
                    lon: -5.6611,
                    error: "Usando ubicación por defecto (Gijón).",
                    loading: false,
                });
            },
            { enableHighAccuracy: true, timeout: 10000 }
        );
    }, []);

    return state;
};