package com.example.ggould.supplyrun.data;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ggould.supplyrun.R;
import com.parse.ParseObject;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {
    private List<ParseObject> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // each data item is just a string in this case
        public TextView mNameTextView;
        public TextView mCustomNameTextView;
        public ViewHolder(View v) {
            super(v);
            mNameTextView = (TextView) v.findViewById(R.id.friend_name_plate);
            mCustomNameTextView = (TextView) v.findViewById(R.id.friend_rename_plate);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v){
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            // Add the buttons

            builder.setTitle(mCustomNameTextView.getText());

            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User saved the dialog
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

    // Provide a suitable constructor (depends on the kind of dataset)
    public FriendsAdapter(List<ParseObject> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public FriendsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friends_recycler_layout, parent, false);

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mNameTextView.setText(mDataset.get(position).getString("friendname"));
        holder.mCustomNameTextView.setText(mDataset.get(position).getString("friend_rename"));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset == null ? 0 : mDataset.size();
    }

    public void updateList(List<ParseObject> friends) {
        mDataset = friends;
        notifyDataSetChanged();
    }
}