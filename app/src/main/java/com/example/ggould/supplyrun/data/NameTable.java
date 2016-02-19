package com.example.ggould.supplyrun.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.parse.ParseObject;

import java.util.List;

public class NameTable {

    public static final String PREFS_NAME = "SupplyRun_FriendNames";

    SharedPreferences mPrefs;
    SharedPreferences.Editor mEditor;

    public NameTable(Context context){
        mPrefs = context.getSharedPreferences(PREFS_NAME, 0);
    }

    public String getPreferredName(String username){
        String prefName = mPrefs.getString(username, "");
        if(prefName.equals("")){
            prefName = username;
        }
        return prefName;
    }

    public void setPreferredName(String username, String name){
        mEditor = mPrefs.edit();
        mEditor.putString(username, name);
        mEditor.apply();
    }

    public void update(List<ParseObject> friends){
        mEditor = mPrefs.edit();
        for(int i=0; i<friends.size(); i++){
            String user = friends.get(i).getString("friendname");
            String rename = friends.get(i).getString("friend_rename");
            mEditor.putString(user, rename);
        }
        mEditor.apply();
    }
}
