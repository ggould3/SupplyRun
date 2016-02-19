package com.example.ggould.supplyrun.data;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ggould.supplyrun.R;
import com.example.ggould.supplyrun.RunDetailActivity;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class RunsAdapter extends RecyclerView.Adapter<RunsAdapter.ViewHolder> {
    private Context mContext;

    private List<ParseObject> mMyRuns;
    private List<ParseObject> mActiveRuns;
    private List<ParseObject> mInactiveRuns;

    private ArrayList<ParseObject> mDataset;

    private NameTable mNameTable;

    String creator;
    boolean active;
    String user = "unknown";

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView mUsername;
        public TextView mPrimary;
        public TextView mSecondary;
        public TextView mSecTime;

        public ViewHolder(View v) {
            super(v);
            mUsername = (TextView) v.findViewById(R.id.nameplate);
            mPrimary = (TextView) v.findViewById(R.id.primary_stop);
            mSecondary = (TextView) v.findViewById(R.id.secondary_stops);
            mSecTime = (TextView) v.findViewById(R.id.secondary_times);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v){
            Intent intent = new Intent(v.getContext(), RunDetailActivity.class);
            intent.putExtra("runID", mUsername.getTag().toString());
            v.getContext().startActivity(intent);

            //Toast.makeText(v.getContext(), mUsername.getTag().toString(), Toast.LENGTH_LONG).show();
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RunsAdapter(Context context, List<ParseObject> myR, List<ParseObject> activeR,
                       List<ParseObject> inactiveR) {
        mContext = context;

        mMyRuns = myR;
        mActiveRuns = activeR;
        mInactiveRuns = inactiveR;

        mDataset = new ArrayList<>();
        if(mMyRuns != null) {
            mDataset.addAll(mMyRuns);
        }
        if (mActiveRuns != null) {
            mDataset.addAll(mActiveRuns);
        }
        if(mInactiveRuns != null){
            mDataset.addAll(mInactiveRuns);
        }

        ParseUser currentUser = ParseUser.getCurrentUser();
        if(currentUser != null){
            user = currentUser.getUsername();
        }else{
            user = "unknown";
        }

        mNameTable = new NameTable(context);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RunsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_runs_layout, parent, false);

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if(mDataset == null){
            return;
        }
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        creator = mDataset.get(position).getString("creator");
        active = mDataset.get(position).getBoolean("active");
        ArrayList<String> places = (ArrayList<String>) mDataset.get(position).get("places");
        ArrayList<String> times = (ArrayList<String>) mDataset.get(position).get("times");

        holder.mUsername.setText(mNameTable.getPreferredName(creator));
        holder.mUsername.setTag(mDataset.get(position).getObjectId());

        if(places != null) {
            holder.mPrimary.setText(places.get(0));
            holder.mSecondary.setText(newLineFormat(places));
        }
        if(times != null) {
            holder.mSecTime.setText(newLineFormat(times));
        }

        if(creator.equals(user)){
            holder.mUsername.setTextColor(Color.parseColor("#00BCD4"));
        }else if(!active){
            holder.mUsername.setTextColor(Color.parseColor("#000000"));
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {

        return mDataset == null ? 0 : mDataset.size();
    }

    public void updateList(List<ParseObject> myR, List<ParseObject> activeR,
                           List<ParseObject> inactiveR) {
        mMyRuns = myR;
        mActiveRuns = activeR;
        mInactiveRuns = inactiveR;

        mDataset = new ArrayList<>();
        if(mMyRuns != null) {
            mDataset.addAll(mMyRuns);
        }
        if (mActiveRuns != null) {
            mDataset.addAll(mActiveRuns);
        }
        if(mInactiveRuns != null){
            mDataset.addAll(mInactiveRuns);
        }

        notifyDataSetChanged();
    }

    private String newLineFormat(ArrayList<String> strings){
        String newLine = "";
        for(int i=1; i<strings.size(); i++){
            newLine += strings.get(i);

            if(i != strings.size()-1){
                newLine += "\n";
            }
        }
        return newLine;
    }
}
