package com.example.ggould.supplyrun.navDrawerFragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ggould.supplyrun.CreateNewActivity;
import com.example.ggould.supplyrun.R;
import com.example.ggould.supplyrun.data.RunsAdapter;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RunsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RunsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RunsFragment extends Fragment {

    List<ParseObject> mMyRuns;
    List<ParseObject> mActiveRuns;
    List<ParseObject> mInactiveRuns;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<String> myDataset;

    private SwipeRefreshLayout swipe;

    public static final String PREFS_NAME = "SupplyRun_DataTimes";
    private final long SYNC_MINUTES = 10;
    private final long SYNC_PERIOD = 1000*60*SYNC_MINUTES;

    private ParseUser mCurrentUser;
    private String mUsername;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public RunsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RunsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RunsFragment newInstance(String param1, String param2) {
        RunsFragment fragment = new RunsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_runs, container, false);

        mCurrentUser = ParseUser.getCurrentUser();
        if(mCurrentUser != null){
            mUsername = mCurrentUser.getUsername();
        }else{
            mUsername = "unknown";
        }

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.runs_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity().getBaseContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new RunsAdapter(getActivity().getBaseContext(), mMyRuns, mActiveRuns, mInactiveRuns);
        mRecyclerView.setAdapter(mAdapter);

        //true uses data, false uses local storage
        populateRunsData((RunsAdapter) mAdapter, false);

        swipe = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe);
        swipe.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("refresh", "onRefresh called from SwipeRefreshLayout");
                        populateRunsData((RunsAdapter) mAdapter, true);
                        swipe.setRefreshing(false);
                    }
                }
        );

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.create_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CreateNewActivity.class);
                startActivity(intent);
            }
        });

        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);

        //getting the current time in milliseconds, and creating a Date object from it:
        Date date = new Date(System.currentTimeMillis());
        //converting it back to a milliseconds representation:
        long millis = date.getTime();

        //Get the last data sync time
        long lastMillis = settings.getLong("runs_data_time", 0);

        if(millis - lastMillis > SYNC_PERIOD){
            //Data is old, refresh and write time of update
            populateRunsData((RunsAdapter) mAdapter, true);

            // Writing data to SharedPreferences
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong("runs_data_time", millis);
            editor.apply();

            // Reading from SharedPreferences
            Log.d("data time log", String.valueOf(millis));
        }else{
            Log.d("data time log", "recent data, using local store");
        }

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
        void onRunsFragmentInteraction(Uri uri);
    }

    private void populateRunsData(final RunsAdapter adapter, boolean fromNetwork){
        ParseQuery<ParseObject> myRuns = ParseQuery.getQuery("runs");
        myRuns.whereEqualTo("creator", mUsername);
        myRuns.whereEqualTo("active", true);
        //myRuns.orderByAscending("createdAt");
        if(!fromNetwork){
            myRuns.fromLocalDatastore();
        }

        ParseQuery<ParseObject> currRuns = ParseQuery.getQuery("runs");
        currRuns.whereEqualTo("friends", mUsername);
        currRuns.whereEqualTo("active", true);
        //currRuns.orderByAscending("createdAt");
        if(!fromNetwork){
            currRuns.fromLocalDatastore();
        }

        ParseQuery<ParseObject> inactiveRuns = ParseQuery.getQuery("runs");
        inactiveRuns.whereEqualTo("friends", mUsername);
        inactiveRuns.whereEqualTo("active", false);
        //inactiveRuns.orderByAscending("createdAt");
        if(!fromNetwork){
            inactiveRuns.fromLocalDatastore();
        }

        List<ParseQuery<ParseObject>> queries = new ArrayList<>();
        queries.add(myRuns);
        queries.add(currRuns);
        //queries.add(inactiveRuns);

        ParseQuery<ParseObject> mainQuery = ParseQuery.or(queries);

        mainQuery.findInBackground(new FindCallback<ParseObject>() {
            public void done(final List<ParseObject> runsList, ParseException e) {
                if (e == null) {
                    fillLists(runsList, adapter);
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
    }

    private void fillLists(List<ParseObject> runsList, RunsAdapter adapter){
        String creator;
        boolean active;
        String user = mUsername;
        mMyRuns = new ArrayList<>();
        mActiveRuns = new ArrayList<>();
        mInactiveRuns = new ArrayList<>();

        for(int i=0; i<runsList.size(); i++){
            creator = runsList.get(i).getString("creator");
            active = runsList.get(i).getBoolean("active");

            if(creator.equals(user)){
                mMyRuns.add(runsList.get(i));
            }else if(active){
                mActiveRuns.add(runsList.get(i));
            }else{
                mInactiveRuns.add(runsList.get(i));
            }
        }

        adapter.updateList(mMyRuns, mActiveRuns, mInactiveRuns);
        swipe.setRefreshing(false);
    }
}
