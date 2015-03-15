package com.uni.swansea.happyplant;

import android.test.ActivityInstrumentationTestCase2;

import java.util.Date;
import java.util.List;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */

public class ApplicationTest
        extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity mFirstTestActivity;
    PlantDatabaseHandler dHandler;

    public ApplicationTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        Date oldDate;
        super.setUp();
        mFirstTestActivity = getActivity();

        //Get Db handler
        dHandler = PlantDatabaseHandler.getHelper(mFirstTestActivity.getApplicationContext());
        //dHandler = new PlantDatabaseHandler(mFirstTestActivity.getApplicationContext(), null, null, 1);

        //Wipe the current data
        dHandler.clearData();

        //Create date 2 and a half hours ago.
        oldDate = new Date(System.currentTimeMillis() - 9000 * 1000);
        dHandler.addStatusData(new PlantStatusData(0, 200, oldDate));
        dHandler.addStatusData(new PlantStatusData(0, 200, oldDate));
        dHandler.addStatusData(new PlantStatusData(0, 200, oldDate));
        dHandler.addStatusData(new PlantStatusData(0, 200, oldDate));

        //Add sample data
        dHandler.addStatusData(new PlantStatusData(1, 100, new Date()));
        dHandler.addStatusData(new PlantStatusData(1, 50, new Date()));
        dHandler.addStatusData(new PlantStatusData(1, 50, new Date()));
        dHandler.addStatusData(new PlantStatusData(1, 50, new Date()));
        dHandler.addStatusData(new PlantStatusData(1, 50, new Date()));

        //Create date an hour and a half ago.
        oldDate = new Date(System.currentTimeMillis() - 5400 * 1000);
        dHandler.addStatusData(new PlantStatusData(2, 100, oldDate));
        dHandler.addStatusData(new PlantStatusData(2, 100, oldDate));
        dHandler.addStatusData(new PlantStatusData(2, 100, oldDate));
        dHandler.addStatusData(new PlantStatusData(2, 100, oldDate));

        //Create date 2 and a half hours ago.
        oldDate = new Date(System.currentTimeMillis() - 9000 * 1000);
        dHandler.addStatusData(new PlantStatusData(2, 200, oldDate));
        dHandler.addStatusData(new PlantStatusData(2, 200, oldDate));
        dHandler.addStatusData(new PlantStatusData(2, 200, oldDate));
        dHandler.addStatusData(new PlantStatusData(2, 200, oldDate));

        dHandler.addRangeValues(new PlantDataRange(0,0,1,10));
        dHandler.addRangeValues(new PlantDataRange(1,1,10,20));
        dHandler.addRangeValues(new PlantDataRange(2,2,20,30));
    }


    @Override
    protected void tearDown() throws Exception {
        //Wipe the current data
        dHandler.clearData();
        super.tearDown();
    }

    public void testDatabaseGetAggregate() {
        List<PlantStatusData> statusData = dHandler.findByTypeGroupedHour(2);
        assertTrue(statusData.size() == 3);
        assertTrue(statusData.get(0).getValue() == 200);
        assertTrue(statusData.get(1).getValue() == 100);

        //System.out.println("Aggregate value 0: " + statusData.get(0).getValue());
    }

    public void testDatabaseGetType() {
        List<PlantStatusData> statusData = dHandler.findByType(2);
        assertTrue(statusData.size() == 12);

//        for (PlantStatusData c : statusData) {
//            //System.out.println(c.getTimeStamp());
//        }
        //System.out.println("Get by type length " + statusDatas.size());
    }
}