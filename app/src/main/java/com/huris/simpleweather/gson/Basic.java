package com.huris.simpleweather.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {

    // 由于JSON中的一些字段可能不太适合作为java的字段来命名
    // 因此这里使用了@SerializedName注解的方式来让JSON字段和JAVA之间建立映射关系
    /**
     * 国家id
     */
    @SerializedName("cnty")
    public String countryId;

    /**
     * 省id
     */
    @SerializedName("admin_area")
    public String provinceId;


    /**
     * 城市id
     */
    @SerializedName("parent_city")
    public String cityId;

    /**
     * 城市名
     */
    @SerializedName("city")
    public String cityName;

    /**
     * 城市id
     */
    @SerializedName("id")
    public String weatherId;

    /**
     * 经度
     */
    @SerializedName("lon")
    public String longitude;

    /**
     * 纬度
     */
    @SerializedName("lat")
    public String latitude;

    /**
     * 更新时间
     */
    public Update update;

    /**
     * 内部类
     */
    public class Update {

        @SerializedName("loc")
        public String updateTime;

    }

}
