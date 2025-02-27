package com.example.studyplatform.ui.study;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyplatform.R;

import java.util.List;
public class StudyGroupAdapter extends RecyclerView.Adapter<StudyGroupAdapter.ViewHolder> {

    private final List<StudyGroup> studyGroups;
    private final String userID; // Add userID as a field
    private final Context context; // Add context as a field

    public StudyGroupAdapter(List<StudyGroup> studyGroups, Context context) {
        this.studyGroups = studyGroups;
        this.context = context; // Initialize context
        this.userID = getUserIdFromSharedPreferences(); // Retrieve userID from SharedPreferences
    }

    // Helper method to retrieve userID from SharedPreferences
    private String getUserIdFromSharedPreferences() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("userID", ""); // Return userID or empty string if not found
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.study_group_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudyGroup group = studyGroups.get(position);
        holder.courseName.setText(group.getCourseName());
        holder.description.setText(group.getDescription());

        // Handle "View" button click
        holder.viewButton.setOnClickListener(v -> {
            // Get NavController from the current fragment/activity
            NavController navController = Navigation.findNavController(v);

            // Pass study group details as arguments
            Bundle bundle = new Bundle();
            bundle.putString("groupName", group.getCourseName());
            bundle.putString("description", group.getDescription());
            bundle.putString("groupID", group.getGroupID()); // Pass groupID
            bundle.putString("userID", userID); // Pass userID

            // Navigate to StudyGroupDetailFragment
            navController.navigate(R.id.studyGroupDetailFragment, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return studyGroups.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView courseName, description;
        Button viewButton;

        ViewHolder(View itemView) {
            super(itemView);
            courseName = itemView.findViewById(R.id.courseNameTextView);
            description = itemView.findViewById(R.id.courseDescriptionTextView);
            viewButton = itemView.findViewById(R.id.viewButton);
        }
    }
}