package com.huris.simpleweather.gson;

public class AQI {

    public AQICity city;

    public class AQICity {

        public String aqi;

        public String pm25;

        // 新加的一个,显示空气质量
        public String qlty;
    }

}