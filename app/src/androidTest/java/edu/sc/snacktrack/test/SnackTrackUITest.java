package edu.sc.snacktrack.test;

import edu.sc.snacktrack.SplashScreenActivity;
import edu.sc.snacktrack.login.LoginActivity;
import edu.sc.snacktrack.main.MainActivity;
import edu.sc.snacktrack.main.new_entry.EditDescriptionActivity;
import edu.sc.snacktrack.main.new_entry.NewEntryActivity;

import com.robotium.solo.*;
import android.test.ActivityInstrumentationTestCase2;


public class SnackTrackUITest extends ActivityInstrumentationTestCase2<SplashScreenActivity> {
  	private Solo solo;
  	
  	public SnackTrackUITest() {
		super(SplashScreenActivity.class);
  	}

  	public void setUp() throws Exception {
        super.setUp();
		solo = new Solo(getInstrumentation());
		getActivity();
  	}
  
   	@Override
   	public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
  	}
  
	public void testRun() {
        //Wait for activity: 'edu.sc.snacktrack.SplashScreenActivity'
		solo.waitForActivity(SplashScreenActivity.class, 2000);
        //Sleep for 4870 milliseconds
		solo.sleep(870);
        //Click on Or log in to existing account
		solo.clickOnView(solo.getView("existingAccountButton"));
        //Sleep for 841 milliseconds
		solo.sleep(841);
        //Click on Empty Text View
		solo.clickOnView(solo.getView("usernameEditText"));
        //Sleep for 5828 milliseconds
		solo.sleep(828);
        //Enter the text: 'willtest'
		solo.clearEditText((android.widget.EditText) solo.getView("usernameEditText"));
		solo.enterText((android.widget.EditText) solo.getView("usernameEditText"), "willtest");
        //Click on Empty Text View
		solo.clickOnView(solo.getView("passwordEditText"));
        //Sleep for 3425 milliseconds
		solo.sleep(425);
        //Enter the text: 'passwor'
		solo.clearEditText((android.widget.EditText) solo.getView("passwordEditText"));
		solo.enterText((android.widget.EditText) solo.getView("passwordEditText"), "passwor");
        //Press next button
		solo.pressSoftKeyboardNextButton();
        //Sleep for 1466 milliseconds
		solo.sleep(466);
        //Click on Log in
		solo.clickOnView(solo.getView("signInButton"));
        //Sleep for 805 milliseconds
		solo.sleep(805);
        //Click on passwor
		solo.clickOnView(solo.getView("passwordEditText"));
        //Sleep for 1114 milliseconds
		solo.sleep(1114);
        //Enter the text: 'password'
		solo.clearEditText((android.widget.EditText) solo.getView("passwordEditText"));
		solo.enterText((android.widget.EditText) solo.getView("passwordEditText"), "password");
        //Press next button
		solo.pressSoftKeyboardNextButton();
        //Sleep for 1117 milliseconds
		solo.sleep(1117);
        //Click on Log in
		solo.clickOnView(solo.getView("signInButton"));
        //Wait for activity: 'edu.sc.snacktrack.main.MainActivity'
		assertTrue("edu.sc.snacktrack.main.MainActivity is not found!", solo.waitForActivity(MainActivity.class));
        //Sleep for 2626 milliseconds
		solo.sleep(626);
        //Click on ImageView
		solo.clickOnView(solo.getView(android.widget.ImageButton.class, 0));
        //Sleep for 862 milliseconds
		solo.sleep(862);
        //Click on Test 2
		solo.clickOnText(java.util.regex.Pattern.quote("Test 2"));
        //Sleep for 1425 milliseconds
		solo.sleep(425);
        //Click on ImageView
		solo.clickOnView(solo.getView(android.widget.ImageButton.class, 0));
        //Sleep for 845 milliseconds
		solo.sleep(845);
        //Click on My Snacks
		solo.clickOnText(java.util.regex.Pattern.quote("My Snacks"));
        //Sleep for 1938 milliseconds
		solo.sleep(938);
        //Click on Empty Text View
		solo.clickOnView(solo.getView("action_new"));
        //Wait for activity: 'edu.sc.snacktrack.main.new_entry.NewEntryActivity'
		assertTrue("edu.sc.snacktrack.main.new_entry.NewEntryActivity is not found!", solo.waitForActivity(NewEntryActivity.class));
        //Sleep for 1319 milliseconds
		solo.sleep(1319);
        //Click on Select an option
		solo.clickOnView(solo.getView("meal_type_spinner"));
        //Sleep for 2146 milliseconds
		solo.sleep(146);
        //Click on Breakfast
		solo.clickOnView(solo.getView("text1", 3));
        //Sleep for 770 milliseconds
		solo.sleep(770);
        //Click on Select an option
		solo.clickOnView(solo.getView("meal_location_spinner"));
        //Sleep for 1910 milliseconds
		solo.sleep(910);
        //Click on Friend's house
		solo.clickOnView(solo.getView("text1", 4));
        //Sleep for 1960 milliseconds
		solo.sleep(960);
        //Click on Empty Text View
		solo.clickOnView(solo.getView("descriptionTextView"));
        //Wait for activity: 'edu.sc.snacktrack.main.new_entry.EditDescriptionActivity'
		assertTrue("edu.sc.snacktrack.main.new_entry.EditDescriptionActivity is not found!", solo.waitForActivity(EditDescriptionActivity.class));
        //Sleep for 9496 milliseconds
		solo.sleep(496);
        //Enter the text: 'Living room art'
		solo.clearEditText((android.widget.EditText) solo.getView("editText"));
		solo.enterText((android.widget.EditText) solo.getView("editText"), "Living room art");
        //Sleep for 694 milliseconds
		solo.sleep(694);
        //Click on Done
		solo.clickOnView(solo.getView("action_done"));
        //Sleep for 2294 milliseconds
		solo.sleep(294);
        //Click on Done
		solo.clickOnView(solo.getView("action_done"));
        //Sleep for 7058 milliseconds
		solo.sleep(588);
        //Press menu back key
		solo.goBack();
        //Sleep for 813 milliseconds
		solo.sleep(813);
        //Press menu back key
		solo.goBack();
        //Sleep for 4247 milliseconds
		solo.sleep(447);
        //Scroll to Wed Feb 03
		android.widget.ListView listView0 = (android.widget.ListView) solo.getView(android.widget.ListView.class, 0);
		solo.scrollListToLine(listView0, 0);
        //Click on Wed Feb 03
		solo.clickInList(1, 0);
        //Sleep for 5256 milliseconds
		solo.sleep(556);
        //Click on ImageView
		solo.clickOnView(solo.getView(android.widget.ImageButton.class, 0));
        //Sleep for 941 milliseconds
		solo.sleep(941);
        //Click on My Snacks
		solo.clickOnText(java.util.regex.Pattern.quote("My Snacks"));
        //Sleep for 4899 milliseconds
		solo.sleep(499);
        //Click on Empty Text View
		solo.clickOnView(solo.getView("action_new"));
        //Wait for activity: 'edu.sc.snacktrack.main.new_entry.NewEntryActivity'
		assertTrue("edu.sc.snacktrack.main.new_entry.NewEntryActivity is not found!", solo.waitForActivity(NewEntryActivity.class));
        //Sleep for 2780 milliseconds
		solo.sleep(280);
        //Click on Done
		solo.clickOnView(solo.getView("action_done"));
        //Sleep for 7040 milliseconds
		solo.sleep(740);
        //Click on ImageView
		solo.clickOnView(solo.getView(android.widget.ImageView.class, 3));
        //Sleep for 1213 milliseconds
		solo.sleep(1213);
        //Click on Log out
		solo.clickInList(1, 0);
        //Wait for activity: 'edu.sc.snacktrack.login.LoginActivity'
		assertTrue("edu.sc.snacktrack.login.LoginActivity is not found!", solo.waitForActivity(LoginActivity.class));
	}
}
