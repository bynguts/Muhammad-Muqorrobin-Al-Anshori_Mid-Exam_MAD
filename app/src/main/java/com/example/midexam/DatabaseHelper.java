package com.example.midexam;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "StudentDB.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "students";
    public static final String COLUMN_ID = "student_id";
    public static final String COLUMN_NAME = "student_name";
    public static final String COLUMN_MAJOR = "major";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_NAME + " TEXT NOT NULL, " +
                COLUMN_MAJOR + " TEXT NOT NULL)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * Inserts a new student. Returns true if successful, false if duplicate ID or failure.
     */
    public boolean insertStudent(String id, String name, String major) {
        if (id == null || id.trim().isEmpty() || checkStudentExists(id.trim())) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, id.trim());
        values.put(COLUMN_NAME, name != null ? name.trim() : "");
        values.put(COLUMN_MAJOR, major != null ? major.trim() : "");

        long result = db.insert(TABLE_NAME, null, values);
        return result != -1;
    }

    /**
     * Checks if a student ID already exists in the database.
     */
    public boolean checkStudentExists(String id) {
        if (id == null) return false;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = ?", new String[]{id.trim()});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    /**
     * Updates an existing student record based on student_id.
     */
    public boolean updateStudent(String id, String name, String major) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name != null ? name.trim() : "");
        values.put(COLUMN_MAJOR, major != null ? major.trim() : "");

        int rows = db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{id.trim()});
        return rows > 0;
    }

    /**
     * Deletes a student record based on student_id.
     */
    public boolean deleteStudent(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{id.trim()});
        return rows > 0;
    }

    /**
     * Retrieves all student records as a Cursor.
     */
    public Cursor getAllStudents() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_ID + " ASC", null);
    }

    /**
     * Retrieves all student records as a List of Student objects.
     */
    public List<Student> getStudentList() {
        List<Student> list = new ArrayList<>();
        Cursor cursor = getAllStudents();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(COLUMN_ID);
                int nameIndex = cursor.getColumnIndex(COLUMN_NAME);
                int majorIndex = cursor.getColumnIndex(COLUMN_MAJOR);

                do {
                    String id = idIndex != -1 ? cursor.getString(idIndex) : "";
                    String name = nameIndex != -1 ? cursor.getString(nameIndex) : "";
                    String major = majorIndex != -1 ? cursor.getString(majorIndex) : "";
                    list.add(new Student(id, name, major));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return list;
    }
}
