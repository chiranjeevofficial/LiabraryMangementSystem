package fms;

import BrahmasmiLiabrary.util;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.*;
import java.util.ArrayList;

public class HomePage implements ActionListener, KeyListener {
    private final Connection con;
    private Student std;
    private final JTabbedPane mainTabbedPanel = new JTabbedPane();
    private final JPanel studentPanel, studentDetailPanel;
    private final JLabel[] studentInfoLabel = new JLabel[7];
    private final JTextField[] studentInfoTextField = new JTextField[5];
    private JComboBox<String> courseComboBox;
    private ButtonGroup genderButtonGroup;
    private JRadioButton male, female;
    private final JButton submitButton = new JButton("Submit");
    private final JButton clearButton = new JButton("Clear");

    public HomePage() { // Non-Parameterized Constructor
        con = util.getConnectionWithMySQL("library","root","admin@2023");
        JFrame mainFrame = new JFrame("Home Page");
        mainTabbedPanel.setSize(700,400);
        studentPanel = new JPanel(null);
        studentDetailPanel = new JPanel(null);
        mainTabbedPanel.add("Student", studentPanel);
        mainTabbedPanel.add("Show Student", studentDetailPanel);
        initializeNewStudentFormPanel();
        //initializeShowStudentPanel();

        BrahmasmiLiabrary.util.setMainFrame(mainFrame,700,400);
        mainFrame.add(mainTabbedPanel);
    }

    void initializeShowStudentPanel() {
        ArrayList<Student> studentList = new ArrayList<>();

        // Retrieve data from the database and add to the list
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM student");
            while(rs.next()) {
                Student student = new Student(rs.getInt("studentId"), rs.getString("studentName"), rs.getString("fatherName"), rs.getString("course"), rs.getInt("age"), rs.getString("gender"), rs.getString("phoneNumber"), rs.getString("address"));
                studentList.add(student);
            }
            con.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        // Display the data in a table
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Student ID");
        model.addColumn("Student Name");
        model.addColumn("Father Name");
        model.addColumn("Course");
        model.addColumn("Age");
        model.addColumn("Gender");
        model.addColumn("Phone Number");
        model.addColumn("Address");

        for(Student student : studentList) {
            Object[] row = new Object[8];
            row[0] = student.getStudentId();
            row[1] = student.getStudentName();
            row[2] = student.getFatherName();
            row[3] = student.getCourse();
            row[4] = student.getAge();
            row[5] = student.getGender();
            row[6] = student.getPhoneNumber();
            row[7] = student.getAddress();
            model.addRow(row);
        }

        JTable table = new JTable(model);
        table.setBounds(0,0,mainTabbedPanel.getWidth(),mainTabbedPanel.getHeight());
        studentDetailPanel.add(table);
    }

    void initializeNewStudentFormPanel() {
        String[] course = {"Select the course","B.Sc IT","B.A","B.Com","B.Sc FT","B.A. Yoga","B.Sc Home Science"};
        courseComboBox = new JComboBox<>(course);
        courseComboBox.setSelectedIndex(0);
        courseComboBox.setBounds(120,71,200,27);
        genderButtonGroup = new ButtonGroup();
        male = new JRadioButton("Male");
        female = new JRadioButton("Female");
        genderButtonGroup.add(male);
        genderButtonGroup.add(female);
        male.setBounds(120,130,100,30);
        female.setBounds(220,130,100,30);
        studentPanel.add(male);
        studentPanel.add(female);

        String[] labelNames = {"Student Name", "Father Name", "Course", "Age", "Gender", "Phone Number", "Address"};
        int y = 10, textFieldY = 10;
        for (int i = 0; i < 7; i++) {
            studentInfoLabel[i] = new JLabel(labelNames[i]);
            studentInfoLabel[i].setBounds(10, y, 100, 30);
            studentPanel.add(studentInfoLabel[i]);
            if (i<5) { // overtake the ArrayIndexOutOfBoundsException
                textFieldY+=i==2?30:0;
                textFieldY+=i==3?30:0;
                studentInfoTextField[i] = new JTextField(20);
                studentInfoTextField[i].setBounds(120, textFieldY, 200, 30);
                studentPanel.add(studentInfoTextField[i]);
                textFieldY += 30;
            }
            y += studentInfoLabel[i].getHeight();
        }
        studentInfoTextField[2].addKeyListener(this);
        studentInfoTextField[3].addKeyListener(this);
        submitButton.setBounds(studentInfoTextField[4].getX(),studentInfoTextField[4].getY()+35,90,30);
        clearButton.setBounds(submitButton.getX()+110, submitButton.getY(),90,30);
        studentPanel.add(courseComboBox);
        studentPanel.add(submitButton);
        studentPanel.add(clearButton);
        submitButton.addActionListener(this);
        clearButton.addActionListener(this);
    }

    public void initializeStudentObject() {
        std = new Student();
        std.setStudentId(0);
        std.setStudentName(studentInfoTextField[0].getText());
        std.setFatherName(studentInfoTextField[1].getText());
        std.setCourse(courseComboBox.getItemAt(courseComboBox.getSelectedIndex()));
        std.setAge(Integer.parseInt(studentInfoTextField[2].getText()));
        std.setGender(getGenderSelected());
        std.setPhoneNumber(studentInfoTextField[3].getText());
        std.setAddress(studentInfoTextField[4].getText());
        System.out.println(std);
    }

    public void clearStudentForm() {
        for (int i = 0 ; i < 5; i++)
            studentInfoTextField[i].setText("");
        genderButtonGroup.clearSelection();
        courseComboBox.setSelectedIndex(0);
    }

    public char getGenderSelected() {
        return male.isSelected()?'M':'F';
    }

    public boolean studentFormValidation() {
        boolean validate = true;
        for (int i = 0; i < studentInfoTextField.length; i++) {
            if (i != 2 && (studentInfoTextField[i].getText().equals("") || studentInfoTextField[i].getText().length() < 3)) {
                validate = false;
                break;
            }
            if (i == 2 && (studentInfoTextField[i].getText().equals("") || studentInfoTextField[i].getText().length() < 1)) {
                validate = false;
                break;
            }
        }
        validate = validate && (male.isSelected() || female.isSelected());
        if (courseComboBox.getSelectedIndex() == 0)
            validate = false;
        System.out.println(validate ? "true" : "false");
        return validate;
    }

    public boolean generateStudentObjectQuery() {
        boolean validate = true;
        try {
            String query = "INSERT INTO student (studentName, fatherName, course, age, gender, phoneNumber, address) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preStmt = con.prepareStatement(query);

            // Set the values for the placeholders in the statement
            preStmt.setString(1, std.getStudentName());
            preStmt.setString(2, std.getFatherName());
            preStmt.setString(3, std.getCourse());
            preStmt.setInt(4, std.getAge());
            preStmt.setString(5, std.getGender());
            preStmt.setString(6, std.getPhoneNumber());
            preStmt.setString(7, std.getAddress());

            // Execute the statement to insert the data
            System.out.println(preStmt.executeUpdate() + " row(s) inserted.");
        } catch (SQLException e) {
            validate = false;
            System.out.println(e.getMessage());
        }
        return validate;
    }
    
    @Override
    public void actionPerformed(@NotNull ActionEvent e) {
        if (submitButton == e.getSource()) {
            if (studentFormValidation()) {
                initializeStudentObject();
                if (generateStudentObjectQuery())
                    clearStudentForm();
                else
                    System.out.println("false");
            }
            else
                JOptionPane.showMessageDialog(null, "Please Completely fill the form", "Alert", JOptionPane.WARNING_MESSAGE);
        }
        if (clearButton == e.getSource())
            clearStudentForm();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if(e.getSource() == studentInfoTextField[2]) {
            char ch = e.getKeyChar();
            if (!(Character.isDigit(ch) || ch == KeyEvent.VK_BACK_SPACE || ch == KeyEvent.VK_DELETE))
                e.consume();
            if (studentInfoTextField[2].getText().length() >= 3)
                e.consume();
        }
        if (e.getSource() == studentInfoTextField[3]) {
            char ch = e.getKeyChar();
            if (!(Character.isDigit(ch) || ch == KeyEvent.VK_BACK_SPACE || ch == KeyEvent.VK_DELETE))
                e.consume();
            if (studentInfoTextField[3].getText().length() >= 10)
                e.consume();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        
    }

    public void keyReleased(KeyEvent e) {

    }
}