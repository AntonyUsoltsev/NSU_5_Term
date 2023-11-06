package ru.nsu.fit.usoltsev;

import org.json.simple.JSONObject;

import java.nio.charset.StandardCharsets;

public record Place(String country, String osm_key, String osm_value, String city, String street, String name) {
    public Place(JSONObject obj) {
        this(get("country", obj),
                get("osm_key", obj),
                get("osm_value", obj),
                get("city", obj),
                get("street", obj),
                get("name", obj));
    }

    private static String get(String key, JSONObject obj) {
        return obj.get(key) != null ? (String) obj.get(key) : "Unknown";

    }

    @Override
    public String toString() {
//        return String.format("Country: %s, class: %s, type: %s, city: %s, street: %s, name of place: %s",
//                country, osm_key, osm_value, city, street, name);
        return new String(String.format("Name of place: %s, class: %s, type: %s, country: %s, city: %s, street: %s",
                name, osm_key, osm_value, country, city, street).getBytes(StandardCharsets.UTF_8));
    }
}
