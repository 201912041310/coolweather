package com.coolweather.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.service.AutoUpdateService;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private static final String TAG ="WeatherActivity" ;

    private ImageView bingPicImg;

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

    private TextView originText;

    public SwipeRefreshLayout swipeRefreshLayout;
    public DrawerLayout drawerLayout;

    private Button navButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        //初始化控件

        bingPicImg = findViewById(R.id.bing_pic_img);
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

        originText =findViewById(R.id.text_origin);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        drawerLayout = findViewById(R.id.drawer_layout);

        navButton = findViewById(R.id.button_nav);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);

        final String weatherId ;

        if (
                //是否有历史查询
                weatherString != null
//        false
        ) {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
            weatherId = weather.basic.cId;
        } else {
            //无缓存时去服务器查询天气
            weatherId = getIntent().getStringExtra("weather_id");
            requestWeather(weatherId);

        }

        String bingPic = prefs.getString("bing_pic",null);
        if (
                //查询是否有图片
                 bingPic!=null
//        false
        ){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });
    }

    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });

            }
        });
    }

    public void requestWeather(String weatherId) {
        String weatherUrl = "https://free-api.heweather.net/s6/weather/now?location=" + weatherId + "&key=de9d992588664ac28c3cc3fd23421541";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
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
                            originText.setText("服务器返回的数据其实是：\r\n"+responseText+"\r\n而我们把它们提取成上面那样");
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败"+weather.status, Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    //处理并展示Weather实体类中的数据
    private void showWeatherInfo(Weather weather) {
        if (weather!=null&&"ok".equals(weather.status)) {
            updateTime.setText("更新时间：" + weather.update.updateTime);
            location.setText("位置：" + weather.basic.locationName);
            cid.setText("位置编号：" + weather.basic.cId);
            condition.setText("天气：" + weather.now.condition_txt);
            minimumTemperature.setText("最低温度：" + weather.now.minimumTemperature + "℃");
            maximumTemperature.setText("最高温度：" + weather.now.maximumTemperature + "℃");
            nowTimeTemperature.setText("当前温度：" + weather.now.nowTimeTemperature + "℃");
            windDegree.setText("风强：" + weather.now.windDegree + "级");
            windDirection.setText("风向：" + weather.now.windDirection);
            windSpeed.setText("风速：" + weather.now.windSpeed + "m/s");
            Intent intent = new Intent(this, AutoUpdateService.class);
            startService(intent);
        }else {
            Toast.makeText(this, "获取天气信息失败："+weather.status, Toast.LENGTH_SHORT).show();
        }
    }

//    @Override
//    public void onBackPressed() {
//        Intent intent = new Intent(this,MainActivity.class);
//        startActivity(intent);
//        finish();
//    }
}
