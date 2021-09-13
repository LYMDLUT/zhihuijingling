package com.example.intelhome;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SmokeActivity extends AppCompatActivity {
    private TextView txt_smoke;
    private Button smoke_back;
    private Switch sw_beep;
    private boolean isopen=false;
    private boolean beep_isopen=false;
    private static final String IOT_IAM_GETTOKEN_URL = "https://iam.cn-north-4.myhuaweicloud.com/v3/auth/tokens";
    private static final String TAG = "SmokeActivity";
    private String AToken;
    private OkHttpClient client;
    private Timer timer;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 111) {
                obtainSmoke();
            }
        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smoke);
        init();

        client = new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");

        RequestBody body = RequestBody.create(mediaType, "{ \n    \"auth\": { \n        \"identity\": { \n            \"methods\": [ \n                \"password\" \n            ], \n            \"password\": { \n                \"user\": { \n                    \"name\": \"hyhyhyh\", \n                    \"password\": \"hyh2013qaz\", \n                    \"domain\": { \n                        \"name\": \"hid_hl-heg_f7n8od9-\" \n                    } \n                } \n            } \n        }, \n        \"scope\": { \n            \"project\": { \n                \"name\": \"cn-north-4\" \n            } \n        } \n    } \n}");

        Request request = new Request.Builder()
                .url("https://iam.cn-north-4.myhuaweicloud.com/v3/auth/tokens")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                AToken = response.header("X-Subject-Token");
                Log.d(TAG, AToken);

                if(!AToken.isEmpty()){
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            handler.sendMessage(handler.obtainMessage(111));
                        }
                    }, 3000, 1000);
                }
            }

        });

        smoke_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(SmokeActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        sw_beep.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    beep_isopen = true;
                    Controlbeep();
                } else {
                    beep_isopen = false;
                    Controlbeep();
                }
            }
        });



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

    private void init(){
        txt_smoke=findViewById(R.id.txt_smoke);
        smoke_back=findViewById(R.id.smoke_back);
        sw_beep=findViewById(R.id.sw_smoke);
    }

    private void Controlbeep()
    {
        MediaType mediaType = MediaType.parse("text/plain");

        RequestBody body;

        if (beep_isopen) {
            body = RequestBody.create(mediaType, "{\"command_name\":\"Beep\",\"paras\":{\"Beep_control\":\"ON\"},\"service_id\":\"Smoke\"}");
        } else {
            body = RequestBody.create(mediaType, "{\"command_name\":\"Beep\",\"paras\":{\"Beep_control\":\"OFF\"},\"service_id\":\"Smoke\"}");
        }
        Request request = new Request.Builder()
                .url("https://iotda.cn-north-4.myhuaweicloud.com/v5/iot/0cdbb8cc6e80f3662f0ec014199129e9/devices/60ee7c68f4b59002868ee1ad_smoke_beep/commands")
                .method("POST", body)
                .addHeader("X-Auth-Token", AToken)
                .addHeader("Content-Type", "text/plain")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SmokeActivity.this, "命令发送成功", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void obtainSmoke() {
        Request request = new Request.Builder()
                .url("https://iotda.cn-north-4.myhuaweicloud.com/v5/iot/0cdbb8cc6e80f3662f0ec014199129e9/devices/60ee7c68f4b59002868ee1ad_smoke001/properties?service_id=Smoke")
                .method("GET", null)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Auth-Token", AToken)
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "getSmoke Failure");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String smoke = null;
                JSONObject object = JSON.parseObject(response.body().string());
                if (object != null) {
                    JSONObject responseObj = (JSONObject) object.get("response");
                    if(responseObj!=null) {
                        JSONArray services = responseObj.getJSONArray("services");
                        if (services != null) {
                            for (int i = 0; i < services.size(); i++) {
                                JSONObject service = (JSONObject) services.get(i);
                                String service_id = (String) service.get("service_id");
                                JSONObject propertiesObj = (JSONObject) service.get("properties");
                                smoke=(String) propertiesObj.get("Smoke_Value");
                                Log.d(TAG, "Smoke_Value: " + smoke );
                                //Log.d(TAG,responseObj.toString());
                            }
                            final String finalSmoke=smoke;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txt_smoke.setText(finalSmoke);
                                }
                            });
                        }
                    }
                }
            }
        });
    }
}
