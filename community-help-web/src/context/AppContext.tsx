import { createContext } from "react";

/**
 * Datos del usuario autenticado que devuelve el backend.
 */
export interface UserData {
  id: string;
  name: string;
  email: string;
  role: "ADMIN" | "USER";
  rating?: number;
  active: boolean;
  emailVerified: boolean;
  latitude?: number | null;
  longitude?: number | null;
}

/**
 * Forma del contexto global de la app.
 * Todos los componentes que necesiten estado global lo obtendrán de aquí.
 */
export interface AppContextType {
  backendURL: string;

  /** true si hay un token válido en memoria */
  isLoggedIn: boolean;
  setIsLoggedIn: (v: boolean) => void;

  /** Datos del usuario logado, null si no hay sesión */
  userData: UserData | null;
  setUserData: (v: UserData | null) => void;

  /** Token JWT en memoria (se pierde al recargar — suficiente para MVP) */
  token: string | null;
  setToken: (t: string | null) => void;

  /** Llama al backend para refrescar los datos del usuario */
  getUserData: () => Promise<void>;

  /** Obtiene los datos del usuario usando un token específico */
  getUserDataWithToken: (jwt: string) => Promise<void>;

  /** Limpia toda la sesión */
  logout: () => void;
}

/**
 * El contexto en sí. Se inicializa vacío — AppContextProvider lo rellena.
 * El cast "as AppContextType" evita tener que poner valores por defecto
 * que nunca se usarán porque el Provider siempre envuelve la app.
 */
export const AppContext = createContext<AppContextType>({} as AppContextType);
