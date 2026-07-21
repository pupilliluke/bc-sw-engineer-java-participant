package com.academy.student;

import java.util.Scanner;

public class StudentManager {

    private static final int MAX_STUDENTS = 20;

    private final Student[] students = new Student[MAX_STUDENTS];
    private int studentCount = 0;
    private final Scanner scanner;

    public StudentManager(Scanner scanner) {
        this.scanner = scanner;
    }

    public void displayMenu() {
        System.out.println("====================================");
        System.out.println("Student Management System");
        System.out.println("====================================");
        System.out.println("1. Add Student");
        System.out.println("2. Display Students");
        System.out.println("3. Search Student");
        System.out.println("4. Average Marks");
        System.out.println("5. Top Student");
        System.out.println("6. Lowest Marks");
        System.out.println("7. Pass / Fail Report");
        System.out.println("8. Sort by Marks");
        System.out.println("9. Class Statistics");
        System.out.println("10. Exit");
        System.out.print("Enter Choice : \n\n");
    }

    // Methods addStudent, displayStudents, searchStudent, calculateAverage
    // will be filled in later steps.

    //Displays all students like this
    //----------------------------------------------------------
    //ID      Name                 Course          Marks
    //----------------------------------------------------------
    //101     John                 Java            91.00
    //----------------------------------------------------------
    public void displayStudents()
    {

        //If studentCount == 0, print No students to display.
        if(studentCount == 0){
            System.out.println("\nNo students left to display\n\n\n");
        }

        System.out.println("\n\n\nStudents List");
        System.out.println("-----------------------------------------------------");
        System.out.printf("%-8s %-20s %-15s %-8s","ID",  "Name", "Course", "Marks");
        System.out.println("\n-----------------------------------------------------");


//        Do this: Loop i from 0 to studentCount - 1 (not to MAX_STUDENTS).
        for(int i = 0; i < studentCount; i++) {
            System.out.printf("%-8d %-20s %-15s %-8.2f%n",
                    students[i].getStudentId(),
                    students[i].getName(),
                    students[i].getCourse(),
                    students[i].getMarks());
        }


        System.out.println("-----------------------------------------------------");
        System.out.println("-----------------------------------------------------\n");

    }

    public Student searchStudent()
    {
        //If no students, print No students to search.
        if (studentCount == 0)
        {
            System.out.println("No students to search");
            return null;
        }

        //Prompt for ID.
        int id =  readPositiveInt("Enter Student ID : ");

        //Find index; if missing, print Student Not Found.
        for(int i = studentCount -1; i >=0; i--) {
            if(students[i].getStudentId() == id )//hit
            {
                System.out.printf("Student Found: %d\n", id);
                students[i].display();
                return students[i]; //student returned
            }
        }//every student searched to this point, no student found

        System.out.printf("Student Not Found: %d\n", id);
        return null;
    }

    public void calculateAverage()
    {
        double sum = 0;

        if(studentCount == 0)
        {
            System.out.println("No students available");
            return;
        }

        for(int i = studentCount -1; i >=0; i--) {
            sum += students[i].getMarks();
        }

        double avgMarks = sum/studentCount;
        System.out.printf("\n\n\nAverage Marks: %.2f \n\n\n",  avgMarks);

    }

    public int hardenValidation(String input) {
        // Reject null or blank input
        if (input == null || input.trim().isEmpty()) {
            System.out.println("Invalid Input. Please Try Again.");
            return -1;
        }

        // Safe to parse, letters like "abc" land here instead of crashing
        int value;
        try {
            value = Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid Input. Please Try Again.");
            return -1;
        }

        // Range check
        if (value < 0 || value > 100) {
            System.out.println("Invalid Input. Must be between 0 and 100.");
            return -1;
        }

        return value;
    }

    //Instructions for adding a student
    public void addStudent() {

        //GUARDS!
        if (studentCount >= MAX_STUDENTS) {
            System.out.println("Student list is full!.");
            return;
        }

        int id = readPositiveInt("Student ID : ");
        if (id == -1) {
            System.out.println("Invalid ID. Must be a positive Integer");
            return;
        }

        if (findStudentIndex(id) != -1) { //id not found == -1
            System.out.println("Student ID already exists.");
            return;
        }

        String name = readNonEmptyLine("Name : ");
        if (name == null)
        {
            return;
        }

        String course = readNonEmptyLine("Course : ");
        if (course == null)
        {
            return;
        }

        double marks = readValidMarks();
        if (marks == -1) {
            return;
        }

        students[studentCount] = new Student(id, name, course, marks);
        studentCount++;
        System.out.println("Student Added Successfully.");
    }

    //Helper ideas below (recommended): readPositiveInt(),
    //                                  readNonEmptyLine(prompt),
    //                                  readValidMarks(),
    //                                  findStudentIndex(id).


    private int readPositiveInt(String prompt) {
        System.out.print(prompt);
        try {
            int value = Integer.parseInt(scanner.nextLine().trim());
            if (value <= 0) {
                System.out.println("Invalid input. Must be a positive number.");
                return -1;
            }
            return value;
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Must be a number.");
            return -1;
        }
    }

    private String readNonEmptyLine(String prompt) {
        System.out.print(prompt);
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) {
            System.out.println("Input cannot be empty.");
            return null;
        }
        return line;
    }

    private double readValidMarks() {
        System.out.print("Marks : ");
        try {
            double marks = Double.parseDouble(scanner.nextLine().trim());
                if (marks < 0 || marks > 100) {
                    System.out.println("Marks must be between 0 and 100.");
                    return -1;
            }
            return marks;
        } catch (NumberFormatException e) {
            System.out.println("Invalid marks. Must be a number.");
            return -1;
        }
    }

    private int findStudentIndex(int id) {
        for (int i = 0; i < studentCount; i++) {
            if (students[i].getStudentId() == id) {
                return i;
            }
        }//all student id's checked, none found at this point
        return -1;
    }

    //BONUS
    public void topStudent() {
        if (studentCount == 0) {
            System.out.println("No students available.");
            return;
        }
        Student top = students[0];
        for (int i = 1; i < studentCount; i++) {
            if (students[i].getMarks() > top.getMarks()) {
                top = students[i];
            }
        }
        System.out.printf("Top Student:");
        top.display();
        System.out.println("\n\n");
    }

    public void lowestMarks() {
        if (studentCount == 0) {
            System.out.println("No students available.");
            return;
        }
        Student lowest = students[0];
        for (int i = 1; i < studentCount; i++) {
            if (students[i].getMarks() < lowest.getMarks()) {
                lowest = students[i];
            }
        }
        System.out.println("Lowest Marks:");
        lowest.display();
        System.out.println("\n\n");

    }

    public void passFailReport() {
        if (studentCount == 0) {
            System.out.println("No students available.");
            return;
        }
        final double PASS_THRESHOLD = 50.0;
        System.out.println("Pass / Fail Report (threshold " + PASS_THRESHOLD + "):");
        for (int i = 0; i < studentCount; i++) {
            String result = students[i].getMarks() >= PASS_THRESHOLD ? "PASS" : "FAIL";
            System.out.printf("%-8d %-20s %-8.2f %s%n",
                    students[i].getStudentId(),
                    students[i].getName(),
                    students[i].getMarks(),
                    result);
        }
        System.out.println("\n\n");

    }

    public void sortByMarks() {
        if (studentCount == 0) {
            System.out.println("No students available.");
            return;
        }
        // Copy only the occupied portion so the original insert order is untouched
        Student[] sorted = java.util.Arrays.copyOf(students, studentCount);
        java.util.Arrays.sort(sorted, (a, b) -> Double.compare(b.getMarks(), a.getMarks()));

        System.out.println("Students by Marks (highest first):");
        for (Student s : sorted) {
            System.out.printf("%-8d %-20s %-8.2f%n",
                    s.getStudentId(), s.getName(), s.getMarks());
        }
        System.out.println("\n\n");

    }

    public void classStatistics() {
        if (studentCount == 0) {
            System.out.println("No students available.");
            return;
        }
        double high = students[0].getMarks();
        double low = students[0].getMarks();
        double sum = students[0].getMarks();
        for (int i = 1; i < studentCount; i++) {
            double m = students[i].getMarks();
            if (m > high) high = m;
            if (m < low) low = m;
            sum += m;
        }
        System.out.println("Class Statistics:");
        System.out.printf("High    : %.2f%n", high);
        System.out.printf("Low     : %.2f%n", low);
        System.out.printf("Average : %.2f%n", sum / studentCount);
        System.out.printf("Count   : %d%n", studentCount);
        System.out.println("\n\n");
    }
}