package com.example.weather.service;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import com.example.weather.R;
import com.example.weather.activity.WeatherActivity;
import com.example.weather.Gson.Weather;
import com.example.weather.util.HttpCallbackListener;
import com.example.weather.util.HttpUtil;
import com.example.weather.util.dataUtils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UpdateService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8 * 60 * 60 * 1000; // 这是8小时的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, UpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 更新天气信息。
     */
    private void updateWeather(){
        String address = "";
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            Weather weather =dataUtils.handleWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId;
            address = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
        }
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
                    final Weather weather = dataUtils.handleWeatherResponse(response);
                            if (weather != null) {
                                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(UpdateService.this).edit();
                                editor.putString("weather", response);
                                editor.apply();
                            }
            }
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }
}

