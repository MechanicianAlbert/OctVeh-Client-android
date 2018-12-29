package com.octech.octvehclient;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.octech.octvehclient.sock.IpRegex;
import com.octech.octvehclient.sock.SockService;



public class MainActivity extends AppCompatActivity implements IpRegex {

    private EditText mEtIp;
    private EditText mEtPort;
    private Button mBtnConfirm;
    private Button mBtnShut;

    private String mIp;
    private int mPort;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        mEtIp = findViewById(R.id.et_ip);
        mEtPort = findViewById(R.id.et_port);
        mBtnConfirm = findViewById(R.id.btn_confirm);
        mBtnShut = findViewById(R.id.btn_shut);
        mBtnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInfo()) {
                    resetSockService(mIp, mPort);
                    startControlActivity();
                    finish();
                }
            }
        });
        mBtnShut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(getApplicationContext(), SockService.class));
            }
        });
    }

    private boolean checkInfo() {
        mIp = mEtIp.getText().toString().trim();
        String portStr = mEtPort.getText().toString().trim();
        if (TextUtils.isEmpty(mIp) || !mIp.matches(IP_REGEX)) {
            showToast(R.string.str_ip_error);
            return false;
        } else if (TextUtils.isEmpty(portStr)) {
            showToast(R.string.str_port_error);
            return false;
        } else {
            try {
                int port = Integer.parseInt(portStr);
                mPort = port;
                return true;
            } catch (Exception e) {
                showToast(R.string.str_port_error);
                return false;
            }
        }
    }

    private void resetSockService(String ip, int port) {
        stopService(new Intent(getApplicationContext(), SockService.class));

        Intent intent = new Intent(getApplicationContext(), SockService.class);
        Bundle bundle = new Bundle();
        bundle.putString("IP", ip);
        bundle.putInt("PORT", port);
        intent.putExtra("CONNECTION_INFO", bundle);
        startService(intent);
    }

    private void startControlActivity() {
        ControlActivity.start(getApplicationContext());
    }

    private void showToast(int msgId) {
        Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getText(msgId), Toast.LENGTH_SHORT).show();
    }
}
