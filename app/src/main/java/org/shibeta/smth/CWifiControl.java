package org.shibeta.smth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by stitchcula on 2016/4/14.
 */
public class CWifiControl {
    //private Context ctx;
    //private Condition condition;
    //private Lock lock;
    public WifiManager wifiManager;
    private WifiManager.WifiLock wifiLock;
    List<ScanResult> wifiList;
    //private WifiReceiver wifiReceiver;
    //private WifiReceiveListener wifiReceiveListener;

    /*
    public interface WifiReceiveListener{
        public void todo(List<WifiMsg> wifiMsgs);
    }
    */

    public CWifiControl(Context ctx){
        //this.ctx=ctx;
        //lock = new ReentrantLock();
        //condition = lock.newCondition();
        wifiManager=(WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
    }

    public void scan(){
        wifiManager.startScan();
        wifiList=wifiManager.getScanResults();
    }

    public List<ScanResult> getList(){
        return wifiList;
    }

    public int connect(String ssid,String pwd){
        WifiConfiguration cfg=create(ssid, pwd);
        int netId=connect(cfg);
        return netId;
    }
    public boolean connect(int netId){
        return wifiManager.enableNetwork(netId,true);
    }
    public int connect(WifiConfiguration cfg){
        int netId=wifiManager.addNetwork(cfg);
        wifiManager.enableNetwork(netId,true);
        return netId;
    }

    public void disconnect(int netId){
        wifiManager.disableNetwork(netId);
        wifiManager.disconnect();
    }

    public void forget(String ssid){}

    public WifiConfiguration create(String ssid,String pwd){
        WifiConfiguration tmpCfg=this.isExsit(ssid);
        if(tmpCfg!=null)
            wifiManager.removeNetwork(tmpCfg.networkId);

        WifiConfiguration cfg=new WifiConfiguration();

        cfg.allowedAuthAlgorithms.clear();
        cfg.allowedGroupCiphers.clear();
        cfg.allowedKeyManagement.clear();
        cfg.allowedPairwiseCiphers.clear();
        cfg.allowedProtocols.clear();

        cfg.SSID="\""+ssid+"\"";
        cfg.preSharedKey="\""+pwd+"\"";
        //cfg.hiddenSSID=false;
        cfg.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        cfg.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        cfg.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        cfg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        cfg.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        cfg.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        cfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        cfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        cfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        cfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        cfg.status=WifiConfiguration.Status.ENABLED;

        return cfg;
    }

    public WifiConfiguration isExsit(String ssid){
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs)
            if (existingConfig.SSID.equals("\""+ssid+"\""))
                return existingConfig;
        return null;
    }

    /*
    public void scan(final WifiReceiveListener wifiReceiveListener){
        this.wifiReceiveListener=wifiReceiveListener;
        wifiReceiver=new WifiReceiver();
        new Thread(new Runnable() {
            @Override
            public void run() {
                open();
                ctx.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                wifiManager.startScan();
                lock.lock();
                try {
                    condition.await(10, TimeUnit.SECONDS);
                }catch(InterruptedException e) {
                    e.printStackTrace();
                }
                lock.unlock();
                ctx.unregisterReceiver(wifiReceiver);
            }
        }).start();
    }
    */

    public boolean open(){
        if(!wifiManager.isWifiEnabled()){
            return wifiManager.setWifiEnabled(true);
        }
        return true;
    }

    public boolean close(){
        if(wifiManager.isWifiEnabled()){
            return wifiManager.setWifiEnabled(false);
        }
        return true;
    }

    public int status(){
        return wifiManager.getWifiState();
    }

    public void lockon(){
        wifiLock.acquire();
    }

    public void unlock(){
        if(wifiLock.isHeld()){
            wifiLock.acquire();
        }
    }

    public void createLock(){
        wifiLock=wifiManager.createWifiLock("shibeta");
    }

    /*
    public class WifiReceiver extends BroadcastReceiver{
        public void onReceive(Context ctx, Intent intent) {
            List<WifiMsg> wifiMsgs=new ArrayList<>();
            List<ScanResult> scanResults=wifiManager.getScanResults();
            for(ScanResult res:scanResults){
                double power=(100+res.level>50.0)?50.0:(100+res.level);
                if(power<0.4) break;
                WifiMsg wifiMsg=new WifiMsg(res.SSID,
                        res.BSSID+"  "+String.valueOf(res.frequency)+"MHz  "+res.capabilities,
                        power/50.0);
                wifiMsgs.add(wifiMsg);
                Log.v("SSID: ", res.SSID);
                Log.v("SSID: ",String.valueOf(res.frequency));
                Log.v("power: ",String.valueOf(power/50.0));
            }
            wifiReceiveListener.todo(wifiMsgs);
        }
    }*/

    public static class WifiMsg{
        public String ssid;
        public String ext;
        public double power;
        public WifiMsg(String ssid,String ext,double power){
            this.ssid=ssid;
            this.ext=ext;
            this.power=power;
        }
    }

    public String freq2channel(int freq){
        String ch=String.valueOf((freq-2407)/5);
        return (ch.length()<2)?(" "+ch):ch;
    }
}
