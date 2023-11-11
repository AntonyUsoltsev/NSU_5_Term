package ru.nsu.fit.usoltsev;

import kotlin.Pair;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@SuppressWarnings("FieldCanBeLocal")
@Slf4j
public class LocationFinder {
    private final String placeName;
    private final int limit = 10;
    private final int radius = 3000;

    public LocationFinder(String placeName) {
        this.placeName = placeName;
    }

    public void test() {

        CompletableFuture<String> locationFuture = getHttpFuture(getLocationVariantsURL(placeName));

        CompletableFuture<Void> resultFuture = locationFuture.thenApplyAsync(locations -> {
            try {
                Place[] places = parseLocations(locations);
                int index = selectedPlace(1, places.length);
                return places[index - 1];
            } catch (ParseException pe) {
                throw new RuntimeException("Error parsing places", pe);
            }
        }).thenComposeAsync(selectedPlace -> {
            System.out.println("\n" + selectedPlace + "\n");

            CompletableFuture<Weather> weatherFuture = getHttpFuture(getWeatherURL(selectedPlace))
                    .thenApplyAsync(response -> {
                        try {
                            return parseWeather(response);
                        } catch (ParseException pe) {
                            throw new RuntimeException("Error parsing weather", pe);
                        }
                    });

            CompletableFuture<InterestingPlace[]> interestingPlacesFuture = getHttpFuture(getInterestLocationURL(selectedPlace))
                    .thenApplyAsync(response -> {
                        try {
                            return parseInterestingPlace(response);
                        } catch (ParseException pe) {
                            throw new RuntimeException("Error parsing interesting places", pe);
                        }
                    })
                    .thenComposeAsync(interestingPlaces -> {
                        List<CompletableFuture<Void>> infoFutures = Arrays.stream(interestingPlaces)
                                .filter(intPlace -> intPlace.getXid() != null)
                                .map(intPlace -> getHttpFuture(getInterestLocationInfoURL(intPlace))
                                        .thenAcceptAsync(stringInterestInfo -> {
                                            try {
                                                parseInterestingPlaceInfo(stringInterestInfo, intPlace);
                                            } catch (ParseException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }))
                                .toList();

                        return CompletableFuture.allOf(infoFutures.toArray(new CompletableFuture[0]))
                                .thenApplyAsync(v -> interestingPlaces);
                    });

            return CompletableFuture.allOf(weatherFuture, interestingPlacesFuture)
                    .thenApplyAsync(v -> new Pair<>(weatherFuture.join(), interestingPlacesFuture.join()));
        }).thenAcceptAsync(pair -> {
            System.out.println(pair.getFirst());
            for (InterestingPlace intPlace : pair.getSecond()) {
                if(intPlace.getXid() != null)
                    System.out.println(intPlace);
            }

        });


        resultFuture.exceptionally(throwable -> {
            throw new RuntimeException(throwable);
        });


        resultFuture.join();


    }

    public void start() {
        try {
            CompletableFuture<String> locationFuture = getHttpFuture(getLocationVariantsURL(placeName));
            String locations = locationFuture.get();

            Place[] places = parseLocations(locations);

            int index = selectedPlace(1, places.length);

            Place selectedPlace = places[index - 1];
            System.out.println("\n" + selectedPlace + "\n");

            CompletableFuture<String> weatherFuture = getHttpFuture(getWeatherURL(selectedPlace));
            String stringWeather = weatherFuture.get();

            Weather weather = parseWeather(stringWeather);
            System.out.println(weather);

            CompletableFuture<String> interestingPlaceFeature = getHttpFuture(getInterestLocationURL(selectedPlace));
            String stringInterest = interestingPlaceFeature.get();
            InterestingPlace[] interestingPlaces = parseInterestingPlace(stringInterest);

            for (var interestingPlace : interestingPlaces) {
                try {
                    if (interestingPlace.getXid() != null) {
                        CompletableFuture<String> interestingPlaceInfoFeature = getHttpFuture(getInterestLocationInfoURL(interestingPlace));
                        String stringInterestInfo = interestingPlaceInfoFeature.get();
                        interestingPlaceInfoFeature.join();
                        parseInterestingPlaceInfo(stringInterestInfo, interestingPlace);
                        System.out.println(interestingPlace);
                    }
                } catch (Exception e) {
                    log.warn(new String(e.getMessage().getBytes(StandardCharsets.UTF_8)), e);
                }
            }
            locationFuture.join();
            weatherFuture.join();
            interestingPlaceFeature.join();


        } catch (ParseException | InterruptedException | ExecutionException ioExc) {

            log.warn(new String(ioExc.getMessage().getBytes(StandardCharsets.UTF_8)));
            throw new RuntimeException(ioExc);
        }
    }


    public CompletableFuture<String> getHttpFuture(@NotNull String url) {
//        System.out.println(new String(url.getBytes(StandardCharsets.UTF_8)));

        OkHttpClient client = new OkHttpClient();
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

    public String getLocationVariantsURL(String placeName) {
        return "https://graphhopper.com/api/1/geocode?q=" + placeName +
                "&key=" + APiKeys.locationApiKey +
                "&limit=" + limit;
    }

    public Place[] parseLocations(@NotNull String json) throws ParseException {
        //System.out.println(new String(json.getBytes(StandardCharsets.UTF_8)));
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

    public int selectedPlace(int minLength, int maxLength) {
        System.out.println("\nInsert number of selected place: ");
        System.out.flush();
        Scanner scanner = new Scanner(System.in);
        int index = scanner.nextInt();
        while (index < minLength || index > maxLength) {
            System.out.println("Insert correct number of selected place: ");
            index = scanner.nextInt();
        }
        return index;
    }

    public String getWeatherURL(@NotNull Place selectPlace) {
        return "https://api.openweathermap.org/data/2.5/weather?" +
                "lat=" + selectPlace.getLatitude() +
                "&lon=" + selectPlace.getLongitude() +
                "&units=metric" +
                "&appid=" + APiKeys.weatherApiKey;
    }

    public Weather parseWeather(@NotNull String json) throws ParseException {
        //System.out.println(new String(json.getBytes(StandardCharsets.UTF_8)));
        JSONParser parser = new JSONParser();
        JSONObject jsonObj = (JSONObject) parser.parse(json);
        return new Weather(jsonObj);
    }

    public String getInterestLocationURL(@NotNull Place selectPlace) {
        return "https://api.opentripmap.com/0.1/en/places/radius?" +
                "radius=" + radius +
                "&lon=" + selectPlace.getLongitude() +
                "&lat=" + selectPlace.getLatitude() +
                "&apikey=" + APiKeys.interestLocationApiKey;
    }

    public InterestingPlace[] parseInterestingPlace(@NotNull String json) throws ParseException {
        // System.out.println(new String(json.getBytes(StandardCharsets.UTF_8)));
        JSONParser parser = new JSONParser();
        JSONObject jsonObj = (JSONObject) parser.parse(json);
        JSONArray features = (JSONArray) jsonObj.get("features");
        int minLen = Math.min(limit, features.size());
        InterestingPlace[] interestingPlaces = new InterestingPlace[minLen];
        for (int i = 0; i < minLen; i++) {
            interestingPlaces[i] = new InterestingPlace((JSONObject) features.get(i));
        }
        return interestingPlaces;
    }


    public String getInterestLocationInfoURL(@NotNull InterestingPlace interestingPlace) {
        return " https://api.opentripmap.com/0.1/ru/places/" +
                "xid/" + interestingPlace.getXid() +
                "?apikey=" + APiKeys.interestLocationApiKey;
    }

    private void parseInterestingPlaceInfo(String json, InterestingPlace interestingPlace) throws ParseException {
        // System.out.println(new String(json.getBytes(StandardCharsets.UTF_8)));
        JSONParser parser = new JSONParser();
        JSONObject jsonObj = (JSONObject) parser.parse(json);
        interestingPlace.setInfo(jsonObj);
    }

}
