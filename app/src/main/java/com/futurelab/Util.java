package com.futurelab;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {
    public static String getCurrentTimeStr(){
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("HH:mm:ss");
        //系统当前时间
        Date date=new Date(System.currentTimeMillis());

        return simpleDateFormat.format(date);
    }
}
