package org.shibeta.smth;

import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import org.shibeta.smth.CWifiControl.WifiMsg;

import java.util.ArrayList;
import java.util.List;


public class ACreateWifiCfg extends AppCompatActivity {
    protected RecyclerView recyclerView;
    protected WifiListAdapter adapter;
    protected RecyclerView.LayoutManager layoutManager;
    protected CWifiControl wifiControl;
    public List<WifiMsg> wifiMsgList=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_wifi_cfg);
        Toolbar toolbar = (Toolbar) findViewById(R.id.create_wifi_cfg_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextInputLayout pwdbox=(TextInputLayout) findViewById(R.id.create_wifi_cfg_pwd);
        pwdbox.setErrorEnabled(true);

        CStore store=new CStore(ACreateWifiCfg.this);
        String ssid=store.get("wifi_ssid");
        if(ssid.length()>0){
            String pwd=store.get("wifi_pwd");
            ((TextInputLayout) findViewById(R.id.create_wifi_cfg_ssid)).getEditText().setText(ssid.toCharArray(),0,ssid.length());
            ((TextInputLayout) findViewById(R.id.create_wifi_cfg_pwd)).getEditText().setText(pwd.toCharArray(),0,pwd.length());
        }

        recyclerView=(RecyclerView)findViewById(R.id.create_wifi_cfg_list);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter=new WifiListAdapter(wifiMsgList);
        recyclerView.setAdapter(adapter);

        wifiControl=new CWifiControl(ACreateWifiCfg.this);

        ImageButton enter=(ImageButton) findViewById(R.id.create_wifi_cfg_enter);
        enter.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                TextInputLayout ssidbox=(TextInputLayout) findViewById(R.id.create_wifi_cfg_ssid);
                TextInputLayout pwdbox=(TextInputLayout) findViewById(R.id.create_wifi_cfg_pwd);
                String ssid=ssidbox.getEditText().getText().toString();
                String pwd=pwdbox.getEditText().getText().toString();
                if(ssid.length()!=0&&(pwd.length()==0||(pwd.length()>=8&&pwd.length()<=16))){
                    CStore store=new CStore(ACreateWifiCfg.this);
                    store.set("wifi_ssid",ssid);
                    store.set("wifi_pwd", pwd);
                    setResult(200);
                    finish();
                }else{
                    pwdbox.setError("请输入8~16位的密码。");
                }
            }
        });
    }

    public void ScanWifi(){
        wifiControl.scan();
        wifiMsgList.clear();
        List<ScanResult> wifiList=wifiControl.getList();
        for(ScanResult res:wifiList){
            if(res.frequency>5000)
                continue;
            double power=(100+res.level>50.0)?50.0:(100+res.level);
            if(power<0.4)
                continue;
            WifiMsg wifiMsg=new WifiMsg(res.SSID,
                    "["+res.BSSID+"]  信道"+wifiControl.freq2channel(res.frequency)+((res.capabilities.equals("[ESS]"))?"  开放":""),
                    power/50.0);
            wifiMsgList.add(wifiMsg);
        }
        adapter.notifyItemRangeChanged(1, adapter.getItemCount());
    }

    public class WifiListAdapter extends RecyclerView.Adapter<WifiListAdapter.ViewHolder>{
        private  List<WifiMsg> list;
        public int recyclerWidth=0;

        public WifiListAdapter(List<WifiMsg> list){
            this.list=list;
            final View view=ACreateWifiCfg.this.findViewById(R.id.create_wifi_cfg_list);
            view.post(new Runnable() {
                @Override
                public void run() {
                    adapter.recyclerWidth=view.getWidth();
                    ScanWifi();
                }
            });
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            public View frame;
            public TextView power;
            public TextView ssid;
            public TextView ext;

            public ViewHolder(View v){
                super(v);
                ssid=(TextView)v.findViewById(R.id.item_wifi_ssid);
                ext=(TextView)v.findViewById(R.id.item_wifi_ext);
                power=(TextView)v.findViewById(R.id.item_wifi_power);
                frame=v.findViewById(R.id.item_wifi_frame);
            }
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
            View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wifi,null);
            ViewHolder viewHolder=new ViewHolder(v);
            return viewHolder;
        }
        @Override
        public void onBindViewHolder(ViewHolder vh,int i){
            WifiMsg wifiMsg=list.get(i);
            vh.ssid.setText(wifiMsg.ssid);
            vh.ext.setText(wifiMsg.ext);
            vh.power.getLayoutParams().width=(int) (recyclerWidth * wifiMsg.power);
            if(!vh.frame.hasOnClickListeners()){
                vh.frame.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView extTextView=(TextView)v.findViewById(R.id.item_wifi_ext);
                        String ext=extTextView.getText().toString();
                        TextView textView=(TextView)v.findViewById(R.id.item_wifi_ssid);
                        String ssid=textView.getText().toString();
                        TextInputLayout ssidbox=(TextInputLayout)ACreateWifiCfg.this.findViewById(R.id.create_wifi_cfg_ssid);
                        ssidbox.getEditText().setText(ssid.toCharArray(),0,ssid.length());
                        String pwd="";
                        TextInputLayout pwdbox=(TextInputLayout)ACreateWifiCfg.this.findViewById(R.id.create_wifi_cfg_pwd);
                        pwdbox.getEditText().setText(pwd.toCharArray(), 0, pwd.length());
                        if(ext.length()>27){
                            pwdbox.getEditText().setEnabled(false);
                        } else{
                            pwdbox.getEditText().setEnabled(true);
                            pwdbox.getEditText().requestFocus();
                        }
                    }
                });
            }
        }
        @Override
        public int getItemCount(){
            return list.size();
        }
    }

    @Override
    public void onBackPressed(){
        setResult(500);
        finish();
    }
}
