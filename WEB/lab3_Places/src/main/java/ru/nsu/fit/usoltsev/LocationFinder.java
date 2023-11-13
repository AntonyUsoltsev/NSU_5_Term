package ru.nsu.fit.usoltsev;

import kotlin.Pair;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("FieldCanBeLocal")
@Slf4j
public class LocationFinder {
    private final String placeName;
    private final int limit;
    private final int radius;

    public LocationFinder(String placeName, int limit, int radius) {
        this.placeName = placeName;
        this.limit = limit;
        this.radius = radius;
    }

    public void start() {
        CompletableFuture<String> locationFuture = getHttpFuture(URLUtils.getLocationVariantsURL(placeName, limit));

        CompletableFuture<Void> resultFuture = locationFuture
                .thenApplyAsync(this::getSelectedPlace)
                .thenComposeAsync(selectedPlace -> {
                    System.out.println("\nSelect: " + selectedPlace + "\n");

                    CompletableFuture<Weather> weatherFuture =
                            getHttpFuture(URLUtils.getWeatherURL(selectedPlace))
                                    .thenApplyAsync(this::getWeather);

                    CompletableFuture<ArrayList<InterestingPlace>> interestingPlacesFuture =
                            getHttpFuture(URLUtils.getInterestLocationURL(selectedPlace, radius))
                                    .thenApplyAsync(this::getInterestingPlaces)
                                    .thenComposeAsync(this::getInterestPlacesInfo);

                    return CompletableFuture.allOf(weatherFuture, interestingPlacesFuture)
                            .thenApplyAsync(v -> new Pair<>(weatherFuture.join(), interestingPlacesFuture.join()));

                })
                .thenAcceptAsync(this::printFindInfo);

        resultFuture.exceptionally(throwable -> {
            log.warn(throwable.getMessage(), throwable);
            throw new RuntimeException(throwable);
        });

        resultFuture.join();
    }

    public CompletableFuture<String> getHttpFuture(@NotNull String url) {
        //  System.out.println(new String(url.getBytes(StandardCharsets.UTF_8)));
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
                    assert response.body() != null;
                    String responseBody = response.body().string();
                    resultFuture.complete(responseBody);
                } catch (IOException e) {
                    resultFuture.completeExceptionally(e);
                }
            }
        });
        return resultFuture;
    }

    private Place getSelectedPlace(String locations) {
        try {
            Place[] places = parseLocations(locations);
            int index = getSelectedPlaceIndex(1, places.length);
            return places[index - 1];
        } catch (ParseException pe) {
            throw new RuntimeException("Error parsing places", pe);
        }
    }

    public Place[] parseLocations(@NotNull String json) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject jsonObj = (JSONObject) parser.parse(json);
        JSONArray jsonArr = (JSONArray) jsonObj.get("hits");
        Place[] places = new Place[jsonArr.size()];

        for (int i = 0; i < jsonArr.size(); i++) {
            JSONObject obj = (JSONObject) jsonArr.get(i);
            places[i] = new Place(obj);
            System.out.println((i + 1) + ": " + places[i]);
        }
        return places;
    }

    public int getSelectedPlaceIndex(int minLength, int maxLength) {
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

    private Weather getWeather(@NotNull String response) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObj = (JSONObject) parser.parse(response);
            return new Weather(jsonObj);
        } catch (ParseException pe) {
            throw new RuntimeException("Error parsing weather", pe);
        }
    }

    public ArrayList<InterestingPlace> getInterestingPlaces(@NotNull String json) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObj = (JSONObject) parser.parse(json);
            JSONArray features = (JSONArray) jsonObj.get("features");
            ArrayList<InterestingPlace> list = new ArrayList<>();
            for (Object feature : features) {
                if (!((String) ((JSONObject) ((JSONObject) feature).get("properties")).get("name")).isBlank()) {
                    list.add(new InterestingPlace(((JSONObject) feature)));
                    if (list.size() >= limit) {
                        break;
                    }
                }
            }
            return list;
        } catch (ParseException pe) {
            throw new RuntimeException("Error parsing interesting places", pe);
        }
    }

    private CompletableFuture<ArrayList<InterestingPlace>> getInterestPlacesInfo(@NotNull ArrayList<InterestingPlace> interestingPlaces) {
        List<CompletableFuture<Void>> infoFutures = interestingPlaces.stream()
                .map(intPlace -> getHttpFuture(URLUtils.getInterestLocationInfoURL(intPlace))
                        .thenAcceptAsync(stringInterestInfo -> {
                            try {
                                JSONParser parser = new JSONParser();
                                JSONObject jsonObj = (JSONObject) parser.parse(stringInterestInfo);
                                intPlace.setInfo(jsonObj);
                            } catch (ParseException pe) {
                                throw new RuntimeException("Error parsing interesting places info", pe);
                            }
                        }))
                .toList();

        return CompletableFuture.allOf(infoFutures.toArray(new CompletableFuture[0]))
                .thenApplyAsync(v -> interestingPlaces);
    }

    private void printFindInfo(@NotNull Pair<Weather, ArrayList<InterestingPlace>> pair) {
        System.out.println(pair.getFirst());
        for (InterestingPlace intPlace : pair.getSecond()) {
            System.out.println(intPlace);
        }
    }
}
