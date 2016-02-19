package com.example.ggould.supplyrun;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CreateNewActivity extends AppCompatActivity {

    List<EditText> allEds = new ArrayList<>();
    List<TextView> allTimes = new ArrayList<>();
    List<TextView> allRems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new);

        final EditText primLocal = (EditText) findViewById(R.id.primary_location);
        final Button addField = (Button) findViewById(R.id.add_new_field);
        final Button addFriends = (Button) findViewById(R.id.add_friends);
        final LinearLayout expandLayout = (LinearLayout) findViewById(R.id.expandable_fields);
        final LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT, 1);

        //First view has neither a time nor remove option
        allTimes.add(null);
        allRems.add(null);
        allEds.add(primLocal);

        addField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflate = getLayoutInflater();
                LinearLayout til = (LinearLayout) inflate.inflate(R.layout.dynamic_edit_field,
                        (ViewGroup) v.getParent(), false);
                til.setLayoutParams(layoutParams);
                allEds.add((EditText) til.findViewById(R.id.new_location));
                allTimes.add((TextView) til.findViewById(R.id.add_time));
                allRems.add((TextView) til.findViewById(R.id.remove));

                expandLayout.addView(til);
            }
        });
        addFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Primary Location must be set to non-whitespace characters
                if(primLocal.getText().toString().trim().equals("")){
                    Toast.makeText(CreateNewActivity.this, "You must set the primary location",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(CreateNewActivity.this, SelectFriendsActivity.class);
                intent.putStringArrayListExtra("stop locations",getLocations());
                intent.putStringArrayListExtra("stop times", getTimes());
                startActivity(intent);
            }
        });
    }

    public void showTimeDialog(View v){
        final TextView tv = (TextView) v;

        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(CreateNewActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                tv.setText(formatTime(selectedHour, selectedMinute));
            }
        }, hour, minute, false);//Yes 24 hour time
        mTimePicker.show();
    }

    public void removeCard(View v){
        //The remove button is nested inside 3 linear layouts
        //Selects Layout in activity and removes dynamically added view
        ((ViewManager)v.getParent().getParent().getParent()).removeView((View)v.getParent().getParent());
        //Adjust lists of views to reflect removal of this view
        int index = allRems.indexOf(v);
        allRems.remove(index);
        allEds.remove(index);
        allTimes.remove(index);
    }

    private String formatTime(int selectedHour, int selectedMinute){
        String timeAmOrPm = "am";
        if(selectedHour > 12){
            timeAmOrPm = "pm";
            selectedHour -= 12;
        }else if(selectedHour == 12){
            timeAmOrPm = "pm";
        }else if(selectedHour == 0){
            selectedHour = 12;
        }
        String twoDigitMinute = String.format("%02d", selectedMinute);
        return selectedHour + ":" + twoDigitMinute + timeAmOrPm;
    }

    public ArrayList<String> getLocations(){
        ArrayList<String> locations = new ArrayList<>();

        locations.add(allEds.get(0).getText().toString());
        for(int i=1; i<allEds.size(); i++){
            locations.add(allEds.get(i).getText().toString());
        }
        return locations;
    }

    public ArrayList<String> getTimes(){
        ArrayList<String> times = new ArrayList<>();

        times.add("first item, no time set");
        for(int i=1; i<allEds.size(); i++){
            times.add(allTimes.get(i).getText().toString());
        }
        return times;
    }

}
