package com.example.studyplatform;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;

import com.example.studyplatform.activities.SignInActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studyplatform.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Retrieve user role from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userRole = sharedPreferences.getString("role", "Student"); // Default to Student
        String userID = sharedPreferences.getString("userID", "Student"); // Default to Student

        // Get the menu from NavigationView
        Menu menu = navigationView.getMenu();

        // Customize menu based on role
        if ("Student".equals(userRole)) {
            menu.clear(); // Remove existing items
            menu.add(Menu.NONE, R.id.nav_home, Menu.NONE, "Home").setIcon(R.drawable.ic_home);
            menu.add(Menu.NONE, R.id.nav_courses, Menu.NONE, "Courses").setIcon(R.drawable.ic_courses);
            menu.add(Menu.NONE, R.id.nav_studyGroup, Menu.NONE, "Study Groups").setIcon(R.drawable.ic_people);
        }

        // Initialize AppBarConfiguration
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_courses, R.id.nav_studyGroup)
                .setOpenableLayout(drawer)
                .build();

        // Setup NavController
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Get the footer layout and the logout button
        View footerView = navigationView.getChildAt(navigationView.getChildCount() - 1);
        View logoutView = footerView.findViewById(R.id.logout_text);

        // Set the OnClickListener for the logout button
        logoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogout();
            }
        });
    }

    private void handleLogout() {
        // Clear user data from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();  // Clears all the saved preferences
        editor.apply();

        // Navigate the user back to the SignInActivity
        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();  // Close MainActivity to prevent the user from navigating back
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu (action bar items)
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
