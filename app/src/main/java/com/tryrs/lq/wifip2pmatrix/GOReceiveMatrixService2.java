package com.tryrs.lq.wifip2pmatrix;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by Administrator on 2018/1/9 0009.
 */

public class GOReceiveMatrixService2 extends Service {
    float[][][] client2Data;
    Handler mHandler;
    float[][][] contentFromClient1=null;


    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(){
            public void run(){
                ServerSocket serverSocket= null;
                try {
                    //兩個intentservice開啟不同的port
                    serverSocket = new ServerSocket(8889);
                    while(true) {
                        Socket s = serverSocket.accept();
                        Log.i("server2", "accept");
                        InputStream inputStream = s.getInputStream();
                        ObjectInputStream objectInputStream=new ObjectInputStream(inputStream);
                        try {
                            Matrix m=(Matrix)objectInputStream.readObject();
                            client2Data=m.a;
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        Log.i("server2","mydata is prepared");
                        //objectInputStream.close();

                        Message message=Message.obtain();
                        message.what=2;
                        mHandler.sendMessage(message);
                        while(contentFromClient1==null){}

                        Log.i("receive client2", Arrays.toString(contentFromClient1[0][0]));

                        OutputStream outputStream=s.getOutputStream();
                        ObjectOutputStream objectOutputStream=new ObjectOutputStream(outputStream);
                        Matrix mm=new Matrix(contentFromClient1);
                        objectOutputStream.writeObject(mm);
                        contentFromClient1=null;
                        //objectOutputStream.close();
                        objectOutputStream.flush();


                    }

                } catch (IOException e) {
                    //s.close();
                    Log.e("xyz2", e.toString());

                }
            }
        }.start();
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
        Log.i("onBind2","onBind");
        return new MyBinderTargetClient2();
    }

    public class MyBinderTargetClient2 extends Binder {
        public float[][][] getContentFromClient2(){
            return client2Data;
        }
        public void setHandler(Handler handler){
            mHandler=handler;
        }
        public void setContentFromClient1(float[][][] content){
            contentFromClient1=content;
        }
    }
}
