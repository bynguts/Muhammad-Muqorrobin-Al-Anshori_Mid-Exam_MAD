package com.example.midexam;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText etStudentName;
    private EditText etStudentID;
    private Spinner spMajor;
    private Button btnAdd;
    private Button btnEdit;
    private Button btnDelete;
    private Button btnView;
    private Button btnClear;

    private ListView lvStudents;
    private TextView tvEmpty;
    private TextView tvStudentCount;

    private DatabaseHelper dbHelper;
    private List<Student> studentList;
    private StudentAdapter studentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize SQLite Database Helper
        dbHelper = new DatabaseHelper(this);

        // Initialize UI Views
        initViews();

        // Setup Major Spinner with Informatics and Information Systems
        setupMajorSpinner();

        // Load existing student records from SQLite
        loadStudentData();

        // Register Button Click Listeners
        setupActionListeners();
    }

    private void initViews() {
        etStudentName = findViewById(R.id.etStudentName);
        etStudentID = findViewById(R.id.etStudentID);
        spMajor = findViewById(R.id.spMajor);

        // Name input filter: prohibit any numeric digits
        InputFilter nameFilter = (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                if (Character.isDigit(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        };
        etStudentName.setFilters(new InputFilter[]{nameFilter});

        // Student ID input filter: allow only numeric digits
        InputFilter idFilter = (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                if (!Character.isDigit(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        };
        etStudentID.setFilters(new InputFilter[]{idFilter});

        btnAdd = findViewById(R.id.btnAdd);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        btnView = findViewById(R.id.btnView);
        btnClear = findViewById(R.id.btnClear);

        lvStudents = findViewById(R.id.lvStudents);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvStudentCount = findViewById(R.id.tvStudentCount);

        studentList = new ArrayList<>();
        studentAdapter = new StudentAdapter(this, R.layout.item_student, studentList);
        lvStudents.setAdapter(studentAdapter);
    }

    private void setupMajorSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.majors_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMajor.setAdapter(adapter);
    }

    private void setupActionListeners() {
        // ADD Button: Insert new student record
        btnAdd.setOnClickListener(v -> addStudent());

        // EDIT Button: Update existing student record by Student ID
        btnEdit.setOnClickListener(v -> editStudent());

        // DELETE Button: Remove student record by Student ID
        btnDelete.setOnClickListener(v -> deleteStudent());

        // VIEW Button: Display all student records in an AlertDialog and refresh list
        btnView.setOnClickListener(v -> viewStudents());

        // CLEAR Button: Reset input fields
        btnClear.setOnClickListener(v -> clearInputs());

        // List item click: Populate fields for easy editing / deleting
        lvStudents.setOnItemClickListener((parent, view, position, id) -> {
            Student selected = studentList.get(position);
            if (selected != null) {
                etStudentID.setText(selected.getId());
                etStudentName.setText(selected.getName());

                // Set spinner selection
                setSpinnerToMajor(selected.getMajor());

                Toast.makeText(MainActivity.this,
                        "Selected: " + selected.getName() + " (" + selected.getId() + ")",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Case 1: ADD student to SQLite.
     * Enforces that Student ID is numeric, non-empty, and UNIQUE.
     * Enforces that Student Name contains no numbers.
     */
    private void addStudent() {
        String name = etStudentName.getText().toString().trim();
        String id = etStudentID.getText().toString().trim();
        String major = spMajor.getSelectedItem() != null ? spMajor.getSelectedItem().toString() : "";

        if (name.isEmpty() || id.isEmpty()) {
            Toast.makeText(this, "Please enter both Student Name and Student ID!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.matches(".*\\d.*")) {
            Toast.makeText(this, "Student Name cannot contain numbers!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!id.matches("\\d+")) {
            Toast.makeText(this, "Student ID must only contain numbers!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check Unique constraint before adding
        if (dbHelper.checkStudentExists(id)) {
            Toast.makeText(this, "Error: Student ID '" + id + "' already exists! Student ID must be UNIQUE.", Toast.LENGTH_LONG).show();
            return;
        }

        boolean isInserted = dbHelper.insertStudent(id, name, major);
        if (isInserted) {
            Toast.makeText(this, "Student added successfully!", Toast.LENGTH_SHORT).show();
            clearInputs();
            loadStudentData();
        } else {
            Toast.makeText(this, "Failed to add student. Please check database constraints.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Case 1: EDIT student in SQLite based on Student ID.
     */
    private void editStudent() {
        String id = etStudentID.getText().toString().trim();
        String name = etStudentName.getText().toString().trim();
        String major = spMajor.getSelectedItem() != null ? spMajor.getSelectedItem().toString() : "";

        if (id.isEmpty()) {
            Toast.makeText(this, "Please enter Student ID to edit!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!id.matches("\\d+")) {
            Toast.makeText(this, "Student ID must only contain numbers!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter Student Name to update!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.matches(".*\\d.*")) {
            Toast.makeText(this, "Student Name cannot contain numbers!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!dbHelper.checkStudentExists(id)) {
            Toast.makeText(this, "Error: Student ID '" + id + "' not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isUpdated = dbHelper.updateStudent(id, name, major);
        if (isUpdated) {
            Toast.makeText(this, "Student updated successfully!", Toast.LENGTH_SHORT).show();
            clearInputs();
            loadStudentData();
        } else {
            Toast.makeText(this, "Failed to update student.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Case 1: DELETE student from SQLite based on Student ID.
     */
    private void deleteStudent() {
        String id = etStudentID.getText().toString().trim();

        if (id.isEmpty()) {
            Toast.makeText(this, "Please enter Student ID to delete!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!id.matches("\\d+")) {
            Toast.makeText(this, "Student ID must only contain numbers!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!dbHelper.checkStudentExists(id)) {
            Toast.makeText(this, "Error: Student ID '" + id + "' not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete student with ID: " + id + "?")
                .setPositiveButton("DELETE", (dialog, which) -> {
                    boolean isDeleted = dbHelper.deleteStudent(id);
                    if (isDeleted) {
                        Toast.makeText(MainActivity.this, "Student deleted successfully!", Toast.LENGTH_SHORT).show();
                        clearInputs();
                        loadStudentData();
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to delete student.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    /**
     * Case 1: VIEW all students.
     * Displays a formatted AlertDialog with all records and refreshes the on-screen list.
     */
    private void viewStudents() {
        loadStudentData();

        if (studentList.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Student Records")
                    .setMessage("No student records found in database.\nUse ADD to insert records.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        StringBuilder builder = new StringBuilder();
        int index = 1;
        for (Student s : studentList) {
            builder.append(index++)
                    .append(". ID: ").append(s.getId()).append("\n")
                    .append("   Name: ").append(s.getName()).append("\n")
                    .append("   Major: ").append(s.getMajor()).append("\n\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("All Students (" + studentList.size() + " total)")
                .setMessage(builder.toString().trim())
                .setPositiveButton("OK", null)
                .show();
    }

    /**
     * Refreshes the student list from SQLite database.
     */
    private void loadStudentData() {
        studentList.clear();
        studentList.addAll(dbHelper.getStudentList());
        studentAdapter.notifyDataSetChanged();

        tvStudentCount.setText(studentList.size() + " record(s)");

        if (studentList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    /**
     * Clears input fields and resets spinner.
     */
    private void clearInputs() {
        etStudentID.setText("");
        etStudentName.setText("");
        spMajor.setSelection(0);
        etStudentName.requestFocus();
    }

    /**
     * Helper to set spinner selection by major string.
     */
    private void setSpinnerToMajor(String major) {
        if (major == null) return;
        for (int i = 0; i < spMajor.getCount(); i++) {
            if (spMajor.getItemAtPosition(i).toString().equalsIgnoreCase(major.trim())) {
                spMajor.setSelection(i);
                break;
            }
        }
    }
}
