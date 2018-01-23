# Airly Console Client

A terminal-based client of Airly API (https://map.airly.eu/)

*Project was made for Object-oriented programming course at AGH University.*

![Screenshot 1](/../screenshots/screenshots/screenshot1.png?raw=true)
![Screenshot 2](/../screenshots/screenshots/screenshot2.png?raw=true)

```
usage: java -jar airly-console-client.jar
    --api-key <arg>     Airly API key
    --history           Displays history of measurement
    --latitude <arg>    Latitude coordinate of an area
    --longitude <arg>   Longitude coordinate of an area.
    --sensor-id <arg>   Sensor ID

Either '--sensor-id' or '--latitude' and '--longitude' must be specified.
Airly API key must be provided either as '--api-key' parameter or as 'API_KEY' environment variable.

Application: © 2018 Piotr Janczyk. License GPLv3+
Data from Airly.eu
```
