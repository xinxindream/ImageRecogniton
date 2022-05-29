package com.example.imagerecogniton.network;

/**
 * @Author:rooterShip
 * @Date:2022/5/29
 * @LastEditors:rooterShip
 * @LastEditTime:2022/5/29
 */

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 接口地址管理
 */
public class ServiceGenerator {
    /**
     * 默认地址
     */
    public static String BASE_URL = "https://aip.baidubce.com";

    /**
     * 创建服务 参数为API服务
     *
     * @param serviceClass 服务接口
     * @param <T>          泛型规范
     * @return api         接口服务器
     */
    public static <T> T createService(Class<T> serviceClass){

        //创建okHttpClient构建器对象
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();

        //设置请求超时时间
        okHttpClientBuilder.connectTimeout(20000, TimeUnit.MILLISECONDS);

        //消息拦截器，因为有时候接口不同在排错的时候 需要先从接口的响应中做分析。利用了消息拦截器可以清楚的看到接口返回的所有内容
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();

        //setLevel用来设置日志打印的级别，其中包括四个级别:NONE,BASIC,HEADER,BODY
        //NONE:NONE,BASIC,HEADER,BODY
        //BASIC:仅记录请求方法、URL、响应状态码及执行时间
        //HEADER:除了 BASIC 中定义的信息之外，还有请求和响应的头信息
        //BODY:除了 HEADERS 中定义的信息之外，还有请求和响应的正文及元数据
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        //为OkHttp添加消息拦截器
        okHttpClientBuilder.addInterceptor(httpLoggingInterceptor);

        //在Retrofit中设置httpclient
        //设置地址

        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                //用Gson把服务端返回的json数据解析成实体
                .addConverterFactory(GsonConverterFactory.create())
                //放入OKHttp(retrofit就是对OKHttp的进一步封装）
                .client(okHttpClientBuilder.build())
                .build();
        //返回创建好的API服务
        return retrofit.create(serviceClass);
    }
}
