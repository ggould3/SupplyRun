package com.example.ggould.supplyrun;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ggould.supplyrun.navDrawerFragments.NavigationActivity;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText emailLogin = (EditText) findViewById(R.id.login_email);
        final EditText passLogin = (EditText) findViewById(R.id.login_pass);
        final Button loginSubmit = (Button) findViewById(R.id.login_submit);
        final TextView signupLink = (TextView) findViewById(R.id.sign_up_link);

        signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
        loginSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             if(!validEmail(emailLogin)){
                 Toast.makeText(getBaseContext(), "Invalid email address",
                         Toast.LENGTH_LONG).show();
             }else if(!validPass(passLogin)){
                 Toast.makeText(getBaseContext(), "Passwords must be at least 6 characters",
                         Toast.LENGTH_LONG).show();
             }else {
                 ParseUser currentUser = ParseUser.getCurrentUser();
                 ParseUser.logOut();
                 ParseUser.logInInBackground(emailLogin.getText().toString().trim(),
                         passLogin.getText().toString().trim(), new LogInCallback() {
                             public void done(ParseUser user, ParseException e) {
                                 if (user != null) {
                                     // Hooray! The user is logged in.
                                     Intent intent = new Intent(LoginActivity.this, NavigationActivity.class);
                                     startActivity(intent);
                                 } else {
                                     // Signup failed. Look at the ParseException to see what happened.
                                     Log.d("Login", "Login error: " + e);
                                 }
                             }
                         });
             }
            }
        });
    }

    private boolean validEmail(EditText email){
        String emailString = email.getText().toString().trim();
        return emailString.contains("@") && emailString.contains(".");
    }

    private boolean validPass(EditText pass){
        String passString = pass.getText().toString().trim();
        return passString.length()>5;
    }

    @Override
    public void onBackPressed() {
    }
}
