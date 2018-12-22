package com.huris.simpleweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.huris.simpleweather.gson.Basic;
import com.huris.simpleweather.gson.Forecast;
import com.huris.simpleweather.gson.Weather;
import com.huris.simpleweather.service.AutoUpdateService;
import com.huris.simpleweather.util.HttpUtil;
import com.huris.simpleweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.huris.simpleweather.util.TimeUtil.getWeekOfDate;
import static com.huris.simpleweather.util.TimeUtil.stringToDate;

public class IdleActivity extends AppCompatActivity {
    private double mapLocalLatitude;

    private double mapLocalLongitude;

    private StringBuilder mapLocalAddress;

    private BaiduMap baiduMap;

    private MapView mapView;

    // 定义一个mWeatherId用于记录城市的天气id
    private String mWeatherId;

    private ScrollView weatherLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        // 尝试从本地缓存中读取数据
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            // 有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            // 第一次肯定是没有缓存的,因此就会从Intent中取出天气id,并调用requestWeather()方法从服务器中请求天气数据
            // 无缓存时去服务器查询天气
            mWeatherId = getIntent().getStringExtra("weather_id");
            // 注意请求数据的时候需要先将ScrollView进行隐藏,不然空数据的界面看上去会很奇怪
//            String weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }

        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_map);
        // 接下来调用findViewById()方法获得了MapView的实例
        mapView = (MapView) findViewById(R.id.bmapView);
        // baiMap是地图的总控制器,调用getMap()方法就能获取到BaiduMap的实例
        // 有了BaiduMap之后我们就能进行各种各样的操作,比如设置地图的缩放级别以及将地图移动到某一个经纬度上
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        navigateTo();
    }

    private void navigateTo() {

        Toast.makeText(this, "nav to " + mapLocalAddress, Toast.LENGTH_SHORT).show();
        // LatLng的构造方法接收两个参数,第一个参数是纬度值,第二个参数是经度值
        LatLng ll = new LatLng(mapLocalLatitude, mapLocalLongitude);
        // 之后调用MapStatusUpdateFactory的newLatLng()方法将LatLng对象传入
        // newLatLng对象返回的也是一个MapStatusUpdate对象
        MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
        // 我们把这个对象传入BaiduMap的animateMApStatus方法中,就可以将地图移动到指定的经纬度上了
        baiduMap.animateMapStatus(update);
        // 百度地图将缩放级别的取值限定在3到19之间,其中小数点的值也是可以取的,值越大,地图显示的信息就越精细
        // 其中MapStatusUpdateFactory的zoomTo()方法接收一个float型的参数用于设置缩放级别
        update = MapStatusUpdateFactory.zoomTo(17f);
        // zoomTo放回一个MapStatusUpdate对象,我们把这个对象传入BaiduMap的animateMapStatus()方法中即可完成缩放功能
        baiduMap.animateMapStatus(update);
        // 使用了一个isFirstLocate变量,这个变量的作用是为了防止多次调用animateMapStatus()方法
        // 因此将地图移动到我们当前位置只需要在程序第一次定位的时候调用一次就可以了
        // 注意,下面这段逻辑必须写到isFirstLocation这个if条件语句的外面
        // 因为让地图移动到我们当前位置只需要在第一次定位的时候执行,但是设备在地图上显示的位置应该是实时变化的
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        // 将Location中包含的经度和纬度分别封装到了MyLocationData.Builder当中
        locationBuilder.latitude(mapLocalLatitude);
        locationBuilder.longitude(mapLocalLongitude);
        MyLocationData locationData = locationBuilder.build();
        // 最后把MyLocation设置到了BaiduMap的setMyLocationData()方法中
        baiduMap.setMyLocationData(locationData);
    }

    private void showWeatherInfo(Weather weather) {
        Basic basic = weather.basic;
        mapLocalAddress = new StringBuilder();
        mapLocalAddress.append(basic.countryId + basic.provinceId + "省" + basic.cityId + "市");
        if (basic.cityId.equals(basic.cityName)) {
            mapLocalAddress.append("市区");
        } else {
        }
        mapLocalLatitude = Double.valueOf(basic.latitude.toString());
        mapLocalLongitude = Double.valueOf(basic.longitude.toString());
    }
    /**
     * 根据天气id请求城市天气信息。
     */
    public void requestWeather(final String weatherId) {
        // 首先使用了参数传入的天气id和之前申请好的APIKey拼装出一个接口地址
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=ae0a16289a764e9484e91f6521a37573";
        // 接着调用HttpUtil.sendOkHttpRequest()方法来向该地址发出请求
        // 服务器会将相应城市的天气信息以JSON格式返回
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                // 在onResponse()回调中先调用Utility.handleWeatherResponse(),将返回的JSON数据转换成Weather对象
                final Weather weather = Utility.handleWeatherResponse(responseText);
                // 再将当前线程切换回主线程
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 如果服务器返回的status状态是"ok",就说明请求天气成功了
                        if (weather != null && "ok".equals(weather.status)) {
                            // 成功后将返回的数据存到SharedPreferences中
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(IdleActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            mWeatherId = weather.basic.weatherId;
                            // 需要调用showWeatherInfo()方法来进行内容的显示
                            showWeatherInfo(weather);
                        } else {
                        }
                        // 在请求结束后,还需要调用SwipeRefreshLayout的setRefreshing()方法并传入false
                        // 用于表示刷新事件的结束,并隐藏刷新进度条
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        });
    }

}


