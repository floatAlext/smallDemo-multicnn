package com.tryrs.lq.wifip2pmatrix;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;



public class MainActivity extends AppCompatActivity {
    WifiP2pManager manager;
    WifiP2pManager.Channel channel;
    BroadcastReceiver mReceiver;
    IntentFilter intentFilter;
    LinkedList<String> names=new LinkedList<>();
    LinkedList<String> address=new LinkedList<>();
    MyAdapter myAdapter;
    ListView lv;
    String GoAddress;
    GOReceiveMatrixService.MyBinderTargetClient1 myBinderTargetClient1;
    GOReceiveMatrixService2.MyBinderTargetClient2 myBinderTargetClient2;
    MainHandler mainHandler=new MainHandler();
    Myconn myconn=new Myconn();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initiate();
        manager=(WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        channel=manager.initialize(this,getMainLooper(),null);
        mReceiver=new WifiP2pBroadcastReceiver(manager,channel,new MyPeerListListener(),new MyConnectionInfoListener(),this);
        intentFilter=new IntentFilter();
        addActions();
        initListView();
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{android
                .Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

    }
    public void addActions(){
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
    }
    public void initiate(){
        Button btDiscover=(Button)findViewById(R.id.bt_discover);
        btDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this,"discovery succeed",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(MainActivity.this,"discovery failed",Toast.LENGTH_SHORT).show();

                    }
                });

            }
        });

        Button btSend=(Button)findViewById(R.id.bt_sendMatrix);
        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),ClientSendMatrixIntentService.class);
                intent.putExtra("GoAddress",GoAddress);
                startService(intent);

            }
        });


    }
    public void initListView(){
        lv=(ListView)findViewById(R.id.lv_showPeers);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("connect");
                builder.setMessage("please connect to this service before sending picture.");
                String name=names.get(position);
                final String addr=address.get(position);
                //不是一開application就自動connect了，而是一次connect之後，後面始終是connected的狀態
                builder.setPositiveButton("connect", new DialogInterface.OnClickListener() {


                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        WifiP2pConfig wifiP2pConfig=new WifiP2pConfig();
                        wifiP2pConfig.deviceAddress=addr;
                        wifiP2pConfig.groupOwnerIntent=15;
                        //下面這句話很重要，connect不再阻塞
                        wifiP2pConfig.wps.setup = WpsInfo.PBC;
                        //每点击一次按钮，就会连接一次，可以重复连接
                        manager.connect(channel, wifiP2pConfig, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                Log.d("liuqi connect","connection succeed");

                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d("liuqi connect","connection failed");
                            }
                        });

                    }
                });

                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                //按了键之后会自动dismiss
                builder.show();


            }
        });


    }
    public class MyPeerListListener implements WifiP2pManager.PeerListListener {

        @Override
        public void onPeersAvailable(WifiP2pDeviceList peers) {
            Collection<WifiP2pDevice> devices=peers.getDeviceList();
            Iterator<WifiP2pDevice> iterator=devices.iterator();
            names.clear();
            address.clear();
            while (iterator.hasNext()){
                WifiP2pDevice device=iterator.next();
                names.add(device.deviceName);
                address.add(device.deviceAddress);
            }
            myAdapter=new MyAdapter(names,address,getApplicationContext());
            lv.setAdapter(myAdapter);
        }
    }

    public class MyConnectionInfoListener implements WifiP2pManager.ConnectionInfoListener{

        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            GoAddress=info.groupOwnerAddress.getHostAddress();
            Log.d("GO",info.isGroupOwner+"");
            if(info.groupFormed && info.isGroupOwner){

                Intent intent1=new Intent(MainActivity.this,GOReceiveMatrixService.class);
                Intent intent2=new Intent(MainActivity.this,GOReceiveMatrixService2.class);

//                startService(intent1);
//                startService(intent2);
                bindService(intent1,myconn,BIND_AUTO_CREATE);
                bindService(intent2,myconn,BIND_AUTO_CREATE);

            }
        }
    }
    private class Myconn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            //Log.i("name",name.getClassName());
            if(name.getClassName().equals("com.tryrs.lq.wifip2pmatrix.GOReceiveMatrixService")) {
                myBinderTargetClient1 = (GOReceiveMatrixService.MyBinderTargetClient1) service;
                myBinderTargetClient1.setHandler(mainHandler);
                Log.i("Myconn", "setHandler");
            }else if(name.getClassName().equals("com.tryrs.lq.wifip2pmatrix.GOReceiveMatrixService2")) {
                myBinderTargetClient2 = (GOReceiveMatrixService2.MyBinderTargetClient2) service;
                myBinderTargetClient2.setHandler(mainHandler);
                Log.i("Myconn", "setHandler2");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
    public class MainHandler extends Handler{
        public void handleMessage(Message message){
            if(message.what==1){
                Log.i("receive msg from","server1");
                float[][][] textFromClient1=myBinderTargetClient1.getContentFromClient1();//我是server1，我已经处理好了我的数据，你赶紧取走发给server2吧
                Toast.makeText(MainActivity.this,"data from client1:"+textFromClient1,Toast.LENGTH_SHORT).show();
                myBinderTargetClient2.setContentFromClient1(textFromClient1);
            }
            else if(message.what==2){
                Log.i("receive msg from","server2");
                float[][][] textFromClient2=myBinderTargetClient2.getContentFromClient2();
                Toast.makeText(MainActivity.this,"data from client2:"+textFromClient2,Toast.LENGTH_SHORT).show();
                myBinderTargetClient1.setContentFromClient2(textFromClient2);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver,intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        //unbindService(myconn);会自己unbind automatically
    }
}
