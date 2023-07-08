package org.example;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;



public class Main implements WeatherService {
    private static final String API_URL = "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&hourly=temperature_2m,rain";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/app";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "12345";

    public static void main(String[] args) {
        Main main = new Main();
        main.startApplication();
    }

    private void startApplication() {
        createTables();

        Scanner scanner = new Scanner(System.in).useLocale(Locale.US);

        while (true) {
            System.out.println("Список команд:");
            System.out.println("1 - Ввести новые координаты");
            System.out.println("2 - Получить список всех зарегистрированных координат");
            System.out.println("3 - Обновить данные прогноза");
            System.out.println("4 - Получить доступные даты-время прогноза");
            System.out.println("5 - Получить прогноз");
            System.out.println("0 - Выход");

            try {
                int command = scanner.nextInt();
                scanner.nextLine();

                switch (command) {
                    case 0 -> {
                        System.out.println("Выход из приложения...");
                        return;
                    }
                    case 1 -> {
                        System.out.print("Введите широту: ");
                        double latitude = scanner.nextDouble();
                        scanner.nextLine();
                        System.out.print("Введите долготу: ");
                        double longitude = scanner.nextDouble();
                        scanner.nextLine();
                        int coordinateId = regLatLong(latitude, longitude);
                        System.out.println("ID сохраненной записи: " + coordinateId);
                    }
                    case 2 -> {
                        List<LatLongTarget> targets = getAllTargets();
                        System.out.println("Список всех зарегистрированных координат:");
                        for (LatLongTarget target : targets) {
                            System.out.println("ID: " + target.getId() + ", Широта: " + target.getLat() + ", Долгота: " + target.getLong());
                        }
                    }
                    case 3 -> {
                        System.out.print("Введите ID координаты для обновления данных: ");
                        int coordinateIdToUpdate = scanner.nextInt();
                        scanner.nextLine();
                        boolean updated = updateForecastData(coordinateIdToUpdate);
                        if (updated) {
                            System.out.println("Данные прогноза обновлены.");
                        } else {
                            System.out.println("Новые данные отсутствуют.");
                        }
                    }
                    case 4 -> {
                        System.out.print("Введите ID координаты для получения доступных дат-времени прогноза: ");
                        int coordinateIdForDates = scanner.nextInt();
                        scanner.nextLine();
                        List<LocalDateTime> availableDates = getAvailableForecastDates(coordinateIdForDates);
                        System.out.println("Список доступных дат-времени прогноза:");
                        for (LocalDateTime dateTime : availableDates) {
                            System.out.println(dateTime);
                        }
                    }
                    case 5 -> {
                        System.out.print("Введите ID координаты: ");
                        int coordinateIdForForecast = scanner.nextInt();
                        scanner.nextLine();
                        System.out.print("Введите дату-время прогноза (yyyy-MM-dd HH:mm): ");
                        String dateTimeStr = scanner.nextLine();
                        try {
                            LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                            ForecastResponse forecast = getForecastByDateTime(coordinateIdForForecast, dateTime);
                            if (forecast != null) {
                                System.out.println("Прогноз для даты-времени " + dateTime + ":");
                                System.out.println("Температура: " + forecast.getTemperature());
                                System.out.println("Осадки: " + forecast.getPrecipitation());
                            } else {
                                System.out.println("Прогноз для указанной даты-времени не найден.");
                            }
                        } catch (DateTimeParseException e) {
                            System.out.println("Ошибка: неправильный формат даты или времени.");
                        }
                    }
                    default -> System.out.println("Неверная команда.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Ошибка: некорректный ввод. Пожалуйста, введите число.");
                scanner.nextLine();
            }
        }
    }

    private void createTables() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            // coordinates
            String coordinatesTableQuery = "CREATE TABLE IF NOT EXISTS coordinates (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "latitude DOUBLE NOT NULL," +
                    "longitude DOUBLE NOT NULL" +
                    ")";
            PreparedStatement coordinatesStatement = connection.prepareStatement(coordinatesTableQuery);
            coordinatesStatement.executeUpdate();

            // forecast
            String forecastTableQuery = "CREATE TABLE IF NOT EXISTS forecast (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "coordinate_id INT NOT NULL," +
                    "generationtime_ms BIGINT NOT NULL," +
                    "utc_offset_seconds INT NOT NULL," +
                    "timezone_abbreviation VARCHAR(255)," +
                    "elevation DOUBLE NOT NULL," +
                    "date_time VARCHAR(255) NOT NULL," +
                    "temperature DOUBLE NOT NULL," +
                    "precipitation DOUBLE NOT NULL," +
                    "FOREIGN KEY (coordinate_id) REFERENCES coordinates(id)" +
                    ")";
            PreparedStatement forecastStatement = connection.prepareStatement(forecastTableQuery);
            forecastStatement.executeUpdate();

            // Set the default value for timezone_abbreviation column to 'GMT'
            String alterTableQuery = "ALTER TABLE forecast " +
                    "ALTER COLUMN timezone_abbreviation SET DEFAULT 'GMT'";
            PreparedStatement alterTableStatement = connection.prepareStatement(alterTableQuery);
            alterTableStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }





    @Override
    public int regLatLong(double latitude, double longitude) {
        try {
            // запрос API
            String apiUrl = String.format(API_URL, latitude, longitude);
            String jsonResponse = sendGetRequest(apiUrl);


            WeatherData weatherData = deserializeWeatherData(jsonResponse);
            if (weatherData != null) {
                return saveToDatabase(latitude, longitude, weatherData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1;
    }

    private String sendGetRequest(String apiUrl) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return response.toString();
        } else {
            System.out.println("Ошибка при получении данных о погоде. Код ответа: " + responseCode);
        }

        return null;
    }

    private WeatherData deserializeWeatherData(String jsonResponse) {
        Gson gson = new Gson();
        JsonParser jsonParser = new JsonParser();
        JsonElement rootElement = jsonParser.parse(jsonResponse);
        JsonObject jsonObject = rootElement.getAsJsonObject();

        WeatherData weatherData = new WeatherData();

        JsonObject hourlyObject = jsonObject.getAsJsonObject("hourly");


        JsonArray timeArray = hourlyObject.getAsJsonArray("time");
        List<String> dateTimeList = new ArrayList<>();
        for (JsonElement timeElement : timeArray) {
            dateTimeList.add(timeElement.getAsString());
        }
        weatherData.setDateTimeList(dateTimeList);


        JsonArray temperatureArray = hourlyObject.getAsJsonArray("temperature_2m");
        List<Double> temperatureList = new ArrayList<>();
        for (JsonElement temperatureElement : temperatureArray) {
            temperatureList.add(temperatureElement.getAsDouble());
        }
        weatherData.setTemperatureList(temperatureList);


        JsonArray precipitationArray = hourlyObject.getAsJsonArray("rain");
        List<Double> precipitationList = new ArrayList<>();
        for (JsonElement precipitationElement : precipitationArray) {
            precipitationList.add(precipitationElement.getAsDouble());
        }
        weatherData.setPrecipitationList(precipitationList);


        JsonObject hourlyUnitsObject = jsonObject.getAsJsonObject("hourly_units");
        String temperatureUnit = hourlyUnitsObject.get("temperature_2m").getAsString();
        String precipitationUnit = hourlyUnitsObject.get("rain").getAsString();

        weatherData.setTemperatureUnit(temperatureUnit);
        weatherData.setPrecipitationUnit(precipitationUnit);

        return weatherData;
    }


    private int saveToDatabase(double latitude, double longitude, WeatherData weatherData) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {

            String coordinatesQuery = "INSERT INTO coordinates (latitude, longitude) VALUES (?, ?)";
            PreparedStatement coordinatesStatement = connection.prepareStatement(coordinatesQuery, Statement.RETURN_GENERATED_KEYS);
            coordinatesStatement.setDouble(1, latitude);
            coordinatesStatement.setDouble(2, longitude);

            int rowsAffected = coordinatesStatement.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet generatedKeys = coordinatesStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int coordinateId = generatedKeys.getInt(1);

                    String forecastQuery = "INSERT INTO forecast (coordinate_id, generationtime_ms, utc_offset_seconds, timezone_abbreviation, elevation, date_time, temperature, precipitation) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement forecastStatement = connection.prepareStatement(forecastQuery, Statement.RETURN_GENERATED_KEYS);
                    forecastStatement.setInt(1, coordinateId);
                    forecastStatement.setLong(2, weatherData.getGenerationTimeMs());
                    forecastStatement.setInt(3, weatherData.getUtcOffsetSeconds());
                    forecastStatement.setString(4, weatherData.getTimezoneAbbreviation());
                    forecastStatement.setDouble(5, weatherData.getElevation());

                    List<String> dateTimeList = weatherData.getDateTimeList();
                    List<Double> temperatureList = weatherData.getTemperatureList();
                    List<Double> precipitationList = weatherData.getPrecipitationList();

                    for (int i = 0; i < dateTimeList.size(); i++) {
                        forecastStatement.setString(6, dateTimeList.get(i));
                        forecastStatement.setDouble(7, temperatureList.get(i));
                        forecastStatement.setDouble(8, precipitationList.get(i));
                        forecastStatement.executeUpdate();
                    }

                    return coordinateId;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }


    @Override
    public List<LocalDateTime> getAvailableForecastDates(int coordinateId) {
        List<LocalDateTime> dateTimeList = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String query = "SELECT date_time FROM forecast WHERE coordinate_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, coordinateId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String dateTimeStr = resultSet.getString("date_time");
                LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr);
                dateTimeList.add(dateTime);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dateTimeList;
    }

    @Override
    public boolean updateForecastData(int coordinateId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String query = "SELECT latitude, longitude FROM coordinates WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, coordinateId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                double latitude = resultSet.getDouble("latitude");
                double longitude = resultSet.getDouble("longitude");

                String apiUrl = String.format(API_URL, latitude, longitude);
                String jsonResponse = sendGetRequest(apiUrl);
                WeatherData weatherData = deserializeWeatherData(jsonResponse);

                if (weatherData != null) {
                    String updateQuery = "UPDATE forecast SET generationtime_ms = ?, utc_offset_seconds = ?, timezone_abbreviation = ?, elevation = ?, " +
                            "temperature = ?, precipitation = ? WHERE coordinate_id = ?";
                    PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                    updateStatement.setLong(1, weatherData.getGenerationTimeMs());
                    updateStatement.setInt(2, weatherData.getUtcOffsetSeconds());
                    updateStatement.setDouble(4, weatherData.getElevation());

                    List<String> dateTimeList = weatherData.getDateTimeList();
                    List<Double> temperatureList = weatherData.getTemperatureList();
                    List<Double> precipitationList = weatherData.getPrecipitationList();

                    for (int i = 0; i < dateTimeList.size(); i++) {
                        updateStatement.setString(3, "GMT"); // Set the timezone abbreviation to "GMT"
                        updateStatement.setDouble(5, temperatureList.get(i));
                        updateStatement.setDouble(6, precipitationList.get(i));
                        updateStatement.setInt(7, coordinateId);
                        updateStatement.executeUpdate();
                    }

                    return true;
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }



    @Override
    public ForecastResponse getForecastByDateTime(int coordinateId, LocalDateTime dateTime) {
        ForecastResponse forecastResponse = null;
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String query = "SELECT temperature, precipitation FROM forecast WHERE coordinate_id = ? AND date_time = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, coordinateId);
            statement.setString(2, dateTime.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                double temperature = resultSet.getDouble("temperature");
                double precipitation = resultSet.getDouble("precipitation");
                forecastResponse = new ForecastResponse(temperature, precipitation);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return forecastResponse;
    }

    @Override
    public List<LatLongTarget> getAllTargets() {
        List<LatLongTarget> targets = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String query = "SELECT id, latitude, longitude FROM coordinates";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                double latitude = resultSet.getDouble("latitude");
                double longitude = resultSet.getDouble("longitude");
                LatLongTarget target = new LatLongTarget(id, latitude, longitude);
                targets.add(target);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return targets;
    }
}

