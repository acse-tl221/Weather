package com.example.weather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.example.weather.Gson.Weather;
import com.example.weather.db.weatherDB;
import com.example.weather.model.City;
import com.example.weather.model.County;
import com.example.weather.model.Province;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class dataUtils {


    public static boolean handleProvincesResponse(weatherDB weatherdb, String response) throws JSONException {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allProvinces = new JSONArray(response);
                for (int i = 0; i < allProvinces.length(); i++) {
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(String.valueOf(provinceObject.getInt("id")));
                    weatherdb.saveProvince(province);
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    /**
     * 解析和处理服务器返回的市级数据
     */
    public static boolean handleCitiesResponse(weatherDB weatherdb,
                                               String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCities = new JSONArray(response);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(String.valueOf(cityObject.getInt("id")));
                    city.setProvinceId(provinceId);
                    weatherdb.saveCity(city);
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    /**
     * 解析和处理服务器返回的县级数据
     */
    public static boolean handleCountiesResponse(weatherDB weatherdb, String response, int cityId)
    {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCounties = new JSONArray(response);
                for (int i = 0; i < allCounties.length(); i++) {
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setCountyCode(String.valueOf(countyObject.getString("weather_id")));
                    county.setCityId(cityId);
                    weatherdb.saveCounty(county);
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

//    public static void handleWeatherResponse(Context context, String response) {
//        try {
//            JSONObject jsonObject = new JSONObject(response);
//            JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
//            String cityName = weatherInfo.getString("city");
//            String weatherCode = weatherInfo.getString("cityid");
//            String temp1 = weatherInfo.getString("temp1");
//            String temp2 = weatherInfo.getString("temp2");
//            String weatherDesp = weatherInfo.getString("weather");
//            String publishTime = weatherInfo.getString("ptime");
//            saveWeatherInfo(context, cityName, weatherCode, temp1, temp2,
//                    weatherDesp, publishTime);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    public static Weather handleWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, Weather.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveWeatherInfo(Context context, String cityName,
                                       String weatherCode, String temp1, String temp2, String weatherDesp, String
                                               publishTime)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日",
                Locale.CHINA);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected", true);
        editor.putString("city_name", cityName);
        editor.putString("weather_code", weatherCode);
        editor.putString("temp1", temp1);
        editor.putString("temp2", temp2);
        editor.putString("weather_desp", weatherDesp);
        editor.putString("publish_time", publishTime);
        editor.putString("current_date", sdf.format(new Date()));
        editor.commit();
    }
}
