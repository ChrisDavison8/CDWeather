package chris.davison.cdweather.models;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class TodayWeatherItem {
    private final List<String> time;
    private final List<String> iconCode;
    private final List<Bitmap> iconBitmap;
    private final List<String> temp;

    public TodayWeatherItem(List<String> time, List<String> iconCode, List<String> temp) {
        this.time = time;
        this.iconCode = iconCode;
        this.iconBitmap = new ArrayList<>();
        this.temp = temp;
    }

    public List<String> getTime() { return time; }
    public List<String> getIconCode() { return iconCode; }
    public List<Bitmap> getIconBitmap() { return iconBitmap; }
    public List<String> getTemp() { return temp; }

    public void setIconBitmap(Bitmap iconBitmap) {
        this.iconBitmap.add(iconBitmap);
    }
}