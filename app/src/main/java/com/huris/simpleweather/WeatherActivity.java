package com.huris.simpleweather;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.bumptech.glide.Glide;
import com.huris.simpleweather.db.City;
import com.huris.simpleweather.db.County;
import com.huris.simpleweather.db.Province;
import com.huris.simpleweather.gson.Basic;
import com.huris.simpleweather.gson.Forecast;
import com.huris.simpleweather.gson.Weather;
import com.huris.simpleweather.service.AutoUpdateService;
import com.huris.simpleweather.util.HttpUtil;
import com.huris.simpleweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.huris.simpleweather.util.TimeUtil.getWeekOfDate;
import static com.huris.simpleweather.util.TimeUtil.stringToDate;

public class WeatherActivity extends AppCompatActivity {

    private int flag = 0;

    private StringBuilder localPosition;

    private String loCounty;

    private Province loProvince;

    private City loCity;

    private List<String> dataList = new ArrayList<>();

    /**
     * 省列表
     */
    private List<Province> provinceList;

    /**
     * 市列表
     */
    private List<City> cityList;

    /**
     * 县列表
     */
    private List<County> countyList;

    // 定位国家
    private String localNation;

    // 定位省
    private String localProvince;

    // 定位城市
    private String localCity;

    // 定位区
    private String localDistrict;

    private TextView windSpeedText;

    private TextView visibilityText;

    private TextView precipitationText;

    private Button mapPosition;

    public LocationClient mLocationClient;

    private TextView positionText;

    public DrawerLayout drawerLayout;

    public SwipeRefreshLayout swipeRefresh;

    private ScrollView weatherLayout;

    private Button navButton;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView qualityText;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    private ImageView bingPicImg;

    // 定义一个mWeatherId用于记录城市的天气id
    private String mWeatherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (Build.VERSION.SDK_INT >= 21) {
//            // 调用getWindow().getDecorView()方法拿到当前活动的DecorView
//            View decorView = getWindow().getDecorView();
//            // 再调用它的setSystemUiVisibility()方法来改变系统UI的显示
//            // View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN和
//            // View.SYSTEM_UI_FLAG_LAYOUT_STABLE表示活动的布局会显示在状态栏上面
//            decorView.setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//            // 调用setStatusBarColor()方法将状态栏设置成透明色
//            getWindow().setNavigationBarColor(Color.TRANSPARENT);
//            getWindow().setStatusBarColor(Color.TRANSPARENT);
//        }

        setContentView(R.layout.activity_weather);
        // 初始化各控件,获取各控件的实例

        positionText = (TextView) findViewById(R.id.position_text_view);

        // 获取新增控件ImageView的实例
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);

        // 实况天气
        windSpeedText = (TextView) findViewById(R.id.windSpeed_text);
        visibilityText = (TextView) findViewById(R.id.vis_text);
        precipitationText = (TextView) findViewById(R.id.precipitation_text);

        // 空气质量
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        qualityText = (TextView) findViewById(R.id.quality_text);

        // 生活建议
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);

        // 首先需要在onCreat()方法中获取到了SwipeRefreshLayout的实例
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        // 然后调用setColorSchemeResources()方法来设置下拉刷新进度条的颜色
        // 这里我们就使用colorPrimary作为进度条的颜色
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        // 首先获取到DrawerLayout和Button的实例
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//        navButton = (Button) findViewById(R.id.nav_button);

        mapPosition = (Button) findViewById(R.id.map_position);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setLogo(R.drawable.local);

        // 当按钮被按下时,切换到地图视图
        mapPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WeatherActivity.this, MapActivity.class);
                startActivity(intent);
//                Toast.makeText(WeatherActivity.this, "hello", Toast.LENGTH_SHORT).show();
            }
        });
        // 首先创建一个LocationClient的实例
        // LocationClient的构建函数接收一个Context参数,
        // 这里调用getApplicationContext()方法来获取一个全局的Context参数并传入
        mLocationClient = new LocationClient(getApplicationContext());
        // 之后调用LocationClient的registerLocationListener()方法来注册一个定位监听器
        // 当获取到位置信息的时候,就会回调这个定位监听器
        mLocationClient.registerLocationListener(new MyLocationListener());
        // 创建一个空的List集合,然后依次判断这3个权限有没有被授权,如果没有被授权就添加到List集合中
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(WeatherActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(WeatherActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(WeatherActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            // 最后将List转换成数组,再调用ActivityCompat.requestPermissions()方法一次性申请
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(WeatherActivity.this, permissions, 1);
        } else {
            requestLocation();
        }


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
        // 调用一个setOnRefreshListener()方法设置一个下拉刷新的监听器
        // 当触发下拉刷新操作的时候,就会回调这个监听器的onRefresh()方法
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                              @Override
                                              public void onRefresh() {
                                                  // 此处我们调用requesWeather()方法请求天气信息就可以了
                                                  requestWeather(mWeatherId);
                                              }
                                          }
        );
        // 在Button的点击事件中调用DrawerLayout的openDrawer()方法打开滑动菜单就可以了
//        navButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                drawerLayout.openDrawer(GravityCompat.START);
//            }
//        });
        // 尝试从SharedPreferences中读取缓存背景图片
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            // 如果有缓存的话直接使用Glide来加载这张图片
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            // 没有的话就调用loadBingPic()方法去请求今日的必应背景图
            loadBingPic();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        // 通过一个循环将申请的每个权限都进行了判断
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            // 如果有任何一个权限被拒绝,就直接调用finish()方法关闭当前程序
                            finish();
                            return;
                        }
                    }
                    // 只有当所有权限都被用户同意了,才会调用requestLocation()方法开始地理位置定位
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 最后要记得活动被销毁的时候一定要调用stop方法来停止定位,
        // 不然程序会在后台不停地进行定位,从而严重消耗手机的电能
        mLocationClient.stop();
//        mapView.onDestroy();
//        baiduMap.setMyLocationEnabled(false);
    }

    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    private void initLocation() {
        // 在initLocation()方法中我们创建一个LocationClientOption对象
        LocationClientOption option = new LocationClientOption();
        // 之后调用它的setScanSpan()方法来设置更新的间隔
        option.setScanSpan(1000); // 这里传入1000,表示1000ms进行一个更新
        // 此处调用了LocationClientOption的setIsNeedAddress()方法,并传入true
        // 表示我们需要获取当前位置详细的地址信息
        option.setIsNeedAddress(true);
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        mLocationClient.setLocOption(option);
    }

    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(final BDLocation location) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    localPosition = new StringBuilder();
//                    addressName = new StringBuilder();
//                    addressName.append(location.getDistrict()).append(location.getStreet());
//                    getSupportActionBar().setSubtitle(addressName);
                    localPosition.append("位置：").append(location.getAddrStr()).append("\n");
                    localPosition.append("经度：").append(location.getLongitude()).append("   ");
                    localPosition.append("纬度：").append(location.getLatitude()).append("\n");
                    // 获取当前地址的详细信息
                    localNation = location.getCountry();
                    localProvince = location.getProvince();
                    localCity = location.getCity();
                    localDistrict = location.getDistrict();


//                    currentPosition.append("国家：").append(location.getCountry()).append("   ");
//                    currentPosition.append("省份：").append(location.getProvince()).append("\n");
//                    currentPosition.append("市：").append(location.getCity()).append("   ");
//                    currentPosition.append("区：").append(location.getDistrict()).append("\n");
//                    currentPosition.append("街道：").append(location.getBuildingName()).append("  ");
                    localPosition.append("定位方式：");
                    if (location.getLocType() == BDLocation.TypeGpsLocation) {
                        localPosition.append("GPS");
                    } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                        localPosition.append("网络");
                    }
                }
            });
        }

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
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            mWeatherId = weather.basic.weatherId;
                            // 需要调用showWeatherInfo()方法来进行内容的显示
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        // 在请求结束后,还需要调用SwipeRefreshLayout的setRefreshing()方法并传入false
                        // 用于表示刷新事件的结束,并隐藏刷新进度条
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        // 每次请求天气信息的时候也会刷新背景图片
        loadBingPic();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.local_city:
                this.swipeRefresh.setRefreshing(true);
                // 查找对应的省份
                loProvince = DataSupport.where("provincename = ?", localProvince.substring(0, 2)).findFirst(Province.class);
                // 查找对应的城市
                cityList = DataSupport.where("provinceid = ?", String.valueOf(loProvince.getId())).find(City.class);
                loCity = null;
                // 在对应的城市中查找相应的县
                for (City city : cityList) {
                    if (city.getCityName().equals(localCity.substring(0, 2))) {
                        loCity = city;
                        break;
                    }
                }
                countyList = DataSupport.where("cityid = ?", String.valueOf(loCity.getId())).find(County.class);
                // 如果是市中心的区,则显示该城市的天气
                loCounty = countyList.get(0).getWeatherId();
                for (County county : countyList) {
                    if (county.getCountyName().equals(localDistrict.substring(0, 2))) {
                        // 如果是该城市的县,则显示该城市的县的天气
                        loCounty = county.getWeatherId();
                        break;
                    }
                }
                this.requestWeather(loCounty);
                break;
        }
        return true;
    }


    /**
     * 根据传入的地址和类型从服务器上查询省市县数据。
     */
    private void queryFromServer(final String address, final String type) {
        // 调用HttpUtil的sendOkHttpRequest()方法向服务器发送请求
        // 响应的数据会送到onResponse()方法中
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, loProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, loCity.getId());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                queryFromServer(address, type);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
//         首先调用HttpUtil.sendOkHttpRequest()来获取必应背景图的链接
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
//                Toast.makeText(WeatherActivity.this,bingPic, Toast.LENGTH_SHORT).show();
//                final String bingPic = "https://source.unsplash.com/random";
                // 之后将这个连接缓存到SharedPreferences当中
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                // 将当前线程切换回主线程
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 使用Glide来加载这张图片就可以了
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 处理并展示Weather实体类中的数据。
     * 从Weather对象中获取数据,然后显示在相应的控件上
     * td
     */

    // TODO: 2018/12/21 Huris
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        StringBuilder currentPosition = new StringBuilder();
        Basic basic = weather.basic;
        currentPosition.append("位置：").append(basic.countryId + basic.provinceId + "省" + basic.cityId + "市");
        if (basic.cityId.equals(basic.cityName)) {
            currentPosition.append("市区").append("\n");
        } else {
            currentPosition.append(basic.cityName).append("\n");
        }
        currentPosition.append("经度：").append(basic.longitude.substring(0, 10)).append("   ");
        currentPosition.append("纬度：").append(basic.latitude.substring(0, 10));
        if(flag == 0)
            positionText.setText(currentPosition);
        getSupportActionBar().setTitle(cityName);
        StringBuilder updateTime = new StringBuilder();
        updateTime.append(weather.basic.update.updateTime.split(" ")[1]);
        updateTime.append("刷新");
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        // 在未来几天的天气预报的部分使用一个for循环来处理每天的天气信息
        int i = 0;
        String day = "";
        for (Forecast forecast : weather.forecastList) {
            // 循环中动态加载forecast_item.xml布局并进行设置相应的数据,然后添加到父布局当中
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            ImageView weatherImage = (ImageView) view.findViewById(R.id.weather_view);
            if (i == 0) day = "今天";
            else if (i == 1) day = "明天";
            else if (i == 2) day = "后天";
            i++;
            // 写好准确日期
            int MONTH = (forecast.date.charAt(5) - '0') * 10 + (forecast.date.charAt(6) - '0');
            int DAY = (forecast.date.charAt(8) - '0') * 10 + (forecast.date.charAt(9) - '0');
            dateText.setText(String.format("%02d.%02d/", MONTH, DAY) +
                    getWeekOfDate(stringToDate(forecast.date, "yyyy-MM-dd")) + "/" + day);
            infoText.setText(forecast.more.info);
            if (forecast.more.info.equals("晴")) {
                weatherImage.setImageResource(R.drawable.ic_qing);
            } else if (forecast.more.info.equals("多云")) {
                weatherImage.setImageResource(R.drawable.ic_duoyun);
            } else if (forecast.more.info.equals("少云")) {
                weatherImage.setImageResource(R.drawable.ic_shaoyun);
            } else if (forecast.more.info.equals("晴间多云")) {
                weatherImage.setImageResource(R.drawable.ic_qingjianduoyun);
            } else if (forecast.more.info.equals("阴")) {
                weatherImage.setImageResource(R.drawable.ic_yin);
            } else if (forecast.more.info.equals("阵雨")) {
                weatherImage.setImageResource(R.drawable.ic_zhenyu);
            } else if (forecast.more.info.equals("强阵雨")) {
                weatherImage.setImageResource(R.drawable.ic_qiangzhenyu);
            } else if (forecast.more.info.equals("雷阵雨")) {
                weatherImage.setImageResource(R.drawable.ic_leizhenyu);
            } else if (forecast.more.info.equals("强雷阵雨")) {
                weatherImage.setImageResource(R.drawable.ic_qiangleizhenyu);
            } else if (forecast.more.info.equals("小雨")) {
                weatherImage.setImageResource(R.drawable.ic_xiaoyu);
            } else if (forecast.more.info.equals("中雨")) {
                weatherImage.setImageResource(R.drawable.ic_zhongyu);
            } else if (forecast.more.info.equals("大雨")) {
                weatherImage.setImageResource(R.drawable.ic_dayu);
            } else if (forecast.more.info.equals("暴雨")) {
                weatherImage.setImageResource(R.drawable.ic_baoyu);
            } else if (forecast.more.info.equals("大暴雨")) {
                weatherImage.setImageResource(R.drawable.ic_dabaoyu);
            } else if (forecast.more.info.equals("特大暴雨")) {
                weatherImage.setImageResource(R.drawable.ic_tedabaoyu);
            } else if (forecast.more.info.equals("小雪")) {
                weatherImage.setImageResource(R.drawable.ic_xiaoxue);
            } else if (forecast.more.info.equals("中雪")) {
                weatherImage.setImageResource(R.drawable.ic_zhongxue);
            } else if (forecast.more.info.equals("大雪")) {
                weatherImage.setImageResource(R.drawable.ic_daxue);
            } else if (forecast.more.info.equals("暴雪")) {
                weatherImage.setImageResource(R.drawable.ic_baoxue);
            } else {
                weatherImage.setImageResource(R.drawable.ic_weizhi);
            }

            StringBuilder minTemperature = new StringBuilder("最低: ");
            minTemperature.append("<font color='#00ccff'><big>" + forecast.temperature.min + "</big></font>");
            minTemperature.append("℃");

            StringBuilder maxTemperature = new StringBuilder("最高: ");
            maxTemperature.append("<font color='#FF4500'><big>" + forecast.temperature.max + "</big></font>");
            maxTemperature.append("℃");

            maxText.setText(Html.fromHtml(String.valueOf(maxTemperature)));
            minText.setText(Html.fromHtml(String.valueOf(minTemperature)));
            forecastLayout.addView(view);
        }
        if (weather.aqi != null) {

            try {
                int aqiNum = Integer.parseInt(weather.aqi.city.aqi);
                if (aqiNum > 300) {
                    aqiText.setText(Html.fromHtml("<font color='#7e0023'><big>" + weather.aqi.city.aqi + "</big></font>"));
                } else if (aqiNum > 200) {
                    aqiText.setText(Html.fromHtml("<font color='#99004c'><big>" + weather.aqi.city.aqi + "</big></font>"));
                } else if (aqiNum > 150) {
                    aqiText.setText(Html.fromHtml("<font color='#ff0000'><big>" + weather.aqi.city.aqi + "</big></font>"));
                } else if (aqiNum > 100) {
                    aqiText.setText(Html.fromHtml("<font color='#ff7e00'><big>" + weather.aqi.city.aqi + "</big></font>"));
                } else if (aqiNum > 50) {
                    aqiText.setText(Html.fromHtml("<font color='#ffff00'><big>" + weather.aqi.city.aqi + "</big></font>"));
                }
            } catch (Exception e) {
                aqiText.setText(Html.fromHtml("<font color='#ff0000'><big>" + weather.aqi.city.aqi + "</big></font>"));
            }

            try {
                int pm25Num = Integer.parseInt(weather.aqi.city.pm25);
                if (pm25Num > 100) {
                    pm25Text.setText(Html.fromHtml("<font color='#f00082'><big>" + weather.aqi.city.pm25 + "</big></font>"));
                } else if (pm25Num > 85) {
                    pm25Text.setText(Html.fromHtml("<font color='#ff0000'><big>" + weather.aqi.city.pm25 + "</big></font>"));
                } else if (pm25Num > 60) {
                    pm25Text.setText(Html.fromHtml("<font color='#ff9900'><big>" + weather.aqi.city.pm25 + "</big></font>"));
                } else if (pm25Num > 35) {
                    pm25Text.setText(Html.fromHtml("<font color='#ffff00'><big>" + weather.aqi.city.pm25 + "</big></font>"));
                } else if (pm25Num > 15) {
                    pm25Text.setText(Html.fromHtml("<font color='#00ff33'><big>" + weather.aqi.city.pm25 + "</big></font>"));
                } else if (pm25Num > 5) {
                    pm25Text.setText(Html.fromHtml("<font color='#00a0ff'><big>" + weather.aqi.city.pm25 + "</big></font>"));
                } else if (pm25Num >= 0) {
                    pm25Text.setText(Html.fromHtml("<font color='#1e3cff'><big>" + weather.aqi.city.pm25 + "</big></font>"));
                }
            } catch (Exception e) {
                pm25Text.setText(Html.fromHtml("<font color='#ff0000'><big>" + weather.aqi.city.pm25 + "</big></font>"));
            }
//            pm25Text.setText(weather.aqi.city.pm25);
            // 当天气状况的子数多于一个的时候,为了能够保持界面美观
            // 需要重新设置字体的大小
            try {
                if (weather.aqi.city.qlty.length() > 1) {
                    // 将字体的大小设置为23,该参数通过调参解决
                    StringBuilder tmp = new StringBuilder();
                    tmp.append("&#160");  // 添加一个空格刚好实现居中
                    tmp.append(weather.aqi.city.qlty.charAt(0)).
                            append(weather.aqi.city.qlty.charAt(1)).append("<br>");
                    tmp.append("&#160");
                    tmp.append(weather.aqi.city.qlty.charAt(2)).
                            append(weather.aqi.city.qlty.charAt(3));
                    int aqiNum = Integer.parseInt(weather.aqi.city.aqi);
                    qualityText.setTextSize((float) 22);
                    if (aqiNum > 300) {
                        qualityText.setText(Html.fromHtml("<font color='#7e0023'><big>" + tmp + "</big></font>"));
                    } else if (aqiNum > 200) {
                        qualityText.setText(Html.fromHtml("<font color='#99004c'><big>" + tmp + "</big></font>"));
                    } else if (aqiNum > 150) {
                        qualityText.setText(Html.fromHtml("<font color='#ff0000'><big>" + tmp + "</big></font>"));
                    } else if (aqiNum > 100) {
                        qualityText.setText(Html.fromHtml("<font color='#ff7e00'><big>" + tmp + "</big></font>"));
                    } else if (aqiNum > 50) {
                        qualityText.setText(Html.fromHtml("<font color='#ffff00'><big>" + tmp + "</big></font>"));
                    } else {
                        qualityText.setText(Html.fromHtml("<font color='#00e400'><big>" + tmp + "</big></font>"));
                    }

                } else {
                    // 否则就显示原来的字体大小
                    int aqiNum = Integer.parseInt(weather.aqi.city.aqi);
                    if (aqiNum > 300) {
                        qualityText.setText(Html.fromHtml("<font color='#7e0023'><big>" + weather.aqi.city.qlty + "</big></font>"));
                    } else if (aqiNum > 200) {
                        qualityText.setText(Html.fromHtml("<font color='#99004c'><big>" + weather.aqi.city.qlty + "</big></font>"));
                    } else if (aqiNum > 150) {
                        qualityText.setText(Html.fromHtml("<font color='#ff0000'><big>" + weather.aqi.city.qlty + "</big></font>"));
                    } else if (aqiNum > 100) {
                        qualityText.setText(Html.fromHtml("<font color='#ff7e00'><big>" + weather.aqi.city.qlty + "</big></font>"));
                    } else if (aqiNum > 50) {
                        qualityText.setText(Html.fromHtml("<font color='#ffff00'><big>" + weather.aqi.city.qlty + "</big></font>"));
                    } else {
                        qualityText.setText(Html.fromHtml("<font color='#00e400'><big>" + weather.aqi.city.qlty + "</big></font>"));
                    }
                }
            } catch (Exception e) {
                qualityText.setText(Html.fromHtml("<font color='#ff0000'><big>" + weather.aqi.city.qlty + "</big></font>"));
            }
        }
        if (weather.now != null) {
            windSpeedText.setText(weather.now.windSpeed);
            visibilityText.setText(weather.now.visibility);
            precipitationText.setText(weather.now.precipitation);
        }
        String comfort = "<font color='#99ffff'><big>舒适度:</big></font>" + weather.suggestion.comfort.info;
        String carWash = "<font color='#99ffff'><big>洗车指数:</big></font>" + weather.suggestion.carWash.info;
        String sport = "<font color='#99ffff'><big>运行建议:</big></font>" + weather.suggestion.sport.info;
        comfortText.setText(Html.fromHtml(comfort));
        carWashText.setText(Html.fromHtml(carWash));
        sportText.setText(Html.fromHtml(sport));
        // 设置完之后要将ScrollView设置为可见
        weatherLayout.setVisibility(View.VISIBLE);
        // 在showWeatherInfo()方法的最后加入启动AutoUpdateService这个服务的代码
        // 这样只要一旦选中了某个城市并成功更新天气之后,AutoUpdateService就会一直在后台运行
        // 并保证10分钟更新一次天气
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

}

//    StringBuilder maxTemperature = new StringBuilder("最高: ");
//            maxTemperature.append("<font color='#FF4500'><big>"+forecast.temperature.max+"</big></font>");
//                    maxTemperature.append("℃");
//
//                    maxText.setText(Html.fromHtml(String.valueOf(maxTemperature)));