package org.example;

import java.time.LocalDateTime;
import java.util.List;

interface WeatherService {
    int regLatLong(double latitude, double longitude);
    List<LatLongTarget> getAllTargets();
    boolean updateForecastData(int coordinateId);
    List<LocalDateTime> getAvailableForecastDates(int coordinateId);
    ForecastResponse getForecastByDateTime(int coordinateId, LocalDateTime dateTime);
}