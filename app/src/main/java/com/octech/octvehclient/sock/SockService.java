package com.octech.octvehclient.sock;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.octech.octvehclient.ControlActivity;
import com.octech.octvehclient.MainActivity;
import com.octech.octvehclient.R;




public class SockService extends Service implements IpRegex, SocketThread.OnConnectResultListener {

    public class SockBinder extends Binder {

        public SockService get() {
            return SockService.this;
        }
    }


    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            Log.e("AAA", "Reply: " + msg.obj);
        }
    };


    private String mIp;
    private int mPort;

    private SockBinder mBinder = new SockBinder();
    private SocketThread mThread;
    private boolean mConnectSuccess;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (parseAndCheck(intent)) {
            onValidConnectionInfo();
        } else {
            onInvalidConnectionInfo();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConnectResult(boolean success) {
        mConnectSuccess = success;
        if (success) {
            asForeground(true);
        } else {
            asForeground(false);
        }
    }


    public boolean isConnectSuccess() {
        return mConnectSuccess;
    }

    public void sendOrder(String order) {
        if (mThread != null) {
            mThread.sendMsg(order);
        }
    }


    private boolean parseAndCheck(Intent intent) {
        Bundle bundle = intent.getBundleExtra("CONNECTION_INFO");
        mIp = bundle.getString("IP");
        mPort = bundle.getInt("PORT");
        return !(TextUtils.isEmpty(mIp) || !mIp.matches(IP_REGEX));
    }

    private void onValidConnectionInfo() {
//        asForeground(true);
        mThread = new SocketThread();
        mThread.init(mIp, mPort, this, mHandler);
    }

    private void onInvalidConnectionInfo() {
        asForeground(false);
        if (mThread != null) {
            mThread.release();
        }
    }

    private void asForeground(boolean success) {
        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle(success ? getApplicationContext().getResources().getText(R.string.str_connect_succed): getApplicationContext().getResources().getText(R.string.str_connect_succed))
                .setContentText(success ? (mIp + ":" + mPort) : "")
                .setContentIntent(getPendingIntent(success))
                .setSmallIcon(R.drawable.ic_launcher)
                .build();
        startForeground(1,notification);
    }

    private PendingIntent getPendingIntent(boolean valid) {
        Intent intent = new Intent(getApplicationContext(),valid ? ControlActivity.class : MainActivity.class);
        return PendingIntent.getActivity(getApplicationContext(),0,intent,0);
    }

}
