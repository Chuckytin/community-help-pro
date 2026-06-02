import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { toast } from "react-toastify";
import Menubar from "../components/Menubar";
import CommunityMap from "../components/map/HelpRequestMap";
import { useAppContext } from "../hooks/useAppContext";
import { useGeolocation } from "../hooks/useGeolocation";
import { HelpRequest, PageResponse } from "../types/helpRequest";
import { Donation } from "../types/donation";

/** Palanca entre solicitudes de ayuda y donaciones */
type MapView = "help-requests" | "donations" | "both";

const Home = () => {
    const navigate = useNavigate();
    const { userData, isLoggedIn, backendURL } = useAppContext();
    const { lat, lon, error: geoError, loading: geoLoading } = useGeolocation();

    const [helpRequests, setHelpRequests] = useState<HelpRequest[]>([]);
    const [donations, setDonations] = useState<Donation[]>([]);
    const [loadingRequests, setLoadingRequests] = useState(false);
    const [loadingDonations, setLoadingDonations] = useState(false);
    const [radiusKm, setRadiusKm] = useState(5);

    /**
     * Palanca de vista del mapa:
     * - "help-requests": solo solicitudes de ayuda (morado)
     * - "donations": solo donaciones (verde)
     * - "both": ambas
     */
    const [mapView, setMapView] = useState<MapView>("both");

    /** Carga solicitudes de ayuda cercanas */
    useEffect(() => {
        if (!lat || !lon || !isLoggedIn) return;
        if (mapView === "donations") return; // No carga si no se muestran

        const fetch = async () => {
            setLoadingRequests(true);
            try {
                const { data } = await axios.get<PageResponse<HelpRequest>>(
                    `${backendURL}/help-requests/nearby`,
                    { params: { lat, lon, radiusMeters: radiusKm * 1000, size: 50 } }
                );
                setHelpRequests(data.content);
            } catch {
                toast.error("No se pudieron cargar las solicitudes cercanas.");
            } finally {
                setLoadingRequests(false);
            }
        };
        fetch();
    }, [lat, lon, radiusKm, isLoggedIn, mapView, backendURL]);

    /** Carga donaciones cercanas */
    useEffect(() => {
        if (!lat || !lon || !isLoggedIn) return;
        if (mapView === "help-requests") return; // No carga si no se muestran

        const fetch = async () => {
            setLoadingDonations(true);
            try {
                const { data } = await axios.get<PageResponse<Donation>>(
                    `${backendURL}/donations/nearby`,
                    { params: { lat, lon, radiusMeters: radiusKm * 1000, size: 50 } }
                );
                setDonations(data.content);
            } catch {
                toast.error("No se pudieron cargar las donaciones cercanas.");
            } finally {
                setLoadingDonations(false);
            }
        };
        fetch();
    }, [lat, lon, radiusKm, isLoggedIn, mapView, backendURL]);

    const formatTime = (s: number) => {
        const m = Math.round(s / 60);
        return m < 60 ? `${m} min` : `${Math.floor(m / 60)}h ${m % 60}min`;
    };

    const formatDist = (m: number) =>
        m < 1000 ? `${Math.round(m)} m` : `${(m / 1000).toFixed(1)} km`;

    // Elementos visibles según la palanca (filtrando los propios)
    const visibleRequests = mapView !== "donations"
        ? helpRequests.filter(r => r.requesterId !== userData?.id)
        : [];
    const visibleDonations = mapView !== "help-requests"
        ? donations.filter(d => d.donorId !== userData?.id)
        : [];

    const totalVisible = visibleRequests.length + visibleDonations.length;
    const isLoading = loadingRequests || loadingDonations;

    return (
        <div className="d-flex flex-column min-vh-100" style={{ background: "#f8f9fa" }}>
            <Menubar />

            <div className="container py-4">
                {!isLoggedIn ? (
                    <div className="text-center py-5">
                        <span style={{ fontSize: "4rem" }}>🤝</span>
                        <h1 className="fw-bold mt-3 mb-2" style={{
                            background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
                            WebkitBackgroundClip: "text", WebkitTextFillColor: "transparent"
                        }}>Community Help</h1>
                        <p className="text-muted fs-5 mb-4">
                            Conectamos personas que quieren ayudar con quienes lo necesitan
                        </p>
                        <button className="btn rounded-pill px-4 py-2 fw-medium" style={{
                            background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
                            color: "white", border: "none"
                        }} onClick={() => navigate("/login")}>
                            Empezar →
                        </button>
                    </div>
                ) : (
                    <div className="row g-4">

                        {/* ── Columna izquierda: perfil ── */}
                        <div className="col-md-3">
                            <div className="card shadow-sm" style={{ borderRadius: 12 }}>
                                <div className="card-body text-center py-4">
                                    <div className="rounded-circle d-flex align-items-center justify-content-center mx-auto mb-3"
                                        style={{
                                            width: 64, height: 64,
                                            background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
                                            fontSize: "1.5rem", color: "white", fontWeight: 700
                                        }}>
                                        {userData?.name[0].toUpperCase()}
                                    </div>
                                    <h6 className="fw-bold mb-0">{userData?.name}</h6>
                                    <p className="text-muted small mb-3">{userData?.email}</p>
                                    {!userData?.emailVerified && (
                                        <button className="btn btn-sm w-100 mb-2" style={{
                                            background: "rgba(102,126,234,0.1)",
                                            border: "1px solid rgba(102,126,234,0.3)",
                                            color: "#667eea", borderRadius: 8, fontSize: "0.8rem"
                                        }} onClick={() => navigate("/email-verify")}>
                                            ⚠️ Verificar email
                                        </button>
                                    )}
                                </div>
                                <div className="list-group list-group-flush" style={{ borderRadius: "0 0 12px 12px" }}>
                                    <button className="list-group-item list-group-item-action d-flex align-items-center gap-2"
                                        onClick={() => navigate("/help-requests/new")}>
                                        <span>🙋</span> Nueva solicitud
                                    </button>
                                    <button className="list-group-item list-group-item-action d-flex align-items-center gap-2"
                                        onClick={() => navigate("/donations/new")}>
                                        <span>🎁</span> Nueva donación
                                    </button>
                                    <button className="list-group-item list-group-item-action d-flex align-items-center gap-2"
                                        onClick={() => navigate("/help-requests/me")}>
                                        <span>📋</span> Mis solicitudes
                                    </button>
                                    <button className="list-group-item list-group-item-action d-flex align-items-center gap-2"
                                        onClick={() => navigate("/donations/me")}>
                                        <span>📦</span> Mis donaciones
                                    </button>
                                    <button className="list-group-item list-group-item-action d-flex align-items-center gap-2"
                                        onClick={() => navigate("/volunteer/assigned")}>
                                        <span>✅</span> Como voluntario
                                    </button>
                                </div>
                            </div>
                        </div>

                        {/* ── Columna derecha: controles + mapa + lista ── */}
                        <div className="col-md-9">

                            {/* Controles: palanca de vista + radio */}
                            <div className="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">

                                {/* Palanca de vista */}
                                <div className="d-flex gap-1" style={{
                                    background: "white", borderRadius: 20, padding: "3px",
                                    border: "1px solid #e2e8f0"
                                }}>
                                    {([
                                        { key: "both", label: "🗺 Todo", count: totalVisible },
                                        { key: "help-requests", label: "🙋 Solicitudes", count: visibleRequests.length },
                                        { key: "donations", label: "🎁 Donaciones", count: visibleDonations.length },
                                    ] as { key: MapView; label: string; count: number }[]).map(({ key, label, count }) => (
                                        <button key={key}
                                            className="btn btn-sm d-flex align-items-center gap-1"
                                            style={{
                                                borderRadius: 16, fontSize: "0.8rem",
                                                background: mapView === key
                                                    ? "linear-gradient(135deg, #667eea 0%, #764ba2 100%)"
                                                    : "transparent",
                                                color: mapView === key ? "white" : "#4a5568",
                                                border: "none", padding: "4px 12px"
                                            }}
                                            onClick={() => setMapView(key)}>
                                            {label}
                                            {!isLoading && (
                                                <span style={{
                                                    background: mapView === key
                                                        ? "rgba(255,255,255,0.25)"
                                                        : "rgba(102,126,234,0.15)",
                                                    color: mapView === key ? "white" : "#667eea",
                                                    borderRadius: 10, fontSize: "0.7rem",
                                                    padding: "0 6px", minWidth: 20, textAlign: "center"
                                                }}>
                                                    {count}
                                                </span>
                                            )}
                                        </button>
                                    ))}
                                </div>

                                {/* Selector de radio */}
                                <div className="d-flex align-items-center gap-1">
                                    <span className="text-muted small">Radio:</span>
                                    {[2, 5, 10, 20].map(km => (
                                        <button key={km} className="btn btn-sm" style={{
                                            borderRadius: 20, fontSize: "0.75rem",
                                            background: radiusKm === km
                                                ? "linear-gradient(135deg, #667eea 0%, #764ba2 100%)"
                                                : "white",
                                            color: radiusKm === km ? "white" : "#4a5568",
                                            border: "1px solid #e2e8f0"
                                        }} onClick={() => setRadiusKm(km)}>
                                            {km} km
                                        </button>
                                    ))}
                                </div>
                            </div>

                            {/* Leyenda del mapa */}
                            <div className="d-flex gap-3 mb-2" style={{ fontSize: "0.75rem", color: "#4a5568" }}>
                                <span>🔵 Tu ubicación</span>
                                {mapView !== "donations" && <span>🟣 Solicitudes de ayuda</span>}
                                {mapView !== "help-requests" && <span>🟢 Donaciones</span>}
                            </div>

                            {/* Mapa */}
                            <div className="card shadow-sm mb-4" style={{ borderRadius: 12, overflow: "hidden" }}>
                                {geoLoading ? (
                                    <div className="d-flex align-items-center justify-content-center p-5">
                                        <div className="spinner-border me-2" style={{ color: "#667eea" }} />
                                        <span className="text-muted">Obteniendo ubicación...</span>
                                    </div>
                                ) : (
                                    <>
                                        {geoError && (
                                            <div className="alert alert-warning m-2 mb-0 py-2 small">
                                                📍 {geoError}
                                            </div>
                                        )}
                                        <CommunityMap
                                            userLat={lat!}
                                            userLon={lon!}
                                            currentUserId={userData?.id ?? ""}
                                            helpRequests={mapView !== "donations" ? helpRequests : []}
                                            donations={mapView !== "help-requests" ? donations : []}
                                            height="380px"
                                        />
                                    </>
                                )}
                            </div>

                            {/* Lista unificada de elementos cercanos */}
                            {isLoading ? (
                                <div className="text-center py-3">
                                    <div className="spinner-border spinner-border-sm me-2" style={{ color: "#667eea" }} />
                                    <span className="text-muted small">Buscando...</span>
                                </div>
                            ) : totalVisible === 0 ? (
                                <div className="text-center py-4 text-muted">
                                    <span style={{ fontSize: "2rem" }}>🔍</span>
                                    <p className="mt-2 mb-0">Nada en un radio de {radiusKm} km</p>
                                    <p className="small">Prueba a ampliar el radio o cambiar la vista</p>
                                </div>
                            ) : (
                                <div className="row g-3">
                                    {/* Cards de solicitudes */}
                                    {visibleRequests.slice(0, mapView === "both" ? 4 : 6).map(r => (
                                        <div key={r.id} className="col-md-6">
                                            <div className="card shadow-sm h-100"
                                                style={{ borderRadius: 10, cursor: "pointer", transition: "transform 0.15s", borderLeft: "3px solid #764ba2" }}
                                                onClick={() => navigate(`/help-requests/${r.id}`)}
                                                onMouseEnter={e => (e.currentTarget.style.transform = "translateY(-2px)")}
                                                onMouseLeave={e => (e.currentTarget.style.transform = "translateY(0)")}>
                                                <div className="card-body">
                                                    <div className="d-flex justify-content-between align-items-start mb-1">
                                                        <span className="badge" style={{
                                                            background: "rgba(118,75,162,0.1)",
                                                            color: "#764ba2", borderRadius: 20, fontSize: "0.7rem"
                                                        }}>🙋 Solicitud</span>
                                                        <span className="text-muted" style={{ fontSize: "0.7rem" }}>{r.type}</span>
                                                    </div>
                                                    <h6 className="fw-semibold mb-1" style={{ color: "#2d3748" }}>{r.title}</h6>
                                                    <p className="text-muted small mb-2" style={{
                                                        overflow: "hidden", display: "-webkit-box",
                                                        WebkitLineClamp: 2, WebkitBoxOrient: "vertical"
                                                    } as React.CSSProperties}>{r.description}</p>
                                                    {r.estimatedDistanceMeters && (
                                                        <div className="d-flex gap-3 text-muted" style={{ fontSize: "0.75rem" }}>
                                                            <span>📍 {formatDist(r.estimatedDistanceMeters)}</span>
                                                            {r.estimatedTravelSeconds && <span>⏱ {formatTime(r.estimatedTravelSeconds)}</span>}
                                                        </div>
                                                    )}
                                                </div>
                                            </div>
                                        </div>
                                    ))}

                                    {/* Cards de donaciones */}
                                    {visibleDonations.slice(0, mapView === "both" ? 4 : 6).map(d => (
                                        <div key={d.id} className="col-md-6">
                                            <div className="card shadow-sm h-100"
                                                style={{ borderRadius: 10, cursor: "pointer", transition: "transform 0.15s", borderLeft: "3px solid #28a745" }}
                                                onClick={() => navigate(`/donations/${d.id}`)}
                                                onMouseEnter={e => (e.currentTarget.style.transform = "translateY(-2px)")}
                                                onMouseLeave={e => (e.currentTarget.style.transform = "translateY(0)")}>
                                                <div className="card-body">
                                                    <div className="d-flex justify-content-between align-items-start mb-1">
                                                        <span className="badge" style={{
                                                            background: "rgba(40,167,69,0.1)",
                                                            color: "#28a745", borderRadius: 20, fontSize: "0.7rem"
                                                        }}>🎁 Donación</span>
                                                        <span className="text-muted" style={{ fontSize: "0.7rem" }}>
                                                            {d.donationType}{d.quantity && d.unit && ` · ${d.quantity} ${d.unit}`}
                                                        </span>
                                                    </div>
                                                    <h6 className="fw-semibold mb-1" style={{ color: "#2d3748" }}>{d.title}</h6>
                                                    <p className="text-muted small mb-2" style={{
                                                        overflow: "hidden", display: "-webkit-box",
                                                        WebkitLineClamp: 2, WebkitBoxOrient: "vertical"
                                                    } as React.CSSProperties}>{d.description}</p>
                                                    {d.estimatedDistanceMeters && (
                                                        <div className="d-flex gap-3 text-muted" style={{ fontSize: "0.75rem" }}>
                                                            <span>📍 {formatDist(d.estimatedDistanceMeters)}</span>
                                                            {d.estimatedTravelSeconds && <span>⏱ {formatTime(d.estimatedTravelSeconds)}</span>}
                                                        </div>
                                                    )}
                                                </div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}

                            {/* Ver más */}
                            {totalVisible > 8 && (
                                <div className="d-flex justify-content-center gap-2 mt-3">
                                    {visibleRequests.length > 4 && (
                                        <button className="btn btn-sm" style={{
                                            background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
                                            color: "white", border: "none", borderRadius: 20
                                        }} onClick={() => navigate("/help-requests")}>
                                            Ver todas las solicitudes ({visibleRequests.length}) →
                                        </button>
                                    )}
                                    {visibleDonations.length > 4 && (
                                        <button className="btn btn-sm" style={{
                                            background: "linear-gradient(135deg, #28a745 0%, #20c997 100%)",
                                            color: "white", border: "none", borderRadius: 20
                                        }} onClick={() => navigate("/donations")}>
                                            Ver todas las donaciones ({visibleDonations.length}) →
                                        </button>
                                    )}
                                </div>
                            )}
                        </div>
                    </div>
                )}
            </div>

            <div className="mt-auto" style={{
                height: 80,
                background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
                clipPath: "ellipse(100% 100% at 50% 100%)"
            }} />
        </div>
    );
};

export default Home;