package edu.sc.snacktrack.login;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.parse.ParseUser;

import edu.sc.snacktrack.R;

public class LoginActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(ParseUser.getCurrentUser() == null){
            if(savedInstanceState == null){
                newAccountMode();
            } else{
                // The activity was recreated from a saved state.
            }
        } else{
            if(savedInstanceState == null){
                existingAccountMode();
            } else{
                // The activity was recreated from a saved state.
            }
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
}
