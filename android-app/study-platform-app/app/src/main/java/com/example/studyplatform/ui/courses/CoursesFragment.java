package com.example.studyplatform.ui.courses;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.studyplatform.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CoursesFragment extends Fragment implements CourseAdapter.OnCourseActionListener {

    private RecyclerView enrolledRecyclerView;
    private RecyclerView availableRecyclerView;
    private CourseAdapter enrolledAdapter;
    private CourseAdapter availableAdapter;
    private List<Course> enrolledCourses;
    private List<Course> availableCourses;
    private String userID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        View root = inflater.inflate(R.layout.fragment_course, container, false);

        // Get the userID from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", getContext().MODE_PRIVATE);
        userID = sharedPreferences.getString("userID", "User");  // Save to the class field

        Log.d("CoursesFragment", "Received userID: " + userID);



        // Initialize RecyclerViews
        enrolledRecyclerView = root.findViewById(R.id.recycler_enrolled_courses);
        availableRecyclerView = root.findViewById(R.id.recycler_available_courses);

        enrolledRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        availableRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize Lists
        enrolledCourses = new ArrayList<>();
        availableCourses = new ArrayList<>();

        // Initialize Adapters with the listener
        enrolledAdapter = new CourseAdapter(enrolledCourses, "Leave", this); // Change button text to "Leave"
        availableAdapter = new CourseAdapter(availableCourses, "Enroll", this);

        enrolledRecyclerView.setAdapter(enrolledAdapter);
        availableRecyclerView.setAdapter(availableAdapter);

        // Fetch enrolled courses for the user
        fetchEnrolledCourses();

        return root;
    }

    // Implement the onEnroll method from the interface
    @Override
    public void onEnroll(int courseID) {
        enrollInCourse(courseID); // Call the enrollInCourse method
    }

    // Implement the onLeave method from the interface
    public void onLeave(int courseID) {
        leaveCourse(courseID); // Call the leaveCourse method
    }

    private void enrollInCourse(int courseID) {
        // API endpoint to enroll in a course
        String url = "http://10.0.2.2:5000/api/courses/enroll";

        try {
            JSONObject enrollmentData = new JSONObject();
            enrollmentData.put("studentID", userID);  // Use studentID instead of userID
            enrollmentData.put("courseID", courseID);

            JsonObjectRequest enrollRequest = new JsonObjectRequest(Request.Method.POST, url, enrollmentData,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // Handle successful enrollment
                            Toast.makeText(getContext(), "Enrolled successfully!", Toast.LENGTH_SHORT).show();

                            // Update the UI: Move the course from available to enrolled
                            Course enrolledCourse = null;
                            for (Course course : availableCourses) {
                                if (course.getCourseID() == courseID) {
                                    enrolledCourse = course;
                                    break;
                                }
                            }

                            if (enrolledCourse != null) {
                                // Add to enrolled courses and remove from available
                                enrolledCourses.add(enrolledCourse);
                                availableCourses.remove(enrolledCourse);
                                availableAdapter.notifyDataSetChanged();
                                enrolledAdapter.notifyDataSetChanged();
                            }
                        }
                    },
                    error -> {
                        // Handle error response
                        Toast.makeText(getContext(), "Failed to enroll. Please try again.", Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    });

            // Add the request to the request queue
            Volley.newRequestQueue(getContext()).add(enrollRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Handle the leave course functionality
    private void leaveCourse(int courseID) {
        // API endpoint to leave the course
        String url = "http://10.0.2.2:5000/api/courses/leave";

        try {
            JSONObject leaveData = new JSONObject();
            leaveData.put("studentID", userID); // Use studentID instead of userID
            leaveData.put("courseID", courseID);

            JsonObjectRequest leaveRequest = new JsonObjectRequest(Request.Method.POST, url, leaveData,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // Handle successful leave
                            Toast.makeText(getContext(), "Left the course successfully!", Toast.LENGTH_SHORT).show();

                            // Update the UI: Move the course from enrolled to available
                            Course leftCourse = null;
                            for (Course course : enrolledCourses) {
                                if (course.getCourseID() == courseID) {
                                    leftCourse = course;
                                    break;
                                }
                            }

                            if (leftCourse != null) {
                                // Add to available courses and remove from enrolled
                                availableCourses.add(leftCourse);
                                enrolledCourses.remove(leftCourse);
                                availableAdapter.notifyDataSetChanged();
                                enrolledAdapter.notifyDataSetChanged();
                            }
                        }
                    },
                    error -> {
                        // Handle error response
                        Toast.makeText(getContext(), "Failed to leave the course. Please try again.", Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    });

            // Add the request to the request queue
            Volley.newRequestQueue(getContext()).add(leaveRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Fetch enrolled courses method...
    private void fetchEnrolledCourses() {
        // Fetch the enrolled courses for the userID from your API
        String url = "http://10.0.2.2:5000/api/courses/enrolled/" + userID;

        // Make a network request using Volley with JsonArrayRequest
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            // Log the entire response to check the structure
                            Log.d("CoursesFragment", "Response: " + response.toString());

                            List<Course> fetchedEnrolledCourses = new ArrayList<>();

                            // Loop through the courses array to extract course details
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject course = response.getJSONObject(i);
                                int courseID = course.getInt("courseID");
                                String courseName = course.getString("courseName");
                                int teacherID = course.getInt("teacherID");

                                fetchedEnrolledCourses.add(new Course(courseID, courseName, teacherID));
                            }

                            // Log the fetched courses list to ensure data is populated
                            Log.d("CoursesFragment", "Fetched Courses: " + fetchedEnrolledCourses.size());

                            // Update the enrolled courses list with the fetched data
                            enrolledCourses.clear();
                            enrolledCourses.addAll(fetchedEnrolledCourses);

                            // Fetch all available courses excluding the enrolled ones
                            fetchAvailableCourses(fetchedEnrolledCourses);

                            // Notify the adapter to refresh the list
                            enrolledAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                error -> {
                    // Handle error response
                    error.printStackTrace();
                });

        // Add the request to the request queue
        Volley.newRequestQueue(getContext()).add(jsonArrayRequest);
    }

    // Fetch available courses method...
    private void fetchAvailableCourses(List<Course> enrolledCourses) {
        // Fetch all courses from the API
        String url = "http://10.0.2.2:5000/api/courses/";

        // Make a network request using Volley with JsonArrayRequest
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            List<Course> fetchedAvailableCourses = new ArrayList<>();

                            // Loop through the courses array to extract course details
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject course = response.getJSONObject(i);
                                int courseID = course.getInt("courseID");
                                String courseName = course.getString("courseName");
                                int teacherID = course.getInt("teacherID");

                                // Add the course if it's not in the enrolled list
                                boolean isEnrolled = false;
                                for (Course enrolledCourse : enrolledCourses) {
                                    if (enrolledCourse.getCourseID() == courseID) {
                                        isEnrolled = true;
                                        break;
                                    }
                                }

                                // If the course is not enrolled, add to availableCourses list
                                if (!isEnrolled) {
                                    fetchedAvailableCourses.add(new Course(courseID, courseName, teacherID));
                                }
                            }

                            // Update the available courses list with the fetched data
                            availableCourses.clear();
                            availableCourses.addAll(fetchedAvailableCourses);

                            // Notify the adapter to refresh the list
                            availableAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                error -> {
                    // Handle error response
                    error.printStackTrace();
                });

        // Add the request to the request queue
        Volley.newRequestQueue(getContext()).add(jsonArrayRequest);
    }
}
