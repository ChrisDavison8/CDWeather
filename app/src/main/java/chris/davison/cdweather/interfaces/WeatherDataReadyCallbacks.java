package chris.davison.cdweather.interfaces;

public interface WeatherDataReadyCallbacks {
    void getCurrentWeatherIconApiCall();
    void setCurrentWeatherViews();
    void getTodaysWeatherIconApiCall();
    void setTodaysWeatherRecViewAdapter();
    void getWeeksWeatherIconApiCall();
    void setWeeksWeatherRecViewAdapter();
}
