package com.huris.simpleweather.gson;

import com.google.gson.annotations.SerializedName;

public class Forecast {

    /**
     * 日期
     */
    public String date;

    /**
     * 温度
     */
    @SerializedName("tmp")
    public Temperature temperature;

    /**
     * 天气情况
     */
    @SerializedName("cond")
    public More more;

    public class Temperature {

        public String max;

        public String min;

    }

    public class More {

        @SerializedName("txt_d")
        public String info;

    }

}