package com.example.ggould.supplyrun;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ggould.supplyrun.navDrawerFragments.NavigationActivity;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Button subButton = (Button) findViewById(R.id.submit);
        subButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                EditText nameField = (EditText) findViewById(R.id.user_name);
                String name = nameField.getText().toString().trim();
                EditText lastField = (EditText) findViewById(R.id.user_last_name);
                String last = lastField.getText().toString().trim();
                EditText emailField = (EditText) findViewById(R.id.user_email);
                String email = emailField.getText().toString().trim();
                EditText passField = (EditText) findViewById(R.id.user_password);
                String pass = passField.getText().toString().trim();
                EditText confField = (EditText) findViewById(R.id.confirm_password);
                String conf = passField.getText().toString().trim();

                if(!validEmail(emailField)){
                    Toast.makeText(getBaseContext(), "Invalid email address",
                            Toast.LENGTH_LONG).show();
                    return;
                }else if(!validPass(passField, confField)){
                    Toast.makeText(getBaseContext(), "Ensure password fields match and contain at least 6 characters",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                ParseUser currentUser = ParseUser.getCurrentUser();
                currentUser.logOut();

                ParseUser user = new ParseUser();
                user.setUsername(email);
                user.setPassword(pass);
                user.setEmail(email);

                // other fields can be set just like with ParseObject
                user.put("first_name", name);
                user.put("last_name", last);

                user.signUpInBackground(new SignUpCallback() {
                    public void done(ParseException e) {
                        if (e == null) {
                            // Hooray! Let them use the app now.
                            Intent intent = new Intent(SignUpActivity.this, NavigationActivity.class);
                            startActivity(intent);
                        } else {
                            // Sign up didn't succeed. Look at the ParseException
                            // to figure out what went wrong
                            Log.d("SignUp", "Sign Up error: "+e);
                        }
                    }
                });

            }
        });
    }

    private boolean validEmail(EditText email){
        String emailString = email.getText().toString().trim();
        return emailString.contains("@") && emailString.contains(".");
    }

    private boolean validPass(EditText pass, EditText conf){
        String passString = pass.getText().toString().trim();
        String confString = conf.getText().toString().trim();
        return passString.length()>5 && passString.equals(confString);
    }
}
