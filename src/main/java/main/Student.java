package main;

// Student is a user who applies for scholarships.
// It extends (inherits from) UserAccount, so it already has the login data
// and the sign-in rules. Here we add only what is special for a student:
// the student's own information and the things a student can do.
// It keeps general student information only. Grades are inside the PDF, not here.
public class Student extends UserAccount {

    // ---------- Attributes ----------
    // They are private, so other classes must use the getters below.

    private int studentId;          // unique ID of the student (same number as the accountId)
    private String studentNumber;   // the school number, kept as text, for example "2024-0001"
    private String lname;           // last name
    private String fname;           // first name
    private String mname;           // middle name (can be empty)
    private String email;           // email, so Benefactors can contact approved applicants
    private String contactNumber;   // phone number
    private String course;          // the student's course


    // ---------- Constructor ----------

    // Purpose: create a new student.
    // The first four values are for the account part (they go to UserAccount),
    // the rest are the student's own information.
    public Student(int studentId, String username, String tempPassword, String createdOn,
                   String studentNumber, String lname, String fname, String mname,
                   String email, String contactNumber, String course) {
        // super(...) calls the constructor of UserAccount.
        // The accountId is the same number as the studentId, and the account type is "Student".
        super(studentId, username, tempPassword, "Student", createdOn);

        this.studentId = studentId;
        this.studentNumber = studentNumber;
        this.lname = lname;
        this.fname = fname;
        this.mname = mname;
        this.email = email;
        this.contactNumber = contactNumber;
        this.course = course;
    }


    // ---------- Methods that replace the abstract methods of UserAccount ----------

    // Purpose: give the name of this role.
    // This replaces (overrides) getRole() of UserAccount.
    public String getRole() {
        return "Student";
    }

    // Purpose: give the menu items that a student can use.
    // This replaces (overrides) getMenuOptions() of UserAccount.
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

    // Purpose: get the scholarships that a student is allowed to see.
    // They are approved and open, and not from a disabled Benefactor.
    // The DataStore does the filtering for us.
    public Scholarship[] browseScholarships(DataStore db) {
        return db.visibleScholarships();
    }

    // Purpose: explain why this student cannot apply to the scholarship.
    // Returns the reason as a text, or an empty text ("") if the student CAN apply.
    // The console prints this text, so the student knows what is wrong.
    public String getApplyProblem(DataStore db, Scholarship scholarship) {
        // Each check keeps its answer (true or false) in a variable with a clear name.
        // We check the problems one by one and stop at the first one we find.

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

        // 3. The student cannot have two pending applications to the same scholarship.
        boolean alreadyApplied = db.hasPendingApplication(this, scholarship);
        if (alreadyApplied) {
            return "You already have a pending application to this scholarship.";
        }

        // 4. A student who holds a scholarship cannot apply to another one.
        //    The only exception is the renewed scholarship the student already holds.
        Scholar holding = db.findBlockingScholar(this);
        boolean holdsAScholarship = (holding != null);
        if (holdsAScholarship) {
            boolean isAnotherScholarship = (holding.getScholarshipId() != scholarship.getScholarshipId());
            if (isAnotherScholarship) {
                return "You already hold a scholarship, so you cannot apply to another one.";
            }
        }

        // 5. A renewed scholarship (cycle 2 or more) accepts only its own scholars.
        boolean isRenewed = (scholarship.getCycleNo() >= 2);
        if (isRenewed) {
            Scholar mine = db.findScholar(this, scholarship);

            // The student is its scholar only if the record exists and is still Active or Probation.
            boolean isItsScholar = false;
            if (mine != null) {
                isItsScholar = mine.blocksApplying();
            }

            if (isItsScholar == false) {
                return "Only the scholars of this scholarship can apply to its renewal.";
            }
        }

        // No problem found.
        return "";
    }

    // Purpose: apply to a scholarship with one combined PDF.
    // credential is the file name of the PDF.
    // Returns the ID of the new application, or 0 if the student cannot apply.
    public int apply(DataStore db, Scholarship scholarship, String credential) {
        // The file name must not be empty.
        boolean fileNameIsEmpty = credential.trim().equals("");
        if (fileNameIsEmpty) {
            return 0;
        }

        // The student must be allowed to apply (no problem text means no problem).
        String problem = getApplyProblem(db, scholarship);
        boolean canApply = problem.equals("");
        if (canApply == false) {
            return 0;
        }

        // All checks passed, so the DataStore saves the application as Submitted.
        return db.createApplication(this, scholarship, credential);
    }

    // Purpose: cancel one of the student's own applications.
    // It works only on a pending application (Submitted, Shortlisted or Waitlisted).
    // Returns true if it was cancelled, and false if it cannot be cancelled.
    public boolean cancelApplication(Application application) {
        // A student can cancel only his or her own application.
        boolean isMyApplication = (application.getStudentId() == studentId);
        if (isMyApplication == false) {
            return false;
        }

        // The Application class decides if its status allows cancelling.
        return application.cancel();
    }


    // ---------- Viewing my own records ----------

    // Purpose: get all the applications of this student (the status and the history).
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
    // A student can have more than one scholar record over time,
    // so we collect the grants of every scholar record into one array.
    public Grant[] viewMyGrants(DataStore db) {
        Scholar[] myScholars = db.scholarsOf(this);

        // Step 1: count how many grants there are in total,
        // because an array needs its size when it is created.
        int total = 0;
        for (int i = 0; i < myScholars.length; i++) {
            Grant[] grantsOfOne = db.grantsOf(myScholars[i]);
            total = total + grantsOfOne.length;
        }

        // Step 2: create the array and copy every grant into it.
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
    // The menu shows only the student's own grants (from viewMyGrants).
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
    // Example: "Cruz, Ana Bautista". The middle name is skipped if it is empty.
    public String getFullName() {
        boolean hasNoMiddleName = mname.trim().equals("");

        if (hasNoMiddleName) {
            return lname + ", " + fname;
        }
        return lname + ", " + fname + " " + mname;
    }

    // Purpose: give a short text of the student for lists and printing.
    // Example: "2024-0001 - Cruz, Ana Bautista (BSIT)"
    // toString() replaces (overrides) the toString() that every Java object has.
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