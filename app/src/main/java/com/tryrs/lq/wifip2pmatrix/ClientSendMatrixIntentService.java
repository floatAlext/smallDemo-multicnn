package com.tryrs.lq.wifip2pmatrix;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by Administrator on 2018/1/8 0008.
 */

public class ClientSendMatrixIntentService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ClientSendMatrixIntentService(String name) {
        super(name);
    }
    public ClientSendMatrixIntentService(){
        super("");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String GoAddress=intent.getStringExtra("GoAddress");
        Socket socket=new Socket();
        try {

            socket.bind(null);
            //different clients connect to same ip address but different port
            socket.connect(new InetSocketAddress(GoAddress,8889),5000);
            OutputStream outputStream=socket.getOutputStream();
            outputStream.write("lalala".getBytes());
            Log.i("connected","lalala");

            InputStream inputStream=socket.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int x;
            while ((x = inputStream.read()) >=0) {
                byteArrayOutputStream.write(x);break;

            }
            byteArrayOutputStream.flush();

            //String str=byteArrayOutputStream.toString();
            String str="333";
            //不能在子线程更新UI
            //Toast.makeText(getApplicationContext(),"finish:"+str,Toast.LENGTH_SHORT).show();
            Log.i("client",str);
//socket要close，service才能loop over,why?
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
