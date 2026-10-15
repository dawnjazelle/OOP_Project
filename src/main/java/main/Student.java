import java.util.LinkedList;

// A student. Keeps the student info and what a student can do.
public class Student extends UserAccount {

    private int studentId;
    private String studentNumber;
    private String lname;
    private String fname;
    private String mname;
    private String email;
    private String contactNumber;
    private String course;

    // studentId is the same number as the accountId
    public Student(int studentId, String username, String password, String createdOn,
                   String studentNumber, String lname, String fname, String mname,
                   String email, String contactNumber, String course) {
        super(studentId, username, password, TYPE_STUDENT, createdOn);
        this.studentId = studentId;
        this.studentNumber = studentNumber;
        this.lname = lname;
        this.fname = fname;
        this.mname = mname;
        this.email = email;
        this.contactNumber = contactNumber;
        this.course = course;
    }

    // The role name
    @Override
    public String getRole() {
        return "Student";
    }

    // The menu of a student
    @Override
    public String[] getMenuOptions() {
        String[] options = new String[5];
        options[0] = "Browse scholarships";
        options[1] = "Applied scholarships";
        options[2] = "My scholarship";
        options[3] = "My grants";
        options[4] = "My profile";
        return options;
    }

    // Scholarships the student can see
    public LinkedList<Scholarship> browseScholarships(DataStore db) {
        return db.visibleScholarships();
    }

    // Gives the reason the student cannot apply. Gives "" if the student can apply.
    public String getApplyProblem(DataStore db, Scholarship scholarship) {
        if (scholarship == null) {
            return "Scholarship not found.";
        }
        if (!scholarship.isVisible()) {
            return "This scholarship is not open.";
        }
        if (scholarship.isDeadlinePassed(db.getToday())) {
            return "The deadline has passed.";
        }
        if (db.hasPendingApplication(this, scholarship)) {
            return "You already have a pending application to this scholarship.";
        }

        // A student who holds a scholarship cannot apply to another one
        Scholar heldScholar = db.findBlockingScholar(this);
        if (heldScholar != null && heldScholar.getScholarshipId() != scholarship.getScholarshipId()) {
            return "You already hold a scholarship.";
        }

        if (!scholarship.canAccept(db, this)) {
            return "Only the scholars of a renewed scholarship can apply to it.";
        }
        return "";
    }

    // Sends the application. Gives null if the student cannot apply.
    public Application apply(DataStore db, Scholarship scholarship, String credential) {
        if (credential == null || credential.trim().equals("")) {
            return null;
        }
        if (!getApplyProblem(db, scholarship).equals("")) {
            return null;
        }
        return db.createApplication(this, scholarship, credential.trim());
    }

    // Cancels a pending application. Gives false if it cannot be cancelled.
    public boolean cancelApplication(Application application) {
        if (application == null) {
            return false;
        }
        if (application.getStudentId() != studentId) {
            return false;
        }
        if (!application.isPending()) {
            return false;
        }
        application.cancel();
        return true;
    }

    // All applications of this student
    public LinkedList<Application> viewMyApplications(DataStore db) {
        return db.applicationsOf(this);
    }

    // The scholarship the student holds now. Gives null if none.
    public Scholar viewMyScholarship(DataStore db) {
        return db.findBlockingScholar(this);
    }

    // All grants of this student
    public LinkedList<Grant> viewMyGrants(DataStore db) {
        LinkedList<Grant> grants = new LinkedList<Grant>();
        LinkedList<Scholar> scholars = db.scholarsOf(this);
        for (int i = 0; i < scholars.size(); i++) {
            Scholar scholar = scholars.get(i);
            grants.addAll(db.grantsOf(scholar));
        }
        return grants;
    }

    // The student says the grant was received. Only a Sent grant can be confirmed.
    public boolean confirmGrant(Grant grant, String today) {
        if (grant == null) {
            return false;
        }
        if (!grant.getStatus().equals(Grant.SENT)) {
            return false;
        }
        grant.confirm(today);
        return true;
    }

    // The student says the grant was not received. Only a Sent grant can be disputed.
    public boolean disputeGrant(Grant grant) {
        if (grant == null) {
            return false;
        }
        if (!grant.getStatus().equals(Grant.SENT)) {
            return false;
        }
        grant.dispute();
        return true;
    }

    // Name as: last name, first name middle name
    public String getFullName() {
        String name = lname + ", " + fname;
        if (mname != null && !mname.trim().equals("")) {
            name = name + " " + mname;
        }
        return name;
    }

    // Text for printing a student
    @Override
    public String toString() {
        return studentNumber + " | " + getFullName() + " | " + course;
    }

    // Getters
    public int getStudentId() {
        return studentId;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public String getLname() {
        return lname;
    }

    public String getFname() {
        return fname;
    }

    public String getMname() {
        return mname;
    }

    public String getEmail() {
        return email;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public String getCourse() {
        return course;
    }
}