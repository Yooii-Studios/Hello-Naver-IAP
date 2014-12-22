package com.naver.iap;

import com.yooiistudios.news.store.IabProducts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NIAPUtils {
    public static final int NIAP_REQUEST_CODE = 100;
	public static final String NIAP_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC273wBt+dcVclW1WKmorA511mMgAjcYwzPWZyhSE8VOg7K9ixm/gLH/GdWxbmU2y+kqO7Z/Onqu4+opHJmZ3Si3dn8NWdrJXQvXfZMUaFV0vo27t5SF7lPVglpWi4QsQDCK+dHIFNFJIIMTecXFk4kQFdCdKdh5q2PcZHPOw1c7QIDAQAB";

    private static final String NAVER_IAB_FULL_VERSION = "1000013194";
    private static final String NAVER_FEATURE_1 = "1000013195";

    private static final Map<String, String> naverSkuMap;
    private static final Map<String, String> googleSkuMap;

    private NIAPUtils() { throw new AssertionError("You MUST not create this class!"); }

    static {
        naverSkuMap = new HashMap<>();
        naverSkuMap.put(IabProducts.SKU_FULL_VERSION, NAVER_IAB_FULL_VERSION);
        naverSkuMap.put(IabProducts.SKU_FEATURE_1, NAVER_FEATURE_1);

        googleSkuMap = new HashMap<>();
        googleSkuMap.put(NAVER_IAB_FULL_VERSION, IabProducts.SKU_FULL_VERSION);
        googleSkuMap.put(NAVER_FEATURE_1, IabProducts.SKU_FEATURE_1);
    }

    public static String convertToGoogleSku(String naverSku) {
        return googleSkuMap.get(naverSku);
    }

    public static String convertToNaverSku(String googleSku) {
        return naverSkuMap.get(googleSku);
    }

    public static ArrayList<String> getAllProducts() {
        // 구글맵의 네이버 프로덕트 id 들
        return new ArrayList<>(googleSkuMap.keySet());
    }
}
