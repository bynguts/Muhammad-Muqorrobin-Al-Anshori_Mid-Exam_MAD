package com.example.midexam;

import org.junit.Test;

import static org.junit.Assert.*;

public class StudentTest {

    @Test
    public void testStudentGettersAndSetters() {
        Student student = new Student("001202500093", "Budi Santoso", "Informatics");
        assertEquals("001202500093", student.getId());
        assertEquals("Budi Santoso", student.getName());
        assertEquals("Informatics", student.getMajor());

        student.setId("001202500094");
        student.setName("Siti Rahma");
        student.setMajor("Information Systems");

        assertEquals("001202500094", student.getId());
        assertEquals("Siti Rahma", student.getName());
        assertEquals("Information Systems", student.getMajor());
    }

    @Test
    public void testStudentEquality() {
        Student s1 = new Student("001202500093", "Budi", "Informatics");
        Student s2 = new Student("001202500093", "Budi", "Informatics");
        Student s3 = new Student("001202500099", "Budi", "Informatics");

        assertEquals(s1, s2);
        assertNotEquals(s1, s3);
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    public void testStudentToString() {
        Student s = new Student("001202500093", "John Doe", "Informatics");
        String str = s.toString();
        assertTrue(str.contains("001202500093"));
        assertTrue(str.contains("John Doe"));
        assertTrue(str.contains("Informatics"));
    }

    @Test
    public void testDatabaseConstants() {
        assertEquals("students", DatabaseHelper.TABLE_NAME);
        assertEquals("student_id", DatabaseHelper.COLUMN_ID);
        assertEquals("student_name", DatabaseHelper.COLUMN_NAME);
        assertEquals("major", DatabaseHelper.COLUMN_MAJOR);
    }
}
