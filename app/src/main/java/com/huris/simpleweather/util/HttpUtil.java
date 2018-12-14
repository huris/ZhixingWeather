package com.huris.simpleweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

// 由于全国所有县市的数据都是从服务器端获取到的,因此这里和服务器的交互是必不可少的
// 所以我们可以在util包下先增加一个HttpUtil类
public class HttpUtil {
    // 由于OkHttp的出色封装,这里和服务器进行交互的代码非常简单,仅仅3行就完成了
    // 我们发起一条Http请求只需要调用sendOkHttpRequest()方法
    // 传入地址address,并注册一个回调来处理服务器响应就可以了
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }

}
