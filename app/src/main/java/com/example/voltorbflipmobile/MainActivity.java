package com.example.voltorbflipmobile;

import android.media.MediaPlayer;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.voltorbflipmobile.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;




public class MainActivity extends AppCompatActivity {




    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    public MediaPlayer background_song = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null).show();
            }
        });

        background_song = MediaPlayer.create(this, R.raw.background_song);
        background_song.setLooping(true);
        background_song.setVolume(0.5f, 0.5f);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

// ======================= ME HERE! =======================

    public void startMusic() {
        if (background_song != null && !background_song.isPlaying()) {
            background_song.start();
        }
    }

    public void stopMusic() {
        if (background_song != null && background_song.isPlaying()) {
            background_song.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (background_song != null) {
            background_song.release();
            background_song = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startMusic();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }







}