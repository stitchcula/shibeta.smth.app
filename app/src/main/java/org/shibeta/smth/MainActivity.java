package org.shibeta.smth;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.app.AlertDialog;
import android.widget.TextView;
import android.os.Handler;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    public Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.mipmap.ic_launcher);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, AScanQR.class), M.scanQR);
            }
        });
    }

    @Override
    protected void onActivityResult(int reqCode,int resCode,Intent data){
        super.onActivityResult(reqCode, resCode, data);
        if(reqCode==M.scanQR){
            if(resCode==200){
                final View fab=findViewById(R.id.fab);
                fab.setVisibility(View.GONE);
                final TextView msgView=(TextView)findViewById(R.id.main_text_msg);
                msgView.setText("连接中……可能需要一点时间。");

                final CWifiControl wifiControl=new CWifiControl(MainActivity.this);
                final int lastNetworkId=(wifiControl.wifiManager.isWifiEnabled())?wifiControl.wifiManager.getConnectionInfo().getNetworkId():-1;

                final Thread GetMessage=new Thread(new Runnable() {
                    public Message handleMessage;
                    CStore store=new CStore(MainActivity.this);
                    @Override
                    public void run() {
                        handleMessage=new Message();
                        String url="http://iot.shibeta.org:809/m/getdata?uin="+store.get("chipId");
                        while (true){
                            try {
                                //todo:
                                Thread.sleep(1000);
                            }catch (Exception e){
                                Log.d("Thread Error",e.toString());
                                handleMessage.what=0x0500;
                                handleMessage.obj=e.toString();
                                MainActivity.this.handler.sendMessage(handleMessage);
                            }
                        }
                    }
                });

                handler=new Handler(){
                    public void handleMessage(Message msg){
                        switch (msg.what){
                            case 0x0200:
                                msgView.setText("配置成功，但是后面的代码还没写_(:зゝ∠)_");
                                if(lastNetworkId>0){
                                    wifiControl.connect(lastNetworkId);
                                    wifiControl.wifiManager.reconnect();
                                }else{
                                    wifiControl.close();
                                }
                                GetMessage.start();
                                break;
                            case 0x0203:
                                msgView.setText("连接成功，配置中……");
                                break;
                            case 0x0404:
                                fab.setVisibility(View.VISIBLE);
                                msgView.setText("无法检测到SMTH。");
                                break;
                            case 0x0401:
                                fab.setVisibility(View.VISIBLE);
                                msgView.setText("无线网络密码错误，请检查后重试。");
                                break;
                            case 0x0424:
                                fab.setVisibility(View.VISIBLE);
                                msgView.setText("SMTH没有找到该无线网络，请检查后重试。");
                                break;
                            case 0x0501:
                                fab.setVisibility(View.VISIBLE);
                                msgView.setText("SMTH由于未知的原因无法连接到该无线网络，请再次尝试。");
                                break;
                            case 0x0500:
                                fab.setVisibility(View.VISIBLE);
                                msgView.setText("貌似出了点小问题……请再次尝试。"+((msg.obj.toString().length()>0)?"\nERROR:\n"+msg.obj.toString():""));
                                break;
                        }
                        super.handleMessage(msg);
                    }
                };

                Thread SetMcu=new Thread(new Runnable() {
                    public Message handleMessage;
                    @Override
                    public void run() {
                        int netId=-1;
                        try {
                            handleMessage = new Message();
                            wifiControl.open();
                            while (wifiControl.wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED)
                                Thread.sleep(100);
                            CStore store = new CStore(MainActivity.this);
                            String chipId = store.get("chipId");
                            WifiConfiguration cfg = wifiControl.create("smth" + chipId, "shibeta" + chipId);
                            if (cfg == null) {
                                handleMessage.what = 0x0404;
                                MainActivity.this.handler.sendMessage(handleMessage);
                                return;
                            }
                            netId = wifiControl.connect(cfg);
                            boolean connected = wifiControl.wifiManager.reconnect();
                            if (netId > 0 && connected)
                                handleMessage.what = 0x0203;
                            else
                                handleMessage.what = 0x0500;
                            MainActivity.this.handler.sendMessage(handleMessage);
                            handleMessage = new Message();
                            while (wifiControl.wifiManager.getConnectionInfo().getIpAddress()<1){
                                Thread.sleep(100);
                            }
                            //new TCP
                            Socket socket=null;
                            for(int i=0;i<10;i++){
                                try {
                                    socket = new Socket("192.168.4.1", 80);
                                    break;
                                }catch (Exception e){
                                    e.printStackTrace();
                                    if(i==9){
                                        handleMessage.what=0x0500;
                                        MainActivity.this.handler.sendMessage(handleMessage);
                                        return;
                                    }
                                    Thread.sleep(100);
                                }
                            }
                            OutputStream outs= socket.getOutputStream();
                            InputStream ins= socket.getInputStream();
                            String outStr="sp:" + store.get("wifi_ssid") + ":" + store.get("wifi_pwd")+"\0";
                            outs.write(outStr.getBytes());
                            outs.flush();
                            byte[] inBuf=new byte[33];
                            while (true) {
                                int readSize=ins.read(inBuf);
                                if (readSize!=-1) {
                                    String sckRes=new String(inBuf,0,readSize);
                                    if (sckRes.equals("2"))
                                        handleMessage.what = 0x0401;
                                    if (sckRes.equals("3"))
                                        handleMessage.what = 0x0424;
                                    if (sckRes.equals("4"))
                                        handleMessage.what = 0x0501;
                                    if (sckRes.equals("5"))
                                        handleMessage.what = 0x0200;
                                    else
                                        handleMessage.what = 0x0500;
                                    MainActivity.this.handler.sendMessage(handleMessage);
                                    break;
                                }
                                Thread.sleep(100);
                            }
                            outs.close();
                            ins.close();
                            socket.close();
                        }catch (Exception e){
                            handleMessage=new Message();
                            Log.d("Thread Error",e.toString());
                            handleMessage.what=0x0500;
                            handleMessage.obj=e.toString();
                            MainActivity.this.handler.sendMessage(handleMessage);
                        }
                        wifiControl.disconnect(netId);
                        wifiControl.wifiManager.removeNetwork(netId);
                        wifiControl.wifiManager.saveConfiguration();
                    }
                });
                SetMcu.start();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            //TODO:setting page
            return true;
        }
        if (id == R.id.action_close) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("确定退出么")
                    .setMessage("完全退出将无法接收到智能家园管家的消息推送哦~")
                    .setPositiveButton("留着管家", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("残忍离开", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            android.os.Process.killProcess(android.os.Process.myPid());
                            System.exit(0);
                        }
                    })
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
