package com.example.studyplatform.ui.StudyGroupDetails;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.studyplatform.R;

import org.json.JSONObject;

import java.util.ArrayList;
public class UploadedFilesAdapter extends ArrayAdapter<JSONObject> {
    private final Context context;
    private final ArrayList<JSONObject> uploadedFiles;
    private final OnDeleteClickListener onDeleteClickListener;
    private final String userRole; // Add user role as a field

    public interface OnDeleteClickListener {
        void onDeleteClick(JSONObject file);
    }

    public UploadedFilesAdapter(Context context, ArrayList<JSONObject> uploadedFiles, OnDeleteClickListener onDeleteClickListener, String userRole) {
        super(context, R.layout.uploaded_file_item, uploadedFiles);
        this.context = context;
        this.uploadedFiles = uploadedFiles;
        this.onDeleteClickListener = onDeleteClickListener;
        this.userRole = userRole; // Initialize user role
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.uploaded_file_item, parent, false);
        }

        // Get the uploaded file at the current position
        JSONObject uploadedFile = uploadedFiles.get(position);

        // Bind data to the views
        TextView fileNameTextView = convertView.findViewById(R.id.fileNameTextView);
        TextView fileLinkTextView = convertView.findViewById(R.id.fileLinkTextView);
        Button deleteButton = convertView.findViewById(R.id.deleteButton);

        try {
            fileNameTextView.setText(uploadedFile.getString("title")); // Use "title" or "fileName" from your API
            fileLinkTextView.setText(uploadedFile.getString("fileLink")); // Use "fileLink" from your API
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Hide the delete button for students
        if (userRole.equals("Student")) {
            deleteButton.setVisibility(View.GONE); // Hide the delete button
        } else {
            deleteButton.setVisibility(View.VISIBLE); // Show the delete button for teachers
        }

        // Set click listener for the delete button
        deleteButton.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(uploadedFile);
            }
        });

        return convertView;
    }

    // Method to add a file to the list and refresh the adapter
    public void addFile(JSONObject file) {
        uploadedFiles.add(file);
        notifyDataSetChanged(); // Refresh the adapter
    }

    // Method to remove a file from the list and refresh the adapter
    public void removeFile(JSONObject file) {
        uploadedFiles.remove(file);
        notifyDataSetChanged(); // Refresh the adapter
    }

    // Method to update the entire list and refresh the adapter
    public void updateList(ArrayList<JSONObject> newList) {
        uploadedFiles.clear();
        uploadedFiles.addAll(newList);
        notifyDataSetChanged(); // Refresh the adapter
    }
}