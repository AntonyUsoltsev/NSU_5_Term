package ru.nsu.fit.usoltsev;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
public class LocationFinder {

    private final String placeName;

    private final int limit = 10;

    private final String locationApiKey = "5c35b29b-7677-4f44-8f3a-7856e989fe22";
    private final String weatherApiKey = "889f9ae3b7234d977bf3f9fa86fbce18";


    public LocationFinder(String placeName) {
        this.placeName = placeName;
    }

    public void start() {
        try {
            CompletableFuture<String> locationFuture = getLocationVariants(placeName);
            String locations = locationFuture.get();
            Place[] places = parseLocations(locations);
            System.out.println("Insert number of selected place: ");
            System.out.flush();
            Scanner scanner = new Scanner(System.in);
            int index = scanner.nextInt();
            while (index < 1 || index > places.length) {
                System.out.println("Insert correct number of selected place: ");
                index = scanner.nextInt();
            }
            System.out.println(places[index - 1]);
            CompletableFuture<String> weatherFuture = getWeather(places[index - 1]);
            String stringWeather = weatherFuture.get();
            Weather weather = parseWeather(stringWeather);
            System.out.println(weather);


            return;
        } catch (IOException | ParseException | InterruptedException | ExecutionException ioExc) {

            log.warn(new String(ioExc.getMessage().getBytes(StandardCharsets.UTF_8)));
            throw new RuntimeException(ioExc);
        }
    }


    public CompletableFuture<String> getLocationVariants(String placeName) throws IOException, ParseException {

        OkHttpClient client = new OkHttpClient();
        String url = "https://graphhopper.com/api/1/geocode?q=" + placeName +
                "&key=" + locationApiKey +
                "&limit=" + limit;

        System.out.println(new String(url.getBytes(StandardCharsets.UTF_8)));
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        CompletableFuture<String> resultFuture = new CompletableFuture<>();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                resultFuture.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull okhttp3.Call call, @NotNull Response response) {
                try {
                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        String responseBody = response.body().string();
                        resultFuture.complete(responseBody);
                    } else {
                        resultFuture.completeExceptionally(new Exception("HTTP request fail"));
                    }
                } catch (IOException e) {
                    resultFuture.completeExceptionally(e);
                }
            }
        });
        return resultFuture;

    }

    public Place[] parseLocations(String json) throws ParseException {
        System.out.println(new String(json.getBytes(StandardCharsets.UTF_8)));
        JSONParser parser = new JSONParser();

        JSONObject jsonObj = (JSONObject) parser.parse(json);
        JSONArray ja = (JSONArray) jsonObj.get("hits");
        Place[] places = new Place[ja.size()];

        for (int i = 0; i < ja.size(); i++) {
            JSONObject obj = (JSONObject) ja.get(i);
            places[i] = new Place(obj);
            String str = places[i].toString();
            System.out.println((i + 1) + ": " + str);
        }
        return places;
    }
    public Weather parseWeather(String json) throws ParseException {
        System.out.println(new String(json.getBytes(StandardCharsets.UTF_8)));
        JSONParser parser = new JSONParser();
        JSONObject jsonObj = (JSONObject) parser.parse(json);
        return new Weather(jsonObj);
    }

    public CompletableFuture<String> getWeather(Place selectPlace) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.openweathermap.org/data/2.5/weather?" +
                "lat=" + selectPlace.getLattitude() +
                "&lon=" + selectPlace.getLongitude() +
                "&units=metric" +
                "&appid=" + weatherApiKey;

        System.out.println(new String(url.getBytes(StandardCharsets.UTF_8)));
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        CompletableFuture<String> resultFuture = new CompletableFuture<>();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                resultFuture.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull okhttp3.Call call, @NotNull Response response) {
                try {
                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        String responseBody = response.body().string();
                        resultFuture.complete(responseBody);
                    } else {
                        resultFuture.completeExceptionally(new Exception("HTTP request fail"));
                    }
                } catch (IOException e) {
                    resultFuture.completeExceptionally(e);
                }
            }
        });
        return resultFuture;

    }

}
