import { useEffect } from "react";
import { MapContainer, TileLayer, Marker, Popup, useMap } from "react-leaflet";
import L from "leaflet";
import { useNavigate } from "react-router-dom";
import { HelpRequest } from "../../types/helpRequest";
import { Donation } from "../../types/donation";

// Fix del icono por defecto de Leaflet en Vite
// eslint-disable-next-line @typescript-eslint/no-explicit-any
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
    iconRetinaUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png",
    iconUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png",
    shadowUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png",
});

/** Marcador azul — posición del usuario */
const userIcon = L.divIcon({
    className: "",
    html: `<div style="width:16px;height:16px;background:#667eea;
        border:3px solid white;border-radius:50%;
        box-shadow:0 2px 6px rgba(0,0,0,0.4)"></div>`,
    iconSize: [16, 16], iconAnchor: [8, 8],
});

/** Marcador morado — solicitudes de ayuda */
const helpRequestIcon = L.divIcon({
    className: "",
    html: `<div style="width:14px;height:14px;background:#764ba2;
        border:2px solid white;border-radius:50%;
        box-shadow:0 2px 4px rgba(0,0,0,0.3)"></div>`,
    iconSize: [14, 14], iconAnchor: [7, 7],
});

/** Marcador verde — donaciones */
const donationIcon = L.divIcon({
    className: "",
    html: `<div style="width:14px;height:14px;background:#28a745;
        border:2px solid white;border-radius:50%;
        box-shadow:0 2px 4px rgba(0,0,0,0.3)"></div>`,
    iconSize: [14, 14], iconAnchor: [7, 7],
});

/** Recentra el mapa cuando cambian las coordenadas */
const RecenterMap = ({ lat, lon }: { lat: number; lon: number }) => {
    const map = useMap();
    useEffect(() => { map.setView([lat, lon], 13); }, [lat, lon, map]);
    return null;
};

interface CommunityMapProps {
    userLat: number;
    userLon: number;
    /** ID del usuario para filtrar sus propias creaciones */
    currentUserId: string;
    helpRequests: HelpRequest[];
    donations: Donation[];
    height?: string;
}

/**
 * Mapa unificado para solicitudes de ayuda y donaciones.
 * - Marcador azul: posición del usuario
 * - Marcadores morados: solicitudes de ayuda (filtra las propias del usuario)
 * - Marcadores verdes: donaciones (filtra las propias del usuario)
 *
 * El filtrado en frontend es una capa extra de seguridad —
 * el backend ya filtra por estado pero no por autoría.
 */
const CommunityMap = ({
    userLat, userLon, currentUserId,
    helpRequests, donations, height = "400px"
}: CommunityMapProps) => {
    const navigate = useNavigate();

    const formatTime = (s: number) => {
        const m = Math.round(s / 60);
        return m < 60 ? `${m} min` : `${Math.floor(m / 60)}h ${m % 60}min`;
    };

    const formatDist = (m: number) =>
        m < 1000 ? `${Math.round(m)} m` : `${(m / 1000).toFixed(1)} km`;

    // Filtra las solicitudes que NO son del usuario actual
    const filteredRequests = helpRequests.filter(r => r.requesterId !== currentUserId);
    const filteredDonations = donations.filter(d => d.donorId !== currentUserId);

    return (
        <MapContainer
            center={[userLat, userLon]}
            zoom={13}
            style={{ height, width: "100%", borderRadius: "8px" }}
        >
            <TileLayer
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
            />
            <RecenterMap lat={userLat} lon={userLon} />

            {/* Posición del usuario */}
            <Marker position={[userLat, userLon]} icon={userIcon}>
                <Popup><span className="fw-semibold">Tu ubicación</span></Popup>
            </Marker>

            {/* Solicitudes de ayuda */}
            {filteredRequests
                .filter(r => r.latitude !== null && r.longitude !== null)
                .map(r => (
                    <Marker key={r.id} position={[r.latitude!, r.longitude!]} icon={helpRequestIcon}>
                        <Popup>
                            <div style={{ minWidth: 160 }}>
                                <p className="fw-semibold mb-1" style={{ color: "#764ba2" }}>
                                    🙋 {r.title}
                                </p>
                                <p className="text-muted small mb-2">{r.type}</p>
                                {r.estimatedDistanceMeters && (
                                    <p className="small mb-2">
                                        📍 {formatDist(r.estimatedDistanceMeters)}
                                        {r.estimatedTravelSeconds && (
                                            <> · ⏱ {formatTime(r.estimatedTravelSeconds)}</>
                                        )}
                                    </p>
                                )}
                                <button className="btn btn-sm w-100" style={{
                                    background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
                                    color: "white", border: "none", borderRadius: 6, fontSize: "0.75rem"
                                }} onClick={() => navigate(`/help-requests/${r.id}`)}>
                                    Ver detalle
                                </button>
                            </div>
                        </Popup>
                    </Marker>
                ))}

            {/* Donaciones */}
            {filteredDonations
                .filter(d => d.latitude !== null && d.longitude !== null)
                .map(d => (
                    <Marker key={d.id} position={[d.latitude!, d.longitude!]} icon={donationIcon}>
                        <Popup>
                            <div style={{ minWidth: 160 }}>
                                <p className="fw-semibold mb-1" style={{ color: "#28a745" }}>
                                    🎁 {d.title}
                                </p>
                                <p className="text-muted small mb-2">
                                    {d.donationType}
                                    {d.quantity && d.unit && ` · ${d.quantity} ${d.unit}`}
                                </p>
                                {d.estimatedDistanceMeters && (
                                    <p className="small mb-2">
                                        📍 {formatDist(d.estimatedDistanceMeters)}
                                        {d.estimatedTravelSeconds && (
                                            <> · ⏱ {formatTime(d.estimatedTravelSeconds)}</>
                                        )}
                                    </p>
                                )}
                                <button className="btn btn-sm w-100" style={{
                                    background: "linear-gradient(135deg, #28a745 0%, #20c997 100%)",
                                    color: "white", border: "none", borderRadius: 6, fontSize: "0.75rem"
                                }} onClick={() => navigate(`/donations/${d.id}`)}>
                                    Ver detalle
                                </button>
                            </div>
                        </Popup>
                    </Marker>
                ))}
        </MapContainer>
    );
};

export default CommunityMap;