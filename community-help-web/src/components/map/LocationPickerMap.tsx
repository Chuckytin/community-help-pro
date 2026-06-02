import { useEffect } from "react";
import { MapContainer, TileLayer, Marker, useMapEvents, useMap } from "react-leaflet";
import L from "leaflet";

// eslint-disable-next-line @typescript-eslint/no-explicit-any
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
    iconRetinaUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png",
    iconUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png",
    shadowUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png",
});

interface LocationPickerMapProps {
    lat: number;
    lon: number;
    /** Se llama cada vez que el usuario hace clic en el mapa */
    onChange: (lat: number, lon: number) => void;
    height?: string;
}

/**
 * Subcomponente que captura clics en el mapa y llama a onChange.
 * Debe estar dentro de MapContainer para usar useMapEvents.
 */
const ClickHandler = ({ onChange }: { onChange: (lat: number, lon: number) => void }) => {
    useMapEvents({
        click(e) {
            onChange(e.latlng.lat, e.latlng.lng);
        },
    });
    return null;
};

/** Mueve el mapa al marcador cuando cambian las coordenadas */
const RecenterMap = ({ lat, lon }: { lat: number; lon: number }) => {
    const map = useMap();
    useEffect(() => { map.setView([lat, lon], map.getZoom()); }, [lat, lon, map]);
    return null;
};

/**
 * Mapa interactivo para seleccionar ubicación en formularios.
 * El usuario hace clic donde quiere colocar la solicitud/donación.
 * Muestra el marcador en la posición seleccionada.
 */
const LocationPickerMap = ({ lat, lon, onChange, height = "300px" }: LocationPickerMapProps) => {
    return (
        <MapContainer
            center={[lat, lon]}
            zoom={14}
            style={{ height, width: "100%", borderRadius: "8px", cursor: "crosshair" }}
        >
            <TileLayer
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
            />
            <RecenterMap lat={lat} lon={lon} />
            <ClickHandler onChange={onChange} />
            <Marker position={[lat, lon]} />
        </MapContainer>
    );
};

export default LocationPickerMap;