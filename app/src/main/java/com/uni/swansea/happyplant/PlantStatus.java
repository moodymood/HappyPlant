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

    public PlantStatusData plantStatusData;
    public PlantDataRange plantStatusRange;
    public int currValue;

    public PlantStatus(PlantStatusData plantStatusData, PlantDataRange plantDataRange) {
        this.plantStatusData=plantStatusData;
        this.plantStatusRange=plantStatusRange;
    }

    public int getCurrValue() {
        return currValue;
    }

    public void setCurrValue(int currValue) {
        this.currValue = currValue;
    }

    public PlantStatusData getPlantStatusData() {
        return plantStatusData;
    }

    public void setPlantStatusData(PlantStatusData plantStatusData) {
        this.plantStatusData = plantStatusData;
    }

    public PlantDataRange getPlantStatusRange() {
        return plantStatusRange;
    }

    public void setPlantStatusRange(PlantDataRange plantStatusRange) {
        this.plantStatusRange = plantStatusRange;
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



    public boolean sensorIsOK(){
        if(this.plantStatusData.getValue() <= plantStatusRange.getMaxValue()
                &&  plantStatusRange.getMinValue() <= this.plantStatusData.getValue())
            return true;
        else
            return false;
    }



}
