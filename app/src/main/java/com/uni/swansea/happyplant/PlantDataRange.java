package com.uni.swansea.happyplant;

import java.util.Date;

/**
 * Created by moody on 12/03/15.
 */
public class PlantDataRange {
    private int _id;
    private int sensorType;
    private int minValue;
    private int maxValue;

    public PlantDataRange() {

    }

    public PlantDataRange(int id, int sensorType, int minValue, int maxValue) {
        this._id = id;
        this.sensorType = sensorType;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }


    public void setID(int id) {
        this._id = id;
    }

    public int getID() {
        return this._id;
    }

    // minValue
    public void setMinValue(int minValue) {
        this.minValue = minValue;
    }

    public int getMinValue() {
        return this.minValue;
    }

    // maxValue
    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    public int getMaxValue() {
        return this.maxValue;
    }


    //Type
    public void setType(int sType) {
        this.sensorType = sType;
    }

    public int getType() {
        return this.sensorType;
    }

}
