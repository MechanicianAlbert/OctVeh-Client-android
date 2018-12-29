package com.octech.octvehclient.sock;

import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;



public class SocketThread extends Thread {

    public interface OnConnectResultListener {
        void onConnectResult(boolean success);
    }


    private String mIp;
    private int mPort;

    private Socket mSocket;
    private BufferedReader mIn;
    private PrintWriter mOut;

    private Handler mMainHandler;
    private OnConnectResultListener mListener;


    @Override
    public void run() {
        try {
            connect();
            listen();
        } catch (Exception e) {
            release();
        }
    }

    public void init(String ip, int port, OnConnectResultListener listener, Handler handler) {
        mIp = ip;
        mPort = port;
        mListener = listener;
        mMainHandler = handler;
        start();
    }

    public void release() {
        try {
            interrupt();
            if (mSocket != null) {
                mSocket.close();
                mSocket = null;
            }
            if (mIn != null) {
                mIn.close();
                mIn = null;
            }
            if (mOut != null) {
                mOut.close();
                mOut = null;
            }
            if (mMainHandler != null) {
                mMainHandler.removeCallbacksAndMessages(null);
                mMainHandler = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        if (mOut != null && mSocket != null && !mSocket.isClosed() && mSocket.isConnected() && !mSocket.isOutputShutdown()) {
            mOut.println(msg);
        }
    }


    private void connect() {
        try {
            mSocket = new Socket(mIp, mPort);
            mIn = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream())), true);
            if (mListener != null) {
                mListener.onConnectResult(true);
            }
        } catch (IOException e) {
            if (mListener != null) {
                mListener.onConnectResult(false);
            }
            interrupt();
        }
    }

    private void listen() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (mSocket != null && mSocket.isConnected() && !mSocket.isClosed() && !mSocket.isInputShutdown()) {
                    String getLine;
                    if ((getLine = mIn.readLine()) != null) {
                        getLine += "\n";
                        reply(getLine);
                    }
                }
            }
        } catch (Exception e) {
            release();
        }
    }

    private void reply(Object o) {
        Message.obtain(mMainHandler, 0, o).sendToTarget();
    }

}
