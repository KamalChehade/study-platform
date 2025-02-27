package com.example.studyplatform.ui.StudyGroupDetails;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.studyplatform.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StudyGroupDetailFragment extends Fragment {

    private static final String BASE_URL = "http://10.0.2.2:5000/api/study-groups/members";
    private static final String REMOVE_MEMBER_URL = "http://10.0.2.2:5000/api/study-groups/remove-member";
    private static final String FETCH_CHAT_MESSAGES_URL = "http://10.0.2.2:5000/api/study-groups/chat";
    private static final String SEND_MESSAGE_URL = "http://10.0.2.2:5000/api/study-groups/chat/send";
    private static final String UPLOAD_MATERIALS_URL = "http://10.0.2.2:5000/api/study-materials/upload"; // URL for file upload

    private static final int PICK_FILE_REQUEST = 1; // Request code for file picker

    private ListView membersListView;
    private MemberAdapter membersAdapter;
    private ArrayList<JSONObject> membersList;
    private ArrayList<JSONObject> uploadedFilesList;
    private UploadedFilesAdapter uploadedFilesAdapter;
    private ListView messagesListView;
    private EditText chatMessageEditText;
    private Button sendMessageButton;
    private ArrayList<JSONObject> messagesList;
    private MessageAdapter messageAdapter;

    private Uri fileUri; // URI of the selected file

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.study_group_details, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        uploadedFilesList = new ArrayList<>();

        // Initialize UI elements
        TextView courseName = view.findViewById(R.id.courseNameTextView);
        TextView courseDescription = view.findViewById(R.id.courseDescriptionTextView);
        membersListView = view.findViewById(R.id.membersListView);
        Button uploadFileButton = view.findViewById(R.id.uploadFileButton);
        TextView membersTextView = view.findViewById(R.id.membersTextView); // "Members" heading

        // Retrieve user role from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userRole = sharedPreferences.getString("role", "Student"); // Default is Student
        String teacherID = sharedPreferences.getString("userID", ""); // Default to empty string if not found

        // Hide Upload File button and Members section for Students
        if (userRole.equals("Student")) {
            uploadFileButton.setVisibility(View.GONE);
            membersTextView.setVisibility(View.GONE);
            membersListView.setVisibility(View.GONE);

        }


        // Initialize the list and adapter for members
        membersList = new ArrayList<>();
        membersAdapter = new MemberAdapter(getContext(), membersList, new MemberAdapter.OnRemoveClickListener() {
            @Override
            public void onRemoveClick(JSONObject member) {
                handleRemoveStudent(member);
            }
        });
        membersListView.setAdapter(membersAdapter);

        // Initialize chat section
        messagesListView = view.findViewById(R.id.messagesListView);
        chatMessageEditText = view.findViewById(R.id.chatMessageEditText);
        sendMessageButton = view.findViewById(R.id.sendMessageButton);

        // Initialize message list and adapter
        messagesList = new ArrayList<>();
        messageAdapter = new MessageAdapter(getContext(), messagesList);
        messagesListView.setAdapter(messageAdapter);

        // Set click listener for the send message button
        sendMessageButton.setOnClickListener(v -> {
            String message = chatMessageEditText.getText().toString();
            if (!message.isEmpty()) {
                sendMessage(message);
            } else {
                showToast("Message cannot be empty");
            }
        });

        // Set click listener for the upload file button
        uploadFileButton.setOnClickListener(v -> openFilePicker());


        uploadedFilesAdapter = new UploadedFilesAdapter(
                getContext(),
                uploadedFilesList,
                new UploadedFilesAdapter.OnDeleteClickListener() {
                    @Override
                    public void onDeleteClick(JSONObject file) {
                        deleteFile(file);
                    }
                },
                userRole
        );
        ListView uploadedFilesListView = view.findViewById(R.id.uploadedFilesListView); // Study materials ListView
        // Initialize study materials ListView and adapter
         uploadedFilesListView.setAdapter(uploadedFilesAdapter);


        // Handle click events on uploaded files
        uploadedFilesListView.setOnItemClickListener((parent, view1, position, id) -> {
            JSONObject uploadedFile = uploadedFilesList.get(position);
            try {
                String fileLink = uploadedFile.getString("fileLink");
                openFile(fileLink);
            } catch (Exception e) {
                e.printStackTrace();
                showToast("Error opening file");
            }
        });

        // Get passed data
        Bundle args = getArguments();
        if (args != null) {
            String groupName = args.getString("groupName");
            String description = args.getString("description");

            // Set text to received values
            courseName.setText(groupName);
            courseDescription.setText(description);

            // Fetch group members
            String groupID = args.getString("groupID");
            fetchGroupMembers(groupID);

            // Fetch chat messages
            fetchChatMessages(groupID);

            // Fetch study materials
            fetchStudyMaterials(groupID, uploadedFilesList, uploadedFilesAdapter);
        }
    }
    private void deleteFile(JSONObject file) {
        try {
            String materialID = file.getString("materialID"); // Assuming your API uses "materialID" to identify files
            Log.d("DeleteFile", "Deleting file with materialID: " + materialID);

            new Thread(() -> {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://10.0.2.2:5000/api/study-materials/delete/" + materialID) // Correct URL
                            .delete()
                            .build();

                    // Log the request URL
                    Log.d("DeleteFile", "Request URL: " + request.url());

                    Response response = client.newCall(request).execute();

                    // Log the response
                    Log.d("DeleteFile", "Response Code: " + response.code());
                    String responseBody = response.body().string();
                    Log.d("DeleteFile", "Response Body: " + responseBody);

                    if (response.isSuccessful()) {
                        // File deleted successfully, remove it from the list
                        getActivity().runOnUiThread(() -> {
                            uploadedFilesList.remove(file);
                            uploadedFilesAdapter.notifyDataSetChanged();
                            showToast("File deleted successfully");
                        });
                    } else {
                        showToast("Failed to delete file");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast("Error: " + e.getMessage());
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Error: " + e.getMessage());
        }
    }

    private void fetchStudyMaterials(String groupID, ArrayList<JSONObject> uploadedFilesList, UploadedFilesAdapter uploadedFilesAdapter) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://10.0.2.2:5000/api/study-materials/" + groupID)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JSONArray jsonArray = new JSONArray(responseBody);

                     ArrayList<JSONObject> uploadedFiles = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        uploadedFiles.add(jsonArray.getJSONObject(i));
                    }

                    // Update the ListView on the main thread
                    getActivity().runOnUiThread(() -> {
                        uploadedFilesList.clear();
                        uploadedFilesList.addAll(uploadedFiles);
                        uploadedFilesAdapter.notifyDataSetChanged();
                    });
                } else {
                    showToast("Failed to fetch study materials");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showToast("Error: " + e.getMessage());
            }
        }).start();
    }
    private void openFile(String fileLink) {


        // Open the file using an Intent
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(fileLink));
        startActivity(intent);
    }
    // Open file picker
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Allow all file types
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    // Handle the result of the file picker
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            fileUri = data.getData();
            if (fileUri != null) {
                uploadFile();
            }
        }
    }

    // Upload the selected file to the backend
    private void uploadFile() {
        if (fileUri == null) {
            showToast("No file selected");
            return;
        }

        new Thread(() -> {
            try {
                // Create a file from the URI
                InputStream inputStream = requireActivity().getContentResolver().openInputStream(fileUri);
                File file = new File(requireContext().getCacheDir(), getFileName(fileUri));
                FileOutputStream outputStream = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.close();
                inputStream.close();

                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                 String teacherID = sharedPreferences.getString("userID", ""); // Default to empty string if not found

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("files", file.getName(), RequestBody.create(file, MediaType.parse("application/octet-stream")))
                        .addFormDataPart("groupID", getArguments().getString("groupID")) // Get groupID from arguments
                        .addFormDataPart("teacherID", teacherID) // Replace with actual teacherID (fetch from SharedPreferences or arguments)
                        .build();

                // Create the HTTP request
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(UPLOAD_MATERIALS_URL) // URL for file upload
                        .post(requestBody)
                        .build();

                // Execute the request
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    // File uploaded successfully, fetch the updated list
                    getActivity().runOnUiThread(() -> {
                        fetchStudyMaterials(getArguments().getString("groupID"), uploadedFilesList, uploadedFilesAdapter);
                       showToast("File uploaded successfully");

                    });
                } else {
                    showToast("Failed to upload file");
                }
            } catch (Exception e) {
                Log.e("UploadError", "Error uploading file", e);
                showToast("Error: " + e.getMessage());
            }
        }).start();
    }

    // Get the file name from the URI
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = requireActivity().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (columnIndex != -1) { // Check if the column index is valid
                        result = cursor.getString(columnIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void fetchGroupMembers(String groupID) {
        // Create a new thread to make the network request
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Prepare the request
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(BASE_URL + "/" + groupID)
                            .build();

                    // Execute the request
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        // Parse JSON response (assuming it's an array of members)
                        String responseBody = response.body().string();
                        JSONArray jsonArray = new JSONArray(responseBody);

                        // Extract member names and other data
                        ArrayList<JSONObject> members = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject memberObject = jsonArray.getJSONObject(i);
                            members.add(memberObject);  // Storing whole member object for universityID
                        }

                        // Update the ListView
                        updateMembersList(members);
                    } else {
                        showToast("Error fetching members");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast("Error: " + e.getMessage());
                }
            }
        }).start();
    }

    private void fetchChatMessages(String groupID) {
        // Create a new thread to make the network request
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Prepare the request
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(FETCH_CHAT_MESSAGES_URL + "/" + groupID)
                            .build();

                    // Execute the request
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        // Parse JSON response (assuming it's an array of messages)
                        String responseBody = response.body().string();
                        JSONArray jsonArray = new JSONArray(responseBody);

                        // Extract message data and log the message and name
                        ArrayList<JSONObject> messages = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject messageObject = jsonArray.getJSONObject(i);
                            String name = messageObject.getString("name");
                            String message = messageObject.getString("message");

                            // Log the message and name
                            Log.d("ChatMessage", name + ": " + message);

                            // Add the message object to the list
                            messages.add(messageObject);
                        }

                        // Update the ListView
                        updateMessagesList(messages);
                    } else {
                        showToast("Error fetching chat messages");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast("Error: " + e.getMessage());
                }
            }
        }).start();
    }

    private void updateMessagesList(final ArrayList<JSONObject> messages) {
        // Run on the main thread to update the ListView
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messagesList.clear();
                messagesList.addAll(messages);
                messageAdapter.notifyDataSetChanged();
            }
        });
    }


    private void updateMembersList(final ArrayList<JSONObject> members) {
        // Run on the main thread to update the ListView
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                membersList.clear();
                membersList.addAll(members);
                membersAdapter.notifyDataSetChanged();
            }
        });
    }


    private void showToast(final String message) {
        // Use the main thread to show the toast
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleRemoveStudent(final JSONObject member) {
        // Extract universityID from the member object
        try {
            final String universityID = member.getString("userID"); // Use userID as universityID
            final String groupID = getArguments().getString("groupID"); // Get groupID

            // Create a new thread to make the DELETE request
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Prepare the DELETE request with groupID and universityID in the request body
                        OkHttpClient client = new OkHttpClient();

                        RequestBody requestBody = new FormBody.Builder()
                                .add("groupID", groupID) // Send groupID
                                .add("universityID", universityID) // Send universityID of the member
                                .build();

                        Request request = new Request.Builder()
                                .url(REMOVE_MEMBER_URL) // URL remains the same
                                .delete(requestBody)  // Use the DELETE method with request body
                                .build();

                        // Execute the request
                        Response response = client.newCall(request).execute();
                        if (response.isSuccessful()) {
                            // Remove the member from the list on success
                            removeMemberFromList(member);
                        } else {
                            showToast("Error deleting member: " + response.message());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showToast("Error: " + e.getMessage());
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Error: " + e.getMessage());
        }
    }

    private void removeMemberFromList(final JSONObject member) {
        // Run on the main thread to update the ListView
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                membersList.remove(member);  // Remove the member from the list
                membersAdapter.notifyDataSetChanged(); // Notify the adapter
                showToast("Member deleted successfully");
            }
        });
    }

    private void sendMessage(final String message) {
        final String groupID = getArguments().getString("groupID");
        final String userID = getArguments().getString("userID"); // Get the userID from arguments

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Prepare the request to send message
                    OkHttpClient client = new OkHttpClient();
                    RequestBody requestBody = new FormBody.Builder()
                            .add("groupID", groupID)
                            .add("userID", userID)
                            .add("message", message)
                            .build();

                    Request request = new Request.Builder()
                            .url(SEND_MESSAGE_URL)
                            .post(requestBody)
                            .build();

                    // Execute the request
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        // Successfully sent the message, clear the message box
                        showToast("Message sent!");
                        chatMessageEditText.setText("");

                        // Fetch the updated chat messages after sending
                        fetchChatMessages(groupID);
                    } else {
                        showToast("Failed to send message");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast("Error: " + e.getMessage());
                }
            }
        }).start();
    }
}
