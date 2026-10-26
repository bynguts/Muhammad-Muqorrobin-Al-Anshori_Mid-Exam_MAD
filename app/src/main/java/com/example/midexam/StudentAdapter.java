package com.example.midexam;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class StudentAdapter extends ArrayAdapter<Student> {

    public interface OnStudentActionListener {
        void onEdit(Student student);
        void onDelete(Student student);
    }

    private final Context context;
    private final int resource;
    private final List<Student> students;
    private final OnStudentActionListener listener;

    public StudentAdapter(@NonNull Context context, int resource, @NonNull List<Student> students, @Nullable OnStudentActionListener listener) {
        super(context, resource, students);
        this.context = context;
        this.resource = resource;
        this.students = students;
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);
        }

        Student student = students.get(position);
        if (student != null) {
            TextView tvName = convertView.findViewById(R.id.tvItemName);
            TextView tvId = convertView.findViewById(R.id.tvItemId);
            TextView tvMajor = convertView.findViewById(R.id.tvItemMajor);
            MaterialButton btnEdit = convertView.findViewById(R.id.btnItemEdit);
            MaterialButton btnDelete = convertView.findViewById(R.id.btnItemDelete);

            if (tvName != null) {
                tvName.setText(student.getName());
            }
            if (tvId != null) {
                tvId.setText("Student ID: " + student.getId());
            }
            if (tvMajor != null) {
                tvMajor.setText(student.getMajor());
            }

            // Direct in-card EDIT action
            if (btnEdit != null) {
                btnEdit.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onEdit(student);
                    }
                });
            }

            // Direct in-card DELETE action
            if (btnDelete != null) {
                btnDelete.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDelete(student);
                    }
                });
            }
        }

        return convertView;
    }
}
