package com.uni.swansea.happyplant;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class PlantStatus implements Serializable {
    public final int TEMP = 0;
    public final int LIGHT = 1;
    public final int HUM = 2;

    public Map<Integer, int[]> sensorsMap;
    public int[] reqValues;

    public PlantStatus(int temp, int light, int hum){

        sensorsMap = new HashMap<>();
        //sensorsMap.put(TEMP, new int[24]);
       // sensorsMap.put(LIGHT, new int[24]);
       // sensorsMap.put(HUM, new int[24]);
        randomFillAll();
        reqValues = new int[3];
        reqValues[TEMP] = temp;
        reqValues[LIGHT] = light;
        reqValues[HUM] = hum;
    }

    public void addValue(int sensor, int value){
        int[] temp = sensorsMap.get(sensor);
        temp[getCurrentHour()]=value;
        sensorsMap.put(sensor,temp);
    }

    public int getValue(int sensor){
        int[] temp = sensorsMap.get(sensor);
        return temp[getCurrentHour()];
    }

    public int getCurrentHour(){
        Date date = new Date();   // given date
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        int res = calendar.get(Calendar.HOUR_OF_DAY);
        return res;

    }

    public void randomFillAll(){
        int[] a = new int[24];
        int[] b = new int[24];
        int[] c = new int[24];
        for(int i = 0; i<24; i++){
            a[i] = i;
            b[i] = i+10;
            c[i] = i+20;
        }
        sensorsMap.put(0,a);
        sensorsMap.put(1,b);
        sensorsMap.put(2,c);
    }

}
