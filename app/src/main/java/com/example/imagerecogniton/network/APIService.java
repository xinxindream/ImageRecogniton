package com.example.imagerecogniton.network;

/**
 * @Author:rooterShip
 * @Date:2022/5/29
 * @LastEditors:rooterShip
 * @LastEditTime:2022/5/29
 */

import com.example.imagerecogniton.model.GetDiscernResultResponse;
import com.example.imagerecogniton.model.GetTokenResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * API服务
 */
public interface APIService {
    /**
     * 获取鉴权认证Token
     * 使用post请求需要带的三个url参数
     * @param grant_type 类型
     * @param client_id API key
     * @param client_secret Secret Key
     * @return GetTokenResponse
     */

    //表示请求体是一个Form表单
    @FormUrlEncoded
    //接受的字符串表示接口path，与baseUrl组成完成的Url
    @POST("/oauth/2.0/token")
    //@Field 表示表单字段
    Call<GetTokenResponse> getToken(@Field("grant_type") String grant_type,
                                    @Field("client_id") String client_id,
                                    @Field("client_secret") String client_secret);

    /**
     * 获取图像识别结果
     * @param accessToken 获取鉴权认证Token
     * @param url 网络图片url
     * @return JsonObject
     */
    @FormUrlEncoded
    @POST("Content-Type:application/x-www-form-urlencoded; charset=utf-8")
    Call<GetDiscernResultResponse> getDiscernResult(@Field("access_token") String accessToken,
                                                    @Field("url") String url);
}
