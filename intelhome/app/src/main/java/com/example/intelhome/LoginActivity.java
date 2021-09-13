package com.example.intelhome;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private String realCode;
    private DBOpenHelper mDBOpenHelper;
    private TextView mTvLoginactivityRegister;
    private EditText mEtLoginactivityUsername;
    private EditText mEtLoginactivityPassword;
    private Button mBtLoginactivityLogin;
    private EditText mEtloginactivityPhonecodes;
    private ImageView mIvloginactivityShowcode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();

        mDBOpenHelper = new DBOpenHelper(this);

        //将验证码用图片的形式显示出来
        mIvloginactivityShowcode.setImageBitmap(Code.getInstance().createBitmap());
        realCode = Code.getInstance().getCode().toLowerCase();

        mEtLoginactivityPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());


    }

    private void initView() {
        // 初始化控件
        mBtLoginactivityLogin = findViewById(R.id.bt_loginactivity_login);
        mTvLoginactivityRegister = findViewById(R.id.tv_loginactivity_register);
        mEtLoginactivityUsername = findViewById(R.id.et_loginactivity_username);
        mEtLoginactivityPassword = findViewById(R.id.et_loginactivity_password);
        mEtloginactivityPhonecodes = findViewById(R.id.et_loginactivity_phoneCodes);
        mIvloginactivityShowcode = findViewById(R.id.iv_loginactivity_showCode);

        // 设置点击事件监听器
        mBtLoginactivityLogin.setOnClickListener(this);
        mIvloginactivityShowcode.setOnClickListener(this);
        mTvLoginactivityRegister.setOnClickListener(this);

    }

    public void onClick(View view) {
        switch (view.getId()) {
            // 跳转到注册界面
            case R.id.tv_loginactivity_register:
                startActivity(new Intent(this, RegisterActivity.class));
                finish();
                break;

            case R.id.iv_loginactivity_showCode:    //改变随机验证码的生成
                mIvloginactivityShowcode.setImageBitmap(Code.getInstance().createBitmap());
                realCode = Code.getInstance().getCode().toLowerCase();
                break;
            case R.id.bt_loginactivity_login:
                String name = mEtLoginactivityUsername.getText().toString().trim();
                String password = mEtLoginactivityPassword.getText().toString().trim();
                String phoneCode = mEtloginactivityPhonecodes.getText().toString().toLowerCase();

                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(password)&&!TextUtils.isEmpty(phoneCode)) {
                    if (phoneCode.equals(realCode)) {
                        ArrayList<User> data = mDBOpenHelper.getAllData();
                        boolean match = false;
                        for (int i = 0; i < data.size(); i++) {
                            User user = data.get(i);
                            if (name.equals(user.getName()) && password.equals(user.getPassword())/*&&phoneCode.equals(realCode)*/) {
                                match = true;
                                break;
                            } else {
                                match = false;
                            }
                        }

                        if (match) {
                            Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, MainActivity.class);
                            startActivity(intent);
                            finish();//销毁此Activity
                        } else {
                            Toast.makeText(this, "用户名或密码不正确，请重新输入", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(this, "验证码错误,注册失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "请输入你的用户名或密码", Toast.LENGTH_SHORT).show();
                }break;


        }


    }
}
