package com.huris.simpleweather.util;

import android.text.TextUtils;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.huris.simpleweather.db.City;
import com.huris.simpleweather.db.County;
import com.huris.simpleweather.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

// 由于服务器返回的省市县数据都是JSON格式的,所以我们最好再提供一个工具类来解析和处理这种数据
// 在util包下新建一个Utility类
public class Utility {
    /**
     * 解析和处理服务器返回的省级数据
     */
    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                // 使用gson解析获得的response
                Gson gson = new Gson();
                // 生成所有省的列表
                List<Province> allProvinces = gson.fromJson(response,
                        new TypeToken<List<Province>>() {
                        }.getType());
                for (Province province : allProvinces) {
                    // 将数据保存到数据库中
                    province.save();
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的市级数据
     */
    public static boolean handleCityResponse(String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                // 使用gson解析获得的response
                Gson gson = new Gson();
                // 生成所有市的列表
                List<City> allCities = gson.fromJson(response,
                        new TypeToken<List<City>>() {
                        }.getType());
                for (City city : allCities) {
                    // 将数据保存到数据库中
                    city.save();
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的县级数据
     */
    public static boolean handleCountyResponse(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                // 使用gson解析获得的response
                Gson gson = new Gson();
                // 生成所有县的列表
                List<County> allCounties = gson.fromJson(response,
                        new TypeToken<List<County>>() {
                        }.getType());
                for (County county : allCounties) {
                    // 将数据保存到数据库中
                    county.save();
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}