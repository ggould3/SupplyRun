package com.example.ggould.supplyrun.navDrawerFragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.ggould.supplyrun.R;
import com.example.ggould.supplyrun.data.FriendsAdapter;
import com.example.ggould.supplyrun.data.NameTable;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FriendsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FriendsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private List<ParseObject> myDataset;

    private SwipeRefreshLayout swipe;

    public static final String PREFS_NAME = "SupplyRun_DataTimes";
    private final long SYNC_MINUTES = 30;
    private final long SYNC_PERIOD = 1000*60*SYNC_MINUTES;

    private NameTable nameTable;

    private OnFragmentInteractionListener mListener;

    public FriendsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FriendsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendsFragment newInstance(String param1, String param2) {
        FriendsFragment fragment = new FriendsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //do nothing
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.friends_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity().getBaseContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new FriendsAdapter(myDataset);
        mRecyclerView.setAdapter(mAdapter);

        //true uses data, false uses local storage
        populateFriendsList((FriendsAdapter) mAdapter, false);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.create_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createFriendDialog(view);
            }
        });

        swipe = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe);
        swipe.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("refresh", "onRefresh called from SwipeRefreshLayout");
                        populateFriendsList((FriendsAdapter) mAdapter, true);
                        swipe.setRefreshing(false);
                    }
                }
        );

        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);

        //getting the current time in milliseconds, and creating a Date object from it:
        Date date = new Date(System.currentTimeMillis());
        //converting it back to a milliseconds representation:
        long millis = date.getTime();

        //Get the last data sync time
        long lastMillis = settings.getLong("friends_data_time", 0);

        if(millis - lastMillis > SYNC_PERIOD){
            //Data is old, refresh and write time of update
            populateFriendsList((FriendsAdapter) mAdapter, true);

            // Writing data to SharedPreferences
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong("friends_data_time", millis);
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
        void onFriendsFragmentInteraction(Uri uri);
    }

    public void populateFriendsList(final FriendsAdapter adapter, final boolean fromNetwork){
        ParseQuery<ParseObject> myFriends = ParseQuery.getQuery("friendships");
        myFriends.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        myFriends.whereEqualTo("userconf", true);
        myFriends.whereEqualTo("friendconf", true);
        myFriends.orderByAscending("friendname");
        if(!fromNetwork){
            myFriends.fromLocalDatastore();
        }
        myFriends.findInBackground(new FindCallback<ParseObject>() {
            public void done(final List<ParseObject> friendsList, ParseException e) {
                if (e == null) {
                    fillFriendsNames(friendsList, adapter);
                    if(fromNetwork) {
                        //update list of preferred names
                        nameTable = new NameTable(getActivity().getBaseContext());
                        nameTable.update(friendsList);
                    }
                    Log.d("my friends", "Retrieved " + friendsList.size() + " records");
                    // Remove the previously cached results.
                    ParseObject.unpinAllInBackground("my friends", new DeleteCallback() {
                        public void done(ParseException e) {
                            if(e == null) {
                                // Cache the new results.
                                ParseObject.pinAllInBackground("my friends", friendsList);
                                Log.d("local store", "successfully updated local cache");
                            }else{
                                Log.d("local store", "error"+e);
                            }
                        }
                    });
                } else {
                    Log.d("friend retrieval", "Error: " + e.getMessage());
                }
            }
        });
    }

    private void fillFriendsNames(List<ParseObject> query, FriendsAdapter adapter){
        myDataset = query;

        adapter.updateList(myDataset);
        swipe.setRefreshing(false);
    }

    private void createFriendDialog(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Add the buttons

        builder.setTitle("Send a friend request");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.text_input_layout, null);
        final EditText inputbox = (EditText) ll.findViewById(R.id.friend_username);


        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(ll);

        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                final String friend = inputbox.getText().toString();

                ParseObject newFriend = new ParseObject("friendships");
                newFriend.put("username", ParseUser.getCurrentUser().getUsername());
                newFriend.put("friendname", friend);
                newFriend.put("userconf", true);
                newFriend.put("friendconf", false);
                newFriend.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e == null){
                            Log.d("new friend", friend);
                        }else{
                            Log.d("new friend", "error: "+e);
                        }
                    }
                });

                ParseObject newRequest = new ParseObject("friendships");
                newRequest.put("username", friend);
                newRequest.put("friendname", ParseUser.getCurrentUser().getUsername());
                newRequest.put("userconf", false);
                newRequest.put("friendconf", true);
                newRequest.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e == null){
                            Log.d("new request", friend);
                        }else{
                            Log.d("new request", "error: "+e);
                        }
                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
