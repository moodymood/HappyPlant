package com.uni.swansea.happyplant.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.util.Log;


/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class AppContent {

    /**
     * An array of sample (dummy) items.
     */
    public static List<Sensor> SENSORS = new ArrayList<Sensor>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<String, Sensor> SENSOR_MAP = new HashMap<String, Sensor>();

    static {
        // Add 3 sample items.
        addSensor(new Sensor("1", 20));
        addSensor(new Sensor("0", 10));

        addSensor(new Sensor("2", 30));
    }

    private static void addSensor(Sensor sensor) {
        SENSORS.add(sensor);
        Log.i("AppContent", "sensor.id: "+ sensor.id);
        SENSOR_MAP.put(sensor.id, sensor);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class Sensor {
        public String id;
        public int currValue;

        public Sensor(String id, int value) {
            this.id = id;
            this.currValue = value;
        }

        @Override
        public String toString() {
            if(id.equals("0"))
                return "Temperature";
            if(id.equals("1"))
                return "Humidity";
            if(id.equals("2"))
                return "Light";
            else
                return "Unknown";

        }
    }
}
