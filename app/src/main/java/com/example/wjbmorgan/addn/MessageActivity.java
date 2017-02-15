package com.example.wjbmorgan.addn;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.florescu.android.rangeseekbar.RangeSeekBar;

/*
This class is a navigation draw activity which displays a number of
report categories.
 */

public class MessageActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // For store and read user's preference.
    public SharedPreferences preference;
    public SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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
        getMenuInflater().inflate(R.menu.message, menu);
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

        // For start hba1c setting fragment.
        if (id == R.id.nav_hba1c) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_message, new hba1c()).commit();
            View content = findViewById(R.id.content_message);
            content.setBackgroundColor(Color.WHITE);
        } else if (id == R.id.nav_dashboard) {

        } else if (id == R.id.nav_diabetes_type) {

        } else if (id == R.id.nav_insulin_regimen) {

        } else if (id == R.id.nav_bmi_sds) {

        } else if (id == R.id.nav_severe_hypo_dka) {

        } else if (id == R.id.nav_completeness_audit) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // When user click the generate button, store values that are not automatically
    // saved to shared preference file, then open the report activity.
    public void generateReport(View view){
        preference = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preference.edit();
        RangeSeekBar ageRange = (RangeSeekBar) findViewById(R.id.age_range);
        RangeSeekBar duration = (RangeSeekBar) findViewById(R.id.duration);
        RangeSeekBar hba1cRange = (RangeSeekBar) findViewById(R.id.hba1c_range);
        int minAge = (int) ageRange.getSelectedMinValue();
        int maxAge = (int) ageRange.getSelectedMaxValue();
        int minDuration = (int) duration.getSelectedMinValue();
        int maxDuration = (int) duration.getSelectedMaxValue();
        int minHba1c = (int) hba1cRange.getSelectedMinValue();
        int maxHba1c = (int) hba1cRange.getSelectedMaxValue();
        editor.putInt("minAge", minAge);
        editor.putInt("maxAge", maxAge);
        editor.putInt("minDuration", minDuration);
        editor.putInt("maxDuration", maxDuration);
        editor.putInt("minHba1c", minHba1c);
        editor.putInt("maxHba1c", maxHba1c);
        editor.commit();
        Intent intent = new Intent(this, Report.class);
        startActivity(intent);
    }
}
