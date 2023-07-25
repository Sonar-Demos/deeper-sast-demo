package com.dashboardmanager.utils;

import org.apache.tomcat.util.buf.UDecoder;

import java.nio.charset.StandardCharsets;

public class EncodingUtils {

    private static EncodingUtils instance = null;

    public static EncodingUtils getInstance() {
        if (EncodingUtils.instance == null) EncodingUtils.instance = new EncodingUtils();
        return EncodingUtils.instance;
    }

    public String decodeParameter(String param) {
        return UDecoder.URLDecode(param, StandardCharsets.UTF_8);
    }
}
