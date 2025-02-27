package com.example.studyplatform.ui.StudyGroupDetails;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.studyplatform.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MessageAdapter extends ArrayAdapter<JSONObject> {
    private Context context;
    private ArrayList<JSONObject> messages;

    public MessageAdapter(Context context, ArrayList<JSONObject> messages) {
        super(context, 0, messages);
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.message_item, parent, false);
        }

        // Get the current message object
        JSONObject messageObject = messages.get(position);

        try {
            // Retrieve the necessary details
            String name = messageObject.getString("name");  // Use 'name' instead of 'username'
            String message = messageObject.getString("message");

            // Find views to display the message and name
            TextView usernameTextView = convertView.findViewById(R.id.usernameTextView);
            TextView messageTextView = convertView.findViewById(R.id.messageTextView);

            // Set the text values to the views
            usernameTextView.setText(name);  // Set name here
            messageTextView.setText(message);  // Set message here
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return convertView;
    }
}
