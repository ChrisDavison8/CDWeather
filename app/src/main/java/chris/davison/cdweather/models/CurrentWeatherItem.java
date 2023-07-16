package chris.davison.cdweather.models;

import android.graphics.Bitmap;

public class CurrentWeatherItem {
    private String location;
    private String weatherDescription;
    private Bitmap iconBitmap;
    private double temperatureHigh;
    private double temperatureLow;
    private String dayNight;

    public CurrentWeatherItem(String location, String weatherDescription, Bitmap iconBitmap,
                              double temperatureHigh, double temperatureLow, String dayNight) {
        this.location = location;
        this.weatherDescription = weatherDescription;
        this.iconBitmap = iconBitmap;
        this.temperatureHigh = temperatureHigh;
        this.temperatureLow = temperatureLow;
        this.dayNight = dayNight;
    }

    public String getLocation() { return location; }
    public String getWeatherDescription() { return weatherDescription; }
    public Bitmap getIconBitmap() { return iconBitmap; }
    public double getTemperatureHigh() { return temperatureHigh; }
    public double getTemperatureLow() { return temperatureLow; }
    public String getDayNight() { return dayNight; }
}
