package com.example.midexam;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class StudentAdapter extends ArrayAdapter<Student> {

    private final Context context;
    private final int resource;
    private final List<Student> students;

    public StudentAdapter(@NonNull Context context, int resource, @NonNull List<Student> students) {
        super(context, resource, students);
        this.context = context;
        this.resource = resource;
        this.students = students;
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

            if (tvName != null) {
                tvName.setText(student.getName());
            }
            if (tvId != null) {
                tvId.setText("ID: " + student.getId());
            }
            if (tvMajor != null) {
                tvMajor.setText(student.getMajor());
            }
        }

        return convertView;
    }
}
