package com.uni.swansea.happyplant;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by michaelwaterworth on 09/03/15.
 */

public class PlantStatusData implements Serializable {

    private int _id;
    private int sensorType;
    private int sensorValue;
    private Date timeStamp;

    public PlantStatusData() {

    }

    public PlantStatusData(int id, int sType, int sValue, Date tStamp) {
        this._id = id;
        this.sensorValue = sValue;
        this.sensorType = sType;
        this.timeStamp = tStamp;
    }

    public PlantStatusData(int sType, int sValue, Date tStamp) {
        this.sensorValue = sValue;
        this.sensorType = sType;
        this.timeStamp = tStamp;
    }

    public void setID(int id) {
        this._id = id;
    }

    public int getID() {
        return this._id;
    }

    // Value
    public void setValue(int sValue) {
        this.sensorValue = sValue;
    }

    public int getValue() {
        return this.sensorValue;
    }

    //Type
    public void setType(int sType) {
        this.sensorType = sType;
    }

    public int getType() {
        return this.sensorType;
    }

    //TimeStamp
    public void setTimeStamp(Date sTimeStamp) {
        this.timeStamp = sTimeStamp;
    }

    public Date getTimeStamp() {
        return this.timeStamp;
    }

}
