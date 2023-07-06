package org.example;

public class WeatherData {
        private int id;
        private int coordinateId;
        private String dateTime;
        private double temperature;
        private double precipitation;

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

        public String getDateTime() {
                return dateTime;
        }

        public void setDateTime(String dateTime) {
                this.dateTime = dateTime;
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
