package edu.sc.snacktrack;

import android.app.Activity;
import android.app.Instrumentation;
import android.support.v4.widget.DrawerLayout;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.FrameLayout;
import android.widget.ListView;


import java.util.List;

/**
 * Created by dowdw on 1/26/2016.
 */
public class MainActivityITest extends ActivityInstrumentationTestCase2<MainActivity> {

    Activity ma = null;
    public MainActivityITest() {
        super(MainActivity.class);

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ma = getActivity();
    }
    @SmallTest
    public void testActivityExists(){
        assertNotNull(ma);
    }

    @SmallTest
    public void testDrawerLayout(){
        DrawerLayout dl = (DrawerLayout)ma.findViewById(R.id.drawer_layout);
        assertNotNull(dl);
    }
   @SmallTest
    public void testListView(){
        ListView lv = (ListView)ma.findViewById(R.id.drawer_list);
        assertNotNull(lv);
    }

    @SmallTest
    public void testFrameLayout(){
        FrameLayout fl = (FrameLayout)ma.findViewById(R.id.content_frame);
        assertNotNull(fl);
    }

    @SmallTest
    @UiThreadTest
    public void testSetTitle(){

        ma.setTitle("testing123");
        assertEquals("testing123", ma.getTitle());
    }
//    @SmallTest
//    public void testActionBarExists(){
//        assertNotNull(ma.getActionBar());
//    }






    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
