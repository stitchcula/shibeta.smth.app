package com.shibeta.smth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

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
        startActivityForResult(new Intent(AScanQR.this,ACreateWifiCfg.class),M.createWifiCfg);
        super.onCreate(savedInstanceState);
        scannerView=new ZBarScannerView(this);
        scannerView.stopCamera();
        //ViewFinderView finderView=new ViewFinderView(this);
        //View view= LayoutInflater.from(this).inflate(R.layout.scan_qr,null);
        //finderView.addView(view);
        //ZBarScannerView.init(finderView);
    }
    @Override
    protected void onActivityResult(int reqCode,int resCode,Intent data){
        super.onActivityResult(reqCode,resCode,data);
        if(reqCode==M.createWifiCfg){
            if(resCode==200){
                View view= LayoutInflater.from(this).inflate(R.layout.scan_qr,null);
                Toolbar toolbar = (Toolbar) view.findViewById(R.id.scan_qr_toolbar);
                setSupportActionBar(toolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                LinearLayout includeLayout=(LinearLayout) view.findViewById(R.id.scan_qr_includeLayout);
                includeLayout.addView(scannerView);
                setContentView(view);
            }
            if(resCode==500){
                finish();
            }
        }
    }
    @Override
    public void handleResult(Result rawRes){
        Intent intent=new Intent();
        intent.putExtra("data",rawRes.getContents());
        intent.putExtra("format",rawRes.getBarcodeFormat().getName());
        setResult(200,intent);
        finish();
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
