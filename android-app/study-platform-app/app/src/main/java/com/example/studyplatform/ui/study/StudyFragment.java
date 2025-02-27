package com.example.studyplatform.ui.study;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.studyplatform.databinding.FragmentStudyBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudyFragment extends Fragment {

    private FragmentStudyBinding binding;
    private RecyclerView recyclerView;
    private StudyGroupAdapter adapter;
    private List<StudyGroup> studyGroups;
    private RequestQueue requestQueue;

    private EditText groupNameEditText, descriptionEditText;
    private Spinner courseDropdown;
    private Button addButton;

    private static final String API_URL = "http://10.0.2.2:5000/api";
    private String universityID;
    private String userID;
    private Map<String, Integer> courseMap = new HashMap<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStudyBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.studyGroupsRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        studyGroups = new ArrayList<>();
        adapter = new StudyGroupAdapter(studyGroups, requireContext());
        recyclerView.setAdapter(adapter);

        groupNameEditText = binding.courseNameEditText;
        descriptionEditText = binding.descriptionEditText;
        courseDropdown = binding.courseDropdown;
        addButton = binding.addButton;

        requestQueue = Volley.newRequestQueue(requireContext());

        // Retrieve universityID, userID, and role from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        universityID = sharedPreferences.getString("universityID", "");
        userID = sharedPreferences.getString("userID", "");
        String role = sharedPreferences.getString("role", ""); // Retrieve the user's role

        if (universityID.isEmpty() || userID.isEmpty()) {
            Toast.makeText(getContext(), "User not found, please log in again.", Toast.LENGTH_SHORT).show();
            return root;
        }

        // Handle UI based on user role
        if (role.equals("Student")) {
            // Disable or hide teacher-specific UI elements
            groupNameEditText.setVisibility(View.GONE);
            descriptionEditText.setVisibility(View.GONE);
            courseDropdown.setVisibility(View.GONE);
            addButton.setVisibility(View.GONE);
        } else if (role.equals("Teacher")) {
            // Fetch courses and enable teacher-specific UI elements
            fetchCourses();
        }

        // Fetch study groups based on user role
        fetchStudyGroups();

        addButton.setOnClickListener(view -> addStudyGroup());

        return root;
    }
    private void fetchStudyGroups() {
        String url = API_URL + "/study-groups/" + userID; // Use the correct API endpoint with userID

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    studyGroups.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject group = response.getJSONObject(i);
                            studyGroups.add(new StudyGroup(
                                    group.getString("groupName"),
                                    group.getString("description"),
                                    group.getString("groupID"), // Include groupID
                                    group.getString("teacherID") // Include teacherID
                            ));
                        }
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.e("StudyFragment", "JSON Parsing error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("StudyFragment", "API Error: " + error.getMessage());
                    Toast.makeText(getContext(), "Error fetching study groups", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(request);
    }

    private void fetchCourses() {
        String url = API_URL + "/courses";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    List<String> courseNames = new ArrayList<>();
                    courseMap.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject course = response.getJSONObject(i);
                            int teacherID = course.getInt("teacherID");
                            if (teacherID == Integer.parseInt(universityID)) {
                                String courseName = course.getString("courseName");
                                int courseID = course.getInt("courseID");
                                courseNames.add(courseName);
                                courseMap.put(courseName, courseID);
                            }
                        }
                        populateSpinner(courseNames);
                    } catch (JSONException e) {
                        Log.e("StudyFragment", "JSON Parsing error: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("StudyFragment", "API Error: " + error.getMessage());
                    Toast.makeText(getContext(), "Error fetching courses", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(request);
    }

    private void populateSpinner(List<String> courseNames) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, courseNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseDropdown.setAdapter(adapter);
    }

    private void addStudyGroup() {
        String groupName = groupNameEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        String selectedCourseName = courseDropdown.getSelectedItem().toString();

        if (groupName.isEmpty() || description.isEmpty() || selectedCourseName.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the correct courseID from the selected course name
        Integer selectedCourseID = courseMap.get(selectedCourseName);
        if (selectedCourseID == null) {
            Toast.makeText(getContext(), "Invalid course selection", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = API_URL + "/study-groups/add";
        JSONObject studyGroupData = new JSONObject();
        try {
            studyGroupData.put("groupName", groupName);
            studyGroupData.put("courseID", selectedCourseID); // Send courseID instead of name
            studyGroupData.put("description", description);
            studyGroupData.put("teacherID", universityID);
        } catch (JSONException e) {
            Log.e("StudyFragment", "JSON Exception: " + e.getMessage());
            return;
        }

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    // Create a new StudyGroup object with all required fields
                    studyGroups.add(new StudyGroup(
                            groupName, // groupName
                            description, // description
                            "123", // groupID
                            universityID // teacherID (use universityID as teacherID)
                    ));
                    adapter.notifyDataSetChanged();
                    groupNameEditText.setText("");
                    descriptionEditText.setText("");
                    Toast.makeText(getContext(), "Study group added", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Log.e("StudyFragment", "API Error: " + error.getMessage());
                    Toast.makeText(getContext(), "Failed to add study group", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public byte[] getBody() {
                return studyGroupData.toString().getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}