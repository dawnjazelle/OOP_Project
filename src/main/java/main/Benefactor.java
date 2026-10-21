package main;

// Benefactor is a user who offers scholarships (a government office, a company,
// a foundation, an alumni group or an LGU).
// It extends (inherits from) UserAccount, so it already has the login data and the sign-in rules.
// Here we add the page details of the Benefactor and the things a Benefactor can do:
// post scholarships, decide applications, manage scholars and record grants.
// A Benefactor can act only on its OWN scholarships, scholars and grants.
public class Benefactor extends UserAccount {

    // ---------- Attributes ----------
    // They are private, so other classes must use the getters below.

    private int benefactorId;         // unique ID of the Benefactor (same number as the accountId)
    private String benefactorName;    // name of the organization or person
    private String category;          // government, private company, foundation, alumni or LGU
    private String description;       // short introduction shown on the Benefactor's page
    private String email;             // email, used to contact approved applicants
    private String contactNumber;     // phone number
    private String address;           // address


    // ---------- Constructor ----------

    // Purpose: create a new Benefactor.
    // The first four values are for the account part (they go to UserAccount),
    // the rest are the page details.
    public Benefactor(int benefactorId, String username, String tempPassword, String createdOn,
                      String benefactorName, String category, String description,
                      String email, String contactNumber, String address) {
        // super(...) calls the constructor of UserAccount.
        // The accountId is the same number as the benefactorId, and the account type is "Benefactor".
        super(benefactorId, username, tempPassword, "Benefactor", createdOn);

        this.benefactorId = benefactorId;
        this.benefactorName = benefactorName;
        this.category = category;
        this.description = description;
        this.email = email;
        this.contactNumber = contactNumber;
        this.address = address;
    }


    // ---------- Methods that replace the abstract methods of UserAccount ----------

    // Purpose: give the name of this role.
    // This replaces (overrides) getRole() of UserAccount.
    public String getRole() {
        return "Benefactor";
    }

    // Purpose: give the menu items that a Benefactor can use.
    // This replaces (overrides) getMenuOptions() of UserAccount.
    public String[] getMenuOptions() {
        String[] options = {
            "My page",
            "My scholarships",
            "Applicants",
            "My scholars",
            "Grants",
            "Summary"
        };
        return options;
    }


    // ---------- Page ----------

    // Purpose: change the details of the Benefactor's own page.
    // Returns true if the page was updated, and false if the name, the category
    // or the contact details (email, contact number, address) are empty.
    public boolean updatePage(String name, String category, String description,
                              String email, String contactNumber, String address) {
        boolean nameIsEmpty = name.trim().equals("");
        boolean categoryIsEmpty = category.trim().equals("");
        boolean emailIsEmpty = email.trim().equals("");
        boolean numberIsEmpty = contactNumber.trim().equals("");
        boolean addressIsEmpty = address.trim().equals("");

        if (nameIsEmpty || categoryIsEmpty || emailIsEmpty || numberIsEmpty || addressIsEmpty) {
            return false;
        }

        this.benefactorName = name.trim();
        this.category = category.trim();
        this.description = description.trim();
        this.email = email.trim();
        this.contactNumber = contactNumber.trim();
        this.address = address.trim();
        return true;
    }


    // ---------- Scholarships ----------

    // Purpose: post a new scholarship.
    // Its approval status starts as Pending, so students cannot see it until the office approves it.
    // deadline is written as yyyy-MM-dd, for example 2026-12-31.
    // Returns the ID of the new scholarship, or 0 if:
    // a text value is empty, the slots are below 1, the grant amount is below 0,
    // or the deadline is not a real date that is today or later.
    public int postScholarship(DataStore db, String title, String academicYear,
                               String qualification, String requirement, String description,
                               int slots, double grantAmount, String deadline) {
        // 1. No text value may be empty.
        boolean titleIsEmpty = title.trim().equals("");
        boolean yearIsEmpty = academicYear.trim().equals("");
        boolean qualificationIsEmpty = qualification.trim().equals("");
        boolean requirementIsEmpty = requirement.trim().equals("");
        boolean descriptionIsEmpty = description.trim().equals("");

        if (titleIsEmpty || yearIsEmpty || qualificationIsEmpty
                || requirementIsEmpty || descriptionIsEmpty) {
            return 0;
        }

        // 2. There must be at least 1 slot, and the grant amount cannot be negative.
        if (slots < 1) {
            return 0;
        }
        if (grantAmount < 0) {
            return 0;
        }

        // 3. The deadline must be a real date, and it must be today or later.
        // Dates written as yyyy-MM-dd can be compared as text: the later date is the bigger text.
        String cleanDeadline = deadline.trim();
        boolean deadlineIsRealDate = isValidDate(cleanDeadline);
        if (deadlineIsRealDate == false) {
            return 0;
        }
        boolean deadlineIsInThePast = (cleanDeadline.compareTo(db.getToday()) < 0);
        if (deadlineIsInThePast) {
            return 0;
        }

        // All checks passed, so the DataStore saves the scholarship as Pending.
        return db.createScholarship(this, title.trim(), academicYear.trim(), qualification.trim(),
                                    requirement.trim(), description.trim(), slots,
                                    grantAmount, cleanDeadline);
    }

    // Purpose: change ONE field of the Benefactor's own scholarship.
    // field is one of: title, academicYear, qualification, requirement, description,
    // slots, deadline, grantAmount. value is the new value, typed as text.
    // The old and the new value are saved in the edit history.
    // A rejected post goes back to Pending, so the office can check it again.
    // Returns true if the change was made, and false if the scholarship is not
    // the Benefactor's, or the field or the value is not valid.
    public boolean editScholarship(DataStore db, Scholarship scholarship, String field, String value) {
        // 1. It must be the Benefactor's own scholarship.
        boolean isMine = (scholarship.getBenefactorId() == benefactorId);
        if (isMine == false) {
            return false;
        }

        // 2. The new value must not be empty.
        String newValue = value.trim();
        if (newValue.equals("")) {
            return false;
        }

        // 3. Find the field, check the new value, and change it.
        // oldValue keeps the value before the change, for the edit history.
        String oldValue = "";

        if (field.equals("title")) {
            oldValue = scholarship.getTitle();
            scholarship.setTitle(newValue);

        } else if (field.equals("academicYear")) {
            oldValue = scholarship.getAcademicYear();
            scholarship.setAcademicYear(newValue);

        } else if (field.equals("qualification")) {
            oldValue = scholarship.getQualification();
            scholarship.setQualification(newValue);

        } else if (field.equals("requirement")) {
            oldValue = scholarship.getRequirement();
            scholarship.setRequirement(newValue);

        } else if (field.equals("description")) {
            oldValue = scholarship.getDescription();
            scholarship.setDescription(newValue);

        } else if (field.equals("slots")) {
            // The value is text, so we change it to a whole number first.
            int newSlots = 0;
            try {
                newSlots = Integer.parseInt(newValue);
            } catch (NumberFormatException e) {
                return false;     // it was not a whole number
            }
            if (newSlots < 1) {
                return false;
            }
            oldValue = "" + scholarship.getSlots();
            scholarship.setSlots(newSlots);

        } else if (field.equals("grantAmount")) {
            // The value is text, so we change it to a number with decimals first.
            double newAmount = 0;
            try {
                newAmount = Double.parseDouble(newValue);
            } catch (NumberFormatException e) {
                return false;     // it was not a number
            }
            if (newAmount < 0) {
                return false;
            }
            oldValue = "" + scholarship.getGrantAmount();
            scholarship.setGrantAmount(newAmount);

        } else if (field.equals("deadline")) {
            // The new deadline must be a real date, and it must be today or later.
            boolean deadlineIsRealDate = isValidDate(newValue);
            if (deadlineIsRealDate == false) {
                return false;
            }
            boolean deadlineIsInThePast = (newValue.compareTo(db.getToday()) < 0);
            if (deadlineIsInThePast) {
                return false;
            }
            oldValue = scholarship.getDeadline();
            scholarship.setDeadline(newValue);

        } else {
            return false;         // there is no field with this name
        }

        // 4. Save the change in the edit history of the scholarship.
        db.createEditHistory(scholarship, field, oldValue, newValue);

        // 5. A rejected post goes back to Pending after it is edited.
        boolean wasRejected = scholarship.getApprovalStatus().equals("Rejected");
        if (wasRejected) {
            scholarship.resubmit();
        }
        return true;
    }

    // Purpose: start a renewal (the Renew button).
    // The scholarship goes back to the office for approval,
    // and its pending applications from the old cycle become Withdrawn.
    // Returns true if the renewal started, and false if it cannot be renewed.
    public boolean renewScholarship(DataStore db, Scholarship scholarship) {
        // It must be the Benefactor's own scholarship.
        boolean isMine = (scholarship.getBenefactorId() == benefactorId);
        if (isMine == false) {
            return false;
        }

        // The Scholarship class decides if it can be renewed.
        boolean renewed = scholarship.renew();
        if (renewed == false) {
            return false;
        }

        // Withdraw every pending application of this scholarship.
        Application[] applications = db.applicationsForScholarship(scholarship);
        for (int i = 0; i < applications.length; i++) {
            if (applications[i].isPending()) {
                applications[i].withdraw();
            }
        }
        return true;
    }

    // Purpose: close the scholarship.
    // Its pending applications become Withdrawn, and its Active and Probation scholars
    // become Completed with the end date set to today.
    // Returns true if it was closed, and false if it is not the Benefactor's or is already closed.
    public boolean closeScholarship(DataStore db, Scholarship scholarship) {
        // It must be the Benefactor's own scholarship.
        boolean isMine = (scholarship.getBenefactorId() == benefactorId);
        if (isMine == false) {
            return false;
        }

        // The Scholarship class decides if it can be closed.
        boolean closed = scholarship.close();
        if (closed == false) {
            return false;
        }

        // Withdraw every pending application of this scholarship.
        Application[] applications = db.applicationsForScholarship(scholarship);
        for (int i = 0; i < applications.length; i++) {
            if (applications[i].isPending()) {
                applications[i].withdraw();
            }
        }

        // complete() works only on Active or Probation scholars,
        // so the scholars with any other status stay as they are.
        Scholar[] scholars = db.scholarsOfScholarship(scholarship);
        for (int i = 0; i < scholars.length; i++) {
            scholars[i].complete(db.getToday());
        }
        return true;
    }


    // ---------- Deciding applications ----------

    // Purpose: return an application to the student, with a comment about what is missing.
    // The student then applies again, which makes a new application record.
    // Returns true if it was returned, and false if the comment is empty
    // or the application is not Submitted (a shortlisted application cannot be returned).
    public boolean returnApplication(Application application, String comment, String today) {
        boolean commentIsEmpty = comment.trim().equals("");
        if (commentIsEmpty) {
            return false;
        }

        // The Application class refuses "Returned" if the status is not Submitted.
        return application.decide("Returned", comment.trim(), today);
    }

    // Purpose: shortlist a Submitted application.
    // It stays in the same record and can be approved, rejected or waitlisted later.
    // Returns true if it was shortlisted, and false if it is not Submitted.
    public boolean shortlistApplication(Application application) {
        return application.shortlist();
    }

    // Purpose: approve an application.
    // It uses one slot, creates the scholar record as Active,
    // and withdraws the other pending applications of the student.
    // Returns the ID of the new scholar record, or 0 if the application is not pending,
    // the student already holds a scholarship or was a scholar of this scholarship before,
    // no slot is left, or there is no room to save the scholar record.
    public int approveApplication(DataStore db, Application application) {
        // 1. The application must still be pending.
        boolean isPending = application.isPending();
        if (isPending == false) {
            return 0;
        }

        // 2. Find the scholarship and the student. The scholarship must be the Benefactor's own.
        Scholarship scholarship = db.findScholarshipById(application.getScholarshipId());
        Student student = db.findStudentById(application.getStudentId());
        boolean recordsAreMissing = (scholarship == null) || (student == null);
        if (recordsAreMissing) {
            return 0;
        }
        boolean isMine = (scholarship.getBenefactorId() == benefactorId);
        if (isMine == false) {
            return 0;
        }

        // 3. A student who already holds a scholarship cannot be approved for another one.
        //    A student can also be a scholar of the same scholarship only once.
        Scholar holding = db.findBlockingScholar(student);
        boolean alreadyScholar = (holding != null);
        if (alreadyScholar) {
            return 0;
        }
        Scholar earlierRecord = db.findScholar(student, scholarship);
        boolean wasScholarHere = (earlierRecord != null);
        if (wasScholarHere) {
            return 0;
        }

        // 4. A slot must be left. Slots are counted per cycle,
        //    and only approved applications use a slot.
        int approvedCount = db.countApprovedInCycle(scholarship, application.getCycleNo());
        boolean noSlotLeft = (approvedCount >= scholarship.getSlots());
        if (noSlotLeft) {
            return 0;
        }

        // 5. The DataStore creates the scholar record (status Active).
        //    We do this first, so if there is no room we stop before changing the application.
        int scholarId = db.createScholar(application);
        if (scholarId == 0) {
            return 0;
        }

        // 6. Mark the application as Approved.
        //    This cannot fail here, because we already checked that it is pending.
        application.decide("Approved", "", db.getToday());

        // 7. Withdraw the other pending applications of this student.
        Application[] allOfStudent = db.applicationsOf(student);
        for (int i = 0; i < allOfStudent.length; i++) {
            if (allOfStudent[i].isPending()) {
                allOfStudent[i].withdraw();
            }
        }
        return scholarId;
    }

    // Purpose: reject a pending application and save the decision date.
    // Returns true if it was rejected, and false if the application is not pending.
    public boolean rejectApplication(Application application, String today) {
        return application.decide("Rejected", "", today);
    }

    // Purpose: put an application on the waiting list (for applicants beyond the slots).
    // Returns true if it was waitlisted, and false if it cannot be waitlisted.
    // Only a Submitted or Shortlisted application can be waitlisted.
    public boolean waitlistApplication(Application application, String today) {
        String status = application.getStatus();
        boolean canWaitlist = status.equals("Submitted") || status.equals("Shortlisted");
        if (canWaitlist == false) {
            return false;
        }
        return application.decide("Waitlisted", "", today);
    }


    // ---------- Renewals ----------

    // Purpose: approve a renewal application of a current scholar.
    // The renewal count goes up by 1, and the scholar stays Active
    // or is placed on Probation (probation is true). Probation needs a comment with the conditions.
    // Returns true if it was approved, and false if the scholar no longer holds
    // the scholarship, there is no slot left, or the application cannot be approved.
    public boolean approveRenewal(DataStore db, Application application,
                                  boolean probation, String comment) {
        // 1. Find the scholar record of this student in this scholarship.
        Student student = db.findStudentById(application.getStudentId());
        Scholarship scholarship = db.findScholarshipById(application.getScholarshipId());
        boolean recordsAreMissing = (student == null) || (scholarship == null);
        if (recordsAreMissing) {
            return false;
        }
        Scholar scholar = db.findScholar(student, scholarship);

        // 2. The scholar must still hold the scholarship (Active or Probation).
        boolean holdsScholarship = false;
        if (scholar != null) {
            holdsScholarship = scholar.blocksApplying();
        }
        if (holdsScholarship == false) {
            return false;
        }

        // 3. The scholarship must be the Benefactor's own.
        boolean isMine = (scholarship.getBenefactorId() == benefactorId);
        if (isMine == false) {
            return false;
        }

        // 4. Probation needs a comment that tells the conditions.
        boolean commentIsEmpty = comment.trim().equals("");
        if (probation && commentIsEmpty) {
            return false;
        }

        // 5. A slot must be left, because renewing scholars count against the slots of the cycle.
        int approvedCount = db.countApprovedInCycle(scholarship, application.getCycleNo());
        boolean noSlotLeft = (approvedCount >= scholarship.getSlots());
        if (noSlotLeft) {
            return false;
        }

        // 6. Mark the application as Approved (this fails if it is not pending).
        boolean approved = application.decide("Approved", comment.trim(), db.getToday());
        if (approved == false) {
            return false;
        }

        // 7. Update the scholar: renewal count + 1, and Active or Probation.
        scholar.addRenewal();
        if (probation) {
            scholar.setStatus("Probation", db.getToday());
        } else {
            scholar.setStatus("Active", db.getToday());
        }
        return true;
    }

    // Purpose: reject a renewal application.
    // A scholar who still holds the scholarship becomes "Not renewed",
    // and the student can apply to other scholarships.
    // Returns true if it was rejected, and false if it cannot be rejected.
    public boolean rejectRenewal(DataStore db, Application application, String comment) {
        // 1. Mark the application as Rejected (this fails if it is not pending).
        boolean rejected = application.decide("Rejected", comment.trim(), db.getToday());
        if (rejected == false) {
            return false;
        }

        // 2. Find the scholar record and set it to "Not renewed".
        Student student = db.findStudentById(application.getStudentId());
        Scholarship scholarship = db.findScholarshipById(application.getScholarshipId());
        boolean recordsAreMissing = (student == null) || (scholarship == null);
        if (recordsAreMissing) {
            return true;       // the application was rejected, there is no scholar to update
        }
        Scholar scholar = db.findScholar(student, scholarship);
        if (scholar != null) {
            // setStatus works only while the scholar still holds the scholarship.
            scholar.setStatus("Not renewed", db.getToday());
        }
        return true;
    }


    // ---------- Scholars ----------

    // Purpose: set the status of a scholar.
    // The Benefactor can set Active, Probation, Suspended or Completed.
    // (Lapsed and Not renewed are set by the system.)
    // Returns true if the status was changed, and false if the status is not allowed
    // or the scholarship of the scholar already ended.
    public boolean setScholarStatus(Scholar scholar, String status, String today) {
        boolean statusIsAllowed = status.equals("Active") || status.equals("Probation")
                || status.equals("Suspended") || status.equals("Completed");
        if (statusIsAllowed == false) {
            return false;
        }

        // The Scholar class refuses the change if the scholarship already ended.
        return scholar.setStatus(status, today);
    }


    // ---------- Grants ----------

    // Purpose: record a grant that was sent to a scholar OUTSIDE the system.
    // The system never sends or holds money, it only keeps the record.
    // method is GCash, Bank transfer or Cash. The reference number is needed unless it is cash.
    // The grant starts as Sent.
    // Returns the ID of the new grant, or 0 if the scholar is not the Benefactor's
    // or a value is not valid.
    public int recordGrant(DataStore db, Scholar scholar, double amount,
                           String method, String referenceNo) {
        // 1. The scholar must be a scholar of the Benefactor's own scholarship.
        Scholarship scholarship = db.findScholarshipById(scholar.getScholarshipId());
        if (scholarship == null) {
            return 0;
        }
        boolean isMyScholar = (scholarship.getBenefactorId() == benefactorId);
        if (isMyScholar == false) {
            return 0;
        }

        // 2. The amount must be above 0.
        boolean amountIsValid = (amount > 0);
        if (amountIsValid == false) {
            return 0;
        }

        // 3. The method must be one of the three allowed methods.
        boolean methodIsValid = method.equals("GCash") || method.equals("Bank transfer")
                || method.equals("Cash");
        if (methodIsValid == false) {
            return 0;
        }

        // 4. A reference number is needed unless the grant was given in cash.
        boolean isCash = method.equals("Cash");
        boolean referenceIsEmpty = referenceNo.trim().equals("");
        boolean referenceIsOk = isCash || (referenceIsEmpty == false);
        if (referenceIsOk == false) {
            return 0;
        }

        // All checks passed, so the DataStore saves the grant as Sent.
        return db.createGrant(scholar, this, amount, method, referenceNo.trim());
    }

    // Purpose: void a wrong grant record. A grant is never edited or deleted, only voided.
    // If needed, the Benefactor records a new grant to replace it.
    // Returns true if it was voided, and false if it is not this Benefactor's grant
    // or it is already voided.
    public boolean voidGrant(Grant grant) {
        boolean isMine = (grant.getBenefactorId() == benefactorId);
        if (isMine == false) {
            return false;
        }
        return grant.markVoided();
    }


    // ---------- Helper ----------

    // Purpose: used by postScholarship and editScholarship.
    // Checks that the text is a real date written as yyyy-MM-dd, for example 2026-12-31.
    // Returns true if it is, and false if it is not.
    // It is private because only this class needs it.
    private boolean isValidDate(String text) {
        // 1. It must have 10 characters, with a dash after the year and after the month.
        if (text.length() != 10) {
            return false;
        }
        boolean firstDashIsThere = (text.charAt(4) == '-');
        boolean secondDashIsThere = (text.charAt(7) == '-');
        if (firstDashIsThere == false || secondDashIsThere == false) {
            return false;
        }

        // 2. The year, the month and the day must be numbers.
        int year = 0;
        int month = 0;
        int day = 0;
        try {
            year = Integer.parseInt(text.substring(0, 4));
            month = Integer.parseInt(text.substring(5, 7));
            day = Integer.parseInt(text.substring(8, 10));
        } catch (NumberFormatException e) {
            return false;         // one of them was not a number
        }

        // 3. The month must be from 1 to 12.
        if (month < 1 || month > 12) {
            return false;
        }

        // 4. The day must fit the month. February has 29 days in a leap year.
        int daysInMonth = 31;
        if (month == 4 || month == 6 || month == 9 || month == 11) {
            daysInMonth = 30;
        }
        if (month == 2) {
            boolean isLeapYear = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
            if (isLeapYear) {
                daysInMonth = 29;
            } else {
                daysInMonth = 28;
            }
        }
        if (day < 1 || day > daysInMonth) {
            return false;
        }
        return true;
    }


    // ---------- Text ----------

    // Purpose: give a short text of the Benefactor for lists and printing.
    // Example: "Bulacan LGU (LGU)"
    // toString() replaces (overrides) the toString() that every Java object has.
    public String toString() {
        return benefactorName + " (" + category + ")";
    }


    // ---------- Getters ----------
    // They let other classes read the private attributes.

    public int getBenefactorId() {
        return benefactorId;
    }

    public String getBenefactorName() {
        return benefactorName;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getEmail() {
        return email;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public String getAddress() {
        return address;
    }
}