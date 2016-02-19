package com.example.ggould.supplyrun;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ggould.supplyrun.navDrawerFragments.NavigationActivity;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class SelectFriendsActivity extends AppCompatActivity {

    List<TextView> allTV = new ArrayList<>();
    List<CheckBox> allCB = new ArrayList<>();
    ArrayList<String> friends = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friends);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Populating locations gathered from intent
        final ArrayList<String> stopLocations = getIntent().getStringArrayListExtra("stop locations");
        final ArrayList<String> stopTimes = getIntent().getStringArrayListExtra("stop times");
        final TextView ls = (TextView) findViewById(R.id.locations_set);
        String localSum = "Error: failed to find stops";
        if(stopLocations.size() == 1){
            localSum = "Selected Stop: "+ stopLocations.get(0);
        }else if(stopLocations.size() == 2){
            localSum = "Selected Stops: "+ stopLocations.get(0)+" and 1 other location";
        }else{
            localSum = "Selected Stops: "+ stopLocations.get(0)+" and "+(stopLocations.size()-1)+" other locations";
        }
        ls.setText(localSum);


        //Gathering confirmed Friends from database
        ParseQuery<ParseObject> query = ParseQuery.getQuery("friendships");
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.whereEqualTo("userconf", true);
        query.whereEqualTo("friendconf", true);
        query.orderByAscending("friendname");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> friendsList, ParseException e) {
                if (e == null) {
                    fillFriendsList(friendsList);
                    Log.d("friends", "Retrieved " + friendsList.size() + " records");
                } else {
                    Log.d("friends", "Error: " + e.getMessage());
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseObject newRun = new ParseObject("runs");
                newRun.put("active", true);
                newRun.put("creator", ParseUser.getCurrentUser().getUsername());
                newRun.addAll("friends", getRecipients());
                newRun.addAll("places", stopLocations);
                newRun.addAll("times", stopTimes);
                newRun.saveInBackground(new SaveCallback() {
                    public void done(ParseException e) {
                        if (e == null) {
                            // Saved successfully.
                            Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SelectFriendsActivity.this, NavigationActivity.class);
                            startActivity(intent);
                        } else {
                            // The save failed.
                            Toast.makeText(getApplicationContext(), "Failed to Save", Toast.LENGTH_SHORT).show();
                            Log.d("runs", "Failed to save "+e);
                        }
                    }
                });

                Log.d("runs", "Submitted run with " + getRecipients().size() + " 2 recipients");
            }
        });

    }

    public void selectItem(View v){
        CheckBox cb = (CheckBox) v.findViewById(R.id.check);
        cb.toggle();
    }

    public ArrayList<String> getRecipients(){
        ArrayList<String> recipients = new ArrayList<>();
        for(int i=0; i<allTV.size(); i++){
            if(allCB.get(i).isChecked()){
                recipients.add(allTV.get(i).getText().toString());
            }
        }
        return recipients;
    }

    public void checkAll(View v){
        for(CheckBox cb: allCB){
            cb.setChecked(true);
        }
    }

    public void uncheckAll(View v){
        for(CheckBox cb: allCB){
            cb.setChecked(false);
        }
    }

    public void fillFriendsList(List<ParseObject> query){
        for(int i=0; i<query.size(); i++){
            friends.add((String) query.get(i).get("friendname"));
        }

        final LinearLayout ll = (LinearLayout) findViewById(R.id.friends_list);
        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);

        for(int i=0; i<friends.size(); i++){
            LayoutInflater inflate = getLayoutInflater();
            LinearLayout fli = (LinearLayout) inflate.inflate(R.layout.friends_list_item,
                    viewGroup, false);
            TextView tv = (TextView) fli.findViewById(R.id.name_holder);
            tv.setText(friends.get(i));
            CheckBox cb = (CheckBox) fli.findViewById(R.id.check);
            allTV.add(tv);
            allCB.add(cb);

            ll.addView(fli);
        }
    }

}
