package com.uni.swansea.happyplant;
import android.util.Log;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class PlantCurrentStatus implements Serializable {

    PlantStatusData[] generalPlantStatusData;
    PlantDataRange[] generalPlantDataRange;

    public boolean sensorIsOK(int sensor){
        if(generalPlantStatusData[sensor].getValue() < generalPlantDataRange[sensor].getMaxValue()
            && generalPlantStatusData[sensor].getValue() > generalPlantDataRange[sensor].getMinValue())
            return true;
        else
            return false;
    }

    public int sensorIsInBounds(int sensor){
        if(generalPlantStatusData[sensor].getValue() > generalPlantDataRange[sensor].getMaxValue()) {
            return 1;
        }
        if(generalPlantStatusData[sensor].getValue() < generalPlantDataRange[sensor].getMinValue()) {
            return -1;
        }
        return 0;
    }


    public boolean plantIsOK(){
        if(!sensorIsOK(PhidgeMetaInfo.TEMP))
            return false;
        else if(!sensorIsOK(PhidgeMetaInfo.LIGHT))
            return false;
        else if(!sensorIsOK(PhidgeMetaInfo.HUM))
            return false;
        else
            return true;
    }


    public String toString(){
        String res = "";
        if(generalPlantStatusData!= null & generalPlantDataRange!= null) {
            for (int i = 0; i < 3; i++) {
                res +=
                        "\nSensor: " + generalPlantStatusData[i].getType() +
                                " value: " + generalPlantStatusData[i].getValue() +
                                " min: " + generalPlantDataRange[i].getMinValue() +
                                " max: " + generalPlantDataRange[i].getMaxValue() +
                                "\n";
            }
        }
        return res;
    }

    // Getter and Setter
    public PlantCurrentStatus(PlantStatusData[] generalPlantStatusData, PlantDataRange[] generalPlantDataRange){

        this.generalPlantStatusData = generalPlantStatusData;
        this.generalPlantDataRange = generalPlantDataRange;
    }

    public PlantStatusData[] getGeneralPlantStatusData(){
        return generalPlantStatusData;
    }

    public PlantStatusData getGeneralPlantStatusData(int sensor){
        return generalPlantStatusData[sensor];
    }

    public PlantDataRange[] getGeneralPlantDataRange(){
        return generalPlantDataRange;
    }

    public PlantDataRange getGeneralPlantDataRange(int sensor){
        return generalPlantDataRange[sensor];
    }

    public void setGeneralPlantStatusData(PlantStatusData[] generalPlantStatusData){
        this.generalPlantStatusData = generalPlantStatusData;
    }
}
