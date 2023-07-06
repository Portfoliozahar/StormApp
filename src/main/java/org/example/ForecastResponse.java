package org.example;

public class ForecastResponse {
    private double temperature;
    private double precipitation;

    public ForecastResponse(double temperature, double precipitation) {
        this.temperature = temperature;
        this.precipitation = precipitation;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(double precipitation) {
        this.precipitation = precipitation;
    }
}
