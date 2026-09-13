package com.clearly.store.catalog.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DeliveryEstimateService {
    private static final String PINCODE_API = "https://api.pincodeapi.in/api/v1/pincode/";
    private static final Duration CACHE_LIFETIME = Duration.ofHours(24);
    private static final DateTimeFormatter DATE_LABEL = DateTimeFormatter.ofPattern("d MMMM", Locale.ENGLISH);

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final Map<String, CachedLocation> locationCache = new ConcurrentHashMap<>();
    private final int minimumBusinessDays;
    private final int maximumBusinessDays;

    public DeliveryEstimateService(
        ObjectMapper objectMapper,
        @Value("${delivery.estimate.minimum-business-days:4}") int minimumBusinessDays,
        @Value("${delivery.estimate.maximum-business-days:7}") int maximumBusinessDays
    ) {
        this.objectMapper = objectMapper;
        this.minimumBusinessDays = minimumBusinessDays;
        this.maximumBusinessDays = maximumBusinessDays;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build();
    }

    public Map<String, Object> estimate(String pincode) {
        String normalized = pincode == null ? "" : pincode.trim();
        if (!normalized.matches("[1-9][0-9]{5}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid 6-digit Indian PIN code");
        }

        Location location = findLocation(normalized);
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        LocalDate earliest = addBusinessDays(today, minimumBusinessDays);
        LocalDate latest = addBusinessDays(today, maximumBusinessDays);

        Map<String, Object> place = new LinkedHashMap<>();
        place.put("district", location.district());
        place.put("state", location.state());

        Map<String, Object> window = new LinkedHashMap<>();
        window.put("from", earliest.toString());
        window.put("to", latest.toString());
        window.put("label", earliest.format(DATE_LABEL) + " – " + latest.format(DATE_LABEL));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("pincode", normalized);
        response.put("serviceable", true);
        response.put("location", place);
        response.put("estimatedDelivery", window);
        response.put("businessDays", Map.of("minimum", minimumBusinessDays, "maximum", maximumBusinessDays));
        response.put("source", "PincodeAPI");
        response.put("estimated", true);
        response.put("disclaimer", "Estimated date. Final timing depends on stock, order time and courier serviceability.");
        return response;
    }

    private Location findLocation(String pincode) {
        CachedLocation cached = locationCache.get(pincode);
        if (cached != null && !cached.expired()) return cached.location();

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(PINCODE_API + pincode))
                .timeout(Duration.ofSeconds(7))
                .header("Accept", "application/json")
                .header("User-Agent", "Clearly-Store/1.0")
                .GET()
                .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "We could not find that PIN code");
            }
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "PIN code verification is temporarily unavailable");
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode offices = root.path("data").path("post_offices");
            if (!root.path("success").asBoolean(false) || !offices.isArray() || offices.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "We could not find that PIN code");
            }

            JsonNode office = offices.get(0);
            Location location = new Location(office.path("district").asText(""), office.path("state").asText(""));
            locationCache.put(pincode, new CachedLocation(location, System.currentTimeMillis()));
            return location;
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "PIN code verification is temporarily unavailable");
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "PIN code verification is temporarily unavailable");
        }
    }

    private LocalDate addBusinessDays(LocalDate date, int days) {
        LocalDate result = date;
        int added = 0;
        while (added < days) {
            result = result.plusDays(1);
            if (result.getDayOfWeek() != DayOfWeek.SUNDAY) added++;
        }
        return result;
    }

    private record Location(String district, String state) {}

    private record CachedLocation(Location location, long createdAt) {
        boolean expired() {
            return System.currentTimeMillis() - createdAt > CACHE_LIFETIME.toMillis();
        }
    }
}
