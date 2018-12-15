package com.huris.simpleweather.gson;

public class AQI {

    public AQICity city;

    public class AQICity {

        /**
         * 显示空气指数
         */
        public String aqi;

        /**
         * 显示pm2.5情况
         */
        public String pm25;

        /**
         * 新加的一个,显示空气质量
         */
        public String qlty;
    }

}