package main;

// Scholar is a student who was approved for a scholarship.
// It is created when a Benefactor approves an application.
// Each scholarship has its own scholars, and a student can be a scholar
// of the same scholarship only once.
// The status of the scholar decides if the student can apply to another scholarship.
public class Scholar {

    // ---------- Attributes ----------
    // They are private, so other classes must use the methods and getters below.

    private int scholarId;        // unique ID of this scholar record
    private int studentId;        // ID of the student who holds the scholarship
    private int scholarshipId;    // ID of the scholarship that is held
    private int applicationId;    // ID of the approved application this record came from
    private String status;        // Active, Probation, Suspended, Lapsed, Not renewed or Completed
    private String startDate;     // when the student started as a scholar, yyyy-MM-dd
    private String endDate;       // when the scholarship ended for the student (empty while it goes on)
    private int renewalCount;     // how many times the scholar's renewal was approved


    // ---------- Constructor ----------

    // Purpose: save a new scholar record.
    // A new scholar always starts as Active, with no end date and 0 renewals.
    public Scholar(int scholarId, int studentId, int scholarshipId,
                   int applicationId, String startDate) {
        this.scholarId = scholarId;
        this.studentId = studentId;
        this.scholarshipId = scholarshipId;
        this.applicationId = applicationId;
        this.startDate = startDate;

        this.status = "Active";
        this.endDate = "";
        this.renewalCount = 0;
    }


    // ---------- Status rules ----------

    // Purpose: tell if the student still holds the scholarship.
    // Returns true while the status is Active or Probation.
    // In that case the student cannot apply to another scholarship.
    public boolean blocksApplying() {
        boolean isActive = status.equals("Active");
        boolean isOnProbation = status.equals("Probation");
        return isActive || isOnProbation;
    }

    // Purpose: change the status of the scholar.
    // newStatus is Active, Probation, Suspended, Lapsed, Not renewed or Completed.
    // It works only while the scholar still holds the scholarship (Active or Probation),
    // because a scholarship that has ended stays ended.
    // When the new status ends the scholarship (Suspended, Lapsed, Not renewed or Completed),
    // the end date is set to today, and the student is free to apply again.
    // Returns true if the status was changed, and false if:
    // - newStatus is not one of the six statuses, or
    // - the scholarship of the student already ended.
    public boolean setStatus(String newStatus, String today) {
        // 1. Only the six known statuses are allowed.
        boolean isKnownStatus = newStatus.equals("Active") || newStatus.equals("Probation")
                || newStatus.equals("Suspended") || newStatus.equals("Lapsed")
                || newStatus.equals("Not renewed") || newStatus.equals("Completed");
        if (isKnownStatus == false) {
            return false;
        }

        // 2. A scholarship that already ended cannot change again.
        if (blocksApplying() == false) {
            return false;
        }

        // 3. Save the new status.
        status = newStatus;

        // 4. If the new status ends the scholarship, save the end date.
        boolean scholarshipEnded = newStatus.equals("Suspended") || newStatus.equals("Lapsed")
                || newStatus.equals("Not renewed") || newStatus.equals("Completed");
        if (scholarshipEnded) {
            endDate = today;
        }
        return true;
    }

    // Purpose: add 1 to the renewal count.
    // It is called when a renewal application of this scholar is approved.
    public void addRenewal() {
        renewalCount = renewalCount + 1;
    }

    // Purpose: mark the scholar as Lapsed.
    // This happens when the scholar did not apply by the deadline of the renewed scholarship.
    // The end date is saved too.
    // Returns true if it was done, and false if the scholar is not Active or Probation.
    public boolean markLapsed(String today) {
        return setStatus("Lapsed", today);
    }

    // Purpose: mark the scholar as Completed, with the end date set to today.
    // It is used when the scholarship is closed or when the scholar graduates.
    // Returns true if it was done, and false if the scholar is not Active or Probation.
    public boolean complete(String today) {
        return setStatus("Completed", today);
    }


    // ---------- Text ----------

    // Purpose: give a short text of the scholar for lists and printing.
    // Example while it goes on: "[1] Student 2, Scholarship 1 - Active, 0 renewals (since 2026-10-20)"
    // Example after it ended:   "[1] Student 2, Scholarship 1 - Completed, 1 renewals (2026-10-20 to 2027-03-31)"
    // toString() replaces (overrides) the toString() that every Java object has.
    public String toString() {
        String text = "[" + scholarId + "] Student " + studentId + ", Scholarship " + scholarshipId
                + " - " + status + ", " + renewalCount + " renewals ";

        boolean hasEnded = (endDate.equals("") == false);
        if (hasEnded) {
            text = text + "(" + startDate + " to " + endDate + ")";
        } else {
            text = text + "(since " + startDate + ")";
        }
        return text;
    }


    // ---------- Getters ----------
    // They let other classes read the private attributes.
    // There are no setters, because the status, the end date and the renewal count
    // change only through the methods above.

    public int getScholarId() {
        return scholarId;
    }

    public int getStudentId() {
        return studentId;
    }

    public int getScholarshipId() {
        return scholarshipId;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public String getStatus() {
        return status;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public int getRenewalCount() {
        return renewalCount;
    }
}