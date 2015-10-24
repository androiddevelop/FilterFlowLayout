package me.codeboy.android.filterflowlayout.example;

import android.os.Bundle;
import android.os.Message;
import android.view.Gravity;

import me.codeboy.android.common.CBActivity;
import me.codeboy.android.lib.FilterFlowLayout;


/**
 * Created by yuedong.lyd on 7/7/15.
 */
public class FlowLayoutActivity extends CBActivity {

    FilterFlowLayout filterFlowLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flowlayout);

        filterFlowLayout = (FilterFlowLayout)findViewById(R.id.filterFlowLayout);

        new Thread(){
            public void run(){
                try {
                    sleep(2000);
                    handler.sendEmptyMessage(0);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }.start();
    }

    @Override
    public void processMessage(Message message) {
        int what =message.what;
        if(what==0) {
            filterFlowLayout.setMaxLines(2);
            new Thread(){
                public void run(){
                    try {
                        sleep(2000);
                        handler.sendEmptyMessage(1);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }.start();
            return ;
        }

        if(what == 1){
            filterFlowLayout.setMaxLines(3);
            new Thread(){
                public void run(){
                    try {
                        sleep(2000);
                        handler.sendEmptyMessage(2);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }.start();
            return ;
        }

        if(what == 2){
            filterFlowLayout.setGravity(Gravity.CENTER);
            new Thread(){
                public void run(){
                    try {
                        sleep(2000);
                        handler.sendEmptyMessage(3);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }.start();
            return ;
        }

        if(what == 3){
            filterFlowLayout.setHorizontalGap(0);
            new Thread(){
                public void run(){
                    try {
                        sleep(2000);
                        handler.sendEmptyMessage(4);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }.start();
            return ;
        }

        if(what == 4){
            filterFlowLayout.setVerticalGap(0);
            new Thread(){
                public void run(){
                    try {
                        sleep(2000);
                        handler.sendEmptyMessage(5);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }.start();
            return ;
        }

    }
}