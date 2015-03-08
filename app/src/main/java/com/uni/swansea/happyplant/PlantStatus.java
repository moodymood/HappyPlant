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
    public String[] unit;
    public String[] labels;

    public PlantStatus(){

        sensorsMap = new HashMap<>();
        initMap();

        minReqValues = new int[3];
        intMin(0,0,0);

        maxReqValues = new int[3];
        initMax(5,5,5);

        unit = new String[3];
        initUnit("C°", "Lux", "%");

        labels = new String[3];
        initLabels("Temperature", "Light", "Humidity");

    }

    public void addValue(int sensor, int value){
        // Convert the value before storing
        int convertedValue = convertValue(sensor, value);
        int[] values = sensorsMap.get(sensor);
        values[getCurrentHour()]= convertedValue;
        sensorsMap.put(sensor,values);
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

    // Convert the value according to the sensor
    public int convertValue(int sensor, int value){
        // Temperature (C°) = (Sensor value * 0.2222) - 61.111
        if(sensor == TEMP){
            return (int) Math.round((value * 0.2222) - 61.111);
        }
        // Light (Lux) = (m * Sensor value) + b
        else if(sensor == LIGHT){
            return (int) Math.round((1.478777 * value) +  33.67076);
        }
        // Humidity (RH%) = (Sensor value * 0.1906) - 40.2
        else{
            return (int) Math.round((value * 0.1906) - 40.2);
        }
    }


    // Initialize the StatusPlant structure
    public void initMap(){
        int[] temp1 = new int[24];
        int[] temp2 = new int[24];
        int[] temp3 = new int[24];
        for(int i = 0; i<24; i++){
            temp1[i] = (int) (Math.random() * 30);
            temp2[i] = (int) (Math.random() * 20);
            temp3[i] = (int) (Math.random() * 100);
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

    public void initUnit(String temp, String light, String hum) {
        unit[TEMP] = temp;
        unit[LIGHT] = light;
        unit[HUM] = hum;
    }

    public void initLabels(String temp, String light, String hum) {
        labels[TEMP] = temp;
        labels[LIGHT] = light;
        labels[HUM] = hum;
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
