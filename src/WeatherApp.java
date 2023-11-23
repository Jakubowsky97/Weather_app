import com.sun.security.jgss.GSSUtil;
import netscape.javascript.JSObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.imageio.IIOException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class WeatherApp {
    public static JSONObject getWeatherData(String locationName){
        JSONArray locationData = getLocationData(locationName);

        Object locationObj = locationData.get(0);

        if (locationObj instanceof JSONObject location) {
            double latitude = (double) location.get("latitude");
            double longitude = (double) location.get("longitude");

            String urlString = "https://api.open-meteo.com/v1/forecast?latitude=" +
                    latitude +
                    "&longitude=" +
                    longitude +
                    "&hourly=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m&daily=temperature_2m_max,temperature_2m_min&timezone=GMT";

            try{
                HttpURLConnection conn = fetchApiResponse(urlString);

                if(conn.getResponseCode() != 200) {
                    System.out.println("Error: Couldn't connect to API");
                    return null;
                }

                StringBuilder resultJson = new StringBuilder();
                Scanner scanner = new Scanner(conn.getInputStream());
                while(scanner.hasNext()) {
                    resultJson.append(scanner.nextLine());
                }

                scanner.close();

                conn.disconnect();

                //parse the JSON string to JSON object
                JSONParser parser = new JSONParser();
                JSONObject resultsJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

                //System.out.println("JSON Response: " + resultsJsonObj.toString());

                JSONObject hourly = (JSONObject) resultsJsonObj.get("hourly");
                JSONObject daily = (JSONObject) resultsJsonObj.get("daily");

                // finding index of current hour
                JSONArray time = (JSONArray) hourly.get("time");
                int index = findIndexOfCurrentTime(time);

                //get temperature form current hour
                JSONArray temperatureArray = (JSONArray) hourly.get("temperature_2m");
                double temperature = (double) temperatureArray.get(index);

                Object weatherCodeObj = hourly.get("weather_code");
                long[] weatherCodeArray;

                if (weatherCodeObj instanceof JSONArray) {
                    JSONArray weatherCode = (JSONArray) weatherCodeObj;
                    weatherCodeArray = new long[weatherCode.size()];

                    // Convert the JSONArray to a long array
                    for (int i = 0; i < weatherCode.size(); i++) {
                        weatherCodeArray[i] = (long) weatherCode.get(i);
                    }
                } else {
                    // Handle the case where weather_code is not a JSONArray
                    System.out.println("Unexpected format for weather_code");
                    return null; // or take appropriate action
                }

                // get weather condition
                String weatherCondition = convertWeatherCode(weatherCodeArray[index]);


                //get humidity
                JSONArray relativeHumidity = (JSONArray) hourly.get("relative_humidity_2m");
                long humidity = (long) relativeHumidity.get(index);

                //get wind speed
                JSONArray windSpeedData = (JSONArray) hourly.get("wind_speed_10m");
                double windSpeed = (double) windSpeedData.get(index);

                // get apparent temp
                JSONArray apparentTempData = (JSONArray) hourly.get("apparent_temperature");
                double apparentTemp = (double) apparentTempData.get(index);

                //get max temperature
                JSONArray maxTempData = (JSONArray) daily.get("temperature_2m_max");
                double maxTemp = (double) maxTempData.get(0);

                //get min temperature
                JSONArray minTempData = (JSONArray) daily.get("temperature_2m_min");
                double minTemp = (double) minTempData.get(0);


                JSONObject weatherData = new JSONObject();
                weatherData.put("temperature", temperature);
                weatherData.put("weather_condition", weatherCondition);
                weatherData.put("humidity", humidity);
                weatherData.put("windspeed", windSpeed);
                weatherData.put("temperature_max", maxTemp);
                weatherData.put("temperature_min", minTemp);
                weatherData.put("apparent_temp", apparentTemp);
                return weatherData;
            }catch(Exception e) {
                e.printStackTrace();
            }

            return null;
        } else {
            System.out.println("Error: Unexpected data format. Unable to retrieve location information.");
            return null;
        }
    }

    public static JSONArray getLocationData(String locationName) {
        locationName = locationName.replaceAll(" ", "+");

        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                locationName + "&count=10&language=en&format=json";

        try{
            HttpURLConnection conn = fetchApiResponse(urlString);

            if(conn.getResponseCode() != 200) {
                System.out.println("Error: Couldn't connect to API");
                return null;
            } else {
                // Store API results
                StringBuilder resultJson =  new StringBuilder();
                Scanner scanner = new Scanner(conn.getInputStream());

                // read and store the resulting json data into String builder
                while(scanner.hasNext()) {
                    resultJson.append(scanner.nextLine());
                }

                scanner.close();
                conn.disconnect();

                //parse the JSON string to JSON object
                JSONParser parser = new JSONParser();
                JSONObject resultsJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

                JSONArray locationData = (JSONArray) resultsJsonObj.get("results");
                return locationData;
            }
        }catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static HttpURLConnection fetchApiResponse(String urlString) {
        try{
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");

            conn.connect();
            return conn;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static int findIndexOfCurrentTime(JSONArray timeList) {
        String currentTime = getCurrentTime();

        for(int i = 0; i < timeList.size(); i++) {
            String time = (String) timeList.get(i);
            if(time.equalsIgnoreCase(currentTime)){
                return i;
            }
        }

        return 0;
    }

    public static String getCurrentTime() {
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");

        String formattedDateTime = currentTime.format(formatter);

        return formattedDateTime;
    }

    private static String convertWeatherCode(long weathercode) {
        String weatherCondition = "";
        if(weathercode == 0L) {
            weatherCondition = "Clear";
        } else if(weathercode <= 3L && weathercode > 0L) {
            weatherCondition = "Cloudy";
        } else if((weathercode >= 51L && weathercode <=67L) || (weathercode >= 80L && weathercode <= 99L)) {
            weatherCondition = "Rain";
        } else if(weathercode >= 71L && weathercode <= 77L) {
            weatherCondition = "Snow";
        }
        return weatherCondition;
    }
}
