package com.coolweather.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private static final String TAG ="WeatherActivity" ;

    private TextView updateTime;
    private TextView location;
    private TextView cid;
    private TextView condition;
    private TextView minimumTemperature;
    private TextView maximumTemperature;
    private TextView nowTimeTemperature;
    private TextView windDegree;
    private TextView windDirection;
    private TextView windSpeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        //初始化控件
        updateTime = findViewById(R.id.text_update_time);
        location = findViewById(R.id.text_location);
        cid = findViewById(R.id.text_cid);
        condition = findViewById(R.id.text_condition);
        minimumTemperature = findViewById(R.id.text_minimum_temperature);
        maximumTemperature = findViewById(R.id.text_maximum_temperature);
        nowTimeTemperature = findViewById(R.id.text_now_time_temperature);
        windDegree = findViewById(R.id.text_wind_degree);
        windDirection = findViewById(R.id.text_wind_direction);
        windSpeed = findViewById(R.id.text_wind_speed);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (//weatherString != null
        false) {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        } else {
            //无缓存时去服务器查询天气
            String weatherId = getIntent().getStringExtra("weather_id");
            requestWeather(weatherId);

        }
    }

    private void requestWeather(String weatherId) {
        String weatherUrl = "https://free-api.heweather.net/s6/weather/now?location=" + weatherId + "&key=de9d992588664ac28c3cc3fd23421541";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather=Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather!=null&&"ok".equals(weather.status)){
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else Toast.makeText(WeatherActivity.this, "获取天气信息失败"+weather.status, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    //处理并展示Weather实体类中的数据
    private void showWeatherInfo(Weather weather) {
        updateTime.setText("更新时间："+weather.update.updateTime);
        location.setText("位置："+weather.basic.locationName);
        cid.setText("位置编号："+weather.basic.cId);
        condition.setText("天气："+weather.now.condition_txt);
        minimumTemperature.setText("最低温度："+weather.now.minimumTemperature+"℃");
        maximumTemperature.setText("最高温度："+weather.now.maximumTemperature+"℃");
        nowTimeTemperature.setText("当前温度："+weather.now.nowTimeTemperature+"℃");
        windDegree.setText("风强："+weather.now.windDegree+"级");
        windDirection.setText("风向："+weather.now.windDirection);
        windSpeed.setText("风速："+weather.now.windSpeed+"m/s");
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        this.finish();
    }
}
