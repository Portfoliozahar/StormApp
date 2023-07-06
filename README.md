# Сервис погоды


для работы надо создать в MySQL 


-- База
```sql
CREATE DATABASE app;
```


##Подключение

private static final String API_URL = "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&hourly=temperature_2m,rain";

private static final String DB_URL = "jdbc:mysql://localhost:3306/app";

private static final String DB_USERNAME = "root";

private static final String DB_PASSWORD = "12345";
