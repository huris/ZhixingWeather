package com.huris.simpleweather.db;

import com.google.gson.annotations.SerializedName;

import org.litepal.crud.DataSupport;

public class Province extends DataSupport {

    private String provinceName;  //provinceName记录省的名字

    private int provinceCode;   //provinceCode记录省的代号

    private int id;     //id是每个实体类中都应该有的字段



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }


    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }
}
