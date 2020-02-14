package com.wherewego.message.entity;

/**
 * @Author:lubeilin
 * @Date:Created in 19:43 2020/2/13
 * @Modified By:
 */
public class MsgVO {
    private String code;//客户端唯一标识
    private String userCode;//用户编码
    private String accessToken;//身份令牌
    private String refreshToken;//刷新令牌

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
