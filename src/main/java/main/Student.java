package main;

// Student is a user who applies for scholarships.
// It extends UserAccount and adds the student's own information and the student actions.
// It keeps general student information only. Grades are inside the PDF, not here.
public class Student extends UserAccount {

    // ---------- Attributes ----------

    private int studentId;          // unique ID of the student (same number as the accountId)
    private String studentNumber;   // school number, kept as text, for example "2024-0001"
    private String lname;           // last name
    private String fname;           // first name
    private String mname;           // middle name (can be empty)
    private String course;          // the student's course


    // ---------- Constructor ----------

    // Purpose: create a new student.
    // The account values (including the email and the contact number) go to UserAccount.
    // The rest are the student's own information.
    public Student(int studentId, String username, String tempPassword, String createdOn,
                   String studentNumber, String lname, String fname, String mname,
                   String email, String contactNumber, String course) {
        super(studentId, username, tempPassword, "Student", createdOn, email, contactNumber);

        this.studentId = studentId;
        this.studentNumber = studentNumber;
        this.lname = lname;
        this.fname = fname;
        this.mname = mname;
        this.course = course;
    }


    // ---------- Methods that override UserAccount ----------

    // Purpose: give the name of this role.
    @Override
    public String getRole() {
        return "Student";
    }

    // Purpose: give the menu items that a student can use.
    @Override
    public String[] getMenuOptions() {
        String[] options = {
            "Browse scholarships",
            "My applied scholarships",
            "My scholarship",
            "My grants",
            "My profile"
        };
        return options;
    }


    // ---------- Browsing and applying ----------

    // Purpose: get the scholarships that a student can see.
    // They are approved and open, and not from a disabled Benefactor.
    public Scholarship[] browseScholarships(DataStore db) {
        return db.visibleScholarships();
    }

    // Purpose: give the reason why this student cannot apply to the scholarship.
    // Returns the reason as a text, or an empty text ("") if the student can apply.
    // It stops at the first problem found.
    public String getApplyProblem(DataStore db, Scholarship scholarship) {
        // 1. The scholarship must be approved and open.
        boolean scholarshipIsOpen = scholarship.isVisible();
        if (scholarshipIsOpen == false) {
            return "This scholarship is not open for applications.";
        }

        // 2. The deadline must not be over.
        boolean deadlineIsOver = scholarship.isDeadlinePassed(db.getToday());
        if (deadlineIsOver) {
            return "The deadline of this scholarship has passed.";
        }

        // 3. No two pending applications to the same scholarship.
        boolean alreadyApplied = db.hasPendingApplication(this, scholarship);
        if (alreadyApplied) {
            return "You already have a pending application to this scholarship.";
        }

        // 4. A student who holds a scholarship cannot apply to another one.
        //    The only exception is the renewed version (cycle 2 or more) of the held scholarship.
        boolean isRenewed = (scholarship.getCycleNo() >= 2);
        Scholar holding = db.findBlockingScholar(this);
        boolean holdsAScholarship = (holding != null);
        if (holdsAScholarship) {
            boolean isTheSameScholarship = (holding.getScholarshipId() == scholarship.getScholarshipId());
            boolean isRenewalOfTheHeldOne = isTheSameScholarship && isRenewed;
            if (isRenewalOfTheHeldOne == false) {
                return "You already hold a scholarship, so you cannot apply to another one.";
            }
        }

        // 5. A renewed scholarship accepts only its own scholars (Active or Probation).
        if (isRenewed) {
            Scholar mine = db.findScholar(this, scholarship);

            boolean isItsScholar = false;
            if (mine != null) {
                isItsScholar = mine.blocksApplying();
            }

            if (isItsScholar == false) {
                return "Only the scholars of this scholarship can apply to its renewal.";
            }
        }

        return "";
    }

    // Purpose: apply to a scholarship with one combined PDF.
    // credential is the file name of the PDF.
    // Returns the ID of the new application, or 0 if the file name is empty
    // or the student cannot apply.
    public int apply(DataStore db, Scholarship scholarship, String credential) {
        boolean fileNameIsEmpty = credential.trim().equals("");
        if (fileNameIsEmpty) {
            return 0;
        }

        String problem = getApplyProblem(db, scholarship);
        boolean canApply = problem.equals("");
        if (canApply == false) {
            return 0;
        }

        return db.createApplication(this, scholarship, credential);
    }

    // Purpose: cancel one of the student's own applications.
    // It works only on a pending application (Submitted, Shortlisted or Waitlisted).
    // Returns true if it was cancelled, and false if it cannot be cancelled.
    public boolean cancelApplication(Application application) {
        boolean isMyApplication = (application.getStudentId() == studentId);
        if (isMyApplication == false) {
            return false;
        }

        return application.cancel();
    }


    // ---------- Viewing my own records ----------

    // Purpose: get all the applications of this student (status and history).
    // This includes the Benefactor's comment on a returned application.
    public Application[] viewMyApplications(DataStore db) {
        return db.applicationsOf(this);
    }

    // Purpose: get the scholarship the student holds now (status Active or Probation).
    // Returns null if the student is not a scholar now.
    public Scholar viewMyScholarship(DataStore db) {
        return db.findBlockingScholar(this);
    }

    // Purpose: get all the grants recorded for this student.
    // The grants of every scholar record of the student are put in one array.
    public Grant[] viewMyGrants(DataStore db) {
        Scholar[] myScholars = db.scholarsOf(this);

        // Count the grants in total.
        int total = 0;
        for (int i = 0; i < myScholars.length; i++) {
            Grant[] grantsOfOne = db.grantsOf(myScholars[i]);
            total = total + grantsOfOne.length;
        }

        // Copy every grant into the result array.
        Grant[] result = new Grant[total];
        int next = 0;
        for (int i = 0; i < myScholars.length; i++) {
            Grant[] grantsOfOne = db.grantsOf(myScholars[i]);
            for (int j = 0; j < grantsOfOne.length; j++) {
                result[next] = grantsOfOne[j];
                next = next + 1;
            }
        }
        return result;
    }


    // ---------- Grants ----------

    // Purpose: the student confirms that the grant was received.
    // today is the current date (yyyy-MM-dd).
    // Returns true if it was confirmed, and false if the grant is not "Sent".
    public boolean confirmGrant(Grant grant, String today) {
        return grant.confirm(today);
    }

    // Purpose: the student says the grant was not received or is wrong.
    // Returns true if it was marked as disputed, and false if the grant is not "Sent".
    public boolean disputeGrant(Grant grant) {
        return grant.dispute();
    }


    // ---------- Name and text ----------

    // Purpose: give the name as "Last name, First name Middle name".
    // Example: "Cruz, Ana Bautista". The middle name is left out if it is empty.
    public String getFullName() {
        boolean hasNoMiddleName = mname.trim().equals("");

        if (hasNoMiddleName) {
            return lname + ", " + fname;
        }
        return lname + ", " + fname + " " + mname;
    }

    // Purpose: give a short text of the student for lists and printing.
    // Example: "2024-0001 - Cruz, Ana Bautista (BSIT)"
    @Override
    public String toString() {
        return studentNumber + " - " + getFullName() + " (" + course + ")";
    }


    // ---------- Getters ----------
    // They let other classes read the private attributes.

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

    public String getCourse() {
        return course;
    }
}