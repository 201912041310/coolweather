package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class Now {

    @SerializedName("cond_txt")
    public String condition_txt;

    @SerializedName("fl")
    public String minimumTemperature;

    @SerializedName("hum")
    public String maximumTemperature;

    @SerializedName("tmp")
    public String nowTimeTemperature;

    @SerializedName("wind_sc")
    public String windDegree;

    @SerializedName("wind_dir")
    public String  windDirection;

    @SerializedName("wind_spd")
    public String windSpeed;


}
