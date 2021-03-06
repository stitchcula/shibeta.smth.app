package org.shibeta.smth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.regex.Pattern;

import me.dm7.barcodescanner.core.ViewFinderView;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

/**
 * Created by stitchcula on 2016/4/13.
 */
public class AScanQR extends AppCompatActivity implements ZBarScannerView.ResultHandler{
    protected ZBarScannerView scannerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scannerView=new ZBarScannerView(this);
        View view= LayoutInflater.from(this).inflate(R.layout.scan_qr,null);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.scan_qr_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        LinearLayout includeLayout=(LinearLayout) view.findViewById(R.id.scan_qr_includeLayout);
        includeLayout.addView(scannerView);
        setContentView(view);
    }
    @Override
    protected void onActivityResult(int reqCode,int resCode,Intent data){
        super.onActivityResult(reqCode, resCode, data);
        if(reqCode==M.createWifiCfg){
            if(resCode==200){
                setResult(200);
                finish();
            }
            if(resCode==500){
                finish();
            }
        }
    }
    @Override
    public void handleResult(Result rawRes){
        scannerView.stopCamera();
        CStore store=new CStore(AScanQR.this);
        String[] arr=rawRes.getContents().split("=");
        String reg="^[0-9]{8}$";
        if(Pattern.matches(reg,arr[1])) {
            store.set("chipId", arr[1]);
            startActivityForResult(new Intent(AScanQR.this, ACreateWifiCfg.class), M.createWifiCfg);
        }
    }
    @Override
    protected void onResume(){
        super.onResume();
        scannerView.setResultHandler(this);
        scannerView.startCamera();
        scannerView.setFlash(false);
        scannerView.setAutoFocus(true);
    }
    @Override
    protected void onPause(){
        super.onPause();
        scannerView.stopCamera();
    }
}
