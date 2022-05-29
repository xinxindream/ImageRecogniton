package com.example.imagerecogniton.model;

/**
 * @Author:rooterShip
 * @Date:2022/5/29
 * @LastEditors:rooterShip
 * @LastEditTime:2022/5/29
 */
/**
 * 获取鉴权认证Token响应实体
 *
 * 请求API接口时返回json字符串其中的key:
 * refresh_token
 * expires_in
 * scope
 * session_key
 * session_token
 * session_secret
 */
public class GetTokenResponse {
    private String refresh_token;
    private long expires_in;
    private String scope;
    private String session_key;
    private String session_token;
    private String session_secret;

    public long getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(long expires_in) {
        this.expires_in = expires_in;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getSession_key() {
        return session_key;
    }

    public void setSession_key(String session_key) {
        this.session_key = session_key;
    }

    public String getSession_token() {
        return session_token;
    }

    public void setSession_token(String session_token) {
        this.session_token = session_token;
    }

    public String getSession_secret() {
        return session_secret;
    }

    public void setSession_secret(String session_secret) {
        this.session_secret = session_secret;
    }

    public String getRefresh_token(){
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token){
        this.refresh_token = refresh_token;
    }
}
