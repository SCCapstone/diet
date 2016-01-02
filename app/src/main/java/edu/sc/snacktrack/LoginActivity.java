package edu.sc.snacktrack;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class LoginActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(savedInstanceState == null){
            newAccountMode();
        } else{
            // The activity was recreated from a saved state.
        }
    }

    public void newAccountMode(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        NewAccountFragment newAccountFragment = new NewAccountFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, newAccountFragment)
                .commit();

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle("New Account");
        }
    }

    public void existingAccountMode(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        LoginExistingFragment loginExistingFragment = new LoginExistingFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, loginExistingFragment)
                .commit();

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle("Log in");
        }
    }

    @Override
    public void onBackPressed() {
        // The default behavior here is to go back to the previous activity.
        // We override this behavior as the user must be logged in to continue.
    }
}
