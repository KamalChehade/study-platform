package com.example.studyplatform.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.studyplatform.MainActivity;
import com.example.studyplatform.R;

import org.json.JSONException;
import org.json.JSONObject;

public class SignInActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private TextView errorTextView, signUpTextView;
    private Button signInButton;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        // Check if user is already logged in
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userJson = sharedPreferences.getString("user", null);
        if (userJson != null) {
            // User is already logged in, navigate to MainActivity
            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close SignInActivity
            return;
        }
        // Initialize views
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        errorTextView = findViewById(R.id.errorTextView);
        signInButton = findViewById(R.id.signInButton);
        signUpTextView = findViewById(R.id.signUpTextView);

        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        // Set click listener for the Sign In button
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

         signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    private void handleLogin() {
        // Get user input
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            errorTextView.setText("Please fill in all fields.");
            errorTextView.setVisibility(View.VISIBLE);
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorTextView.setText("Invalid email format.");
            errorTextView.setVisibility(View.VISIBLE);
            return;
        }

         JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("email", email);
            requestBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Make API call
        String url = "http://10.0.2.2:5000/api/auth/login";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    try {
                        // Check if there's an error in the response
                        if (response.has("error")) {
                            String errorMessage = response.getString("error");
                            if ("wrong password".equals(errorMessage)) {
                                errorTextView.setText("Your password is wrong.");
                            } else {
                                errorTextView.setText(errorMessage);
                            }
                            errorTextView.setVisibility(View.VISIBLE);
                            Toast.makeText(SignInActivity.this, "Login failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                        } else {
                            // Save user info in SharedPreferences (similar to localStorage in JS)
                            String userJson = response.getJSONObject("user").toString();
                            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("user", userJson);
                            editor.putString("universityID", response.getJSONObject("user").getString("universityID")); // Store universityID
                            editor.putString("userID", response.getJSONObject("user").getString("userID")); // Store userID
                            editor.putString("name", response.getJSONObject("user").getString("name")); // Store name
                            editor.putString("role", response.getJSONObject("user").getString("role")); // Store role

                            editor.apply();




                            // Get the user role (student or teacher)
                            String role = response.getJSONObject("user").getString("role");
                            String teacherId = response.getJSONObject("user").getString("universityID");
                            String userId = response.getJSONObject("user").getString("userID");
                            String name = response.getJSONObject("user").getString("name");
                            Toast.makeText(SignInActivity.this, "Login successful! Role: " + role , Toast.LENGTH_SHORT).show();

                            // If the user is a student, show a toast and don't navigate anywhere
                            if ("Student".equals(role)) {
                                Toast.makeText(SignInActivity.this, "You are a student", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish(); // Close SignInActivity
                            }

                            // For Teacher or other roles, route to MainActivity
                            if ("Teacher".equals(role)) {
                                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish(); // Close SignInActivity
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        errorTextView.setText("Please try again.");
                        errorTextView.setVisibility(View.VISIBLE);
                        Toast.makeText(SignInActivity.this, "Please try again.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("SignInActivity", "Error: " + error.getMessage());
                    errorTextView.setText(" Try again .");
                    errorTextView.setVisibility(View.VISIBLE);
                    Toast.makeText(SignInActivity.this, " Try again.", Toast.LENGTH_SHORT).show();
                }
        );

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

}
