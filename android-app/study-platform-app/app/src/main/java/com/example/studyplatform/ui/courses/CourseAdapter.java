package com.example.studyplatform.ui.courses;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyplatform.R;

import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private List<Course> courseList;
    private String buttonText;
    private OnCourseActionListener onCourseActionListener;

    public CourseAdapter(List<Course> courseList, String buttonText, OnCourseActionListener onCourseActionListener) {
        this.courseList = courseList;
        this.buttonText = buttonText;
        this.onCourseActionListener = onCourseActionListener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courseList.get(position);
        holder.courseName.setText(course.getCourseName());
        holder.actionButton.setText(buttonText);

        // Handle button click for enrolling or leaving course
        holder.actionButton.setOnClickListener(v -> {
            if (buttonText.equals("Enroll")) {
                if (onCourseActionListener != null) {
                    onCourseActionListener.onEnroll(course.getCourseID());
                }
            } else if (buttonText.equals("Leave")) {
                // Handle leave course action
                if (onCourseActionListener != null) {
                    onCourseActionListener.onLeave(course.getCourseID());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView courseName;
        Button actionButton;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            courseName = itemView.findViewById(R.id.text_course_name);
            actionButton = itemView.findViewById(R.id.btn_action);
        }
    }


    public interface OnCourseActionListener {
        void onEnroll(int courseID);
        void onLeave(int courseID);
    }
}
