package com.huris.simpleweather.gson;

import com.google.gson.annotations.SerializedName;

public class other {

    /**
     * 风速
     */
    @SerializedName("wind_spd")
    public String windSpeed;

    /**
     * 能见度
     */
    @SerializedName("vis")
    public String visibility;

    /**
     * 降水量
     */
    @SerializedName("pcpn")
    public String precipitation;


}