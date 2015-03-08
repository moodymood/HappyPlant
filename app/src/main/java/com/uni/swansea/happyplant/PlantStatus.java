package com.uni.swansea.happyplant;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class PlantStatus implements Serializable {
    // Phidget sensors must be attached as following:
    public static final int TEMP = 0;
    public static final int LIGHT = 1;
    public static final int HUM = 2;

    public Map<Integer, int[]> sensorsMap;
    public int[] minReqValues;
    public int[] maxReqValues;

    public PlantStatus(){

        sensorsMap = new HashMap<>();
        initMap();

        minReqValues = new int[3];
        intMin(0,0,0);

        maxReqValues = new int[3];
        initMax(5,5,5);

    }

    public void addValue(int sensor, int value){
        int[] temp = sensorsMap.get(sensor);
        temp[getCurrentHour()]=value;
        sensorsMap.put(sensor,temp);
    }

    public int getCurrValue(int sensor){
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


    // Initialize the StatusPlant structure
    public void initMap(){
        int[] temp1 = new int[24];
        int[] temp2 = new int[24];
        int[] temp3 = new int[24];
        for(int i = 0; i<24; i++){
            temp1[i] = 0;
            temp2[i] = 10;
            temp3[i] = 20;
        }
        sensorsMap.put(TEMP,temp1);
        sensorsMap.put(LIGHT,temp2);
        sensorsMap.put(HUM,temp3);
    }

    public void intMin(int temp, int light, int hum){

        minReqValues[TEMP] = temp;
        minReqValues[LIGHT] = light;
        minReqValues[HUM] = hum;
    }

    public void initMax(int temp, int light, int hum) {
        maxReqValues[TEMP] = temp;
        maxReqValues[LIGHT] = light;
        maxReqValues[HUM] = hum;
    }

    public boolean sensorIsOK(int sensor){
        int currValue = getCurrValue(sensor);
        if(this.minReqValues[sensor] <= currValue &&  currValue <= this.maxReqValues[sensor])
            return true;
        else
            return false;
    }

    public boolean plantIsOK(){
        // If they are all OK return true;
        if(!sensorIsOK(0))
            return false;
        if(!sensorIsOK(1))
            return false;
        if(!sensorIsOK(2))
            return false;
        return true;
    }

}
