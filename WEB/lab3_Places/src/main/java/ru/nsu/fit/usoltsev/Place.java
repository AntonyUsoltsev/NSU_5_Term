package ru.nsu.fit.usoltsev;

import lombok.Getter;
import org.json.simple.JSONObject;

import java.nio.charset.StandardCharsets;

public class Place{
    String country;
    String osm_key;
    String osm_value;
    String city;
    String street;
    String name;
    @Getter
    double lattitude;
    @Getter
    double longitude;

    public Place(JSONObject obj) {
        country = get("country", obj);
        osm_key = get("osm_key", obj);
        osm_value = get("osm_value", obj);
        city = get("city", obj);
        street = get("street", obj);
        name = get("name", obj);
        lattitude = (double) ((JSONObject) obj.get("point")).get("lat");
        longitude = (double) ((JSONObject) obj.get("point")).get("lng");
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
