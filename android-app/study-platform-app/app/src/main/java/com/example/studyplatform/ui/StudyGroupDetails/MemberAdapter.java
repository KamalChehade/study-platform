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

import java.util.List;

public class MemberAdapter extends ArrayAdapter<JSONObject> {

    private Context context;
    private List<JSONObject> members;
    private OnRemoveClickListener onRemoveClickListener;

    public MemberAdapter(Context context, List<JSONObject> members, OnRemoveClickListener listener) {
        super(context, 0, members);
        this.context = context;
        this.members = members;
        this.onRemoveClickListener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.member_list_item, parent, false);
        }

        JSONObject member = getItem(position);
        TextView memberNameTextView = convertView.findViewById(R.id.memberNameTextView);
        Button removeButton = convertView.findViewById(R.id.deleteButton);

        try {
            memberNameTextView.setText(member.getString("name"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set a click listener on the "Remove" button
        removeButton.setOnClickListener(v -> onRemoveClickListener.onRemoveClick(member));

        return convertView;
    }

    public interface OnRemoveClickListener {
        void onRemoveClick(JSONObject member);
    }
}
