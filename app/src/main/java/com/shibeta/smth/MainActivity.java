package com.shibeta.smth;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.app.AlertDialog;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

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
                startActivityForResult(new Intent(MainActivity.this, AScanQR.class),M.scanQR);
            }
        });
    }

    @Override
    protected void onActivityResult(int reqCode,int resCode,Intent data){
        super.onActivityResult(reqCode,resCode,data);
        if(reqCode==M.scanQR){
            if(resCode==200){
                TextView msg=(TextView)findViewById(R.id.main_text_msg);
                msg.setText("连接中……可能需要一点时间。\n"+
                    "Debug:\n"+
                    "data:"+data.getStringExtra("data")+"\n"+
                    "format:"+data.getStringExtra("format")
                );
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

        //noinspection SimplifiableIfStatement
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
