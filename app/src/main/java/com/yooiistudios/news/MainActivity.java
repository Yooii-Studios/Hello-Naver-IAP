package com.yooiistudios.news;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.naver.android.appstore.iap.InvalidProduct;
import com.naver.android.appstore.iap.NIAPHelper;
import com.naver.android.appstore.iap.NIAPHelperErrorType;
import com.naver.android.appstore.iap.Product;
import com.naver.android.appstore.iap.Purchase;
import com.naver.iap.NIAPUtils;
import com.yooiistudios.news.common.MNLog;
import com.yooiistudios.news.common.Md5Utils;
import com.yooiistudios.news.store.IabProducts;

import java.util.List;


public class MainActivity extends ActionBarActivity implements NIAPHelper.OnInitializeFinishedListener {
    private Button mProVersionButton;
    private Button mFeature1Button;
    private TextView mLogTextView;

    // Naver
    private NIAPHelper mNIAPHelper = null;
    private boolean mNIAPHalfLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProVersionButton = (Button) findViewById(R.id.pro_version_button);
        mFeature1Button = (Button) findViewById(R.id.feature_1_button);
        mLogTextView = (TextView) findViewById(R.id.log_text_view);

        initIap();
        updateUI();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        // release niapHelper
        if (mNIAPHelper != null) {
            mNIAPHelper.terminate();
            mNIAPHelper = null;
        }
    }

    private void initIap() {
        if (IabProducts.STORE_TYPE == IabProducts.StoreType.GOOGLE) {

        } else if (IabProducts.STORE_TYPE == IabProducts.StoreType.NAVER) {
            mNIAPHelper = new NIAPHelper(this, NIAPUtils.NIAP_PUBLIC_KEY);
            mNIAPHelper.initialize(this);
        }
    }

    /**
     * NIAPHelper callback
     */
    @Override
    public void onSuccess() {
        // 리스너 호출 시점에 액티비티가 종료되었을 경우 리스너도 종료
        if (mNIAPHelper == null) {
            return;
        }

        // 전체 아이템의 정보를 불러옴
        mNIAPHelper.getProductDetailsAsync(IabProducts.makeProductKeyList(), new NIAPHelper.GetProductDetailsListener() {
            @Override
            public void onSuccess(List<Product> products, List<InvalidProduct> invalidProducts) {
                // test
                mLogTextView.setText(products.size() + " 개의 아이템을 가져왔어요!");

                // 가격 정보 갱신
                applyProductPrice(products);

                // 구매 목록 콜백이 이미 진행이 되었으면 완료 후 바로 업데이트
                if (readyToUpdateUI()) {
                    updateUI();
                }
            }

            @Override
            public void onFail(NIAPHelperErrorType niapHelperErrorType) {
//                Toast.makeText(MainActivity.this, "상품 로딩 실패ㅠ", Toast.LENGTH_SHORT).show();
                mLogTextView.setText("상품 로딩 실패ㅠ");
            }
        });

        // 구매 목록 요청
        mNIAPHelper.getPurchasesAsync(new NIAPHelper.GetPurchasesListener() {
            @Override
            public void onSuccess(List<Purchase> purchases) {
                // 구매 목록 저장
                IabProducts.saveIabProducts(MainActivity.this, purchases);

                // 전체 목록 콜백이 이미 진행이 되었으면 완료 후 바로 업데이트
                if (readyToUpdateUI()) {
                    updateUI();
                }
            }

            @Override
            public void onFail(NIAPHelperErrorType niapHelperErrorType) {
                mLogTextView.setText(niapHelperErrorType.getErrorDetails());
            }
        });
    }

    @Override
    public void onFail(NIAPHelperErrorType niapHelperErrorType) {
        mLogTextView.setText(niapHelperErrorType.getErrorDetails());
    }

    /**
     * UI
     */
    private void applyProductPrice(List<Product> products) {
        for (Product product : products) {
            if (product.getProductCode().equals(
                    NIAPUtils.convertToNaverSku(IabProducts.SKU_FULL_VERSION))) {
                mProVersionButton.setText("₩ " + product.getProductPrice());
            } else if (product.getProductCode().equals(
                    NIAPUtils.convertToNaverSku(IabProducts.SKU_FEATURE_1))) {
                mFeature1Button.setText("₩ " + product.getProductPrice());
            }
        }
    }

    private boolean readyToUpdateUI() {
        if (mNIAPHalfLoaded) {
            return true;
        } else {
            mNIAPHalfLoaded = true;
            return false;
        }
    }

    private void updateUI() {
        mLogTextView.setText("UI 업데이트 완료");

        if (IabProducts.containsSku(this, IabProducts.SKU_FULL_VERSION)) {
            MNLog.now("풀버전 구매");
            mProVersionButton.setText("Purchased");
            mProVersionButton.setEnabled(false);
            mFeature1Button.setText("Purchased");
        } else {
            mProVersionButton.setEnabled(true);
            mFeature1Button.setEnabled(true);
        }
        if (IabProducts.containsSku(this, IabProducts.SKU_FEATURE_1)) {
            MNLog.now("피처 1 구매");
            mFeature1Button.setText("Purchased");
            mFeature1Button.setEnabled(false);
        } else {
            mFeature1Button.setEnabled(true);
        }
    }

    /**
     * Button Click
     */
    public void onProVersionClicked(View view) {
        purchaseProduct(IabProducts.SKU_FULL_VERSION);
    }

    public void onFeature1Clicked(View view) {
        purchaseProduct(IabProducts.SKU_FEATURE_1);
    }

    /**
     * NIAPPurchase callback
     */
    private void purchaseProduct(String googleProductSku) {
        if (IabProducts.STORE_TYPE == IabProducts.StoreType.GOOGLE) {

        } else if (IabProducts.STORE_TYPE == IabProducts.StoreType.NAVER) {
            mNIAPHelper.requestPayment(this, NIAPUtils.convertToNaverSku(googleProductSku),
                    Md5Utils.getMd5String(googleProductSku), NIAPUtils.NIAP_REQUEST_CODE,
                    new NIAPHelper.RequestPaymentListener() {
                        @Override
                        public void onSuccess(Purchase purchase) {
                            String purchasedProductGoogleSku =
                                    NIAPUtils.convertToGoogleSku(purchase.getProductCode());
                            // MD5 암호화 복호화를 통해 보낸 payload 와 구매된 payload 를 비교
                            if (purchase.getDeveloperPayload().equals(Md5Utils.getMd5String(purchasedProductGoogleSku))) {
                                // 구매 저장
                                IabProducts.saveIabProduct(MainActivity.this, purchasedProductGoogleSku);
                                // UI 적용
                                mLogTextView.setText(purchase.getProductCode() + " 구매 완료");
                                updateUI();
                            }
                        }

                        @Override
                        public void onCancel() {
                            mLogTextView.setText("구매가 취소되었습니다");
                        }

                        @Override
                        public void onFail(NIAPHelperErrorType niapHelperErrorType) {
                            mLogTextView.setText(niapHelperErrorType.getErrorDetails());
                        }
                    }
            );
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (IabProducts.STORE_TYPE == IabProducts.StoreType.GOOGLE) {

        } else if (IabProducts.STORE_TYPE == IabProducts.StoreType.NAVER) {
            // 아래 코드는 반드시 포함되어야 합니다.
            if (mNIAPHelper.handleActivityResult(requestCode, resultCode, data)) {
                // NIAPHelper 가 구매 결과를 처리 완료
            } else {
                // NIAPHelper 가 구매 결과를 처리하지 않음.
                super.onActivityResult(requestCode, resultCode, data);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
