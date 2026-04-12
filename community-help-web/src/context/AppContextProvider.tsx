import { useEffect, useState } from "react";
import axios from "axios";
import { toast } from "react-toastify";
import { AppContext, UserData } from "./AppContext";
import { AppConstants } from "../util/constants";

export const AppContextProvider = ({
  children,
}: {
  children: React.ReactNode;
}) => {
  const backendURL = AppConstants.BACKEND_URL;
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userData, setUserData] = useState<UserData | null>(null);
  const [token, setToken] = useState<string | null>(null);

  /**
   * Cada vez que el token cambia, lo inyecta en el header Authorization de axios.
   * Esto cubre todas las llamadas posteriores al login, pero NO la llamada
   * inmediata que sigue al setToken (React actualiza el estado de forma asíncrona).
   * Para ese caso usa getUserDataWithToken.
   */
  useEffect(() => {
    if (token) {
      axios.defaults.headers.common["Authorization"] = `Bearer ${token}`;
    } else {
      delete axios.defaults.headers.common["Authorization"];
    }
  }, [token]);

  /**
   * Obtiene el perfil del usuario usando el token ya inyectado en axios.
   * Úsala cuando el token ya está en el header (llamadas posteriores al login).
   */
  const getUserData = async () => {
    try {
      const response = await axios.get<UserData>(`${backendURL}/users/me`);
      setUserData(response.data);
    } catch {
      toast.error("No se pudo obtener el perfil del usuario.");
    }
  };

  /**
   * Obtiene el perfil del usuario pasando el token explícitamente.
   * Úsala justo después del login/register, antes de que el useEffect
   * haya tenido tiempo de inyectar el token en axios.
   */
  const getUserDataWithToken = async (jwt: string) => {
    try {
      const response = await axios.get<UserData>(`${backendURL}/users/me`, {
        headers: { Authorization: `Bearer ${jwt}` },
      });
      setUserData(response.data);
    } catch {
      toast.error("No se pudo obtener el perfil del usuario.");
    }
  };

  const logout = () => {
    setToken(null);
    setIsLoggedIn(false);
    setUserData(null);
  };

  return (
    <AppContext.Provider
      value={{
        backendURL,
        isLoggedIn,
        setIsLoggedIn,
        userData,
        setUserData,
        token,
        setToken,
        getUserData,
        getUserDataWithToken,
        logout,
      }}
    >
      {children}
    </AppContext.Provider>
  );
};
