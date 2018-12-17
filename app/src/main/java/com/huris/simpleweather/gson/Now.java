package com.huris.simpleweather.gson;

import com.google.gson.annotations.SerializedName;

public class Now {

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

    /**
     * 显示温度
     */
    @SerializedName("tmp")
    public String temperature;

    /**
     * 显示当前的天气状况
     */
    @SerializedName("cond")
    public More more;

    public class More {

        /**
         * 显示当前的天气状况
         */
        @SerializedName("txt")
        public String info;

    }

}