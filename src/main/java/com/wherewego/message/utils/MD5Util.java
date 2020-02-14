package com.wherewego.message.utils;

import org.springframework.util.DigestUtils;

/**
 * @Author:lubeilin
 * @Date:Created in 18:24 2020/2/13
 * @Modified By:
 */
public class MD5Util {
    //盐，用于混淆md5
    private static final String slat = "**********************";
    /**
     * 生成md5
     * @param str
     * @return
     */
    public static String getMD5(String str) {
        String base = str +"/"+slat;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }
}
