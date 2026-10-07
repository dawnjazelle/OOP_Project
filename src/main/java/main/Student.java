public class Student extends Account {

    private int studentId;
    private String studentNumber;
    private String lname;
    private String fname;
    private String mname;
    private String email;
    private String contactNumber;
    private String course;

    // the first 5 values go up to Account, the rest belong to Student
    public Student(int accountId, String username, String tempPassword, String createdOn,
                   int studentId, String studentNumber, String lname, String fname,
                   String mname, String email, String contactNumber, String course) {
        super(accountId, username, tempPassword, "Student", createdOn);
        this.studentId = studentId;
        this.studentNumber = studentNumber;
        this.lname = lname;
        this.fname = fname;
        this.mname = mname;
        this.email = email;
        this.contactNumber = contactNumber;
        this.course = course;
    }

    // the two abstract methods of Account, now with a body: this is overriding
    @Override
    public String getRole() {
        return "Student";
    }

    @Override
    public String[] getMenuOptions() {
        String[] options = {
            "Browse and apply",
            "Search scholarships",
            "My applications",
            "My scholarship",
            "My profile"
        };
        return options;
    }

    // example: Ana R. Santos (the middle initial is skipped if there is no middle name)
    public String getFullName() {
        if (mname.equals("")) {
            return fname + " " + lname;
        }
        char initial = mname.charAt(0);
        return fname + " " + initial + ". " + lname;
    }

    @Override
    public String toString() {
        return studentNumber + " " + getFullName() + " (" + course + ")";
    }

    // getters (the UML does not draw these)
    public int getStudentId() { return studentId; }
    public String getStudentNumber() { return studentNumber; }
    public String getLname() { return lname; }
    public String getFname() { return fname; }
    public String getMname() { return mname; }
    public String getEmail() { return email; }
    public String getContactNumber() { return contactNumber; }
    public String getCourse() { return course; }
}