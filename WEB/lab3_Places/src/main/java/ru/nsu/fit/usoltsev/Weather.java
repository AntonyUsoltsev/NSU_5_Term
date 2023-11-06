package ru.nsu.fit.usoltsev;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Weather {

    String weatherState;
    double temp;
    double tempFeelLike;
    long pressure;
    long humidity;
    double windSpeed;
    long cloud;

    public Weather(JSONObject obj) {
        weatherState = (String) ((JSONObject) ((JSONArray) obj.get("weather")).get(0)).get("main");
        temp = (double) ((JSONObject) obj.get("main")).get("temp");
        tempFeelLike = (double) ((JSONObject) obj.get("main")).get("feels_like");
        windSpeed = (double) ((JSONObject) obj.get("wind")).get("speed");
        pressure = (long) ((JSONObject) obj.get("main")).get("pressure");
        humidity = (long) ((JSONObject) obj.get("main")).get("humidity");
        cloud = (long) ((JSONObject) obj.get("clouds")).get("all");
    }

    @Override
    public String toString() {
        return String.format("""
                        Current weather: %s,
                            Temperature: %.2f C, feels like: %.2f C
                            Pressure: %d hPa
                            Humidity: %d %%
                            Wind speed: %.2f m/s
                            Cloud percent: %d %%
                        """,
                weatherState, temp, tempFeelLike, pressure, humidity, windSpeed, cloud);
    }
}
