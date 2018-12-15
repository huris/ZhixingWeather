package com.huris.simpleweather.gson;

import com.google.gson.annotations.SerializedName;

public class Suggestion {

    /**
     * 舒适程度
     */
    @SerializedName("comf")
    public Comfort comfort;

    /**
     * 洗车情况
     */
    @SerializedName("cw")
    public CarWash carWash;

    /**
     * 运动
     */
    public Sport sport;

    public class Comfort {

        @SerializedName("txt")
        public String info;

    }

    public class CarWash {

        @SerializedName("txt")
        public String info;

    }

    public class Sport {

        @SerializedName("txt")
        public String info;

    }

}
