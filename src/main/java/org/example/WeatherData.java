package org.example;

import java.util.List;

class WeatherData {
        private int id;
        private int coordinateId;
        private long generationTimeMs;
        private int utcOffsetSeconds;
        private String timezoneAbbreviation;
        private double elevation;
        private List<String> dateTimeList;
        private List<Double> temperatureList;
        private List<Double> precipitationList;
        private String temperatureUnit;
        private String precipitationUnit;

        public int getId() {
                return id;
        }

        public void setId(int id) {
                this.id = id;
        }

        public int getCoordinateId() {
                return coordinateId;
        }

        public void setCoordinateId(int coordinateId) {
                this.coordinateId = coordinateId;
        }

        public long getGenerationTimeMs() {
                return generationTimeMs;
        }

        public void setGenerationTimeMs(long generationTimeMs) {
                this.generationTimeMs = generationTimeMs;
        }

        public int getUtcOffsetSeconds() {
                return utcOffsetSeconds;
        }

        public void setUtcOffsetSeconds(int utcOffsetSeconds) {
                this.utcOffsetSeconds = utcOffsetSeconds;
        }

        public String getTimezoneAbbreviation() {
                return timezoneAbbreviation;
        }

        public void setTimezoneAbbreviation(String timezoneAbbreviation) {
                this.timezoneAbbreviation = timezoneAbbreviation;
        }

        public double getElevation() {
                return elevation;
        }

        public void setElevation(double elevation) {
                this.elevation = elevation;
        }

        public List<String> getDateTimeList() {
                return dateTimeList;
        }

        public void setDateTimeList(List<String> dateTimeList) {
                this.dateTimeList = dateTimeList;
        }

        public List<Double> getTemperatureList() {
                return temperatureList;
        }

        public void setTemperatureList(List<Double> temperatureList) {
                this.temperatureList = temperatureList;
        }

        public List<Double> getPrecipitationList() {
                return precipitationList;
        }

        public void setPrecipitationList(List<Double> precipitationList) {
                this.precipitationList = precipitationList;
        }

        public String getTemperatureUnit() {
                return temperatureUnit;
        }

        public void setTemperatureUnit(String temperatureUnit) {
                this.temperatureUnit = temperatureUnit;
        }

        public String getPrecipitationUnit() {
                return precipitationUnit;
        }

        public void setPrecipitationUnit(String precipitationUnit) {
                this.precipitationUnit = precipitationUnit;
        }
}
