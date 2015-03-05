package com.uni.swansea.happyplant;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.phidgets.*;
import com.phidgets.event.*;


import com.uni.swansea.happyplant.content.AppContent;

import org.w3c.dom.Text;

/**
 * A fragment representing a single Sensor detail screen.
 * This fragment is either contained in a {@link SensorListActivity}
 * in two-pane mode (on tablets) or a {@link SensorDetailActivity}
 * on handsets.
 */
public class SensorDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "1";

    /**
     * The dummy content this fragment is presenting.
     */
    private AppContent.Sensor mSensor;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SensorDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mSensor = AppContent.SENSOR_MAP.get(getArguments().getString(ARG_ITEM_ID));

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sensor_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mSensor != null) {
            // ((TextView) rootView.findViewById(R.id.sensor_detail)).setText(String.valueOf(mSensor.currValue));
            TextView currTemp = (TextView) container.findViewById(R.id.currValue);
            currTemp.setText(String.valueOf(mSensor.currValue));
        }

        return rootView;
    }
}
