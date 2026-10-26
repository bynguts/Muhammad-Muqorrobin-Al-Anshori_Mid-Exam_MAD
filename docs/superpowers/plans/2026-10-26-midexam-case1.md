# Student Management App (Mid Exam Case 1) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a complete Android SQLite CRUD application in Java implementing Case 1 of Mid Examination IT 2025 (Student Name, Student ID [UNIQUE], Major [Informatics / Information Systems], with ADD, EDIT, DELETE, VIEW functionality).

**Architecture:** 
- `DatabaseHelper` extending `SQLiteOpenHelper` managing `students` table with `student_id` as PRIMARY KEY (enforcing uniqueness).
- `Student` model class representing entity.
- `MainActivity` handling UI components (EditText for name and ID, Spinner for major, buttons for ADD, EDIT, DELETE, VIEW) and ListView/AlertDialog for viewing records.

**Tech Stack:** Android (Java), AndroidX, Material Components, SQLite, JUnit.

**Spec:** Mid Examination IT 2025 Class 1 docx specification: Case 1 (Odd digit ID ending in 3).

## Global Constraints

- Platform: Android SDK minSdk 24, targetSdk 37, Java 11.
- Language: Java.
- Student ID MUST be unique (enforced via SQLite PRIMARY KEY and programmatic check).
- Major options: "Informatics" and "Information Systems".
- Operations required: ADD, EDIT, DELETE, VIEW.

## Review Focus

1. Duplicate Student ID on ADD: Must be rejected and notify user (unique constraint respected).
2. Empty fields on ADD/EDIT/DELETE: Must show validation warning toast instead of crashing.
3. Non-existent Student ID on EDIT/DELETE: Must handle gracefully and notify user that record was not found.
4. VIEW with no records: Must display message that no records exist rather than empty white space or crash.
5. Item selection from list: Tapping a record in the list populates the input fields for fast EDIT/DELETE.

---

### Task 1: Database Helper & Entity Model

**Files:**
- Create: `app/src/main/java/com/example/midexam/Student.java`
- Create: `app/src/main/java/com/example/midexam/DatabaseHelper.java`
- Create: `app/src/test/java/com/example/midexam/DatabaseHelperTest.java` (or local robolectric/sqlite test if applicable, or JUnit test for model & SQL contract)

**Interfaces:**
- Produces: `DatabaseHelper` with methods:
  - `boolean insertStudent(String id, String name, String major)`
  - `boolean updateStudent(String id, String name, String major)`
  - `boolean deleteStudent(String id)`
  - `boolean checkStudentExists(String id)`
  - `Cursor getAllStudents()`

- [ ] **Step 1: Create `Student.java` entity model**
- [ ] **Step 2: Create `DatabaseHelper.java` implementing SQLiteOpenHelper**
- [ ] **Step 3: Add unit tests in `app/src/test/java/com/example/midexam/` verifying contract and constraints**
- [ ] **Step 4: Run `./gradlew testDebugUnitTest` and verify success**

---

### Task 2: Layout Design (activity_main.xml & custom student row)

**Files:**
- Modify: `app/src/main/res/layout/activity_main.xml`
- Create: `app/src/main/res/layout/item_student.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values/colors.xml`

**Interfaces:**
- Produces:
  - EditText: `etStudentName` (hint: "Enter Student Name")
  - EditText: `etStudentID` (hint: "Enter Student ID")
  - Spinner: `spMajor` (options: "Informatics", "Information Systems")
  - Buttons: `btnAdd`, `btnEdit`, `btnDelete`, `btnView`
  - ListView / RecyclerView: `lvStudents` for displaying records
  - Clean card item layout `item_student.xml` with student ID, name, and major tags.

- [ ] **Step 1: Define strings and arrays (major options) in `strings.xml`**
- [ ] **Step 2: Design `activity_main.xml` matching docx image with inputs, buttons, and list**
- [ ] **Step 3: Create `item_student.xml` for student list display**

---

### Task 3: Implement MainActivity Controller & CRUD Logic

**Files:**
- Modify: `app/src/main/java/com/example/midexam/MainActivity.java`
- Create: `app/src/main/java/com/example/midexam/StudentAdapter.java`

**Interfaces:**
- Consumes: `DatabaseHelper`, `Student`
- Connects:
  - ADD button -> checks validation, checks uniqueness, inserts into SQLite, clears inputs, updates list.
  - EDIT button -> checks validation, updates student matching ID in SQLite, updates list.
  - DELETE button -> confirms and deletes student matching ID from SQLite, clears inputs, updates list.
  - VIEW button -> displays AlertDialog summary of all students and refreshes the student list.
  - Item click on list -> loads student data into form fields for quick edit/delete.

- [ ] **Step 1: Implement `StudentAdapter.java` for custom list representation**
- [ ] **Step 2: Implement input handling, Spinner adapter, and button click listeners in `MainActivity.java`**
- [ ] **Step 3: Implement validation, UNIQUE check, Toast feedback, and clear form helpers**

---

### Task 4: Verification and Build Validation

**Files:**
- Test: Build APK with `gradlew assembleDebug`
- Test: Run unit test suite with `gradlew testDebugUnitTest`

- [ ] **Step 1: Run unit tests**
- [ ] **Step 2: Compile debug APK with `gradlew assembleDebug`**
- [ ] **Step 3: Verify no compilation or runtime resource errors**
