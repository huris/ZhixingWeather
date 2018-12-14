package com.huris.simpleweather.db;

import org.litepal.crud.DataSupport;

public class City extends DataSupport {
    private int id; //id是每个实体类中都应该有的字段
    private String cityName;  //记录市的名字
    private int cityCode;   //记录市的代号
    private int provinceId; //记录当前市所属省的id值

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }
}
