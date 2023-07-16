package chris.davison.cdweather.models;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class WeekWeatherItem {
    private List<String> day;
    private List<String> iconCode;
    private List<String> temperatureHigh;
    private List<String> temperatureLow;
    private List<Bitmap> iconBitmap;

    public WeekWeatherItem(List<String> day, List<String> iconCode, List<String> temperatureHigh,
                           List<String> temperatureLow) {
        this.day = day;
        this.iconCode = iconCode;
        this.temperatureHigh = temperatureHigh;
        this.temperatureLow = temperatureLow;
        this.iconBitmap = new ArrayList<>();
    }

    public List<String> getDay() { return day; }
    public List<String> getIconCode() { return iconCode; }
    public List<String> getTemperatureHigh() { return temperatureHigh; }
    public List<String> getTemperatureLow() { return temperatureLow; }
    public List<Bitmap> getIconBitmap() { return iconBitmap; }
    public void setIconBitmap(Bitmap iconBitmap) {
        this.iconBitmap.add(iconBitmap);
    }
}