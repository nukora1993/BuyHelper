package com.futurelab;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.futurelab.Util.getCurrentTimeStr;

//为了使得悬浮窗即使关闭了Activity也能更新，使用Service操作悬浮窗
public class FloatingLayoutService extends Service {
    private static final String TAG="FloatingServiceLog";
    private WindowManager mWindowManager;
    private LayoutInflater mLayoutInflater;
    private View mFloatingLayout;
    private boolean stopService;

    public FloatingLayoutService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //获得实例
        mWindowManager=(WindowManager)getSystemService(Context.WINDOW_SERVICE);
        mLayoutInflater=LayoutInflater.from(this);
        mFloatingLayout=mLayoutInflater.inflate(R.layout.floating_layout,null);

        ((Button)mFloatingLayout.findViewById(R.id.button_exit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(FloatingLayoutService.this,"You clicked button",Toast.LENGTH_SHORT).show();
//                stopService=true;
//                stopSelf();
                //干掉application,一了百了
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });

        ((Button)mFloatingLayout.findViewById(R.id.button_allow_access_permission)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent accessibilityIntent=new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                accessibilityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(accessibilityIntent);
            }
        });

        //电脑上使用adb shell命令时可以的，但是不知道为什么再程序中就不行，可能需要root权限？
        ((Button)mFloatingLayout.findViewById(R.id.button_sim_click)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String[] testCmd=new String[]{"df","-h"};
//                String[] tapCmd=new String[]{"input","tap","100","200"};
//                ProcessBuilder pb=new ProcessBuilder(tapCmd);
//
//                try{
//                    Process p=pb.start();
//                    BufferedReader reader=new BufferedReader(new InputStreamReader(
//                            p.getInputStream()
//                    ));
//                    StringBuilder sb=new StringBuilder();
//                    String line;
//                    while((line=reader.readLine())!=null){
//                        sb.append(line+"\n");
//                    }
//
//                    Log.d(TAG,"execute cmd,result:"+sb);
//                }catch (IOException e){
//                    e.printStackTrace();
//                }catch(RuntimeException e){
//                    e.printStackTrace();
//                }
            }
        });

    }

    private Handler mUpdateFloatingLayoutHandler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            String curTime=(String)msg.obj;
            mFloatingLayout.findViewById(R.id.text_view_cur_time);
            TextView textViewCurTime=mFloatingLayout.findViewById(R.id.text_view_cur_time);
            textViewCurTime.setText(curTime);

        }
    };



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand");
        try{
            //配置浮动窗口参数
            WindowManager.LayoutParams lp=new WindowManager.LayoutParams();
            lp.width= WindowManager.LayoutParams.WRAP_CONTENT;
            lp.height=WindowManager.LayoutParams.WRAP_CONTENT;
            //浮动窗口的位置
            lp.gravity= Gravity.LEFT|Gravity.TOP;
            lp.format= PixelFormat.TRANSPARENT;
            //系统窗口，需要权限
            lp.type=WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            //弹出窗口时保持屏幕常亮
            //允许该窗口后的窗口接受事件，否则其他窗口无法收到事件
            //不允许该窗口获取焦点，不需要接受输入(这个不太理解，但是如果不设置会导致其他程序的back无效)
            lp.flags= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

//            lp.flags= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
//                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            //浮动窗口位置
            lp.x=500;
            lp.y=500;
            mWindowManager.addView(mFloatingLayout,lp);
        }catch (RuntimeException e){
            //有报错，说明权限问题
            //打开系统设置
            Intent settingIntent=new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);

            settingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(settingIntent);
            e.printStackTrace();
        }

        //开启线程更新时间
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(!stopService){
                    String curTimeStr=getCurrentTimeStr();
                    Message updateTimeMessage=Message.obtain();
                    updateTimeMessage.obj=curTimeStr;
                    try{
                        //让thread睡一会，不需要太快
                        Thread.sleep(100);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }

                    mUpdateFloatingLayoutHandler.sendMessage(updateTimeMessage);
                }

            }
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        //关闭悬浮窗
        mWindowManager.removeView(mFloatingLayout);
    }
}
