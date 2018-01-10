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

public class GOReceiveMatrixService extends Service {
    float[][][] client1Data;
    Handler mHandler;
    float[][][] contentFromClient2=null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("onCreate1","onCreate");
        new Thread(){
            public void run(){
                ServerSocket serverSocket= null;
                try {
                    serverSocket = new ServerSocket(8888);
                    while(true) {//while makes destroy impossible
                        Socket s = serverSocket.accept();
                        Log.i("server", "accept");
                        InputStream inputStream = s.getInputStream();
                        ObjectInputStream objectInputStream=new ObjectInputStream(inputStream);
                        try {
                            Matrix m=(Matrix)objectInputStream.readObject();
                            client1Data=m.a;
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        //objectInputStream.close();

                        Message message=Message.obtain();
                        message.what=1;
                        mHandler.sendMessage(message);
                        Log.i("server1","mydata is prepared");
                        while(contentFromClient2==null){}


                        Log.i("receive client1", Arrays.toString(contentFromClient2[0][0]));

                        //send matrix to client
                        OutputStream outputStream=s.getOutputStream();
                        ObjectOutputStream objectOutputStream=new ObjectOutputStream(outputStream);
                        Matrix mm=new Matrix(contentFromClient2);
                        objectOutputStream.writeObject(mm);
                        contentFromClient2=null;
                        //objectOutputStream.close();
                        //s.close();
                        objectOutputStream.flush();



                    }

                } catch (IOException e) {
                    Log.e("xyz", e.toString());
                }

            }
        }.start();
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
        Log.i("onBind1","onBind");
        return new MyBinderTargetClient1();
    }

    public class MyBinderTargetClient1 extends Binder {
        public float[][][] getContentFromClient1(){
            return client1Data;
        }
        public void setHandler(Handler handler){
            mHandler=handler;
        }
        public void setContentFromClient2(float[][][] content){
            contentFromClient2=content;
        }

    }
}
