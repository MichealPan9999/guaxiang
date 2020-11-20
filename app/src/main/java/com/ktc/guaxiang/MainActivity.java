package com.ktc.guaxiang;

import android.content.Context;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private Context mContext;
    private Button mBtnStart;
    private TextView mTvResult;

    private WebView webContent;
    private static int START_GENERATE_GUA = 1;
    private static int GENERATE_GUA_1 = 2;
    private static int GENERATE_GUA_2 = 3;
    private static int GENERATE_GUA_FINAL = 4;
    private static int GENERATE_YAO = 5;


    private final int CORE_POOL_SIZE = 4;//核心线程数
    private final int MAX_POOL_SIZE = 10;//最大线程数
    private final long KEEP_ALIVE_TIME = 10;//空闲线程超时时间
    private ThreadPoolExecutor executorPool;
    private boolean mCanStartFlag = false;//判断是否可以开始进行，一段时间内只能执行一次
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            int i1 = 0;
            int i2 = 0;
            if (msg.what == START_GENERATE_GUA) {
                startGetGuaxiang();
            } else if (msg.what == GENERATE_GUA_1) {
                i1 = msg.arg1;
                mTvResult.append(new Date() + " , " + msg.arg1 + " " + Utils.bagua[msg.arg1] + "\n");
            } else if (msg.what == GENERATE_GUA_2) {
                i2 = msg.arg1;
                mTvResult.append(new Date() + " , " + msg.arg1 + " " + Utils.bagua[msg.arg1] + "\n");
            } else if (msg.what == GENERATE_GUA_FINAL) {
                String final_guaxiang = Utils.guaxiang[msg.arg1][msg.arg2];
                mTvResult.append(new Date() + " 生成 " + msg.arg1 + "" + msg.arg2 + " : " + final_guaxiang + "\n");
                String url = "http://www.baidu.com/s?&ie=utf-8&oe=UTF-8&wd=" + final_guaxiang+"卦详解";
                Log.d("panzq","=== url "+url);
                webContent.loadUrl(url);
            } else if (msg.what == GENERATE_YAO) {
                mTvResult.append(new Date() + " 第几爻:" + (msg.arg1 + 1) + "\n");
                mCanStartFlag = true;
                Log.d("panzq", "-------------结束-----");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;
        mCanStartFlag = true;
        initViews();
        //创建线程池
        initExec();
    }

    private void initExec() {
        executorPool = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(3),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
    }

    private void initViews() {
        mBtnStart = findViewById(R.id.btn_start);
        mTvResult = findViewById(R.id.tv_result);
        webContent = findViewById(R.id.web_content);
        webContent.getSettings().setJavaScriptEnabled(true);//getSettiongs()用于设置一些浏览器属性，这里让WebView支持JavaScript脚本
        webContent.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });//当需要从一个网页跳转到另一个网页是，希望目标网页仍然在当前WebView显示，而不是打开浏览器
        //是否使用WebView内置的放大机制，貌似设置了这条以后下面那条不用设置了
        webContent.getSettings().setBuiltInZoomControls(true);
        //设置WebView是否支持放大
        webContent.getSettings().setSupportZoom(true);


        //以下两条设置可以使页面适应手机屏幕的分辨率，完整的显示在屏幕上
        //设置是否使用WebView推荐使用的窗口
        webContent.getSettings().setUseWideViewPort(true);
        //设置WebView加载页面的模式
        webContent.getSettings().setLoadWithOverviewMode(true);
    }

    public void start(View view) {
        if (view.getId() == R.id.btn_start) {
            Log.d("panzq", "开始 ===== " + mCanStartFlag);
            if (mCanStartFlag) {
                mCanStartFlag = false;
                mHandler.removeCallbacksAndMessages(null);
                mTvResult.setText(" 正在生成：" + "\n");
                mHandler.removeMessages(START_GENERATE_GUA);
                mHandler.sendEmptyMessageDelayed(START_GENERATE_GUA, 1000);
            }
        }
    }

    private void startGetGuaxiang() {
        if (executorPool != null) {
            executorPool.execute(new WorkerThread());
        }

    }

    public int getRandomValue(int max) {
        Random random = new Random();
        int randomVal = random.nextInt(max);
        int tempVal = 0;
        for (int i = 0; i < 50; i++) {
            while (tempVal == randomVal) {
                randomVal = random.nextInt(max);
            }
            tempVal = randomVal;
        }
        return randomVal;
    }

    class WorkerThread implements Runnable {
        @Override
        public void run() {
            int i1 = getRandomValue(8);
            Message msg1 = mHandler.obtainMessage();
            msg1.what = GENERATE_GUA_1;
            msg1.arg1 = i1;
            mHandler.removeMessages(GENERATE_GUA_1);
            mHandler.sendMessageDelayed(msg1, 1000);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int i2 = getRandomValue(8);
            Message msg2 = mHandler.obtainMessage();
            msg2.what = GENERATE_GUA_2;
            msg2.arg1 = i2;
            mHandler.removeMessages(GENERATE_GUA_2);
            mHandler.sendMessageDelayed(msg2, 1200);

            Message msg3 = mHandler.obtainMessage();
            msg3.what = GENERATE_GUA_FINAL;
            msg3.arg1 = i1;
            msg3.arg2 = i2;
            mHandler.removeMessages(GENERATE_GUA_FINAL);
            mHandler.sendMessageDelayed(msg3, 1500);

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int i3 = getRandomValue(6);
            Message msg4 = mHandler.obtainMessage();
            msg4.what = GENERATE_YAO;
            msg4.arg1 = i3;
            mHandler.removeMessages(GENERATE_YAO);
            mHandler.sendMessageDelayed(msg4, 2000);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webContent.canGoBack()){
            webContent.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}