package chris.davison.cdweather.util;

import android.app.Activity;

import chris.davison.cdweather.interfaces.WeatherDataReadyCallbacks;

public class CustomCallbackTask implements Runnable {

    private final Runnable task;
    private final WeatherDataReadyCallbacks callback;
    private final Activity activity;
    private final String taskType;

    public CustomCallbackTask(Runnable task, WeatherDataReadyCallbacks callback, Activity activity,
                              String taskType) {
        this.task = task;
        this.callback = callback;
        this.activity = activity;
        this.taskType = taskType;
    }

    public void run() {
        task.run();
        // Run on ui/main thread.
        if (taskType.equals("CURRENT")) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    callback.getCurrentWeatherIconApiCall();
                }
            });
        } else if (taskType.equals("CURRENTICON")) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    callback.setCurrentWeatherViews();
                }
            });
        } else if (taskType.equals("TODAY")) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    callback.getTodaysWeatherIconApiCall();
                }
            });
        } else if (taskType.equals("TODAYICONS")) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    callback.setTodaysWeatherRecViewAdapter();
                }
            });
        } else if (taskType.equals("WEEK")) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    callback.getWeeksWeatherIconApiCall();
                }
            });
        } else if (taskType.equals("WEEKICONS")) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    callback.setWeeksWeatherRecViewAdapter();
                }
            });
        }
    }
}