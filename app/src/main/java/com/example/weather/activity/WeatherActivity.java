package com.example.weather.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weather.Gson.Forecast;
import com.example.weather.Gson.Weather;
import com.example.weather.R;
import com.example.weather.service.UpdateService;
import com.example.weather.util.HttpCallbackListener;
import com.example.weather.util.HttpUtil;
import com.example.weather.util.dataUtils;

public class WeatherActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    private ImageView bingPicImg;

    private String mWeatherId;
    /**
     * 切换城市按钮
     */
    private Button switchCity;
    /**
     * 更新天气按钮
     */
    private Button refreshWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        //隐藏标题栏
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        setContentView(R.layout.activity_weather);
        // 初始化各控件
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        switchCity = (Button) findViewById(R.id.switch_city);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//去服务器查询天气
        mWeatherId = getIntent().getStringExtra("weather_id");
        System.out.println(mWeatherId);
        queryWeatherInfo(mWeatherId);
        switchCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WeatherActivity.this, AreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * 查询县级代号所对应的天气代号。
     */

    private void queryWeatherInfo(String weatherCode) {
        showProgressDialog();
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherCode + "&key=bc0418b57b2d4918819d3974ac1285d9";
        queryFromServer(weatherUrl, "weatherCode");
    }
    /**
     * 根据传入的地址和类型去向服务器查询天气代号或者天气信息。
     */
    private void queryFromServer(final String address, final String type) {
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(final String response) {
            if ("weatherCode".equals(type)) {// 处理服务器返回的天气信息
                System.out.println(response);
                final Weather weather = dataUtils.handleWeatherResponse(response);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            mWeatherId = weather.basic.weatherId;
                            editor.putString("weather", response);
                            editor.apply();
                            //System.out.println(mWeatherId);
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                       // swipeRefresh.setRefreshing(false);
                    }
                });
                }
            }
            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "请求失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 处理并展示Weather实体类中的数据。
     */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        System.out.println(cityName+updateTime);
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运行建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
        closeProgressDialog();
        Intent intent = new Intent(this, UpdateService.class);
        startService(intent);
    }
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在更新天气数据...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}