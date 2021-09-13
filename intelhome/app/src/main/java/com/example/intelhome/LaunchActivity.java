package com.example.intelhome;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

public class LaunchActivity extends AppCompatActivity {
    private Button step;
    private boolean ismain=false;
    private int i=3;
    private Timer timer = null;
    private TimerTask task = null;

    @Override

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        init();
        Handler x=new Handler();

        step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ismain=true;
                Intent intent=new Intent(LaunchActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });
        x.postDelayed(new splashhandler(),3000);
        startTime();

    }

    private void init(){
        step=findViewById(R.id.step);
    }

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            step.setText("跳过"+"|"+Integer.toString(i));
            startTime();
        }
    };

    public void startTime() {
        timer = new Timer();
        task= new TimerTask() {
            @Override
            public void run() {
                if (i > 0) {   //加入判断不能小于0
                    i--;
                    Message message = handler.obtainMessage();
                    message.arg1 = i;
                    handler.sendMessage(message);
                }
            }
        };
        timer.schedule(task, 1000);
    }



    class splashhandler implements Runnable{
        @Override
        public void run() {
            if(ismain==false) {
                startActivity(new Intent(getApplication(), LoginActivity.class));// 这个线程的作用3秒后就是进入到你的主界面
                LaunchActivity.this.finish();// 把当前的LaunchActivity结束掉
            }
        }
    }
}
