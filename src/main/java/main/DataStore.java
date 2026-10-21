package main;

// DataStore keeps ALL the objects of the system in arrays and stands in for the database.
// It creates new objects, finds them by their ID, and gives back lists of them.
// (Searching, filtering and counting are added in part 2.)
//
// How an array with a counter works:
// An array has a fixed size, so each array has a counter that tells how many places are filled.
// A new object goes into the place the counter points to, then the counter goes up by 1.
// Loops run up to the counter, so they never touch the empty places.
//
// The create methods return the ID of the new object, or 0 if it could not be created.
public class DataStore {

    // ---------- Attributes ----------
    // They are private, so other classes must use the methods below.

    private UserAccount[] accounts;       // every account: Students and Benefactors together
    private Student[] students;           // the Students only
    private Benefactor[] benefactors;     // the Benefactors only
    private Scholarship[] scholarships;
    private Application[] applications;
    private Scholar[] scholars;
    private Grant[] grants;

    private int accountCount;             // how many places of each array are filled
    private int studentCount;
    private int benefactorCount;
    private int scholarshipCount;
    private int applicationCount;
    private int scholarCount;
    private int grantCount;

    private int nextAccountId;            // the ID the next new object will get
    private int nextScholarshipId;        // (a Student and a Benefactor take their ID from nextAccountId,
    private int nextEditId;               //  because the accountId, studentId and benefactorId are the same number)
    private int nextApplicationId;
    private int nextScholarId;
    private int nextGrantId;

    private String today;                 // the current date, written as yyyy-MM-dd


    // ---------- Constructor ----------

    // Purpose: make an empty DataStore.
    // The arrays are empty, every counter starts at 0, and every ID starts at 1.
    // The sizes below are enough for a class project. When an array is full,
    // its create method returns 0.
    public DataStore() {
        accounts = new UserAccount[200];
        students = new Student[150];
        benefactors = new Benefactor[50];
        scholarships = new Scholarship[100];
        applications = new Application[500];
        scholars = new Scholar[300];
        grants = new Grant[500];

        accountCount = 0;
        studentCount = 0;
        benefactorCount = 0;
        scholarshipCount = 0;
        applicationCount = 0;
        scholarCount = 0;
        grantCount = 0;

        nextAccountId = 1;
        nextScholarshipId = 1;
        nextEditId = 1;
        nextApplicationId = 1;
        nextScholarId = 1;
        nextGrantId = 1;

        today = "2026-10-20";     // a starting date. Use setToday to change it.
    }


    // ---------- The current date ----------

    // Purpose: give the current date of the system, written as yyyy-MM-dd.
    // Every "today" in the system comes from here.
    public String getToday() {
        return today;
    }

    // Purpose: change the current date of the system.
    // This lets us test deadlines without waiting for real days to pass.
    // The console must ask for a real date written as yyyy-MM-dd. An empty text is ignored.
    public void setToday(String newToday) {
        boolean isEmpty = newToday.trim().equals("");
        if (isEmpty == false) {
            today = newToday.trim();
        }
    }


    // ---------- Create methods ----------
    // Each one makes a new object, puts it in its array, and returns its ID.
    // They return 0 when the array is full or a value is not valid.

    // Purpose: create a Student account together with the Student.
    // The office gives the temporary password. The middle name may be empty,
    // every other value is required.
    // Returns the ID of the new Student, or 0 if:
    // - an array is full,
    // - a required value is empty,
    // - the temporary password has less than 4 characters,
    // - the username is already used, or
    // - the student number is already used.
    public int createStudent(String username, String tempPassword, String studentNumber,
                             String lname, String fname, String mname, String email,
                             String contactNumber, String course) {
        // 1. There must be room in the arrays.
        boolean hasNoRoom = (accountCount == accounts.length) || (studentCount == students.length);
        if (hasNoRoom) {
            return 0;
        }

        // 2. The required values must not be empty.
        boolean usernameIsEmpty = username.trim().equals("");
        boolean numberIsEmpty = studentNumber.trim().equals("");
        boolean lnameIsEmpty = lname.trim().equals("");
        boolean fnameIsEmpty = fname.trim().equals("");
        boolean emailIsEmpty = email.trim().equals("");
        boolean contactIsEmpty = contactNumber.trim().equals("");
        boolean courseIsEmpty = course.trim().equals("");
        if (usernameIsEmpty || numberIsEmpty || lnameIsEmpty || fnameIsEmpty
                || emailIsEmpty || contactIsEmpty || courseIsEmpty) {
            return 0;
        }

        // 3. The temporary password must have at least 4 characters.
        int passwordLength = tempPassword.trim().length();
        boolean passwordIsLongEnough = (passwordLength >= 4);
        if (passwordIsLongEnough == false) {
            return 0;
        }

        // 4. The username must not be used by another account.
        UserAccount sameUsername = findAccountByUsername(username.trim());
        boolean usernameIsTaken = (sameUsername != null);
        if (usernameIsTaken) {
            return 0;
        }

        // 5. The student number must not be used by another student.
        for (int i = 0; i < studentCount; i++) {
            boolean numberIsTaken = students[i].getStudentNumber().equals(studentNumber.trim());
            if (numberIsTaken) {
                return 0;
            }
        }

        // All checks passed. The account ID and the student ID are the same number.
        int newId = nextAccountId;
        Student student = new Student(newId, username.trim(), tempPassword, today,
                studentNumber.trim(), lname.trim(), fname.trim(), mname.trim(),
                email.trim(), contactNumber.trim(), course.trim());

        accounts[accountCount] = student;     // the same object goes in both arrays
        accountCount = accountCount + 1;
        students[studentCount] = student;
        studentCount = studentCount + 1;

        nextAccountId = nextAccountId + 1;
        return newId;
    }

    // Purpose: create a Benefactor account together with the Benefactor.
    // The office gives the temporary password. The description may be empty,
    // every other value is required.
    // Returns the ID of the new Benefactor, or 0 if:
    // - an array is full,
    // - a required value is empty,
    // - the temporary password has less than 4 characters, or
    // - the username is already used.
    public int createBenefactor(String username, String tempPassword, String benefactorName,
                                String category, String description, String email,
                                String contactNumber, String address) {
        // 1. There must be room in the arrays.
        boolean hasNoRoom = (accountCount == accounts.length) || (benefactorCount == benefactors.length);
        if (hasNoRoom) {
            return 0;
        }

        // 2. The required values must not be empty.
        boolean usernameIsEmpty = username.trim().equals("");
        boolean nameIsEmpty = benefactorName.trim().equals("");
        boolean categoryIsEmpty = category.trim().equals("");
        boolean emailIsEmpty = email.trim().equals("");
        boolean contactIsEmpty = contactNumber.trim().equals("");
        boolean addressIsEmpty = address.trim().equals("");
        if (usernameIsEmpty || nameIsEmpty || categoryIsEmpty || emailIsEmpty
                || contactIsEmpty || addressIsEmpty) {
            return 0;
        }

        // 3. The temporary password must have at least 4 characters.
        int passwordLength = tempPassword.trim().length();
        boolean passwordIsLongEnough = (passwordLength >= 4);
        if (passwordIsLongEnough == false) {
            return 0;
        }

        // 4. The username must not be used by another account.
        UserAccount sameUsername = findAccountByUsername(username.trim());
        boolean usernameIsTaken = (sameUsername != null);
        if (usernameIsTaken) {
            return 0;
        }

        // All checks passed. The account ID and the Benefactor ID are the same number.
        int newId = nextAccountId;
        Benefactor benefactor = new Benefactor(newId, username.trim(), tempPassword, today,
                benefactorName.trim(), category.trim(), description.trim(),
                email.trim(), contactNumber.trim(), address.trim());

        accounts[accountCount] = benefactor;
        accountCount = accountCount + 1;
        benefactors[benefactorCount] = benefactor;
        benefactorCount = benefactorCount + 1;

        nextAccountId = nextAccountId + 1;
        return newId;
    }

    // Purpose: save a new scholarship of a Benefactor.
    // Benefactor.postScholarship already checked the values, so here we only need room.
    // The scholarship starts as Pending, and today is saved as the date it was posted.
    // Returns the ID of the new scholarship, or 0 if the array is full.
    public int createScholarship(Benefactor benefactor, String title, String academicYear,
                                 String qualification, String requirement, String description,
                                 int slots, double grantAmount, String deadline) {
        boolean hasNoRoom = (scholarshipCount == scholarships.length);
        if (hasNoRoom) {
            return 0;
        }

        int newId = nextScholarshipId;
        Scholarship scholarship = new Scholarship(newId, benefactor.getBenefactorId(), title,
                academicYear, qualification, requirement, description, slots,
                grantAmount, deadline, today);

        scholarships[scholarshipCount] = scholarship;
        scholarshipCount = scholarshipCount + 1;
        nextScholarshipId = nextScholarshipId + 1;
        return newId;
    }

    // Purpose: save one change of a scholarship in its edit history.
    // The scholarship itself keeps the record (it owns its edit history array),
    // so DataStore only makes the record and gives it to the scholarship.
    // Returns the ID of the new edit record.
    public int createEditHistory(Scholarship scholarship, String field,
                                 String oldValue, String newValue) {
        int newId = nextEditId;
        ScholarshipEditHistory edit = new ScholarshipEditHistory(newId,
                scholarship.getScholarshipId(), field, oldValue, newValue, today);

        scholarship.addEdit(edit);
        nextEditId = nextEditId + 1;
        return newId;
    }

    // Purpose: save a new application of a student.
    // Student.apply already checked the rules.
    // The application starts as Submitted with the current cycle number of the scholarship,
    // and today is saved as the submission date.
    // Returns the ID of the new application, or 0 if the array is full.
    public int createApplication(Student student, Scholarship scholarship, String credential) {
        boolean hasNoRoom = (applicationCount == applications.length);
        if (hasNoRoom) {
            return 0;
        }

        int newId = nextApplicationId;
        Application application = new Application(newId, student.getStudentId(),
                scholarship.getScholarshipId(), credential, scholarship.getCycleNo(), today);

        applications[applicationCount] = application;
        applicationCount = applicationCount + 1;
        nextApplicationId = nextApplicationId + 1;
        return newId;
    }

    // Purpose: save a new scholar record for an approved application.
    // The scholar starts as Active, and today is saved as the start date.
    // Returns the ID of the new scholar record, or 0 if the array is full.
    public int createScholar(Application application) {
        boolean hasNoRoom = (scholarCount == scholars.length);
        if (hasNoRoom) {
            return 0;
        }

        int newId = nextScholarId;
        Scholar scholar = new Scholar(newId, application.getStudentId(),
                application.getScholarshipId(), application.getApplicationId(), today);

        scholars[scholarCount] = scholar;
        scholarCount = scholarCount + 1;
        nextScholarId = nextScholarId + 1;
        return newId;
    }

    // Purpose: save a new grant that a Benefactor sent to a scholar.
    // Benefactor.recordGrant already checked the values.
    // The grant starts as Sent, and today is saved as the date sent.
    // Returns the ID of the new grant, or 0 if the array is full.
    public int createGrant(Scholar scholar, Benefactor benefactor, double amount,
                           String method, String referenceNo) {
        boolean hasNoRoom = (grantCount == grants.length);
        if (hasNoRoom) {
            return 0;
        }

        int newId = nextGrantId;
        Grant grant = new Grant(newId, scholar.getScholarId(), benefactor.getBenefactorId(),
                amount, method, today, referenceNo);

        grants[grantCount] = grant;
        grantCount = grantCount + 1;
        nextGrantId = nextGrantId + 1;
        return newId;
    }


    // ---------- Find one object ----------
    // Each one looks through the filled places of an array.
    // They return the object, or null if there is none.

    // Purpose: find an account by its username (used when a user signs in).
    // The account can be a Student or a Benefactor, so we return the common type UserAccount.
    public UserAccount findAccountByUsername(String username) {
        for (int i = 0; i < accountCount; i++) {
            if (accounts[i].getUsername().equals(username)) {
                return accounts[i];
            }
        }
        return null;
    }

    // Purpose: find a Student by the student ID (not the student number).
    public Student findStudentById(int id) {
        for (int i = 0; i < studentCount; i++) {
            if (students[i].getStudentId() == id) {
                return students[i];
            }
        }
        return null;
    }

    // Purpose: find a Benefactor by the Benefactor ID.
    public Benefactor findBenefactorById(int id) {
        for (int i = 0; i < benefactorCount; i++) {
            if (benefactors[i].getBenefactorId() == id) {
                return benefactors[i];
            }
        }
        return null;
    }

    // Purpose: find a scholarship by its ID.
    public Scholarship findScholarshipById(int id) {
        for (int i = 0; i < scholarshipCount; i++) {
            if (scholarships[i].getScholarshipId() == id) {
                return scholarships[i];
            }
        }
        return null;
    }

    // Purpose: find an application by its ID.
    public Application findApplicationById(int id) {
        for (int i = 0; i < applicationCount; i++) {
            if (applications[i].getApplicationId() == id) {
                return applications[i];
            }
        }
        return null;
    }

    // Purpose: find a scholar record by its ID.
    public Scholar findScholarById(int id) {
        for (int i = 0; i < scholarCount; i++) {
            if (scholars[i].getScholarId() == id) {
                return scholars[i];
            }
        }
        return null;
    }

    // Purpose: find a grant by its ID.
    public Grant findGrantById(int id) {
        for (int i = 0; i < grantCount; i++) {
            if (grants[i].getGrantId() == id) {
                return grants[i];
            }
        }
        return null;
    }

    // Purpose: find the scholar record of a student in one scholarship (with any status).
    // Returns null if the student never became a scholar of that scholarship.
    public Scholar findScholar(Student student, Scholarship scholarship) {
        for (int i = 0; i < scholarCount; i++) {
            boolean sameStudent = (scholars[i].getStudentId() == student.getStudentId());
            boolean sameScholarship = (scholars[i].getScholarshipId() == scholarship.getScholarshipId());
            if (sameStudent && sameScholarship) {
                return scholars[i];
            }
        }
        return null;
    }

    // Purpose: find the scholarship that the student holds now (status Active or Probation).
    // A student who holds one cannot apply for another.
    // Returns null if the student does not hold any scholarship now.
    public Scholar findBlockingScholar(Student student) {
        for (int i = 0; i < scholarCount; i++) {
            boolean sameStudent = (scholars[i].getStudentId() == student.getStudentId());
            boolean holdsIt = scholars[i].blocksApplying();
            if (sameStudent && holdsIt) {
                return scholars[i];
            }
        }
        return null;
    }

    // Purpose: tell if the student already has a pending application to this scholarship.
    // Pending means Submitted, Shortlisted or Waitlisted.
    public boolean hasPendingApplication(Student student, Scholarship scholarship) {
        for (int i = 0; i < applicationCount; i++) {
            boolean sameStudent = (applications[i].getStudentId() == student.getStudentId());
            boolean sameScholarship = (applications[i].getScholarshipId() == scholarship.getScholarshipId());
            boolean isPending = applications[i].isPending();
            if (sameStudent && sameScholarship && isPending) {
                return true;
            }
        }
        return false;
    }


    // ---------- Lists ----------
    // Each list method works in two steps, because an array needs its size when it is created:
    // Step 1: count how many objects match.
    // Step 2: make an array of exactly that size and copy the matching objects into it.
    // So the array we return has no empty places.

    // Purpose: the scholarships that students are allowed to see.
    // They must be approved and open, and their Benefactor must not be disabled.
    public Scholarship[] visibleScholarships() {
        // Step 1: count.
        int total = 0;
        for (int i = 0; i < scholarshipCount; i++) {
            Benefactor owner = findBenefactorById(scholarships[i].getBenefactorId());
            boolean ownerIsActive = false;
            if (owner != null) {
                ownerIsActive = owner.isActive();
            }
            if (scholarships[i].isVisible() && ownerIsActive) {
                total = total + 1;
            }
        }

        // Step 2: copy.
        Scholarship[] result = new Scholarship[total];
        int next = 0;
        for (int i = 0; i < scholarshipCount; i++) {
            Benefactor owner = findBenefactorById(scholarships[i].getBenefactorId());
            boolean ownerIsActive = false;
            if (owner != null) {
                ownerIsActive = owner.isActive();
            }
            if (scholarships[i].isVisible() && ownerIsActive) {
                result[next] = scholarships[i];
                next = next + 1;
            }
        }
        return result;
    }

    // Purpose: the scholarships that wait for the office to approve or reject them.
    public Scholarship[] pendingScholarships() {
        int total = 0;
        for (int i = 0; i < scholarshipCount; i++) {
            if (scholarships[i].getApprovalStatus().equals("Pending")) {
                total = total + 1;
            }
        }

        Scholarship[] result = new Scholarship[total];
        int next = 0;
        for (int i = 0; i < scholarshipCount; i++) {
            if (scholarships[i].getApprovalStatus().equals("Pending")) {
                result[next] = scholarships[i];
                next = next + 1;
            }
        }
        return result;
    }

    // Purpose: the scholarships posted by one Benefactor (with any status).
    public Scholarship[] scholarshipsOf(Benefactor benefactor) {
        int total = 0;
        for (int i = 0; i < scholarshipCount; i++) {
            if (scholarships[i].getBenefactorId() == benefactor.getBenefactorId()) {
                total = total + 1;
            }
        }

        Scholarship[] result = new Scholarship[total];
        int next = 0;
        for (int i = 0; i < scholarshipCount; i++) {
            if (scholarships[i].getBenefactorId() == benefactor.getBenefactorId()) {
                result[next] = scholarships[i];
                next = next + 1;
            }
        }
        return result;
    }

    // Purpose: all the applications of one student (the status and the history).
    public Application[] applicationsOf(Student student) {
        int total = 0;
        for (int i = 0; i < applicationCount; i++) {
            if (applications[i].getStudentId() == student.getStudentId()) {
                total = total + 1;
            }
        }

        Application[] result = new Application[total];
        int next = 0;
        for (int i = 0; i < applicationCount; i++) {
            if (applications[i].getStudentId() == student.getStudentId()) {
                result[next] = applications[i];
                next = next + 1;
            }
        }
        return result;
    }

    // Purpose: all the applications sent to one scholarship (with any status).
    public Application[] applicationsForScholarship(Scholarship scholarship) {
        int total = 0;
        for (int i = 0; i < applicationCount; i++) {
            if (applications[i].getScholarshipId() == scholarship.getScholarshipId()) {
                total = total + 1;
            }
        }

        Application[] result = new Application[total];
        int next = 0;
        for (int i = 0; i < applicationCount; i++) {
            if (applications[i].getScholarshipId() == scholarship.getScholarshipId()) {
                result[next] = applications[i];
                next = next + 1;
            }
        }
        return result;
    }

    // Purpose: all the scholar records of one student over time (with any status).
    public Scholar[] scholarsOf(Student student) {
        int total = 0;
        for (int i = 0; i < scholarCount; i++) {
            if (scholars[i].getStudentId() == student.getStudentId()) {
                total = total + 1;
            }
        }

        Scholar[] result = new Scholar[total];
        int next = 0;
        for (int i = 0; i < scholarCount; i++) {
            if (scholars[i].getStudentId() == student.getStudentId()) {
                result[next] = scholars[i];
                next = next + 1;
            }
        }
        return result;
    }

    // Purpose: all the scholars of one scholarship (with any status).
    public Scholar[] scholarsOfScholarship(Scholarship scholarship) {
        int total = 0;
        for (int i = 0; i < scholarCount; i++) {
            if (scholars[i].getScholarshipId() == scholarship.getScholarshipId()) {
                total = total + 1;
            }
        }

        Scholar[] result = new Scholar[total];
        int next = 0;
        for (int i = 0; i < scholarCount; i++) {
            if (scholars[i].getScholarshipId() == scholarship.getScholarshipId()) {
                result[next] = scholars[i];
                next = next + 1;
            }
        }
        return result;
    }

    // Purpose: all the grants recorded for one scholar record (with any status).
    public Grant[] grantsOf(Scholar scholar) {
        int total = 0;
        for (int i = 0; i < grantCount; i++) {
            if (grants[i].getScholarId() == scholar.getScholarId()) {
                total = total + 1;
            }
        }

        Grant[] result = new Grant[total];
        int next = 0;
        for (int i = 0; i < grantCount; i++) {
            if (grants[i].getScholarId() == scholar.getScholarId()) {
                result[next] = grants[i];
                next = next + 1;
            }
        }
        return result;
    }


    // ---------- Slots ----------

    // Purpose: count the approved applications of one scholarship in one cycle.
    // Slots are counted per cycle, and only approved applications use a slot,
    // so a shortlisted or waitlisted application does not.
    // Benefactor uses this to check if a slot is left before approving.
    public int countApprovedInCycle(Scholarship scholarship, int cycleNo) {
        int total = 0;
        for (int i = 0; i < applicationCount; i++) {
            boolean sameScholarship = (applications[i].getScholarshipId() == scholarship.getScholarshipId());
            boolean sameCycle = (applications[i].getCycleNo() == cycleNo);
            boolean isApproved = applications[i].getStatus().equals("Approved");
            if (sameScholarship && sameCycle && isApproved) {
                total = total + 1;
            }
        }
        return total;
    }


   // ---------- Searching and filtering ----------
    // These methods get an array (a list), and give back a NEW array with only the matching objects.
    // They never change the objects. Searching is not case sensitive ("ana" finds "Ana").
    // A filter value that is left empty means "any".
    //
    // How they work: first we go through the list and write true or false in a boolean array
    // (true = this object matches), and we count the matches.
    // Then we make an array of exactly that size and copy the matching objects into it.
 
    // Purpose: search the scholarships that students can see, by title or Benefactor name.
    // An empty keyword gives all of them.
    public Scholarship[] searchScholarships(String keyword) {
        Scholarship[] visible = visibleScholarships();
        String word = keyword.trim().toLowerCase();
 
        // Step 1: mark the matches and count them.
        boolean[] matches = new boolean[visible.length];
        int total = 0;
        for (int i = 0; i < visible.length; i++) {
            String title = visible[i].getTitle().toLowerCase();
 
            String ownerName = "";
            Benefactor owner = findBenefactorById(visible[i].getBenefactorId());
            if (owner != null) {
                ownerName = owner.getBenefactorName().toLowerCase();
            }
 
            boolean titleMatches = title.contains(word);
            boolean ownerMatches = ownerName.contains(word);
            if (titleMatches || ownerMatches) {
                matches[i] = true;
                total = total + 1;
            }
        }
 
        // Step 2: copy the matches.
        Scholarship[] result = new Scholarship[total];
        int next = 0;
        for (int i = 0; i < visible.length; i++) {
            if (matches[i]) {
                result[next] = visible[i];
                next = next + 1;
            }
        }
        return result;
    }
 
    // Purpose: filter an array of scholarships by the category of the Benefactor,
    // the academic year, or the deadline. A value left empty means "any".
    // deadline works like "closing on or before": it keeps the scholarships whose deadline
    // is on that date or earlier. It is written as yyyy-MM-dd.
    public Scholarship[] filterScholarships(Scholarship[] list, String category,
                                            String year, String deadline) {
        String wantedCategory = category.trim();
        String wantedYear = year.trim();
        String wantedDeadline = deadline.trim();
 
        boolean[] matches = new boolean[list.length];
        int total = 0;
        for (int i = 0; i < list.length; i++) {
            // The category is kept by the Benefactor, so we look the Benefactor up.
            String ownerCategory = "";
            Benefactor owner = findBenefactorById(list[i].getBenefactorId());
            if (owner != null) {
                ownerCategory = owner.getCategory();
            }
 
            boolean anyCategory = wantedCategory.equals("");
            boolean categoryMatches = anyCategory || ownerCategory.equalsIgnoreCase(wantedCategory);
 
            boolean anyYear = wantedYear.equals("");
            boolean yearMatches = anyYear || list[i].getAcademicYear().equalsIgnoreCase(wantedYear);
 
            boolean anyDeadline = wantedDeadline.equals("");
            boolean deadlineMatches = anyDeadline || list[i].getDeadline().compareTo(wantedDeadline) <= 0;
 
            if (categoryMatches && yearMatches && deadlineMatches) {
                matches[i] = true;
                total = total + 1;
            }
        }
 
        Scholarship[] result = new Scholarship[total];
        int next = 0;
        for (int i = 0; i < list.length; i++) {
            if (matches[i]) {
                result[next] = list[i];
                next = next + 1;
            }
        }
        return result;
    }
 
    // Purpose: filter an array of applications by status. An empty status gives all of them.
    public Application[] filterApplicationsByStatus(Application[] list, String status) {
        String wantedStatus = status.trim();
 
        boolean[] matches = new boolean[list.length];
        int total = 0;
        for (int i = 0; i < list.length; i++) {
            boolean anyStatus = wantedStatus.equals("");
            boolean statusMatches = anyStatus || list[i].getStatus().equalsIgnoreCase(wantedStatus);
            if (statusMatches) {
                matches[i] = true;
                total = total + 1;
            }
        }
 
        Application[] result = new Application[total];
        int next = 0;
        for (int i = 0; i < list.length; i++) {
            if (matches[i]) {
                result[next] = list[i];
                next = next + 1;
            }
        }
        return result;
    }
 
    // Purpose: search an array of applications by the student's name or student number.
    // An application keeps only the student ID, so we look the student up.
    // The name can be typed as "Ana Cruz" or as "Cruz Ana". An empty keyword gives all of them.
    public Application[] searchApplications(Application[] list, String keyword) {
        String word = keyword.trim().toLowerCase();
 
        boolean[] matches = new boolean[list.length];
        int total = 0;
        for (int i = 0; i < list.length; i++) {
            Student student = findStudentById(list[i].getStudentId());
 
            boolean found = false;
            if (student != null) {
                // One text with the name in both orders and the student number.
                String text = (student.getFname() + " " + student.getLname() + " "
                        + student.getLname() + " " + student.getFname() + " "
                        + student.getStudentNumber()).toLowerCase();
                found = text.contains(word);
            }
 
            if (found) {
                matches[i] = true;
                total = total + 1;
            }
        }
 
        Application[] result = new Application[total];
        int next = 0;
        for (int i = 0; i < list.length; i++) {
            if (matches[i]) {
                result[next] = list[i];
                next = next + 1;
            }
        }
        return result;
    }
 
    // Purpose: search an array of scholars by the student's name or student number.
    // It works like searchApplications.
    public Scholar[] searchScholars(Scholar[] list, String keyword) {
        String word = keyword.trim().toLowerCase();
 
        boolean[] matches = new boolean[list.length];
        int total = 0;
        for (int i = 0; i < list.length; i++) {
            Student student = findStudentById(list[i].getStudentId());
 
            boolean found = false;
            if (student != null) {
                String text = (student.getFname() + " " + student.getLname() + " "
                        + student.getLname() + " " + student.getFname() + " "
                        + student.getStudentNumber()).toLowerCase();
                found = text.contains(word);
            }
 
            if (found) {
                matches[i] = true;
                total = total + 1;
            }
        }
 
        Scholar[] result = new Scholar[total];
        int next = 0;
        for (int i = 0; i < list.length; i++) {
            if (matches[i]) {
                result[next] = list[i];
                next = next + 1;
            }
        }
        return result;
    }
 
    // Purpose: filter an array of scholars by status. An empty status gives all of them.
    public Scholar[] filterScholarsByStatus(Scholar[] list, String status) {
        String wantedStatus = status.trim();
 
        boolean[] matches = new boolean[list.length];
        int total = 0;
        for (int i = 0; i < list.length; i++) {
            boolean anyStatus = wantedStatus.equals("");
            boolean statusMatches = anyStatus || list[i].getStatus().equalsIgnoreCase(wantedStatus);
            if (statusMatches) {
                matches[i] = true;
                total = total + 1;
            }
        }
 
        Scholar[] result = new Scholar[total];
        int next = 0;
        for (int i = 0; i < list.length; i++) {
            if (matches[i]) {
                result[next] = list[i];
                next = next + 1;
            }
        }
        return result;
    }
 
 
    // ---------- Counting: the Benefactor summary ----------
 
    // Purpose: count the applications of one scholarship with a given status.
    public int countApplicationsByStatus(Scholarship scholarship, String status) {
        int total = 0;
        for (int i = 0; i < applicationCount; i++) {
            boolean sameScholarship = (applications[i].getScholarshipId() == scholarship.getScholarshipId());
            boolean sameStatus = applications[i].getStatus().equalsIgnoreCase(status.trim());
            if (sameScholarship && sameStatus) {
                total = total + 1;
            }
        }
        return total;
    }
 
    // Purpose: count the scholars of one scholarship with a given status.
    public int countScholarsByStatus(Scholarship scholarship, String status) {
        int total = 0;
        for (int i = 0; i < scholarCount; i++) {
            boolean sameScholarship = (scholars[i].getScholarshipId() == scholarship.getScholarshipId());
            boolean sameStatus = scholars[i].getStatus().equalsIgnoreCase(status.trim());
            if (sameScholarship && sameStatus) {
                total = total + 1;
            }
        }
        return total;
    }
 
    // Purpose: count the grants of one Benefactor with a given status.
    public int countGrantsByStatus(Benefactor benefactor, String status) {
        int total = 0;
        for (int i = 0; i < grantCount; i++) {
            boolean sameBenefactor = (grants[i].getBenefactorId() == benefactor.getBenefactorId());
            boolean sameStatus = grants[i].getStatus().equalsIgnoreCase(status.trim());
            if (sameBenefactor && sameStatus) {
                total = total + 1;
            }
        }
        return total;
    }
 
    // Purpose: add up the amounts of the grants of one Benefactor with a given status.
    public double totalGrantAmountByStatus(Benefactor benefactor, String status) {
        double total = 0;
        for (int i = 0; i < grantCount; i++) {
            boolean sameBenefactor = (grants[i].getBenefactorId() == benefactor.getBenefactorId());
            boolean sameStatus = grants[i].getStatus().equalsIgnoreCase(status.trim());
            if (sameBenefactor && sameStatus) {
                total = total + grants[i].getAmount();
            }
        }
        return total;
    }
 
 
    // ---------- Counting: the Office summary (all Benefactors) ----------
    // These only read the records. They never change them.
 
    // Purpose: count all scholarships with a given approval status (Pending, Approved or Rejected).
    public int countAllScholarshipsByApproval(String approvalStatus) {
        int total = 0;
        for (int i = 0; i < scholarshipCount; i++) {
            if (scholarships[i].getApprovalStatus().equalsIgnoreCase(approvalStatus.trim())) {
                total = total + 1;
            }
        }
        return total;
    }
 
    // Purpose: count all applications with a given status.
    public int countAllApplicationsByStatus(String status) {
        int total = 0;
        for (int i = 0; i < applicationCount; i++) {
            if (applications[i].getStatus().equalsIgnoreCase(status.trim())) {
                total = total + 1;
            }
        }
        return total;
    }
 
    // Purpose: count all scholars with a given status.
    public int countAllScholarsByStatus(String status) {
        int total = 0;
        for (int i = 0; i < scholarCount; i++) {
            if (scholars[i].getStatus().equalsIgnoreCase(status.trim())) {
                total = total + 1;
            }
        }
        return total;
    }
 
    // Purpose: count all grants with a given status.
    public int countAllGrantsByStatus(String status) {
        int total = 0;
        for (int i = 0; i < grantCount; i++) {
            if (grants[i].getStatus().equalsIgnoreCase(status.trim())) {
                total = total + 1;
            }
        }
        return total;
    }
 
 
    // ---------- Office read-only views ----------
    // The office can look at ALL records, filtered by Benefactor, scholarship, student or status.
    // A value left empty means "any": use null for the Benefactor, scholarship and student,
    // and an empty text for the status.
 
    // Purpose: give the applications that match the filters.
    public Application[] filterAllApplications(Benefactor benefactor, Scholarship scholarship,
                                               Student student, String status) {
        String wantedStatus = status.trim();
 
        boolean[] matches = new boolean[applicationCount];
        int total = 0;
        for (int i = 0; i < applicationCount; i++) {
            // An application belongs to a scholarship, and the scholarship to a Benefactor.
            Scholarship ofApplication = findScholarshipById(applications[i].getScholarshipId());
 
            boolean anyBenefactor = (benefactor == null);
            boolean benefactorMatches = anyBenefactor;
            if (anyBenefactor == false && ofApplication != null) {
                benefactorMatches = (ofApplication.getBenefactorId() == benefactor.getBenefactorId());
            }
 
            boolean anyScholarship = (scholarship == null);
            boolean scholarshipMatches = anyScholarship;
            if (anyScholarship == false) {
                scholarshipMatches = (applications[i].getScholarshipId() == scholarship.getScholarshipId());
            }
 
            boolean anyStudent = (student == null);
            boolean studentMatches = anyStudent;
            if (anyStudent == false) {
                studentMatches = (applications[i].getStudentId() == student.getStudentId());
            }
 
            boolean anyStatus = wantedStatus.equals("");
            boolean statusMatches = anyStatus || applications[i].getStatus().equalsIgnoreCase(wantedStatus);
 
            if (benefactorMatches && scholarshipMatches && studentMatches && statusMatches) {
                matches[i] = true;
                total = total + 1;
            }
        }
 
        Application[] result = new Application[total];
        int next = 0;
        for (int i = 0; i < applicationCount; i++) {
            if (matches[i]) {
                result[next] = applications[i];
                next = next + 1;
            }
        }
        return result;
    }
 
    // Purpose: give the scholars that match the filters. It works like filterAllApplications.
    public Scholar[] filterAllScholars(Benefactor benefactor, Scholarship scholarship,
                                       Student student, String status) {
        String wantedStatus = status.trim();
 
        boolean[] matches = new boolean[scholarCount];
        int total = 0;
        for (int i = 0; i < scholarCount; i++) {
            Scholarship ofScholar = findScholarshipById(scholars[i].getScholarshipId());
 
            boolean anyBenefactor = (benefactor == null);
            boolean benefactorMatches = anyBenefactor;
            if (anyBenefactor == false && ofScholar != null) {
                benefactorMatches = (ofScholar.getBenefactorId() == benefactor.getBenefactorId());
            }
 
            boolean anyScholarship = (scholarship == null);
            boolean scholarshipMatches = anyScholarship;
            if (anyScholarship == false) {
                scholarshipMatches = (scholars[i].getScholarshipId() == scholarship.getScholarshipId());
            }
 
            boolean anyStudent = (student == null);
            boolean studentMatches = anyStudent;
            if (anyStudent == false) {
                studentMatches = (scholars[i].getStudentId() == student.getStudentId());
            }
 
            boolean anyStatus = wantedStatus.equals("");
            boolean statusMatches = anyStatus || scholars[i].getStatus().equalsIgnoreCase(wantedStatus);
 
            if (benefactorMatches && scholarshipMatches && studentMatches && statusMatches) {
                matches[i] = true;
                total = total + 1;
            }
        }
 
        Scholar[] result = new Scholar[total];
        int next = 0;
        for (int i = 0; i < scholarCount; i++) {
            if (matches[i]) {
                result[next] = scholars[i];
                next = next + 1;
            }
        }
        return result;
    }
 
    // Purpose: give the grants that match the filters.
    // A grant keeps the Benefactor ID, and the scholar record tells the scholarship and the student.
    public Grant[] filterAllGrants(Benefactor benefactor, Scholarship scholarship,
                                   Student student, String status) {
        String wantedStatus = status.trim();
 
        boolean[] matches = new boolean[grantCount];
        int total = 0;
        for (int i = 0; i < grantCount; i++) {
            Scholar ofGrant = findScholarById(grants[i].getScholarId());
 
            boolean anyBenefactor = (benefactor == null);
            boolean benefactorMatches = anyBenefactor;
            if (anyBenefactor == false) {
                benefactorMatches = (grants[i].getBenefactorId() == benefactor.getBenefactorId());
            }
 
            boolean anyScholarship = (scholarship == null);
            boolean scholarshipMatches = anyScholarship;
            if (anyScholarship == false && ofGrant != null) {
                scholarshipMatches = (ofGrant.getScholarshipId() == scholarship.getScholarshipId());
            } else if (anyScholarship == false) {
                scholarshipMatches = false;
            }
 
            boolean anyStudent = (student == null);
            boolean studentMatches = anyStudent;
            if (anyStudent == false && ofGrant != null) {
                studentMatches = (ofGrant.getStudentId() == student.getStudentId());
            } else if (anyStudent == false) {
                studentMatches = false;
            }
 
            boolean anyStatus = wantedStatus.equals("");
            boolean statusMatches = anyStatus || grants[i].getStatus().equalsIgnoreCase(wantedStatus);
 
            if (benefactorMatches && scholarshipMatches && studentMatches && statusMatches) {
                matches[i] = true;
                total = total + 1;
            }
        }
 
        Grant[] result = new Grant[total];
        int next = 0;
        for (int i = 0; i < grantCount; i++) {
            if (matches[i]) {
                result[next] = grants[i];
                next = next + 1;
            }
        }
        return result;
    }
 
 
    // ---------- The lapsed check ----------
 
    // Purpose: after the deadline of a RENEWED scholarship, mark every scholar who has not applied
    // for the renewal as Lapsed. A lapsed scholar is free to apply to other scholarships.
    // today is the current date, written as yyyy-MM-dd.
    // It does something only when the scholarship is renewed (cycle 2 or more), is live
    // (approved and open, with no new renewal waiting for the office), and its deadline is over.
    // A scholar "has applied" when there is an application of that student to this scholarship
    // for the current cycle (with any status).
    // Returns how many scholars were marked as Lapsed.
    public int markLapsedScholars(Scholarship scholarship, String today) {
        // 1. Only a renewed scholarship has a renewal deadline.
        boolean isRenewed = (scholarship.getCycleNo() >= 2);
        if (isRenewed == false) {
            return 0;
        }
 
        // 2. It must be live: approved and open, and not waiting for another renewal.
        boolean isLive = scholarship.isVisible() && (scholarship.isRenewalPending() == false);
        if (isLive == false) {
            return 0;
        }
 
        // 3. The deadline must be over.
        boolean deadlineIsOver = scholarship.isDeadlinePassed(today);
        if (deadlineIsOver == false) {
            return 0;
        }
 
        // 4. Check every scholar of this scholarship.
        int marked = 0;
        for (int i = 0; i < scholarCount; i++) {
            boolean ofThisScholarship = (scholars[i].getScholarshipId() == scholarship.getScholarshipId());
 
            if (ofThisScholarship) {
                // Did this scholar apply in the current cycle?
                boolean hasApplied = false;
                for (int j = 0; j < applicationCount; j++) {
                    boolean sameStudent = (applications[j].getStudentId() == scholars[i].getStudentId());
                    boolean sameScholarship = (applications[j].getScholarshipId() == scholarship.getScholarshipId());
                    boolean sameCycle = (applications[j].getCycleNo() == scholarship.getCycleNo());
                    if (sameStudent && sameScholarship && sameCycle) {
                        hasApplied = true;
                    }
                }
 
                // Scholar.markLapsed works only on an Active or Probation scholar.
                if (hasApplied == false) {
                    boolean wasMarked = scholars[i].markLapsed(today);
                    if (wasMarked) {
                        marked = marked + 1;
                    }
                }
            }
        }
        return marked;
    }
 
 
    // ---------- Sample data ----------
 
    // Purpose: fill the DataStore with sample accounts, scholarships, applications,
    // scholars and grants, so the system can be tried without typing everything.
    // Every sample account has the password "temp1234".
    // Sign in as: dost, smfoundation or alumni (Benefactors),
    // or ana, ben, cara, dan, ella or fred (Students).
    // The scholarship deadlines are in December 2026 and January 2027,
    // so load the sample data while the date of the system is earlier than that.
    public void loadSampleData() {
        // --- Benefactors ---
        int dostId = createBenefactor("dost", "temp1234", "DOST Scholarship Office", "government",
                "National scholarships for science and technology students.",
                "dost@example.com", "09171230001", "Bicutan, Taguig");
        int smId = createBenefactor("smfoundation", "temp1234", "SM Foundation", "foundation",
                "College scholarships for students from low income families.",
                "sm@example.com", "09171230002", "Pasay City");
        int alumniId = createBenefactor("alumni", "temp1234", "BulSU Alumni Association", "alumni",
                "Support for BulSU students from the alumni.",
                "alumni@example.com", "09171230003", "Malolos, Bulacan");
 
        Benefactor dost = findBenefactorById(dostId);
        Benefactor sm = findBenefactorById(smId);
        Benefactor alumni = findBenefactorById(alumniId);
 
        // --- Students ---
        int anaId = createStudent("ana", "temp1234", "2024-0001", "Cruz", "Ana", "Bautista",
                "ana@example.com", "09181230001", "BSIT");
        int benId = createStudent("ben", "temp1234", "2024-0002", "Reyes", "Ben", "",
                "ben@example.com", "09181230002", "BSIT");
        int caraId = createStudent("cara", "temp1234", "2024-0003", "Santos", "Cara", "Lopez",
                "cara@example.com", "09181230003", "BSCS");
        int danId = createStudent("dan", "temp1234", "2024-0004", "Dela Cruz", "Dan", "",
                "dan@example.com", "09181230004", "BSIT");
        int ellaId = createStudent("ella", "temp1234", "2024-0005", "Garcia", "Ella", "Mae",
                "ella@example.com", "09181230005", "BSED");
        int fredId = createStudent("fred", "temp1234", "2024-0006", "Villanueva", "Fred", "",
                "fred@example.com", "09181230006", "BSCS");
 
        Student ana = findStudentById(anaId);
        Student ben = findStudentById(benId);
        Student cara = findStudentById(caraId);
        Student dan = findStudentById(danId);
        Student ella = findStudentById(ellaId);
        Student fred = findStudentById(fredId);
 
        // --- Scholarships (the office approves the first three, the fourth stays Pending) ---
        int merit = dost.postScholarship(this, "DOST Merit Scholarship", "2026-2027",
                "General weighted average of 1.75 or better",
                "One PDF with the grades, the certificate of indigency and a recommendation letter",
                "Monthly allowance for high achieving students.", 2, 8000, "2026-12-15");
        int college = sm.postScholarship(this, "SM College Scholarship", "2026-2027",
                "Family income below 250,000 pesos a year",
                "One PDF with the grades and the income tax return or certificate of no income",
                "Tuition support for one deserving student.", 1, 5000, "2026-12-31");
        int books = alumni.postScholarship(this, "Alumni Book Grant", "2026-2027",
                "Any BulSU student with no failing grade",
                "One PDF with the grades and a short letter",
                "A book allowance for each semester.", 3, 2000, "2027-01-15");
        sm.postScholarship(this, "SM Tech Scholarship", "2026-2027",
                "Students of IT and computer science",
                "One PDF with the grades and a project portfolio",
                "Support for technology students (waiting for the office).", 2, 6000, "2027-01-31");
 
        findScholarshipById(merit).approve();
        findScholarshipById(college).approve();
        findScholarshipById(books).approve();
 
        // --- Applications ---
        Scholarship meritScholarship = findScholarshipById(merit);
        Scholarship collegeScholarship = findScholarshipById(college);
        Scholarship booksScholarship = findScholarshipById(books);
 
        int anaApplication = ana.apply(this, meritScholarship, "ana_requirements.pdf");
        int benApplication = ben.apply(this, meritScholarship, "ben_requirements.pdf");
        int fredApplication = fred.apply(this, meritScholarship, "fred_requirements.pdf");
        int caraApplication = cara.apply(this, collegeScholarship, "cara_requirements.pdf");
        int danApplication = dan.apply(this, collegeScholarship, "dan_requirements.pdf");
        int ellaApplication = ella.apply(this, booksScholarship, "ella_requirements.pdf");
 
        // --- Decisions ---
        dost.shortlistApplication(findApplicationById(anaApplication));      // Ana: Shortlisted
        dost.approveApplication(this, findApplicationById(benApplication));  // Ben: Approved, becomes a scholar
        dost.rejectApplication(findApplicationById(fredApplication), today); // Fred: Rejected
        sm.returnApplication(findApplicationById(danApplication),
                "The income certificate is missing.", today);                // Dan: Returned
        sm.waitlistApplication(findApplicationById(caraApplication), today); // Cara: Waitlisted
        alumni.approveApplication(this, findApplicationById(ellaApplication)); // Ella: Approved, becomes a scholar
 
        // --- Grants (Ben's first grant is confirmed, his second is still Sent) ---
        Scholar benScholar = findScholar(ben, meritScholarship);
        int firstGrant = dost.recordGrant(this, benScholar, 4000, "GCash", "GC-2026-0001");
        dost.recordGrant(this, benScholar, 4000, "Bank transfer", "BT-2026-0007");
        ben.confirmGrant(findGrantById(firstGrant), today);
    }
}    