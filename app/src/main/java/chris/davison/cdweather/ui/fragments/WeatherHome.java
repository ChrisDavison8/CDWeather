package chris.davison.cdweather.ui.fragments;

import static android.content.DialogInterface.BUTTON_POSITIVE;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import chris.davison.cdweather.R;
import chris.davison.cdweather.adapters.TodaysWeatherAdapter;
import chris.davison.cdweather.adapters.WeeksWeatherAdapter;
import chris.davison.cdweather.interfaces.WeatherDataReadyCallbacks;
import chris.davison.cdweather.models.CurrentWeatherItem;
import chris.davison.cdweather.models.TodayWeatherItem;
import chris.davison.cdweather.models.WeekWeatherItem;
import chris.davison.cdweather.util.CustomCallbackTask;
import chris.davison.cdweather.util.HttpRequests;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeatherHome extends Fragment implements WeatherDataReadyCallbacks, DialogInterface.OnClickListener {
    private final String API_KEY = "";
    private LocationCallback locationCallback;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;
    private final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};
    private ConstraintLayout constraintLayout;
    private TextView location;
    private ImageView currentWeatherImage;
    private TextView currentDescription;
    private TextView tempHighLow;
    private RecyclerView todaysWeatherRecView;
    private RecyclerView weeksWeatherRecView;
    private JSONObject currentWeatherJsonData;
    private JSONObject todaysWeatherJsonData;
    private JSONObject weeksWeatherJsonData;
    private Bitmap imageIconBitmap;
    private double latitude = 0;
    private double longitude = 0;
    private CurrentWeatherItem currentWeatherItem;
    private TodayWeatherItem todayWeatherItem;
    private WeekWeatherItem weekWeatherItem;

    public WeatherHome() {
    }

    public static WeatherHome newInstance() {
        return new WeatherHome();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_weather_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setUpCallbacks();
        setPermissionLaunchers();
        checkPermissions();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == BUTTON_POSITIVE) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + requireActivity().getPackageName()));
            startActivity(intent);
        }
    }

    public void initViews(View view) {
        constraintLayout = view.findViewById(R.id.weatherHomeCl);
        location = view.findViewById(R.id.weatherHomeLocationTv);
        currentWeatherImage = view.findViewById(R.id.weatherHomeCurrentIconIv);
        currentDescription = view.findViewById(R.id.weatherHomeCurrentWeatherTv);
        tempHighLow = view.findViewById(R.id.weatherHomeWeatherTempHighLowTv);
        todaysWeatherRecView = view.findViewById(R.id.weatherHomeTodaysForecastRv);
        weeksWeatherRecView = view.findViewById(R.id.weatherHomeWeeklyForecastRv);
    }

    public void setUpCallbacks() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location lastLocation = locationResult.getLastLocation();
                if (lastLocation != null) {
                    latitude = lastLocation.getLatitude();
                    longitude = lastLocation.getLongitude();
                    getCurrentWeatherApiCall();
                    getTodaysWeatherApiCall();
                    getWeeksWeatherApiCall();
                }
            }
        };
    }

    public void setPermissionLaunchers() {
        locationPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                        isGranted -> {
                    checkPermissions();
                        });
    }

    public void checkPermissions() {
        if (ContextCompat
                .checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED || ContextCompat
                .checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            requestNewLocationData();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
                || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            String message = getResources().getString(R.string.alert_dialog_location);
            new AlertDialog.Builder(requireContext())
                    .setMessage(message)
                    .setPositiveButton(getString(R.string.alert_dialog_ok_button), this)
                    .setNegativeButton(R.string.alert_dialog_dismiss_button, null)
                    .create().show();
        } else {
            requestPermissions();
        }
    }

    public void requestPermissions() {
        locationPermissionLauncher.launch(PERMISSIONS);
    }

    public void requestNewLocationData() {
        LocationRequest locationRequest = new LocationRequest
                .Builder(Priority.PRIORITY_HIGH_ACCURACY, 60000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(60000)
                .setMaxUpdateDelayMillis(60000)
                .build();

        FusedLocationProviderClient fusedLocationProviderClient = LocationServices
                .getFusedLocationProviderClient(requireView().getContext());

        if (ContextCompat
                .checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED || ContextCompat
                .checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,
                    Looper.myLooper());
        }
    }

    public void getCurrentWeatherApiCall() {
        if (latitude == 0 || longitude == 0) {
            return;
        }

        String url = String
                .format(Locale.getDefault(),
                        "https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&" +
                        "appid=%s&units=metric", latitude, longitude, API_KEY);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                HttpRequests httpRequests = new HttpRequests();
                currentWeatherJsonData = httpRequests.makeHttpGETRequest(url);
            }
        };
        CustomCallbackTask customCallbackTask = new CustomCallbackTask(runnable,
                (WeatherDataReadyCallbacks) this, requireActivity(), "CURRENT");
        Thread thread = new Thread(customCallbackTask);
        thread.start();
    }

    public void getCurrentWeatherIconApiCall() {
        try {
            String iconCode = currentWeatherJsonData.getJSONArray("weather")
                    .getJSONObject(0).getString("icon").trim();
            String url = String.format("https://openweathermap.org/img/wn/%s@2x.png", iconCode);

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    HttpRequests httpRequests = new HttpRequests();
                    imageIconBitmap = httpRequests.getImageFromUrl(url);
                }
            };
            CustomCallbackTask customCallbackTask = new CustomCallbackTask(runnable,
                    (WeatherDataReadyCallbacks) this, requireActivity(), "CURRENTICON");
            Thread thread = new Thread(customCallbackTask);
            thread.start();
        } catch (JSONException e) {
            Log.d("JSONEXCEPTION: ", e.getMessage());
        }
    }

    public void setCurrentWeatherViews() {
        try {
            currentWeatherItem = new
                    CurrentWeatherItem(currentWeatherJsonData.getString("name"),
                    currentWeatherJsonData.getJSONArray("weather")
                            .getJSONObject(0).getString("main").trim(),
                    imageIconBitmap,
                    Double.parseDouble(currentWeatherJsonData.getJSONObject("main")
                            .getString("temp_max").trim()),
                    Double.parseDouble(currentWeatherJsonData.getJSONObject("main")
                            .getString("temp_min").trim()), currentWeatherJsonData
                    .getJSONArray("weather").getJSONObject(0).getString("icon")
                    .trim().substring(2));
        } catch (JSONException e) {
            Log.d("JSONEXCEPTION: ", e.getMessage());
        }

        setBackground(currentWeatherItem.getWeatherDescription(),
                currentWeatherItem.getDayNight());

        location.setText(currentWeatherItem.getLocation());
        currentDescription.setText(currentWeatherItem.getWeatherDescription());
        currentWeatherImage.setImageBitmap(currentWeatherItem.getIconBitmap());
        String formattedTemps = String.format(Locale.ENGLISH, "H: %.0f\u2103  L: %.0f\u2103",
                currentWeatherItem.getTemperatureHigh(), currentWeatherItem.getTemperatureLow());
        tempHighLow.setText(formattedTemps);
    }

    public void getTodaysWeatherApiCall() {
        if (latitude == 0 || longitude == 0) {
            return;
        }

        String url = String
                .format(Locale.getDefault(),
                        "https://api.openweathermap.org/data/2.5/forecast?lat=%f&lon=%f&" +
                        "cnt=10&appid=%s&units=metric", latitude, longitude, API_KEY);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                HttpRequests httpRequests = new HttpRequests();
                todaysWeatherJsonData = httpRequests.makeHttpGETRequest(url);
            }
        };
        CustomCallbackTask customCallbackTask = new CustomCallbackTask(runnable,
                (WeatherDataReadyCallbacks) this, requireActivity(), "TODAY");
        Thread thread = new Thread(customCallbackTask);
        thread.start();
    }

    public void getTodaysWeatherIconApiCall() {
        try {
            List<String> time = new ArrayList<>();
            List<String> iconCode = new ArrayList<>();
            List<String> temp = new ArrayList<>();
            JSONArray jsonArray = todaysWeatherJsonData.getJSONArray("list");
            JSONObject jsonObject;
            String formattedTemp, formattedTime;
            double tempField;
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            Pattern pattern = Pattern.compile("(\\d*)(.*)(\\w\\w)");
            LocalDateTime localDateTime;

            for (int i = 0, size = jsonArray.length(); i < size; i++) {
                jsonObject = jsonArray.getJSONObject(i);
                long x = Integer.parseInt(jsonObject.get("dt").toString().trim());
                long y = Integer.parseInt(todaysWeatherJsonData.getJSONObject("city")
                        .getString("timezone"));
                Instant instant = Instant.ofEpochMilli((x+y)*1000);
                localDateTime = LocalDateTime.from(formatter.parse(instant.toString()));
                formattedTime = localDateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT));
                Matcher matcher = pattern.matcher(formattedTime);
                if (matcher.find()) {
                    formattedTime = matcher.group(1) + matcher.group(3);
                } else {
                    formattedTime = "";
                }
                time.add(formattedTime);

                iconCode.add(jsonObject.getJSONArray("weather").getJSONObject(0)
                        .getString("icon").trim());

                tempField = Double.parseDouble(jsonObject.getJSONObject("main")
                        .get("temp").toString().trim());
                formattedTemp = String.format(Locale.ENGLISH, "%.0f\u2103", tempField);
                temp.add(formattedTemp);
            }

            todayWeatherItem = new TodayWeatherItem(time, iconCode, temp);

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (todayWeatherItem.getIconBitmap() != null) {
                        todayWeatherItem.getIconBitmap().clear();
                    }
                    HttpRequests httpRequests = new HttpRequests();
                    for (int i = 0, size = todayWeatherItem.getIconCode().size(); i < size; i++) {
                        String url = String.format("https://openweathermap.org/img/wn/%s@2x.png",
                                todayWeatherItem.getIconCode().get(i));
                        todayWeatherItem.setIconBitmap(httpRequests.getImageFromUrl(url));
                    }
                }
            };
            CustomCallbackTask customCallbackTask = new CustomCallbackTask(runnable,
                    (WeatherDataReadyCallbacks) this, requireActivity(), "TODAYICONS");
            Thread thread = new Thread(customCallbackTask);
            thread.start();
        } catch (JSONException e) {
            Log.d("JSONEXCEPTION: ", e.getMessage());
        }
    }

    public void setTodaysWeatherRecViewAdapter() {
        todaysWeatherRecView.setHasFixedSize(true);
        TodaysWeatherAdapter adapter = new TodaysWeatherAdapter(todayWeatherItem.getTime(),
                todayWeatherItem.getIconBitmap(), todayWeatherItem.getTemp());
        todaysWeatherRecView.setAdapter(adapter);
    }

    public void getWeeksWeatherApiCall() {
        if (latitude == 0 || longitude == 0) {
            return;
        }

        String url = String
                .format(Locale.getDefault(),
                        "https://api.openweathermap.org/data/2.5/forecast?lat=%f&" +
                                "lon=%f&cnt=40&appid=%s&units=metric", latitude, longitude, API_KEY);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                HttpRequests httpRequests = new HttpRequests();
                weeksWeatherJsonData = httpRequests.makeHttpGETRequest(url);
            }
        };
        CustomCallbackTask customCallbackTask = new CustomCallbackTask(runnable,
                (WeatherDataReadyCallbacks) this, requireActivity(), "WEEK");
        Thread thread = new Thread(customCallbackTask);
        thread.start();
    }

    public void getWeeksWeatherIconApiCall() {
        List<String> day = new ArrayList<>();
        List<String> iconCode = new ArrayList<>();
        List<String> minTemp = new ArrayList<>();
        List<String> maxTemp = new ArrayList<>();
        LocalDateTime curLocalDateTime;
        int useTime;
        Instant instant;
        long x, y;

        try {
            JSONArray jsonArray = weeksWeatherJsonData.getJSONArray("list");
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            x = Integer.parseInt(jsonArray.getJSONObject(0).get("dt").toString().trim());
            y = Integer.parseInt(weeksWeatherJsonData.getJSONObject("city")
                    .getString("timezone"));
            instant = Instant.ofEpochMilli((x+y)*1000);
            curLocalDateTime = LocalDateTime.from(formatter.parse(instant.toString()));
            if (curLocalDateTime.getHour() <= 15) {
                useTime = curLocalDateTime.getHour();
                while (useTime < 13) {
                    useTime += 3;
                }
            } else {
                useTime = curLocalDateTime.getHour();
            }

            for (int i = 0, size = jsonArray.length(); i < size; i++) {
                x = Integer.parseInt(jsonArray.getJSONObject(i).get("dt").toString().trim());
                y = Integer.parseInt(weeksWeatherJsonData.getJSONObject("city")
                        .getString("timezone"));
                instant = Instant.ofEpochMilli((x+y)*1000);
                curLocalDateTime = LocalDateTime.from(formatter.parse(instant.toString()));

                if (day.isEmpty() && curLocalDateTime.getHour() == useTime) {
                    day.add(curLocalDateTime.getDayOfWeek().toString().substring(0, 3));
                    iconCode.add(jsonArray.getJSONObject(i).getJSONArray("weather")
                            .getJSONObject(0).getString("icon"));
                    minTemp.add(jsonArray.getJSONObject(i).getJSONObject("main")
                            .getString("temp_min"));
                    maxTemp.add(jsonArray.getJSONObject(i).getJSONObject("main")
                            .getString("temp_max"));
                } else if (curLocalDateTime.getHour() == 13 || curLocalDateTime.getHour() == 14 ||
                        curLocalDateTime.getHour() == 15) {
                    day.add(curLocalDateTime.getDayOfWeek().toString().substring(0, 3));
                    iconCode.add(jsonArray.getJSONObject(i).getJSONArray("weather")
                            .getJSONObject(0).getString("icon"));
                    minTemp.add(jsonArray.getJSONObject(i).getJSONObject("main")
                            .getString("temp_min"));
                    maxTemp.add(jsonArray.getJSONObject(i).getJSONObject("main")
                            .getString("temp_max"));
                }
            }

            weekWeatherItem = new WeekWeatherItem(day, iconCode, maxTemp, minTemp);

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (weekWeatherItem.getIconBitmap() != null) {
                        weekWeatherItem.getIconBitmap().clear();
                    }
                    HttpRequests httpRequests = new HttpRequests();
                    for (int i = 0, size = weekWeatherItem.getIconCode().size(); i < size; i++) {
                        String url = String.format("https://openweathermap.org/img/wn/%s@2x.png",
                                weekWeatherItem.getIconCode().get(i));
                        weekWeatherItem.setIconBitmap(httpRequests.getImageFromUrl(url));
                    }
                }
            };
            CustomCallbackTask customCallbackTask = new CustomCallbackTask(runnable,
                    (WeatherDataReadyCallbacks) this,
                    requireActivity(), "WEEKICONS");
            Thread thread = new Thread(customCallbackTask);
            thread.start();
        } catch (Exception e) {
            Log.d("EXCEPTION: ", e.getMessage());
        }
    }

    public void setWeeksWeatherRecViewAdapter() {
        Drawable drawable = ContextCompat.getDrawable(requireContext(),
                R.drawable.recycler_view_divider);
        int inset = getResources().getDimensionPixelSize(R.dimen.weather_card_view_divider_margin);
        InsetDrawable insetDivider = new InsetDrawable(drawable, inset, 0, inset, 0);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireContext(),
                LinearLayout.VERTICAL);
        dividerItemDecoration.setDrawable(insetDivider);

        weeksWeatherRecView.setHasFixedSize(true);
        if (weeksWeatherRecView.getAdapter() == null) {
            weeksWeatherRecView.addItemDecoration(dividerItemDecoration);
        }
        WeeksWeatherAdapter adapter = new WeeksWeatherAdapter(weekWeatherItem.getDay(),
                weekWeatherItem.getIconBitmap(), weekWeatherItem.getTemperatureHigh(),
                weekWeatherItem.getTemperatureLow());
        weeksWeatherRecView.setAdapter(adapter);

        hideInitialLoadingViews();
    }

    public void setBackground(String conditions, String dayNight) {
        BitmapDrawable bitmapDrawable;
        Bitmap bitmap;
        if (conditions.toLowerCase().contains("rain")) {
            if (dayNight.equals("d")) {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rainy_day);
            } else {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rainy_night);
            }
            bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
            constraintLayout.setBackground(bitmapDrawable);
        } else if (conditions.toLowerCase().contains("cloud")) {
            if (dayNight.equals("d")) {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cloudy_day);
            } else {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cloudy_night);
            }
            bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
            constraintLayout.setBackground(bitmapDrawable);
        } else if ( conditions.toLowerCase().contains("snow")) {
            if (dayNight.equals("d")) {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.snowy_day);
            } else {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.snowy_night);
            }
            bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
            constraintLayout.setBackground(bitmapDrawable);
        } else if (conditions.toLowerCase().contains("clear")) {
            if (dayNight.equals("d")) {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.clear_sky_day);
            } else {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.clear_sky_night);
            }
            bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
            constraintLayout.setBackground(bitmapDrawable);
        } else {
            if (dayNight.equals("d")) {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.clear_sky_day);
            } else {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.clear_sky_night);
            }
            bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
            constraintLayout.setBackground(bitmapDrawable);
        }
    }

    public void hideInitialLoadingViews() {
        if (requireView().findViewById(R.id.weatherHomeLoadingIv).getVisibility() == View.VISIBLE) {
            ImageView imageView = requireView().findViewById(R.id.weatherHomeLoadingIv);
            imageView.setVisibility(View.GONE);
            ProgressBar progressBar = requireView().findViewById(R.id.weatherHomeLoadingPb);
            progressBar.setVisibility(View.GONE);
            TextView textView = requireView().findViewById(R.id.weatherHomeLoadingLinkTv);
            textView.setVisibility(View.GONE);
        }
    }
}