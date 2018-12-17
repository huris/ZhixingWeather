package com.huris.simpleweather;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity {

    public LocationClient mLocationClient;

    private TextView positionText;

    private MapView mapView;

    private BaiduMap baiduMap;

    private boolean isFirstLocate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 首先需要调用SDKInitializer的initialize()方法来进行初始化操作
        // initialize()方法接收一个Context参数,这里调用getApplicationContext()方法
        // 注意初始化操作一定要在setContextview()方法的前面,否则会出现错误
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_map);

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());

        // 接下来调用findViewById()方法获得了MapView的实例
        mapView = (MapView) findViewById(R.id.bmapView);

        // baiMap是地图的总控制器,调用getMap()方法就能获取到BaiduMap的实例
        // 有了BaiduMap之后我们就能进行各种各样的操作,比如设置地图的缩放级别以及将地图移动到某一个经纬度上
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        positionText = (TextView) findViewById(R.id.position_text_view);
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MapActivity.this, permissions, 1);
        } else {
            requestLocation();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    private void navigateTo(BDLocation location) {
        if (isFirstLocate) {
            Toast.makeText(this, "nav to " + location.getAddrStr(), Toast.LENGTH_SHORT).show();
            // LatLng的构造方法接收两个参数,第一个参数是纬度值,第二个参数是经度值
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
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
            isFirstLocate = false;
        }
        // 注意,下面这段逻辑必须写到isFirstLocation这个if条件语句的外面
        // 因为让地图移动到我们当前位置只需要在第一次定位的时候执行,但是设备在地图上显示的位置应该是实时变化的
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        // 将Location中包含的经度和纬度分别封装到了MyLocationData.Builder当中
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        // 最后把MyLocation设置到了BaiduMap的setMyLocationData()方法中
        baiduMap.setMyLocationData(locationData);
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
            // 当定位到设备当前位置的时候
            if (location.getLocType() == BDLocation.TypeGpsLocation
                    || location.getLocType() == BDLocation.TypeNetWorkLocation) {
                // 直接把BDLocation对象传给navigateTo()方法,这样就能够让地图移动到设备所在的位置了
                navigateTo(location);
            }
        }

    }

    /**
     * 需要重写onResume(),onPause(),onDestroy()这三个方法
     * 这里对MapView进行管理,以保证资源能够及时地得到释放
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 最后要记得活动被销毁的时候一定要调用stop方法来停止定位,
        // 不然程序会在后台不停地进行定位,从而严重消耗手机的电能
        mLocationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }
}