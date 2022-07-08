package com.example.weather.util;

import org.json.JSONException;

public interface HttpCallbackListener {
    void onFinish(String response) throws JSONException;
    void onError(Exception e);
}
