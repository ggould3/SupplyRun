package com.example.ggould.supplyrun.navDrawerFragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.ggould.supplyrun.LoginActivity;
import com.example.ggould.supplyrun.R;
import com.parse.ParseUser;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        TripsFragment.OnFragmentInteractionListener,
        WalletFragment.OnFragmentInteractionListener,
        DestinationFragment.OnFragmentInteractionListener,
        FriendsFragment.OnFragmentInteractionListener,
        RunsFragment.OnFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        TextView navUser = (TextView) headerView.findViewById(R.id.nav_user);

        try {
            ParseUser currentUser = ParseUser.getCurrentUser();
            if (currentUser != null) {
                navUser.setText(currentUser.getUsername());
            } else {
                //Sign in user if none available
                Intent intent = new Intent(NavigationActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        }catch(Exception e){
            Log.d("Sign in", "caught exception");
            Intent intent = new Intent(NavigationActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        Fragment fragment;
        FragmentManager fragmentManager = getFragmentManager(); // For AppCompat use getSupportFragmentManager
        fragment = Fragment.instantiate(getBaseContext(), RunsFragment.class.getName());

        Intent intent = getIntent();
        if(intent.hasExtra("fragmentKey")){
            switch (intent.getIntExtra("fragmentKey",0)){
                case 0:
                    //do this
                    break;
                case 1:
                    //do this
                    break;
                case 2:
                    //do this
                    break;
                case 3:
                    //do this
                    break;
                default:
                    //this should not occur
                    break;
            }
        }

        fragmentManager.beginTransaction()
                .replace(R.id.fragment_holder, fragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment fragment;
        FragmentManager fragmentManager = getFragmentManager(); // For AppCompat use getSupportFragmentManager
        fragment = Fragment.instantiate(getBaseContext(), RunsFragment.class.getName());

        if (id == R.id.nav_trips) {
            fragment = new RunsFragment();
        } else if (id == R.id.nav_conv) {
            fragment = new WalletFragment();
        } else if (id == R.id.nav_friends) {
            fragment = new FriendsFragment();
        } else if (id == R.id.nav_log) {
            ParseUser currentUser = ParseUser.getCurrentUser();
            currentUser.logOut();
            finish();
            startActivity(getIntent());
        } else if (id == R.id.nav_settings) {

        }

        fragmentManager.beginTransaction()
                .replace(R.id.fragment_holder, fragment)
                .commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onTripsFragmentInteraction(Uri uri){

    }

    @Override
    public void onWalletFragmentInteraction(Uri uri){

    }

    @Override
    public void onDestinationFragmentInteraction(Uri uri){

    }

    @Override
    public void onFriendsFragmentInteraction(Uri uri){

    }

    @Override
    public void onRunsFragmentInteraction(Uri uri){

    }
}
