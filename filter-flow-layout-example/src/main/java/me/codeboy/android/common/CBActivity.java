package me.codeboy.android.common;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;

/**
 * Created by yuedong.lyd on 6/8/15.
 */
public abstract  class CBActivity extends Activity{
    public CBHandler.UnleakHandler handler ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new CBHandler.UnleakHandler(this);
    }

    /**
     * 处理消息
     * @param msg
     */
    public abstract void processMessage(Message msg);
}