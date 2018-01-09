package com.tryrs.lq.wifip2pmatrix;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Administrator on 2018/1/8 0008.
 */

public class GOReceiveMatrixIntentService2 extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    String str;
    Handler mHandler;
    String contentFromClient1=null;
    public GOReceiveMatrixIntentService2(String name) {
        super(name);
    }
    public GOReceiveMatrixIntentService2() {
        super("");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        ServerSocket serverSocket= null;
        try {
            //兩個intentservice開啟不同的port
            serverSocket = new ServerSocket(8888);
            while(true) {
                Socket s = serverSocket.accept();
                Log.i("server2", "accept");
                InputStream inputStream = s.getInputStream();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                //OutputStream outputStream=s.getOutputStream();

                int x;
                while ((x = inputStream.read()) >=0) {
                    byteArrayOutputStream.write(x);

                }

                str=byteArrayOutputStream.toString();
                Log.i("server2", str);
                Message message=Message.obtain();
                message.what=2;
                mHandler.sendMessage(message);
                while(contentFromClient1==null){}
                OutputStream outputStream=s.getOutputStream();
                outputStream.write(contentFromClient1.getBytes());
                s.close();
                contentFromClient1=null;

            }

        } catch (IOException e) {
            Log.e("xyz2", e.toString());

        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("onCreate2","onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("onDestroy2","onDestroy");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.i("onStartCommand2","onStartCommand");
        return super.onStartCommand(intent, flags, startId);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinderTargetClient2();
    }

    public class MyBinderTargetClient2 extends Binder {
        public String getContentFromClient2(){
            return "server2&"+str;
        }
        public void setHandler(Handler handler){
            mHandler=handler;
        }
        public void setContentFromClient1(String content){
            contentFromClient1=content;
        }
    }

}
