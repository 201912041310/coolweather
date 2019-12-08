package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {
    @SerializedName("location")
    public String locationName;

    @SerializedName("cid")
    public String cId;
}
