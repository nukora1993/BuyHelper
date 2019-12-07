package com.futurelab;

import android.accessibilityservice.AccessibilityService;
import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Random;

import static com.futurelab.Util.getCurrentTimeStr;

public class MyAccessibilityService extends AccessibilityService {
    private static final String TAG="AccessibilityServiceLog";

    private boolean isServiceStop;
    private boolean isThreadStart;
    public MyAccessibilityService(){

    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG,"onServiceConnected");


    }

    private Handler mPerformClickHandler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
//            super.handleMessage(msg);
            //获取当前活动窗口的根节点,但是该方法经常为null
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if(root!=null){
                Log.d(TAG,root.toString());
                //获取结算按钮
                List<AccessibilityNodeInfo> submitButtons = root.findAccessibilityNodeInfosByText("结算");
                Log.d(TAG, submitButtons.toString());
                //既然已经点了结算,那么只有两种结果,要么被盾,要么可以购买
                //被盾就没办法了,所以只要可以购买就一直循环点击
                if (submitButtons.size() == 1) {
                    submitButtons.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    //循环最多的等待时间
                    int patience=1000;
                    int curDuration=0;
                    long start=System.currentTimeMillis();
                    //是否已经点击
                    boolean clicked=false;
                    while(!clicked&&curDuration<=patience){
                        //重新获取root
                        root = getRootInActiveWindow();
                        if(root==null){
                            continue;
                        }
                        //获取提交订单按钮
                        List<AccessibilityNodeInfo> realSubmitButtons = root.findAccessibilityNodeInfosByText("提交订单");
                        Log.d(TAG, realSubmitButtons.toString());
                        if (realSubmitButtons.size() == 1) {
                            realSubmitButtons.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            clicked=true;
                        }
                        long end=System.currentTimeMillis();
                        curDuration=(int)(end-start);
                        Log.d(TAG,"duration:"+curDuration);
                    }
                }else{
                    return;
                }


            }

//
//
//            Log.d(TAG,windowInfos.size()+"");
//            if(windowInfos.size()!=0){
//                for(AccessibilityWindowInfo windowInfo:windowInfos) {
//                    AccessibilityNodeInfo root = windowInfo.getRoot();
//
//                    Log.d(TAG, root.toString());
//                    //获取结算按钮
//                    List<AccessibilityNodeInfo> submitButtons = root.findAccessibilityNodeInfosByText("结算");
//                    Log.d(TAG, submitButtons.toString());
//                    if (submitButtons.size() == 1) {
//                        submitButtons.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
//
//                    }
//
//                }
//            }


        }
    };

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
//        Log.d(TAG,"onAccessibilityEvent"+event);
        //实际测试发现，只有第一次调用的时候能够得到文字
        for(CharSequence text:event.getText()){
            String content=text.toString();
            Log.d(TAG,content);
        }

        //只有还没有启动的时候才能启动线程
        if(!isThreadStart){
            Log.d(TAG,"now start thread");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        if (isServiceStop) {
                            isThreadStart=false;
                            break;
                        }
                        String curTime = getCurrentTimeStr();
                        //在指定的时间触发
                        if (curTime.equals("9:59:59") || curTime.equals("21:59:59")) {
                            Log.d(TAG,"curTime:"+curTime);
//                            Log.d(TAG,"time end with 5");
                            try{
                                int randomSleep=new Random().nextInt(400)+50;
                                Thread.sleep(randomSleep);
                            }catch(InterruptedException e){
                                e.printStackTrace();
                            }
                            Message message=Message.obtain();
                            mPerformClickHandler.sendMessage(message);
                            try{
                                Thread.sleep(1000);
                            }catch(InterruptedException e){
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }).start();

            isThreadStart=true;
        }

    }

    @Override
    public void onInterrupt() {
        Log.d(TAG,"onInterrupt");
    }


}
