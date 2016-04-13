package com.shibeta.smth;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;


public class ACreateWifiCfg extends AppCompatActivity {

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

    @Override
    public void onBackPressed(){
        setResult(500);
        finish();
    }

}
