package com.huris.simpleweather.gson;

import com.google.gson.annotations.SerializedName;

public class Now {

    // 显示温度
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More {

        // 显示当前的天气状况
        @SerializedName("txt")
        public String info;

    }

}