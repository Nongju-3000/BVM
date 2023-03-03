package com.credo.bvm;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

@SuppressLint("MissingPermission")
public class MainActivity extends AppCompatActivity {

    private BLEService mBLEService;
    private BluetoothDevice BVM_device, CPRBAND_device;
    private BVM_Adapter bvm_adapter;
    private CPRBAND_Adapter cprband_adapter;
    private static int depth_true, depth_false, depth_over, breathcount, cor_breathcount;

    private ArrayList<Integer> breathlist = new ArrayList<>();

    private int BREATH_MIN = 16000;
    private static final int BREATH_MAX = 19500;
    private boolean isfirst = true;

    private final String[] REQUIRED_PERMISSIONS = new String[]{
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.BLUETOOTH",
            "android.permission.BLUETOOTH_ADMIN",
            "android.permission.BLUETOOTH_CONNECT",
            "android.permission.BLUETOOTH_PRIVILEGED",
            "android.permission.BLUETOOTH_SCAN"};

    private LineChart cprChart, cprbandChart, bvmChart;
    private LineData cprlineData, bvmlineData;
    private LineDataSet cprlineDataSet, bvmlineDataSet;
    private ImageView angleImageView, arrowDown, ani01, ani02, lungTel, testLung;
    private ClipDrawable lungClip;
    private TextView bvmStatus, cprStatus, bvmTime, cprTime, bvmCount, cprCount, bvmTotal, cprTotal, depthText, arrowDownText, BVMTimerTv;
    private RecyclerView bvmRecyclerView, cprRecyclerView;
    private Button bvmButton, cprButton, settingButton, searchButton, connectButton, standardCRPBtnTel, depthBtnCprTel, depthBtnCprUp;
    private View fragmentBvm, fragmentCpr, fragmentSettings, depthCPRViewTel, innerView;
    private Drawable drawable, lungclip;

    public static final int MAX_LEVEL = 10000;
    public static final int LEVEL_DIFF_UP = 500;
    public static final int LEVEL_DIFF_DOWN = 500;
    public static final int DELAY = 4;

    private int bvmLevel = 100;
    private boolean isbreath, isthreadrun = false;

    private float sizebylevel, current_time, prev_time = 0f;

    private int prev_size = 0;
    
    private boolean ispeak = false;

    private float x = 0;
    private float y = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        getWindow().addFlags(View.KEEP_SCREEN_ON);

        handler = new Handler();
        bvmhandler = new Handler();

        bvmButton = findViewById(R.id.bvm_button);
        cprButton = findViewById(R.id.cpr_button);
        settingButton = findViewById(R.id.setting_button);
        fragmentBvm = findViewById(R.id.fragment_bvm);
        fragmentCpr = findViewById(R.id.fragment_cpr);
        fragmentSettings = findViewById(R.id.fragment_setting);
        bvmTime = fragmentBvm.findViewById(R.id.bvm_time_value_tv);
        bvmCount = fragmentBvm.findViewById(R.id.bvm_count_value_tv);
        bvmTotal = fragmentBvm.findViewById(R.id.bvm_total_value_tv);
        bvmChart = fragmentBvm.findViewById(R.id.bvm_chart);
        innerView = fragmentBvm.findViewById(R.id.BVM_speed_inner_gauge);
        BVMTimerTv = fragmentBvm.findViewById(R.id.BVM_timer_tv);
        cprChart = fragmentCpr.findViewById(R.id.cpr_chart);
        cprbandChart = fragmentCpr.findViewById(R.id.cpr_chart_band);
        depthText = fragmentCpr.findViewById(R.id.depth_text);
        ani01 = fragmentCpr.findViewById(R.id.ani01);
        ani02 = fragmentCpr.findViewById(R.id.ani02);
        depthBtnCprTel = fragmentCpr.findViewById(R.id.depth_btn_cpr_tel);
        cprTime = fragmentCpr.findViewById(R.id.cpr_time_value_tv);
        testLung = fragmentCpr.findViewById(R.id.test_lung01);
        lungClip = (ClipDrawable) testLung.getDrawable();
        lungTel = fragmentCpr.findViewById(R.id.lung_tel);
        arrowDown = fragmentCpr.findViewById(R.id.arrow_down);
        arrowDownText = fragmentCpr.findViewById(R.id.arrow_down_text);
        angleImageView = fragmentCpr.findViewById(R.id.angle_);
        depthBtnCprUp = fragmentCpr.findViewById(R.id.depth_btn_cpr_up);
        standardCRPBtnTel = fragmentCpr.findViewById(R.id.standardCPR_btn_tel);
        depthCPRViewTel = fragmentCpr.findViewById(R.id.depthCPR_view_tel);
        cprTotal = fragmentCpr.findViewById(R.id.cpr_total_value_tv);
        cprCount = fragmentCpr.findViewById(R.id.cpr_count_value_tv);
        bvmRecyclerView = fragmentSettings.findViewById(R.id.bvm_recyclerview);
        cprRecyclerView = fragmentSettings.findViewById(R.id.cpr_recyclerview);
        searchButton = fragmentSettings.findViewById(R.id.search_btn);
        connectButton = fragmentSettings.findViewById(R.id.connect_btn);
        bvmStatus = fragmentSettings.findViewById(R.id.bvm_status);
        cprStatus = fragmentSettings.findViewById(R.id.cprband_status);
        drawable = getDrawable(R.drawable.circle_inner_drawable);
        lungclip = getDrawable(R.drawable.lung_normal_clip);

        bvm_adapter = new BVM_Adapter();
        cprband_adapter = new CPRBAND_Adapter();

        bvmRecyclerView.setAdapter(bvm_adapter);
        bvmRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cprRecyclerView.setAdapter(cprband_adapter);
        cprRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, 1);

        if(mBLEService == null) {
            Intent intent = new Intent(this, BLEService.class);
            bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        }



        for(int i = 0 ; i < 100; i++){
            temp_level.add(100*i);
        }
        for(int i = 99 ; i >= 0; i--){
            temp_level.add(100*i);
        }


        depthCPRViewTel.post(() -> {
            depth_true = depthCPRViewTel.getHeight();
            depth_over = depthCPRViewTel.getHeight() + 35;
            depth_false = depthCPRViewTel.getHeight() / 3;
        });

        bvmButton.setOnClickListener(v -> {
            fragmentBvm.setVisibility(View.VISIBLE);
            fragmentCpr.setVisibility(View.INVISIBLE);
            fragmentSettings.setVisibility(View.INVISIBLE);
        });

        cprButton.setOnClickListener(v -> {
            fragmentBvm.setVisibility(View.INVISIBLE);
            fragmentCpr.setVisibility(View.VISIBLE);
            fragmentSettings.setVisibility(View.INVISIBLE);
        });

        settingButton.setOnClickListener(v -> {
            fragmentBvm.setVisibility(View.INVISIBLE);
            fragmentCpr.setVisibility(View.INVISIBLE);
            fragmentSettings.setVisibility(View.VISIBLE);
        });

        searchButton.setOnClickListener(v -> {
            if(mBLEService != null){
                if(bvmStatus.getText().equals("Connected")) {
                    mBLEService.disconnectBVM();
                }
                if(cprStatus.getText().equals("Connected")) {
                    mBLEService.disconnectBAND();
                }
                bvmStatus.setText(R.string.disconnect);
                cprStatus.setText(R.string.disconnect);
                handler.removeCallbacksAndMessages(null);
                breathlist.clear();
                cprlineData = null;
                bvmlineData = null;
                cprChart.clear();
                bvmChart.clear();
                cprbandChart.clear();
                cprlineData = new LineData();
                bvmlineData = new LineData();
                cprlineData = createCPRLineData();
                bvmlineData = createBVMLineData();
                setLineChart();
                BVM_device = null;
                CPRBAND_device = null;
                bvm_adapter.clearBVM_Data();
                cprband_adapter.clearCPRBAND_Data();
                mBLEService.startBLEscan();
            }
        });

        connectButton.setOnClickListener(v -> {
            if(mBLEService != null){
                mBLEService.stopBLEscan();
                bvm_adapter.clearBVM_Data();
                cprband_adapter.clearCPRBAND_Data();
                if(BVM_device != null) {
                    mBLEService.connectBVM(BVM_device);
                    bvmStatus.setText(R.string.connecting);
                }
                if(CPRBAND_device != null) {
                    mBLEService.connectBAND(CPRBAND_device);
                    cprStatus.setText(R.string.connecting);
                }
            }
        });

        bvm_adapter.setOnItemClickListener(new BVM_Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                BVM_device = bvm_adapter.getItem(pos);
                bvmStatus.setText(BVM_device.getAddress());
            }
        });

        cprband_adapter.setOnItemClickListener(new CPRBAND_Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                CPRBAND_device = cprband_adapter.getItem(pos);
                cprStatus.setText(CPRBAND_device.getAddress());
            }
        });

        //calculateSize(bvmLevel);
    }

    /*Thread t = new Thread(() -> {
        isbreath = false;
        for(int i = 5; i > 0; i--){
            try{
                Log.e("Thread", "Thread is running" + i);
                if(isbreath){
                    i = 5;
                    isbreath = false;
                }
                BVMTimerTv.setText(String.valueOf(i));
                Thread.sleep(1000);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        BVMTimerTv.setText("");
    });*/



    // Start the thread
    public void startThread() {
        t = new Thread(() -> {
            isbreath = false;
            for(int i = 5; i >= 0; i--){
                try{
                    if(i == 0){
                        BVMTimerTv.setText("");
                    }
                    if(isbreath){
                        i = 5;
                        isbreath = false;
                    }
                    BVMTimerTv.setText(String.valueOf(i));
                    Thread.sleep(1000);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    // Stop the thread
    public void stopThread() {
        if(t != null) {
            t.interrupt();
            t = null;
        }
    }

    // Restart the thread
    public void restartThread() {
        stopThread();
        startThread();
    }

    private void displayBVMBreath(int time){

        /*new Thread(()-> {
            for(int i = 0; i <= 20; i++) {
                try {
                    int finalI = i;
                    runOnUiThread(() -> {
                        ValueAnimator animator = ValueAnimator.ofInt(prev_size, (int)(finalI * 5 * sizebylevel));
                        Log.e("size", String.valueOf(sizebylevel));
                        Log.e("prev_size", String.valueOf(prev_size));
                        animator.setDuration(250);
                        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                                int animatedValue = (int) animation.getAnimatedValue();
                                ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(animatedValue, animatedValue);
                                innerView.setLayoutParams(layoutParams);
                                innerView.setX(x);
                                innerView.setY(y);
                            }
                        });
                        animator.start();

                        prev_size = (int)(finalI * 5 * sizebylevel);
                    });
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();*/


            if (((3000 <= time) && ( 5000 >= time )) || ((7000 <= time) && (9000 >= time))){
                drawable.setTint(getColor(R.color.breathline));// 주황
                innerView.setBackground(drawable);
            } else if (( time <= 3000 ) || ( time >= 9000 )){// 빨강
                drawable.setTint(getColor(R.color.compressline));
                innerView.setBackground(drawable);
            } else {
                cor_breathcount++;// 초록
                bvmCount.setText(String.valueOf(cor_breathcount));
                cprCount.setText(String.valueOf(cor_breathcount));
                drawable.setTint(getColor(R.color.lineColor));
                innerView.setBackground(drawable);
            }
        Animation bvmAnimation = AnimationUtils.loadAnimation(this, R.anim.scale);
        innerView.startAnimation(bvmAnimation);

        //Log.e("thread t isalive", String.valueOf(t.isAlive()));
        //Log.e("thread t isInterrupted", String.valueOf(t.isInterrupted()));
        if(isthreadrun){
            isbreath = true;
        } else{
            t = new Thread(() -> {
                isthreadrun = true;
                isbreath = false;
                for(int i = 5; i > 0; i--){
                    try{
                        Log.e("Thread", "Thread is running" + i);
                        if(isbreath){
                            i = 5;
                            isbreath = false;
                        }
                        BVMTimerTv.setText(String.valueOf(i));
                        Thread.sleep(1000);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
                BVMTimerTv.setText("");
                isthreadrun =false;
            });
            t.start();
        }

        // restartThread();

        //bvmhandler.removeCallbacks(bvmtimer);
        /*runOnUiThread(() -> {
            bvmhandler.removeCallbacks(bvmtimer);
            bvmhandler.post(bvmtimer);
        });*/



    }
    Thread t;

    private void displayDepth(String data){
        Animation animation = null;
        int depthdata = Integer.parseInt(data);
        if(depthdata == 0){
            animation = new TranslateAnimation(0, 0, 0, 0);
        }
        if(0 < depthdata && depthdata < 35){
            animation = new TranslateAnimation(0, 0, 0, depth_false);
        }
        if(depthdata >= 35 && depthdata <= 60){
            animation = new TranslateAnimation(0, 0, 0, depth_true);
        }
        if(depthdata > 60){
            animation = new TranslateAnimation(0, 0, 0, depth_over);
        }
        if(depthdata >= 70){
            depthdata = 70;
        }
        int finalDepthdata = depthdata;
        animation.setDuration(350);
        animation.setFillAfter(false);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (finalDepthdata >= 35 && finalDepthdata <= 60) {
                    standardCRPBtnTel.setBackground(getDrawable(R.drawable.anne_point_green));
                } else if ((0 < finalDepthdata && finalDepthdata < 30) || (60 < finalDepthdata)) {
                    standardCRPBtnTel.setBackground(getDrawable(R.drawable.anne_point_red));
                }
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                //final Animation animation1 = new TranslateAnimation(0, 0, 0, 20);
                final Animation animation1 = new TranslateAnimation(0, 0, 0, 0);
                animation1.setDuration(200);
                animation1.setFillAfter(false);

                depthCPRViewTel.setBackgroundColor(Color.parseColor("#777777"));
                standardCRPBtnTel.setBackground(getDrawable(R.drawable.anne_point));
                depthBtnCprUp.startAnimation(animation1);
                depthCPRViewTel.startAnimation(animation1);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        if (finalDepthdata >= 35 && finalDepthdata <= 60) {
            Animation animation1 = new TranslateAnimation(0, ani01.getWidth() + 100, 0, 0);
            animation1.setDuration(550);
            animation1.setFillAfter(false);
            animation1.setInterpolator(new AccelerateDecelerateInterpolator());

            Animation animation2 = new TranslateAnimation(0, -ani02.getWidth() - 100, 0, 0);
            animation2.setDuration(550);
            animation2.setFillAfter(false);
            animation2.setInterpolator(new AccelerateDecelerateInterpolator());

            ani01.startAnimation(animation1);
            ani02.startAnimation(animation2);

            if (current_time - prev_time > 0.3f){
                cprlineData.addEntry(new Entry(current_time, -3f, "depth_cor"), 0);
                cprlineData.addEntry(new Entry(current_time + 0.15f, finalDepthdata, "depth_cor"), 0);
                cprlineData.addEntry(new Entry(current_time + 0.3f, -3f, "depth_cor"), 0);
                cprlineData.notifyDataChanged();
                // cprbandChart.notifyDataSetChanged();
                // cprbandChart.invalidate();
                prev_time = current_time;
            }
        } else{
            if (current_time - prev_time > 0.3f){
                cprlineData.addEntry(new Entry(current_time, -3f, "depth_wrong"), 1);
                cprlineData.addEntry(new Entry(current_time + 0.15f, finalDepthdata, "depth_wrong"), 1);
                cprlineData.addEntry(new Entry(current_time + 0.3f, -3f, "depth_wrong"), 1);
                cprlineData.notifyDataChanged();
                // cprbandChart.notifyDataSetChanged();
                // cprbandChart.invalidate();
                prev_time = current_time;
            }
        }

        depthCPRViewTel.startAnimation(animation);
        depthBtnCprUp.startAnimation(animation);

        new Thread(() -> runOnUiThread(() -> {
            depthText.setText(String.valueOf(finalDepthdata));
        })).start();
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BLEService.BLEServiceBinder binder = (BLEService.BLEServiceBinder) iBinder;
            mBLEService = binder.getService();
            mBLEService.setBVMDataUpdateListener(new BLEService.BVMDataUpdateListener() {
                @Override
                public void onBVMDataUpdated(String data, String label) {
                    if(label.equals("Breath")){
                        // Log.e("BVM_device", "Breath: " + data);
                        int breath = Integer.parseInt(data);
                        breathlist.add(breath);
                        if(breathlist.size() == 10){
                            BREATH_MIN = (int) breathlist.stream().mapToDouble(d -> d).average().orElse(0.0);
                            cprChart.getAxisLeft().setAxisMinimum(BREATH_MIN);
                            cprChart.notifyDataSetChanged();
                            bvmChart.getAxisLeft().setAxisMinimum(BREATH_MIN);
                            bvmChart.notifyDataSetChanged();
                        }
                        if(breath < 3500){
                            try {
                                mBLEService.disconnectBVM();
                                Toast.makeText(getApplicationContext(), "BVM is Disconnected, Check BVM Battery", Toast.LENGTH_SHORT).show();
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                            return;
                        }

                        if(breathlist.size() > 10) {
                            // BREATH_MIN = 0%, 20000 = 70%
                            // float breath_per = (breath - BREATH_MIN) / ((20000 - BREATH_MIN) / 70.0f);
                            bvmlineData.addEntry(new Entry(current_time, breath, "breath"), 0);
                            // cprlineData.addEntry(new Entry(current_time, breath_per, "breath"), 2);
                            bvmlineData.notifyDataChanged();
                            // cprlineData.notifyDataChanged();
                        }
                        //cprChart.notifyDataSetChanged();
                        //bvmChart.notifyDataSetChanged();
                        /*if(isfirst){
                            isfirst = false;
                            BREATH_MIN = breath;
                        }*/
                        /*double breath_per = (breath - BREATH_MIN)/10;
                        if(breath_per > 100){
                            breath_per = 100;
                        } else if(breath_per < 0){
                            breath_per = 0;
                        }
                        moveLungClip((int)breath_per);
                        double finalBreath_per = breath_per;
                        new Thread(() -> runOnUiThread(() -> {
                            displayBVMBreath((int) finalBreath_per);
                        })).start();*/
                    } else if(label.equals("Time")){
                        Log.e("BVM_device", "Time: " + data);
                        breathcount++;
                        int time = Integer.parseInt(data);
                        if (((3000 <= time) && ( 5000 >= time )) || ((7000 <= time) && (9000 >= time))){
                            int level = lungClip.getLevel();
                            lungClip = (ClipDrawable) getDrawable(R.drawable.lung_orange_clip);
                            testLung.setImageDrawable(lungClip);
                            lungClip.setLevel(level);
                        } else if (( time <= 3000 ) || ( time >= 9000 )){// 빨강
                            int level = lungClip.getLevel();
                            lungClip = (ClipDrawable) getDrawable(R.drawable.lung_red_clip);
                            testLung.setImageDrawable(lungClip);
                            lungClip.setLevel(level);
                        } else {
                            int level = lungClip.getLevel();
                            lungClip = (ClipDrawable) getDrawable(R.drawable.lung_normal_clip);
                            testLung.setImageDrawable(lungClip);
                            lungClip.setLevel(level);
                        }
                        moveLungClip();
                        displayBVMBreath(Integer.parseInt(data));
                        cprTotal.setText(String.valueOf(breathcount));
                        bvmTotal.setText(String.valueOf(breathcount));
                    }
                }

                @Override
                public void onBVMDataUpdated(BluetoothDevice device) {
                    BVM_device = device;
                    bvm_adapter.addBVM_Data(device);
                }

                @Override
                public void onBVMConnectionUpdated(String data, String label) {
                    if (data.equals("Connected")){
                        Log.e("BVM_device", "onServiceConnected: " + BVM_device.getName() + " " + BVM_device.getAddress());
                    } else if (data.equals("GATT_SUCCESS")) {
                        StartTime = SystemClock.uptimeMillis();
                        bvmStatus.setText(R.string.connected);
                        handler.postDelayed(timer, 0);
                        breathcount = 0;
                        cor_breathcount = 0;
                        cprTotal.setText("0");
                        bvmTotal.setText("0");
                        cprCount.setText("0");
                        bvmCount.setText("0");
                            /*delay3.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mBLEService.writeBVMCharacteristic("f3");
                                }
                            }, 1000);*/
                    }
                }
            });

            mBLEService.setCPRBANDDataUpdateListener(new BLEService.CPRBANDDataUpdateListener() {
                @Override
                public void onCPRBANDDataUpdated(String data, String label) {
                    if(label.equals("Depth")) {
                        displayDepth(data);
                    } else if(label.equals("Angle")) {
                        int angle = Integer.parseInt(data);
                        new Thread(() -> runOnUiThread(() -> {
                            if(angle <= 30)
                                angleImageView.setImageResource(R.drawable.angle_green);
                            else if(angle <= 60)
                                angleImageView.setImageResource(R.drawable.angle_orange);
                            else
                                angleImageView.setImageResource(R.drawable.angle_red);
                        })).start();
                    }
                }

                @Override
                public void onCPRBANDDataUpdated(BluetoothDevice device) {
                    CPRBAND_device = device;
                    Log.e("CPRBAND_device", "onServiceConnected: " + device.getName() + " " + device.getAddress());
                    cprband_adapter.addCPRBAND_Data(device);
                }

                @Override
                public void onCPRBANDConnectionUpdated(String data, String label) {
                    if (data.equals("Connected")){
                        Log.e("CPRBAND_device", "onServiceConnected: " + CPRBAND_device.getName() + " " + CPRBAND_device.getAddress());
                    } else if (data.equals("GATT_SUCCESS")) {
                        StartTime = SystemClock.uptimeMillis();
                        cprStatus.setText(R.string.connected);
                        handler.postDelayed(timer, 0);
                            /*delay.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mBLEService.writeCPRCharacteristic("f1");
                                }
                            }, 1000);

                            delay2.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mBLEService.writeCPRCharacteristic("f3");
                                }
                            }, 2000);*/
                    }
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBLEService = null;
        }
    };

    private long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L;
    private int Seconds, Minutes, MilliSeconds, Seconds_ = 0;
    private Handler handler, bvmhandler;

    public Runnable bvmtimer = new Runnable() {
        @Override
        public void run() {
            for(int i = 5; i >= 0; i--){
                try{
                    if(i == 0){
                        BVMTimerTv.setVisibility(View.INVISIBLE);
                    }
                    BVMTimerTv.setText(String.valueOf(i));
                    Thread.sleep(1000);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    };

    public Runnable timer = new Runnable() {
        @Override
        public void run() {
            MillisecondTime = SystemClock.uptimeMillis() - StartTime;

            current_time = MillisecondTime / 1000.0f;

            UpdateTime = TimeBuff + MillisecondTime;

            Seconds_ = (int) (UpdateTime / 1000);

            Minutes = Seconds_ / 60;

            Seconds = Seconds_ % 60;

            MilliSeconds = (int) (UpdateTime % 1000);

            /*cpr_timer.setText(String.format("%02d", Minutes) + ":"
                    + String.format("%02d", Seconds) + ":"
                    + String.format("%02d", MilliSeconds));*/

            cprTime.setText(String.format("%02d", Minutes) + ":"
                    + String.format("%02d", Seconds));
            bvmTime.setText(String.format("%02d", Minutes) + ":"
                    + String.format("%02d", Seconds));

            float moveView = current_time - 4;
            if(moveView < 1)
                moveView = 0;

            cprChart.moveViewToX(moveView);
            cprbandChart.moveViewToX(moveView);
            bvmChart.moveViewToX(moveView);

            handler.postDelayed(this, 0);
        }
    };

    private LineData createCPRLineData() {
        LineData lineData = new LineData();
        lineData.addDataSet(createSet1());
        lineData.addDataSet(createSet2());
        lineData.addDataSet(createSet3());

        return lineData;
    }

    private LineData createBVMLineData() {
        LineData lineData = new LineData();
        lineData.addDataSet(createSet1());

        return lineData;
    }

    private LimitLine createll(float val){
        LimitLine ll1 = new LimitLine(val, " ");
        ll1.setLineWidth(2f);
        ll1.enableDashedLine(10f, 0f, 0f);
        ll1.setLineColor(ContextCompat.getColor(getApplicationContext(), R.color.lineColor));

        return ll1;
    }

    private void setLineChart(){
        cprChart.getAxisRight().setEnabled(false);
        cprChart.getLegend().setEnabled(false);
        cprChart.getDescription().setEnabled(false);
        cprChart.setPinchZoom(false);
        cprChart.setScaleEnabled(false);
        cprChart.setTouchEnabled(true);
        /*cprChart.getAxisLeft().removeAllLimitLines();*/
        /*cprChart.getAxisLeft().addLimitLine(createll(35.0f));*/
        /*cprChart.getAxisLeft().addLimitLine(createll(60.0f));*/
        cprChart.setData(bvmlineData);
        XAxis cprxAxis = cprChart.getXAxis();
        cprxAxis.setEnabled(true);
        cprxAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        cprxAxis.setAxisMinimum(0f);
        cprxAxis.setAxisMaximum(3600f);
        cprxAxis.setTextColor(Color.WHITE);
        cprxAxis.setDrawGridLines(false);
        cprxAxis.setDrawLabels(false);
        YAxis cpryAxis = cprChart.getAxisLeft();
        cpryAxis.setAxisMaximum(20000f);
        cpryAxis.setAxisMinimum(15000f);
        cpryAxis.setDrawLabels(false);
        cprChart.fitScreen();
        cprChart.zoom(720f,1f, 0, 0);
        cprChart.invalidate();

        bvmChart.getAxisRight().setEnabled(false);
        bvmChart.getLegend().setEnabled(false);
        bvmChart.getDescription().setEnabled(false);
        bvmChart.setPinchZoom(false);
        bvmChart.setScaleEnabled(false);
        bvmChart.setTouchEnabled(true);
        bvmChart.setData(bvmlineData);
        XAxis bvmxAxis = bvmChart.getXAxis();
        bvmxAxis.setEnabled(true);
        bvmxAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        bvmxAxis.setAxisMinimum(0f);
        bvmxAxis.setAxisMaximum(3600f);
        bvmxAxis.setTextColor(Color.WHITE);
        bvmxAxis.setDrawGridLines(false);
        bvmxAxis.setDrawLabels(false);
        YAxis bvmyAxis = bvmChart.getAxisLeft();
        bvmyAxis.setAxisMaximum(20000f);
        bvmyAxis.setAxisMinimum(15000f);
        bvmyAxis.setDrawLabels(false);
        bvmChart.fitScreen();
        bvmChart.zoom(720f,1f, 0, 0);
        bvmChart.invalidate();

        cprbandChart.getAxisRight().setEnabled(false);
        cprbandChart.getLegend().setEnabled(false);
        cprbandChart.getDescription().setEnabled(false);
        cprbandChart.setPinchZoom(false);
        cprbandChart.setScaleEnabled(false);
        cprbandChart.setTouchEnabled(true);
        cprbandChart.getAxisLeft().removeAllLimitLines();
        cprbandChart.getAxisLeft().addLimitLine(createll(35.0f));
        cprbandChart.getAxisLeft().addLimitLine(createll(60.0f));
        cprbandChart.setData(cprlineData);
        XAxis cprbandxAxis = cprbandChart.getXAxis();
        cprbandxAxis.setEnabled(true);
        cprbandxAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        cprbandxAxis.setAxisMinimum(0f);
        cprbandxAxis.setAxisMaximum(3600f);
        cprbandxAxis.setTextColor(Color.WHITE);
        cprbandxAxis.setDrawGridLines(false);
        cprbandxAxis.setDrawLabels(false);
        YAxis cprbandyAxis = cprbandChart.getAxisLeft();
        cprbandyAxis.setAxisMaximum(70f);
        cprbandyAxis.setAxisMinimum(0f);
        cprbandyAxis.setTextColor(Color.WHITE);
        cprbandChart.fitScreen();
        cprbandChart.zoom(720f,1f, 0, 0);
        cprbandChart.invalidate();
    }

    private LineDataSet createSet1() {
        LineDataSet set = new LineDataSet(null, "depth_cor");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.rgb(255,255,255));
        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setDrawHorizontalHighlightIndicator(false);
        set.setDrawHighlightIndicators(false);
        set.setDrawValues(false);
        set.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        set.setForm(Legend.LegendForm.NONE);
        return set;
    }

    private LineDataSet createSet2(){
        LineDataSet set = new LineDataSet(null, "depth_wrong");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.rgb(255,0,0));
        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setDrawHorizontalHighlightIndicator(false);
        set.setDrawValues(false);
        set.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        set.setForm(Legend.LegendForm.NONE);
        return set;
    }

    private LineDataSet createSet3(){
        LineDataSet set = new LineDataSet(null, "breath");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.rgb(255,210,0));
        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setDrawHorizontalHighlightIndicator(false);
        set.setDrawValues(false);
        set.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        set.setForm(Legend.LegendForm.NONE);
        return set;
    }

    private int mLevel = 0;
    private int fromLevel = 0;
    private int toLevel = 0;

    private ArrayList<Integer> temp_level = new ArrayList<>();
    private Handler peaklungHandler = new Handler(Looper.getMainLooper());
    private Handler mUpHandler = new Handler(Looper.getMainLooper());
    private Handler mDownHandler = new Handler(Looper.getMainLooper());
    private Runnable animateUpImage = () -> doTheUpAnimation(fromLevel, toLevel);
    private Runnable animateDownImage = () -> doTheDownAnimation(fromLevel, toLevel);
    
    public void moveLungClip() {

        new Thread(() -> {
            for(int i = 0; i <= 20; i++) {
                try {
                    toLevel = i * 500;
                    if(i == 20){
                        toLevel = 0;
                    }
                    if (toLevel > fromLevel) {
                        // cancel previous process first
                        mDownHandler.removeCallbacks(animateDownImage);
                        fromLevel = toLevel;

                        mUpHandler.post(animateUpImage);
                    } else {
                        // cancel previous process first
                        mUpHandler.removeCallbacks(animateUpImage);
                        fromLevel = toLevel;

                        mDownHandler.post(animateDownImage);
                    }
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private void doTheUpAnimation(int fromLevel, int toLevel) {
        mLevel += LEVEL_DIFF_UP;
        lungClip.setLevel(mLevel);
        if (mLevel <= toLevel) {
            mUpHandler.postDelayed(animateUpImage, DELAY);
        } else {
            mUpHandler.removeCallbacks(animateUpImage);
            fromLevel = toLevel;
        }
    }

    private void doTheDownAnimation(int fromLevel, int toLevel) {
        mLevel -= LEVEL_DIFF_DOWN;
        lungClip.setLevel(mLevel);
        if (mLevel >= toLevel) {
            mDownHandler.postDelayed(animateDownImage, DELAY);
        } else {
            mDownHandler.removeCallbacks(animateDownImage);
            fromLevel = toLevel;
            ispeak = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    private void calculateSize(int level){
        innerView.post(() -> {
            int size = innerView.getWidth();
            x = innerView.getX();
            y = innerView.getY();
            Log.e("inner_size", String.valueOf(size));
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(0, 0);
            innerView.setLayoutParams(layoutParams);
            innerView.setX(x);
            innerView.setY(y);
            Log.e("inner_level", String.valueOf(level));
            sizebylevel = (float) size / level;
        });

    }
}

