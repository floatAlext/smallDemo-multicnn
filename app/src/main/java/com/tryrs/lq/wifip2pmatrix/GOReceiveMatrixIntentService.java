package com.tryrs.lq.wifip2pmatrix;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Administrator on 2018/1/8 0008.
 */

public class GOReceiveMatrixIntentService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    String str;
    Handler mHandler;
    String contentFromClient2=null;

    public GOReceiveMatrixIntentService(String name) {
        super(name);
    }
    //Service的实例化是系统来完成的，而且系统是用参数为空的构造函数来实例化Service的
    public GOReceiveMatrixIntentService(){
        super("");//这就是thread的name
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        ServerSocket serverSocket= null;
        try {
            serverSocket = new ServerSocket(8889);
            while(true) {//while makes destroy impossible
                Socket s = serverSocket.accept();
                Log.i("server", "accept");
                InputStream inputStream = s.getInputStream();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                //OutputStream outputStream=s.getOutputStream();
                int x;
                while ((x = inputStream.read()) >=0) {
                    byteArrayOutputStream.write(x);

                }
                str=byteArrayOutputStream.toString();
                Message message=Message.obtain();
                message.what=1;
                mHandler.sendMessage(message);
                while(contentFromClient2==null){}


                Log.i("server", str);
                OutputStream outputStream=s.getOutputStream();
                outputStream.write(contentFromClient2.getBytes());
                s.close();
                contentFromClient2=null;

            }

        } catch (IOException e) {
            Log.e("xyz", e.toString());
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("onCreate1","onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("onDestroy1","onDestroy");
    }
    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.i("onStartCommand1","onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    //IntentService启动不支持bindService
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinderTargetClient1();
    }

    public class MyBinderTargetClient1 extends Binder{
        public String getContentFromClient1(){
            return "server1&"+str;
        }
        public void setHandler(Handler handler){
            mHandler=handler;
        }
        public void setContentFromClient2(String content){
            contentFromClient2=content;
        }

    }


}
