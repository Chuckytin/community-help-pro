package com.communityhelp.app.common.openroute.service;

import com.communityhelp.app.common.openroute.dto.TravelTimeResponse;
import com.communityhelp.app.common.openroute.model.TransportMode;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Locale;

/**
 * Cliente de infraestructura encargado de comunicarse con la API OpenRouteService.
 * Obtiene rutas, distancias y tiempos de viaje.
 */
@Service
@RequiredArgsConstructor
public class OpenRouteService {

    @Value("${openroute.api.key}")
    private String apiKey;

    @Value("${openroute.api.path-url}")
    private String apiPathUrl;

    private final RestClient openRouteRestClient;

    /**
     * Solicita a OpenRouteService la distancia y duración entre dos puntos.
     */
    public TravelTimeResponse getTravelTime(Point from, Point to, TransportMode mode) {

        String url = buildApiUrl(
                from.getY(), from.getX(),
                to.getY(), to.getX(),
                mode
        );

        String response = openRouteRestClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);

        return parseResponse(response);
    }

    /**
     * Construye la URL final de la petición a la API.
     */
    private String buildApiUrl(double fromLat, double fromLon,
                               double toLat, double toLon,
                               TransportMode mode) {

        return String.format(
                Locale.US,
                apiPathUrl,
                mode.apiValue,
                apiKey,
                fromLon, fromLat,
                toLon, toLat
        );
    }

    /**
     * Parsea la respuesta JSON devuelta por OpenRouteService.
     */
    private TravelTimeResponse parseResponse(String jsonResponse) {

        JSONObject json = new JSONObject(jsonResponse);

        JSONObject summary = json.getJSONArray("features")
                .getJSONObject(0)
                .getJSONObject("properties")
                .getJSONObject("summary");

        return new TravelTimeResponse(
                summary.getDouble("distance"),
                summary.getDouble("duration")
        );
    }

}