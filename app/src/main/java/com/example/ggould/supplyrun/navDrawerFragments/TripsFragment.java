package com.example.ggould.supplyrun.navDrawerFragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ggould.supplyrun.CreateNewActivity;
import com.example.ggould.supplyrun.R;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class TripsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public TripsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_trips, container, false);

        FloatingActionButton createFab = (FloatingActionButton) rootView.findViewById(R.id.create_fab);
        createFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CreateNewActivity.class);
                startActivity(intent);
            }
        });

        final LinearLayout rh = (LinearLayout) rootView.findViewById(R.id.runs_holder);
        final LinearLayout orh = (LinearLayout) rootView.findViewById(R.id.old_runs_holder);
        final LinearLayout mrh = (LinearLayout) rootView.findViewById(R.id.my_runs_holder);
        final TextView myRunHeader = (TextView) rootView.findViewById(R.id.my_runs_header);
        final TextView runHeader = (TextView) rootView.findViewById(R.id.friends_run_header);
        final TextView oldRunHeader = (TextView) rootView.findViewById(R.id.old_runs_header);

        //true uses data, false uses local store
        populateData(rootView, true);

        myRunHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mrh.getVisibility() == View.GONE) {
                    mrh.setVisibility(View.VISIBLE);
                } else if (mrh.getVisibility() == View.VISIBLE) {
                    mrh.setVisibility(View.GONE);
                }
            }
        });

        runHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rh.getVisibility() == View.GONE) {
                    rh.setVisibility(View.VISIBLE);
                } else if (rh.getVisibility() == View.VISIBLE) {
                    rh.setVisibility(View.GONE);
                }
            }
        });

        oldRunHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (orh.getVisibility() == View.GONE) {
                    orh.setVisibility(View.VISIBLE);
                } else if (orh.getVisibility() == View.VISIBLE) {
                    orh.setVisibility(View.GONE);
                }
            }
        });

        final SwipeRefreshLayout swipe = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe);
        swipe.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("refresh", "onRefresh called from SwipeRefreshLayout");
                        Fragment fragment;
                        FragmentManager fragmentManager = getFragmentManager();
                        fragment = Fragment.instantiate(getActivity().getBaseContext(),
                                TripsFragment.class.getName());
                        fragmentManager.beginTransaction()
                                .replace(R.id.fragment_holder, fragment)
                                .commit();
                        swipe.setRefreshing(false);
                    }
                }
        );

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onTripsFragmentInteraction(Uri uri);
    }

    //Passing true uses network data, false uses the local datastore
    private void populateData(View v, boolean fromNetwork){
        final View rootView = v;
        final LinearLayout rh = (LinearLayout) rootView.findViewById(R.id.runs_holder);
        final LinearLayout orh = (LinearLayout) rootView.findViewById(R.id.old_runs_holder);
        final LinearLayout mrh = (LinearLayout) rootView.findViewById(R.id.my_runs_holder);

        ParseQuery<ParseObject> myRuns = ParseQuery.getQuery("runs");
        myRuns.whereEqualTo("creator", ParseUser.getCurrentUser().getUsername());
        myRuns.whereEqualTo("active", true);
        myRuns.orderByAscending("createdAt");
        if(!fromNetwork){
            myRuns.fromLocalDatastore();
        }
        myRuns.findInBackground(new FindCallback<ParseObject>() {
            public void done(final List<ParseObject> runsList, ParseException e) {
                if (e == null) {
                    fillActiveTrips(rootView, runsList, mrh);
                    Log.d("my runs", "Retrieved " + runsList.size() + " records");
                    // Remove the previously cached results.
                    ParseObject.unpinAllInBackground("my runs", new DeleteCallback() {
                        public void done(ParseException e) {
                            if(e == null) {
                                // Cache the new results.
                                ParseObject.pinAllInBackground("my runs", runsList);
                                Log.d("local store", "successfully updated local cache");
                            }else{
                                Log.d("local store", "error"+e);
                            }
                        }
                    });
                } else {
                    Log.d("run retrieval", "Error: " + e.getMessage());
                }
            }
        });

        ParseQuery<ParseObject> query = ParseQuery.getQuery("runs");
        query.whereEqualTo("friends", ParseUser.getCurrentUser().getUsername());
        query.whereEqualTo("active", true);
        query.orderByAscending("createdAt");
        if(!fromNetwork){
            query.fromLocalDatastore();
        }
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(final List<ParseObject> runsList, ParseException e) {
                if (e == null) {
                    fillActiveTrips(rootView, runsList, rh);
                    Log.d("runs", "Retrieved " + runsList.size() + " records");
                    // Remove the previously cached results.
                    ParseObject.unpinAllInBackground("runs", new DeleteCallback() {
                        public void done(ParseException e) {
                            if (e == null) {
                                // Cache the new results.
                                ParseObject.pinAllInBackground("runs", runsList);
                                Log.d("local store", "successfully updated local cache");
                            } else {
                                Log.d("local store", "error" + e);
                            }
                        }
                    });
                } else {
                    Log.d("run retrieval", "Error: " + e.getMessage());
                }
            }
        });

        ParseQuery<ParseObject> inactiveRuns = ParseQuery.getQuery("runs");
        inactiveRuns.whereEqualTo("friends", ParseUser.getCurrentUser().getUsername());
        inactiveRuns.whereEqualTo("active", false);
        inactiveRuns.orderByAscending("createdAt");
        if(!fromNetwork){
            inactiveRuns.fromLocalDatastore();
        }
        inactiveRuns.findInBackground(new FindCallback<ParseObject>() {
            public void done(final List<ParseObject> runsList, ParseException e) {
                if (e == null) {
                    fillInactiveTrips(rootView, runsList, orh);
                    Log.d("old runs", "Retrieved " + runsList.size() + " records");
                    ParseObject.unpinAllInBackground("old runs", new DeleteCallback() {
                        public void done(ParseException e) {
                            if (e == null) {
                                // Cache the new results.
                                ParseObject.pinAllInBackground("old runs", runsList);
                                Log.d("local store", "successfully updated local cache");
                            } else {
                                Log.d("local store", "error" + e);
                            }
                        }
                    });
                } else {
                    Log.d("old run retrieval", "Error: " + e.getMessage());
                }
            }
        });
    }

    public void fillActiveTrips(View rootView, List<ParseObject> runsList, LinearLayout rh){
        if(runsList.size()>0){
            LinearLayout noRuns = (LinearLayout) rootView.findViewById(R.id.no_runs);
            noRuns.setVisibility(View.GONE);
            rh.setVisibility(View.VISIBLE);
        }
        LayoutInflater inflate = getActivity().getLayoutInflater();

        for(int i=0; i<runsList.size(); i++){
            LinearLayout rdl = (LinearLayout) inflate.inflate(R.layout.run_display_layout,
                    (ViewGroup) rootView.getParent(), false);
            LinearLayout secHolder = (LinearLayout) rdl.findViewById(R.id.secondary_locations);
            TextView nameTV = (TextView) rdl.findViewById(R.id.runner_name);
            TextView primTV = (TextView) rdl.findViewById(R.id.primary_location);
            View spacer = rdl.findViewById(R.id.spacer);

            String creator = runsList.get(i).getString("creator");
            if(creator.equals(ParseUser.getCurrentUser().getUsername())){
                nameTV.setTextColor(Color.parseColor("#00BCD4"));
            }
            nameTV.setText(creator);

            ArrayList<String> places = (ArrayList<String>) runsList.get(i).get("places");
            ArrayList<String> times = (ArrayList<String>) runsList.get(i).get("times");
            primTV.setText(places.get(0));

            for(int j=1; j<places.size(); j++){
                LinearLayout rsd = (LinearLayout) inflate.inflate(R.layout.run_secondary_display,
                        (ViewGroup) rootView.getParent(), false);
                TextView placeTV = (TextView) rsd.findViewById(R.id.secondary_place);
                TextView timeTV = (TextView) rsd.findViewById(R.id.secondary_time);

                placeTV.setText(places.get(j));
                timeTV.setText(times.get(j));

                secHolder.addView(rsd);
            }
            rh.addView(rdl);

            if(i == runsList.size()-1){
                spacer.setVisibility(View.GONE);
            }
        }
    }

    public void fillInactiveTrips(View rootView, List<ParseObject> oldRuns, LinearLayout orh){
        if(oldRuns.size()>0){
            LinearLayout noRuns = (LinearLayout) rootView.findViewById(R.id.no_old_runs);
            noRuns.setVisibility(View.GONE);
        }
        LayoutInflater inflate = getActivity().getLayoutInflater();

        for(int i=0; i<oldRuns.size(); i++){
            LinearLayout rdl = (LinearLayout) inflate.inflate(R.layout.run_display_layout,
                    (ViewGroup) rootView.getParent(), false);
            LinearLayout secHolder = (LinearLayout) rdl.findViewById(R.id.secondary_locations);
            TextView nameTV = (TextView) rdl.findViewById(R.id.runner_name);
            TextView primTV = (TextView) rdl.findViewById(R.id.primary_location);
            View spacer = rdl.findViewById(R.id.spacer);

            nameTV.setText(oldRuns.get(i).getString("creator"));
            ArrayList<String> places = (ArrayList<String>) oldRuns.get(i).get("places");
            ArrayList<String> times = (ArrayList<String>) oldRuns.get(i).get("times");
            primTV.setText(places.get(0));

            for(int j=1; j<places.size(); j++){
                LinearLayout rsd = (LinearLayout) inflate.inflate(R.layout.run_secondary_display,
                        (ViewGroup) rootView.getParent(), false);
                TextView placeTV = (TextView) rsd.findViewById(R.id.secondary_place);
                TextView timeTV = (TextView) rsd.findViewById(R.id.secondary_time);

                placeTV.setText(places.get(j));
                timeTV.setText(times.get(j));

                secHolder.addView(rsd);
            }
            orh.addView(rdl);

            if(i == oldRuns.size()-1){
                spacer.setVisibility(View.GONE);
            }
        }
    }
}
