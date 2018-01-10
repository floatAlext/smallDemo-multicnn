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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

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
            socket.connect(new InetSocketAddress(GoAddress,8888),5000);
            OutputStream outputStream=socket.getOutputStream();
            //outputStream.write("lalala".getBytes());
            ObjectOutputStream objectOutputStream=new ObjectOutputStream(outputStream);
            Log.i("connected","lalala");
            float[][][] data1={{{(float) 1.1,(float) 1.2,(float) 1.3},{(float) 2.1,(float) 2.2,(float) 2.3}},
                    {{(float) 1.4,(float) 1.5,(float) 1.6},{(float) 2.4,(float) 2.5,(float) 2.6}}};
            float[][][] data2={{{(float) 1.1,(float) 1.2,(float) 1.3,0},{(float) 2.1,(float) 2.2,(float) 2.3,0}},
                    {{(float) 1.4,(float) 1.5,(float) 1.6,0},{(float) 2.4,(float) 2.5,(float) 2.6,0}}};
            Matrix a=new Matrix(data1);
            objectOutputStream.writeObject(a);
            objectOutputStream.flush();
            //objectOutputStream.close();

            InputStream inputStream=socket.getInputStream();
            ObjectInputStream objectInputStream=new ObjectInputStream(inputStream);
            try {
                Matrix m=(Matrix) objectInputStream.readObject();
                Log.i("client1", Arrays.toString(m.a[0][0]));

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            //objectInputStream.close();
            //socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
