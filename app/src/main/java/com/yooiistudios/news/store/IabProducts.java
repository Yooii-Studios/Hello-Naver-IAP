package com.yooiistudios.news.store;

import android.content.Context;
import android.content.SharedPreferences;

import com.naver.android.appstore.iap.Purchase;
import com.naver.iap.NIAPUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by StevenKim in Morning Kit from Yooii Studios Co., LTD. on 2014. 1. 9.
 *
 * SKIabProducts
 */
public class IabProducts {
    public static final String SKU_FULL_VERSION = "pro_version";
    public static final String SKU_FEATURE_1 = "feature_1";

    private static final String SHARED_PREFERENCES_IAB = "SHARED_PREFERENCES_IAB";

    public enum StoreType {
        GOOGLE, NAVER, AMAZON, SAMSUNG
    }

    public static final StoreType STORE_TYPE = StoreType.NAVER;

    public static ArrayList<String> makeProductKeyList() {
        ArrayList<String> iabKeyList = new ArrayList<String>();
        if (STORE_TYPE == StoreType.GOOGLE) {
            iabKeyList.add(SKU_FULL_VERSION);
            iabKeyList.add(SKU_FEATURE_1);
        } else if (STORE_TYPE == StoreType.NAVER) {
            iabKeyList = NIAPUtils.getAllProducts();
        }
        return iabKeyList;
    }

    // 구매완료시 적용
    public static void saveIabProduct(Context context, String sku) {
        SharedPreferences prefs;
        prefs = context.getSharedPreferences(SHARED_PREFERENCES_IAB, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(sku, true).apply();
    }

    public static boolean containsSku(Context context, String sku) {
        return loadOwnedIabProducts(context).contains(sku);
    }

    // 구매된 아이템들을 로드
    public static List<String> loadOwnedIabProducts(Context context) {
        List<String> ownedSkus = new ArrayList<>();

        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_IAB, Context.MODE_PRIVATE);
        if (prefs.getBoolean(SKU_FULL_VERSION, false)) {
            ownedSkus.add(SKU_FULL_VERSION);
            ownedSkus.add(SKU_FEATURE_1);
        } else {
            if (prefs.getBoolean(SKU_FEATURE_1, false)) {
                ownedSkus.add(SKU_FEATURE_1);
            }
        }
        return ownedSkus;
    }

    /**
     * For Naver Store Mode
     */
    // 구매 목록을 새로 저장
    public static void saveIabProducts(Context context, List<Purchase> purchases) {
        SharedPreferences.Editor edit = context.getSharedPreferences(SHARED_PREFERENCES_IAB, Context.MODE_PRIVATE).edit();
        edit.clear(); // 모두 삭제 후 다시 추가
        for (Purchase purchase : purchases) {
            if (purchase.getPurchaseType() == Purchase.PurchaseType.APPROVED) {
                edit.putBoolean(NIAPUtils.convertToGoogleSku(purchase.getProductCode()), true);
            }
        }
        edit.apply();
    }
}
