package com.octech.octvehclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.octech.octvehclient.rocker.RockerView;
import com.octech.octvehclient.sock.SockService;


/**
 * Created by Albert on 2018/12/27
 */
public class ControlActivity extends AppCompatActivity implements RockerView.OnRockListener {

    private final ServiceConnection CONNECTION = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService = ((SockService.SockBinder) iBinder).get();
            if (mService != null) {
                if (mService.isConnectSuccess()) {
                    showToast(R.string.str_connect_succed);
                } else {
                    showToast(R.string.str_connect_failed);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };


    private SockService mService;
    private boolean mHasBind;

    private RockerView mRv;
    private ImageView mIvShut;

    private String mLastOrder;


    public static void start(Context context) {
        Intent intent = new Intent(context, ControlActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_ctrl);
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release();
    }

    @Override
    public void onRock(float ratioX, float ratioY) {
        handleOrder(parseOrder(ratioX, ratioY));
    }

    @Override
    public void onReset() {
        handleOrder("SPACE");
    }


    private void init() {
        initView();
        initService();
    }

    private void release() {
        try {
            mService = null;
            if (mHasBind) {
                unbindService(CONNECTION);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        mRv = findViewById(R.id.rv);
        mIvShut = findViewById(R.id.iv_shut);
        mRv.addOnRockListener(this);
        mIvShut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unbindService(CONNECTION);
                stopService(new Intent(getApplicationContext(), SockService.class));
                finish();
            }
        });
    }

    private void initService() {
        Intent intent = new Intent(getApplicationContext(), SockService.class);
        mHasBind = bindService(intent, CONNECTION, 0);
    }

    private void handleOrder(String order) {
        if (!TextUtils.equals(mLastOrder, order)) {
            mLastOrder = order;
            if (mService != null) {
                mService.sendOrder(order);
            }
        }
    }

    private String parseOrder(float ratioX, float ratioY) {
//        if (Math.abs(ratioX) < 0.3 && Math.abs(ratioY) < 0.3) {
//            return "SPACE";
//        } else {
//            boolean isVertical = Math.abs(ratioX) < Math.abs(ratioY);
//            if (isVertical) {
//                if (ratioY < 0) {
//                    return "W";
//                } else {
//                    return "S";
//                }
//            } else {
//                if (ratioX < 0) {
//                    return "A";
//                } else {
//                    return "D";
//                }
//            }
//        }
        String order = "";
        if (ratioX > -0.25 && ratioX < 0.25 && ratioY < -0.75) {
            order = "W";
        } else if (ratioX > 0.25 && ratioX < 0.75 && ratioY > -0.75 && ratioY < -0.25) {
            order = "E";
        } else if (ratioX > 0.75 && ratioY > -0.25 && ratioY < 0.25) {
            order = "D";
        } else if (ratioX > 0.25 && ratioX < 0.75 && ratioY > 0.25 && ratioY < 0.75) {
            order = "C";
        } else if (ratioX > -0.25 && ratioX < 0.25 && ratioY > 0.75) {
            order = "S";
        } else if (ratioX > -0.75 && ratioX < -0.25 && ratioY > 0.25 && ratioY < 0.75) {
            order = "Z";
        } else if (ratioX < -0.75 && ratioY > -0.25 && ratioY < 0.25) {
            order = "A";
        } else if (ratioX > -0.75 && ratioX < -0.25 && ratioY > -0.75 && ratioY < -0.25) {
            order = "Q";
        }
//        Log.e("AAA", order);
        return order;
    }

    private void showToast(int msgId) {
        Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getText(msgId), Toast.LENGTH_SHORT).show();
    }
}
