package com.example.studyplatform.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.studyplatform.R;

import org.json.JSONException;
import org.json.JSONObject;

public class SignUpActivity extends AppCompatActivity {

    private EditText nameEditText, emailEditText, passwordEditText, universityIdEditText;
    private Spinner roleSpinner;
    private Button signUpButton;
    private TextView errorTextView, signInTextView;
    private RequestQueue requestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize views
        nameEditText = findViewById(R.id.fullNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        universityIdEditText = findViewById(R.id.UniIdEditText);
        roleSpinner = findViewById(R.id.roleSpinner);
        signUpButton = findViewById(R.id.signInButton);
        errorTextView = findViewById(R.id.errorTextView);
        signInTextView = findViewById(R.id.signUpTextView);

        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        // Set click listener for the Sign Up button
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRegister();
            }
        });

        // Set click listener for the Sign In text
        signInTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to SignInActivity
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });
    }

    private void handleRegister() {
        // Get user input
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String universityID = universityIdEditText.getText().toString().trim();
        String role = roleSpinner.getSelectedItem().toString();

        // Validate inputs
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || universityID.isEmpty()) {
            errorTextView.setText("Please fill in all fields.");
            errorTextView.setVisibility(View.VISIBLE);
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorTextView.setText("Invalid email format.");
            errorTextView.setVisibility(View.VISIBLE);
            return;
        }

        // Create JSON object for the request body
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("name", name);
            requestBody.put("email", email);
            requestBody.put("password", password);
            requestBody.put("role", role);
            requestBody.put("universityID", universityID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Make API call
        String url = "http://10.0.2.2:5000/api/auth/register";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    try {
                        if (response.has("error")) {
                            // If there's an error in the response, log it but continue
                            String errorMessage = response.getString("error");
                            Log.e("SignUpActivity", "API Error: " + errorMessage);

                            // Redirect to SignInActivity even if there's an error
                            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                            startActivity(intent);
                            finish(); // Close SignUpActivity
                        } else {
                            // Success scenario
                            String successMessage = response.getString("message");
                            Toast.makeText(SignUpActivity.this, successMessage, Toast.LENGTH_SHORT).show();

                            // Redirect to SignInActivity on success
                            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                            startActivity(intent);
                            finish(); // Close SignUpActivity
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("SignUpActivity", "JSON Exception: " + e.getMessage());

                        // Redirect to SignInActivity even on JSON exception
                        Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                        startActivity(intent);
                        finish();
                    }
                },

                error -> {
                    Log.e("SignUpActivity", "Volley Error: " + error.getMessage());

                    // Redirect to SignInActivity even if a server or network error occurs
                    Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                    startActivity(intent);
                    finish(); // Close SignUpActivity
                }
        );


        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }
}
