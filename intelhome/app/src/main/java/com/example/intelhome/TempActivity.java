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


public class TempActivity extends AppCompatActivity {
    //========界面定义
    private Switch sw_temp;
    private TextView txt_temp;
    private Switch sw_water;
    private TextView txt_water;
    private Switch sw_light;
    private TextView txt_luminance;
    private Button temp_back;

    //=========物联网部分定义
    private static final String IOT_IAM_GETTOKEN_URL = "https://iam.cn-north-4.myhuaweicloud.com/v3/auth/tokens";
    private static final String TAG = "TempActivity";
    private String AToken;
    private OkHttpClient client;
    private Timer timer;
    public boolean light_isopen = false;
    public boolean air_isopen = false;
    public boolean water_isopen = false;
    //======================
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 111) {
                obtainLuminance();
                obtaintemp();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);
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

                if (!AToken.isEmpty()) {
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

        temp_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TempActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        sw_light.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    light_isopen = true;
                    Controllight();
                } else {
                    light_isopen = false;
                    Controllight();
                }

            }
        });

        sw_temp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    air_isopen = true;
                    Controlair();
                } else {
                    air_isopen = false;
                    Controlair();
                }

            }
        });

        sw_water.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    water_isopen = true;
                    Controlwater();
                } else {
                    water_isopen = false;
                    Controlwater();
                }

            }
        });
    }

    ;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

    private void init() {
        sw_temp = findViewById(R.id.sw_temp);
        txt_temp = findViewById(R.id.txt_temp);
        sw_water = findViewById(R.id.sw_water);
        txt_water = findViewById(R.id.txt_water);
        sw_light = findViewById(R.id.sw_light);
        txt_luminance = findViewById(R.id.txt_luminance);
        temp_back = findViewById(R.id.temp_back);
    }


    private void obtainLuminance() {
        Request request = new Request.Builder()
                .url("https://iotda.cn-north-4.myhuaweicloud.com/v5/iot/0cdbb8cc6e80f3662f0ec014199129e9/devices/60de67fe496bac029eefee87_light001/properties?service_id=smart_light_control")
                .method("GET", null)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Auth-Token", AToken)
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "getLuminace Failure");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Log.d(TAG, response.body().string());
                String luminance = null;
                JSONObject object = JSON.parseObject(response.body().string());
                if (object != null) {
                    JSONObject responseObj = (JSONObject) object.get("response");
                    if (responseObj != null) {
                        JSONArray services = responseObj.getJSONArray("services");
                        if (services != null) {
                            for (int i = 0; i < services.size(); i++) {
                                JSONObject service = (JSONObject) services.get(i);
                                String service_id = (String) service.get("service_id");
                                JSONObject propertiesObj = (JSONObject) service.get("properties");
                                luminance = (String) propertiesObj.get("luminance");
                                Log.d(TAG, "luminance: " + luminance);
                                //Log.d(TAG,responseObj.toString());
                            }
                            final String finalLuminance = luminance;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txt_luminance.setText(finalLuminance);
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    private void obtaintemp() {
        Request request = new Request.Builder()
                .url("https://iotda.cn-north-4.myhuaweicloud.com/v5/iot/0cdbb8cc6e80f3662f0ec014199129e9/devices/60de67fe496bac029eefee87_temp/properties?service_id=smart_light_control")
                .method("GET", null)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Auth-Token", AToken)
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "getLuminace Failure");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Log.d(TAG, response.body().string());
                String temp = null, humidity = null;
                JSONObject object = JSON.parseObject(response.body().string());
                if (object != null) {
                    JSONObject responseObj = (JSONObject) object.get("response");
                    if (responseObj != null) {
                        JSONArray services = responseObj.getJSONArray("services");
                        if (services != null) {
                            for (int i = 0; i < services.size(); i++) {
                                JSONObject service = (JSONObject) services.get(i);
                                String service_id = (String) service.get("service_id");
                                JSONObject propertiesObj = (JSONObject) service.get("properties");
                                temp = (String) propertiesObj.get("temp");
                                humidity = (String) propertiesObj.get("humidity");
                                Log.d(TAG, " temp: " + temp + " humidity: " + humidity);
                            }
                            final String finalTemp = temp;
                            final String finalHumidity = humidity;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txt_temp.setText(finalTemp);
                                    txt_water.setText(finalHumidity);
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    private void Controllight() {
        MediaType mediaType = MediaType.parse("text/plain");

        RequestBody body;

        if (light_isopen) {
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
                        Toast.makeText(TempActivity.this, "命令发送成功", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    ;

    private void Controlair() {
        MediaType mediaType = MediaType.parse("text/plain");

        RequestBody body;

        if (air_isopen) {
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
                        Toast.makeText(TempActivity.this, "命令发送成功", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    ;

    private void Controlwater() {
        MediaType mediaType = MediaType.parse("text/plain");

        RequestBody body;

        if (water_isopen) {
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
                        Toast.makeText(TempActivity.this, "命令发送成功", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }



}
