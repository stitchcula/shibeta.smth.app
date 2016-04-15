package org.shibeta.smth;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by stitchcula on 2016/4/12.
 */
public class CStore {
    protected Context ctx;

    public CStore(Context ctx){
        this.ctx=ctx;
    }

    public String get(String key){
        SharedPreferences read=this.ctx.getSharedPreferences("data",this.ctx.MODE_WORLD_READABLE);
        return read.getString(key,"");
    }

    public boolean set(String key,String value){
        SharedPreferences.Editor editor=this.ctx.getSharedPreferences("data",this.ctx.MODE_WORLD_WRITEABLE).edit();
        editor.putString(key,value);
        return editor.commit();
    }
}
