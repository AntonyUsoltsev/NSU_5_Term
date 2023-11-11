package ru.nsu.fit.usoltsev;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.nio.charset.StandardCharsets;

public class InterestingPlace {
    private String name;
    @Getter
    private String xid;
    private double latitude;
    private double longitude;
    private String info;

    public InterestingPlace(@NotNull JSONObject feature) {

        String checkName = (String) ((JSONObject) feature.get("properties")).get("name");
        if (!checkName.isBlank()) {
            name = checkName;
            xid = (String) ((JSONObject) feature.get("properties")).get("xid");
            longitude = (double) ((JSONArray) ((JSONObject) feature.get("geometry")).get("coordinates")).get(0);
            latitude = (double) ((JSONArray) ((JSONObject) feature.get("geometry")).get("coordinates")).get(1);
        }
    }

    public void setInfo(@NotNull JSONObject obj) {
        if (obj.get("wikipedia_extracts") != null) {
            info = (String) ((JSONObject) obj.get("wikipedia_extracts")).get("text");
        }
        else{
            info = "Information absent";
        }

    }

    @Override
    public String toString() {
        return new String(String.format("""
                        Interesting place: %s
                            Latitude: %.3f, longitude: %.3f
                            Info: %s
                        """,
                name, latitude, longitude, info).getBytes(StandardCharsets.UTF_8));
    }
}
