package com.example.midexam;

import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements StudentAdapter.OnStudentActionListener {

    private TextView tvFormTitle;
    private MaterialButton btnCancelEdit;
    private EditText etStudentName;
    private EditText etStudentID;
    private TextView tvNameWarning;
    private TextView tvIdWarning;

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

    private static final int COLOR_ERROR = Color.parseColor("#D32F2F");
    private boolean isEditMode = false;
    private String editingStudentId = null;

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

        // Setup real-time red warning validation
        setupInputValidationWatchers();

        // Setup Major Spinner with Informatics and Information Systems
        setupMajorSpinner();

        // Load existing student records from SQLite
        loadStudentData();

        // Register Button Click Listeners
        setupActionListeners();
    }

    private void initViews() {
        tvFormTitle = findViewById(R.id.tvFormTitle);
        btnCancelEdit = findViewById(R.id.btnCancelEdit);

        etStudentName = findViewById(R.id.etStudentName);
        etStudentID = findViewById(R.id.etStudentID);
        tvNameWarning = findViewById(R.id.tvNameWarning);
        tvIdWarning = findViewById(R.id.tvIdWarning);
        spMajor = findViewById(R.id.spMajor);

        btnAdd = findViewById(R.id.btnAdd);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        btnView = findViewById(R.id.btnView);
        btnClear = findViewById(R.id.btnClear);

        lvStudents = findViewById(R.id.lvStudents);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvStudentCount = findViewById(R.id.tvStudentCount);

        studentList = new ArrayList<>();
        // StudentAdapter with listener for in-card EDIT and DELETE actions
        studentAdapter = new StudentAdapter(this, R.layout.item_student, studentList, this);
        lvStudents.setAdapter(studentAdapter);
    }

    /**
     * Real-time listeners for instant red warning feedback:
     * - If Student Name contains numbers -> Turns text red & shows clean red label below.
     * - If Student ID contains letters/symbols -> Turns text red & shows clean red label below.
     */
    private void setupInputValidationWatchers() {
        final int defaultTextColor = ContextCompat.getColor(this, R.color.text_primary);

        // Watcher for Student Name
        etStudentName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s != null ? s.toString() : "";
                if (input.matches(".*\\d.*")) {
                    etStudentName.setTextColor(COLOR_ERROR);
                    tvNameWarning.setVisibility(View.VISIBLE);
                } else {
                    etStudentName.setTextColor(defaultTextColor);
                    tvNameWarning.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Watcher for Student ID
        etStudentID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s != null ? s.toString() : "";
                if (!input.isEmpty() && !input.matches("\\d+")) {
                    etStudentID.setTextColor(COLOR_ERROR);
                    tvIdWarning.setVisibility(View.VISIBLE);
                } else {
                    etStudentID.setTextColor(defaultTextColor);
                    tvIdWarning.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
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
        // ADD Button: If in edit mode, it saves updates; if in add mode, inserts new student.
        btnAdd.setOnClickListener(v -> {
            if (isEditMode) {
                saveEditStudent();
            } else {
                addStudent();
            }
        });

        // EDIT Button: Save updates if in edit mode or edits student based on entered ID
        btnEdit.setOnClickListener(v -> saveEditStudent());

        // DELETE Button: Remove student record by Student ID
        btnDelete.setOnClickListener(v -> deleteStudentById(etStudentID.getText().toString().trim(), null));

        // VIEW Button: Display all student records in an AlertDialog and refresh list
        btnView.setOnClickListener(v -> viewStudents());

        // CLEAR Button: Reset input fields
        btnClear.setOnClickListener(v -> clearInputs());

        // Cancel Edit button
        btnCancelEdit.setOnClickListener(v -> cancelEditMode());

        // Tapping card also triggers edit mode
        lvStudents.setOnItemClickListener((parent, view, position, id) -> {
            Student selected = studentList.get(position);
            if (selected != null) {
                onEdit(selected);
            }
        });
    }

    /**
     * Interface callback: Direct EDIT action clicked on student card.
     */
    @Override
    public void onEdit(Student student) {
        if (student == null) return;
        isEditMode = true;
        editingStudentId = student.getId();

        tvFormTitle.setText("✏️ Edit Student (ID: " + student.getId() + ")");
        btnCancelEdit.setVisibility(View.VISIBLE);

        etStudentID.setText(student.getId());
        etStudentID.setEnabled(false); // Lock ID during edit as it's the UNIQUE primary key

        etStudentName.setText(student.getName());
        setSpinnerToMajor(student.getMajor());

        btnAdd.setText("UPDATE");
        etStudentName.requestFocus();
        etStudentName.setSelection(etStudentName.getText().length());

        Toast.makeText(this, "Editing " + student.getName(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Interface callback: Direct DELETE action clicked on student card.
     */
    @Override
    public void onDelete(Student student) {
        if (student == null) return;
        deleteStudentById(student.getId(), student.getName());
    }

    private void cancelEditMode() {
        isEditMode = false;
        editingStudentId = null;
        tvFormTitle.setText("Add New Student");
        btnCancelEdit.setVisibility(View.GONE);
        etStudentID.setEnabled(true);
        btnAdd.setText(R.string.btn_add);
        clearInputs();
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
            etStudentName.setTextColor(COLOR_ERROR);
            tvNameWarning.setVisibility(View.VISIBLE);
            etStudentName.requestFocus();
            return;
        }

        if (!id.matches("\\d+")) {
            etStudentID.setTextColor(COLOR_ERROR);
            tvIdWarning.setVisibility(View.VISIBLE);
            etStudentID.requestFocus();
            return;
        }

        // Check Unique constraint before adding
        if (dbHelper.checkStudentExists(id)) {
            etStudentID.setTextColor(COLOR_ERROR);
            tvIdWarning.setVisibility(View.VISIBLE);
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
     * Case 1: EDIT / UPDATE student in SQLite based on Student ID.
     */
    private void saveEditStudent() {
        String id = isEditMode && editingStudentId != null ? editingStudentId : etStudentID.getText().toString().trim();
        String name = etStudentName.getText().toString().trim();
        String major = spMajor.getSelectedItem() != null ? spMajor.getSelectedItem().toString() : "";

        if (id.isEmpty()) {
            Toast.makeText(this, "Please enter Student ID to edit!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!id.matches("\\d+")) {
            etStudentID.setTextColor(COLOR_ERROR);
            tvIdWarning.setVisibility(View.VISIBLE);
            etStudentID.requestFocus();
            return;
        }

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter Student Name to update!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.matches(".*\\d.*")) {
            etStudentName.setTextColor(COLOR_ERROR);
            tvNameWarning.setVisibility(View.VISIBLE);
            etStudentName.requestFocus();
            return;
        }

        if (!dbHelper.checkStudentExists(id)) {
            Toast.makeText(this, "Error: Student ID '" + id + "' not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isUpdated = dbHelper.updateStudent(id, name, major);
        if (isUpdated) {
            Toast.makeText(this, "Student updated successfully!", Toast.LENGTH_SHORT).show();
            cancelEditMode();
            loadStudentData();
        } else {
            Toast.makeText(this, "Failed to update student.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Case 1: DELETE student from SQLite based on Student ID.
     */
    private void deleteStudentById(String id, String optionalName) {
        if (id == null || id.isEmpty()) {
            Toast.makeText(this, "Please enter or select a Student ID to delete!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!id.matches("\\d+")) {
            etStudentID.setTextColor(COLOR_ERROR);
            tvIdWarning.setVisibility(View.VISIBLE);
            etStudentID.requestFocus();
            return;
        }

        if (!dbHelper.checkStudentExists(id)) {
            Toast.makeText(this, "Error: Student ID '" + id + "' not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        String displayName = optionalName != null && !optionalName.isEmpty() ? optionalName : ("ID: " + id);

        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete " + displayName + "?")
                .setPositiveButton("DELETE", (dialog, which) -> {
                    boolean isDeleted = dbHelper.deleteStudent(id);
                    if (isDeleted) {
                        Toast.makeText(MainActivity.this, "Student deleted successfully!", Toast.LENGTH_SHORT).show();
                        if (isEditMode && id.equals(editingStudentId)) {
                            cancelEditMode();
                        } else {
                            clearInputs();
                        }
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
     * Clears input fields and resets spinner and warning indicators.
     */
    private void clearInputs() {
        final int defaultTextColor = ContextCompat.getColor(this, R.color.text_primary);

        etStudentID.setText("");
        etStudentID.setTextColor(defaultTextColor);
        etStudentID.setError(null);
        tvIdWarning.setVisibility(View.GONE);

        etStudentName.setText("");
        etStudentName.setTextColor(defaultTextColor);
        etStudentName.setError(null);
        tvNameWarning.setVisibility(View.GONE);

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
