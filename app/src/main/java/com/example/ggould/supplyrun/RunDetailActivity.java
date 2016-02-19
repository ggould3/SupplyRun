package com.example.ggould.supplyrun;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.ggould.supplyrun.data.NameTable;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class RunDetailActivity extends AppCompatActivity {

    private String mRunID;
    private String mCreator;
    private NameTable mNameTable;

    private ProgressBar spinner;
    private LinearLayout mainLayout;
    private Toolbar mToolbar;
    private TextView mNumRecipients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_detail);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);

        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        mainLayout = (LinearLayout) findViewById(R.id.main_layout);
        mNumRecipients = (TextView) findViewById(R.id.recipients);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch(Exception e){
            Log.d("exception", "error:"+e);
        }

        Intent intent = getIntent();
        if(intent.hasExtra("runID")) {
            mRunID = intent.getStringExtra("runID");
            //String creator = intent.getStringExtra("creator");
            mToolbar.setTitle("");
            getRunData();
        }else{
            Log.d("intent", "no runID found");
        }
        setSupportActionBar(mToolbar);

    }

    private void getRunData(){
        ParseQuery<ParseObject> mainQuery = ParseQuery.getQuery("runs");
        mainQuery.whereEqualTo("objectId", mRunID);
        mainQuery.findInBackground(new FindCallback<ParseObject>() {
            public void done(final List<ParseObject> runsList, ParseException e) {
                if (e == null) {
                    populateFields(runsList);
                    Log.d("runs detail", "Retrieved " + runsList.size() + " records");
//                    // Remove the previously cached results.
//                    ParseObject.unpinAllInBackground("runs", new DeleteCallback() {
//                        public void done(ParseException e) {
//                            if (e == null) {
//                                // Cache the new results.
//                                ParseObject.pinAllInBackground("runs", runsList);
//                                Log.d("local store", "successfully updated local cache");
//                            } else {
//                                Log.d("local store", "error" + e);
//                            }
//                        }
//                    });
                } else {
                    Log.d("run retrieval", "Error: " + e.getMessage());
                }
            }
        });
    }

    private void populateFields(List<ParseObject> runsList){
        ParseObject run;
        mNameTable = new NameTable(this);

        if(runsList.size() == 0){
            return;
        }else if(runsList.size() > 1){
            return;
        }else{
            run = runsList.get(0);
        }

        ArrayList<String> recips = (ArrayList<String>) run.get("friends");
        ArrayList<String> stops = (ArrayList<String>) run.get("places");
        ArrayList<String> times = (ArrayList<String>) run.get("times");
        mCreator = run.getString("creator");

        mToolbar.setTitle(mNameTable.getPreferredName(run.getString("creator")));
        mNumRecipients.setText("You and " + (recips.size() - 1) + " others");

        LinearLayout stopHolder = (LinearLayout) findViewById(R.id.stop_detail_layouts);
        TextView tv;

        for(int i=0; i<stops.size(); i++){
            LinearLayout stopDetail = (LinearLayout) getLayoutInflater()
                    .inflate(R.layout.stop_detail_layout, mainLayout, false);
            tv = (TextView) stopDetail.findViewById(R.id.stop_name);
            tv.setText(stops.get(i));
            tv = (TextView) stopDetail.findViewById(R.id.stop_time);
            tv.setText(times.get(i));
            if(i == stops.size()-1){
                View divider = stopDetail.findViewById(R.id.divider);
                divider.setVisibility(View.GONE);
            }
            stopHolder.addView(stopDetail);
        }

        spinner.setVisibility(View.GONE);
        mainLayout.setVisibility(View.VISIBLE);
    }

    public void launchRequestDialog(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Add the buttons

        builder.setTitle("Send a request");

        final LinearLayout dialogBody = (LinearLayout) getLayoutInflater()
                .inflate(R.layout.request_dialog_layout, mainLayout, false);

        TextView name1 = (TextView) dialogBody.findViewById(R.id.creator_name1);
        TextView name2 = (TextView) dialogBody.findViewById(R.id.creator_name2);
        TextView place = (TextView) dialogBody.findViewById(R.id.place_name);
        final EditText currency = (EditText) dialogBody.findViewById(R.id.currency);
        currency.setOnKeyListener(new OnKeyListener() {
              public boolean onKey(View v, int keyCode, KeyEvent event) {
                  if (keyCode == 66) {
                      InputMethodManager manager = (InputMethodManager) v.getContext()
                              .getSystemService(INPUT_METHOD_SERVICE);
                      if (manager != null)
                          manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                      currency.clearFocus();
                      dialogBody.requestFocus();
                      return true; //this is required to stop sending key event to parent
                  }
                  return false;
              }
          });

        name1.setText(mNameTable.getPreferredName(mCreator));
        name2.setText(mNameTable.getPreferredName(mCreator));
        ViewGroup parent = (ViewGroup) v.getParent();
        TextView sibling = (TextView) parent.findViewById(R.id.stop_name);
        place.setText(sibling.getText());

        builder.setView(dialogBody);

        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
