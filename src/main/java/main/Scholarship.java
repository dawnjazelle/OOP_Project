package main;

// Scholarship is a scholarship posted by a Benefactor.
// It keeps the details of the scholarship, its cycle (which run it is),
// its status (Open or Closed) and its approval status
// (Pending, Approved, Rejected, Renewal Pending or Renewal Rejected).
// Students can see it only when it is approved and open.
// It also owns the array of its edit records (the edit history).
public class Scholarship {

    // ---------- Attributes ----------

    private int scholarshipId;        // unique ID of the scholarship
    private int benefactorId;         // ID of the Benefactor who posted it
    private String title;             // name of the scholarship
    private String academicYear;      // the school year it covers, for example "2026-2027"
    private String qualification;     // who may apply
    private String requirement;       // the documents the student must put in the one PDF
    private String description;       // what the scholarship is about
    private int slots;                // how many applicants can be approved per cycle
    private String deadline;          // last day to submit an application, yyyy-MM-dd
    private double grantAmount;       // optional amount each scholar receives (0 means none)
    private int cycleNo;              // which run of the scholarship this is (1, 2, 3, ...)
    private String status;            // "Open" or "Closed"
    private String approvalStatus;    // "Pending", "Approved", "Rejected", "Renewal Pending"
                                      // or "Renewal Rejected" (set by the office)
    private String createdOn;         // date the scholarship was posted, yyyy-MM-dd

    private ScholarshipEditHistory[] editHistory;   // the edit records of this scholarship
    private int editCount;                          // how many places of editHistory are filled


    // ---------- Constructor ----------

    // Purpose: create a new scholarship when a Benefactor posts it.
    // A new scholarship starts as cycle 1, Open and Pending,
    // so students cannot see it until the office approves it.
    public Scholarship(int scholarshipId, int benefactorId, String title, String academicYear,
                       String qualification, String requirement, String description,
                       int slots, double grantAmount, String deadline, String createdOn) {
        this.scholarshipId = scholarshipId;
        this.benefactorId = benefactorId;
        this.title = title;
        this.academicYear = academicYear;
        this.qualification = qualification;
        this.requirement = requirement;
        this.description = description;
        this.slots = slots;
        this.grantAmount = grantAmount;
        this.deadline = deadline;
        this.createdOn = createdOn;

        this.cycleNo = 1;
        this.status = "Open";
        this.approvalStatus = "Pending";

        this.editHistory = new ScholarshipEditHistory[10];   // starts with 10 places
        this.editCount = 0;
    }


    // ---------- Visibility and applying ----------

    // Purpose: tell if students can see this scholarship.
    // Returns true only when it is approved and open.
    public boolean isVisible() {
        boolean isApproved = approvalStatus.equals("Approved");
        boolean isOpen = status.equals("Open");
        return isApproved && isOpen;
    }

    // Purpose: tell if the post waits for the office to approve or reject it.
    // Returns true when the approval status is Pending (a new post)
    // or Renewal Pending (a renewed post).
    public boolean isWaitingForOffice() {
        boolean isNewPost = approvalStatus.equals("Pending");
        boolean isRenewal = approvalStatus.equals("Renewal Pending");
        return isNewPost || isRenewal;
    }

    // Purpose: tell if the post is a renewed post that waits for the office.
    // Returns true only when the approval status is Renewal Pending.
    public boolean isRenewalWaiting() {
        return approvalStatus.equals("Renewal Pending");
    }

    // Purpose: tell if the deadline is over.
    // today is the current date, yyyy-MM-dd.
    // Returns true when today is after the deadline (the deadline day itself is still allowed).
    // Dates in yyyy-MM-dd form are compared as text.
    public boolean isDeadlinePassed(String today) {
        return today.compareTo(deadline) > 0;
    }

    // Purpose: tell if this scholarship can take an application from this student now.
    // It must be visible and the deadline must not be over.
    // A renewed scholarship (cycle 2 or more) accepts only its own Active or Probation scholars.
    // Returns true if the student can apply, and false if not.
    public boolean canAccept(DataStore db, Student student) {
        // 1. It must be visible to students.
        if (isVisible() == false) {
            return false;
        }

        // 2. The deadline must not be over.
        if (isDeadlinePassed(db.getToday())) {
            return false;
        }

        // 3. A renewed scholarship accepts only its own scholars.
        boolean isRenewed = (cycleNo >= 2);
        if (isRenewed) {
            Scholar scholar = db.findScholar(student, this);

            boolean isItsScholar = false;
            if (scholar != null) {
                isItsScholar = scholar.blocksApplying();    // Active or Probation
            }
            if (isItsScholar == false) {
                return false;
            }
        }

        return true;
    }


    // ---------- Office decisions ----------

    // Purpose: the office approves a post that waits for it.
    // The approval status becomes Approved.
    // A Pending post (a new post) keeps its cycle number.
    // A Renewal Pending post (a renewed post) goes up by 1 cycle.
    // Returns true if it was approved, and false if the post is not waiting for the office.
    public boolean approve() {
        boolean isNewPost = approvalStatus.equals("Pending");
        boolean isRenewal = approvalStatus.equals("Renewal Pending");
        if (isNewPost == false && isRenewal == false) {
            return false;
        }

        if (isRenewal) {
            cycleNo = cycleNo + 1;
        }
        approvalStatus = "Approved";
        return true;
    }

    // Purpose: the office rejects a post that waits for it.
    // A Pending post becomes Rejected, and a Renewal Pending post becomes Renewal Rejected.
    // The reason is told to the Benefactor outside the system, so it is not saved here.
    // Returns true if it was rejected, and false if the post is not waiting for the office.
    public boolean reject() {
        if (approvalStatus.equals("Pending")) {
            approvalStatus = "Rejected";
            return true;
        }
        if (approvalStatus.equals("Renewal Pending")) {
            approvalStatus = "Renewal Rejected";
            return true;
        }
        return false;
    }


    // ---------- Benefactor actions ----------

    // Purpose: after a rejection, send the edited post to the office again.
    // A Rejected post goes back to Pending, and a Renewal Rejected post goes back to Renewal Pending.
    // Returns true if it was sent again, and false if the post is not rejected.
    public boolean resubmit() {
        if (approvalStatus.equals("Rejected")) {
            approvalStatus = "Pending";
            return true;
        }
        if (approvalStatus.equals("Renewal Rejected")) {
            approvalStatus = "Renewal Pending";
            return true;
        }
        return false;
    }

    // Purpose: start a renewal (the Renew button).
    // The approval status becomes Renewal Pending, so the scholarship is hidden from students
    // until the office approves it. The cycle number goes up only when the office approves (see approve()).
    // Returns true if the renewal started, and false if the scholarship is not visible
    // (a scholarship that waits for the office cannot start another renewal).
    public boolean renew() {
        if (isVisible() == false) {
            return false;
        }

        approvalStatus = "Renewal Pending";
        return true;
    }

    // Purpose: close the scholarship.
    // Returns true if it was closed, and false if it is already Closed.
    public boolean close() {
        boolean isClosed = status.equals("Closed");
        if (isClosed) {
            return false;
        }

        status = "Closed";
        return true;
    }


    // ---------- Edit history ----------

    // Purpose: add one edit record to the history of this scholarship.
    // When the array is full, it is replaced by one of twice the size, with the records copied.
    public void addEdit(ScholarshipEditHistory edit) {
        boolean isFull = (editCount == editHistory.length);
        if (isFull) {
            ScholarshipEditHistory[] biggerArray = new ScholarshipEditHistory[editHistory.length * 2];
            for (int i = 0; i < editCount; i++) {
                biggerArray[i] = editHistory[i];
            }
            editHistory = biggerArray;
        }

        editHistory[editCount] = edit;
        editCount = editCount + 1;
    }

    // Purpose: get the edit records of this scholarship, from the oldest to the newest.
    // The returned array has exactly editCount places.
    public ScholarshipEditHistory[] getEditHistory() {
        ScholarshipEditHistory[] result = new ScholarshipEditHistory[editCount];
        for (int i = 0; i < editCount; i++) {
            result[i] = editHistory[i];
        }
        return result;
    }


    // ---------- Text ----------

    // Purpose: give a short text of the scholarship for lists and printing.
    // Example: "[1] Merit Scholarship (2026-2027, cycle 1) - deadline 2026-12-31 - Open, Approved"
    @Override
    public String toString() {
        return "[" + scholarshipId + "] " + title + " (" + academicYear + ", cycle " + cycleNo
                + ") - deadline " + deadline + " - " + status + ", " + approvalStatus;
    }


    // ---------- Setters ----------
    // They change the details of the scholarship (used by Benefactor.editScholarship).
    // A setter ignores an empty value.
    // The status, the approval status, the cycle number and the ID have no setters.
    // They change only through the methods above.

    public void setTitle(String newTitle) {
        boolean isEmpty = newTitle.trim().equals("");
        if (isEmpty == false) {
            title = newTitle.trim();
        }
    }

    public void setAcademicYear(String newYear) {
        boolean isEmpty = newYear.trim().equals("");
        if (isEmpty == false) {
            academicYear = newYear.trim();
        }
    }

    public void setQualification(String newQualification) {
        boolean isEmpty = newQualification.trim().equals("");
        if (isEmpty == false) {
            qualification = newQualification.trim();
        }
    }

    public void setRequirement(String newRequirement) {
        boolean isEmpty = newRequirement.trim().equals("");
        if (isEmpty == false) {
            requirement = newRequirement.trim();
        }
    }

    public void setDescription(String newDescription) {
        boolean isEmpty = newDescription.trim().equals("");
        if (isEmpty == false) {
            description = newDescription.trim();
        }
    }

    public void setSlots(int newSlots) {
        if (newSlots >= 1) {
            slots = newSlots;
        }
    }

    public void setDeadline(String newDeadline) {
        boolean isEmpty = newDeadline.trim().equals("");
        if (isEmpty == false) {
            deadline = newDeadline.trim();
        }
    }

    public void setGrantAmount(double newAmount) {
        if (newAmount >= 0) {
            grantAmount = newAmount;
        }
    }


    // ---------- Getters ----------
    // They let other classes read the private attributes.

    public int getScholarshipId() {
        return scholarshipId;
    }

    public int getBenefactorId() {
        return benefactorId;
    }

    public String getTitle() {
        return title;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public String getQualification() {
        return qualification;
    }

    public String getRequirement() {
        return requirement;
    }

    public String getDescription() {
        return description;
    }

    public int getSlots() {
        return slots;
    }

    public String getDeadline() {
        return deadline;
    }

    public double getGrantAmount() {
        return grantAmount;
    }

    public int getCycleNo() {
        return cycleNo;
    }

    public String getStatus() {
        return status;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public String getCreatedOn() {
        return createdOn;
    }
}