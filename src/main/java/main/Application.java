package main;

// Application is ONE application attempt of a student to a scholarship.
// Every attempt is its own record. For example, when an application is returned,
// the student applies again, and the new application is a new record.
// This way the history of the student is kept.
public class Application {

    // ---------- Attributes ----------
    // They are private, so other classes must use the methods and getters below.

    private int applicationId;        // unique ID of this application attempt
    private int studentId;            // ID of the student who applied
    private int scholarshipId;        // ID of the scholarship applied to
    private String credential;        // the file name of the one combined PDF the student uploaded
    private int cycleNo;              // the cycle of the scholarship when the student applied
    private String status;            // Submitted, Shortlisted, Returned, Approved, Rejected,
                                      // Waitlisted, Cancelled or Withdrawn
    private String benefactorComment; // the Benefactor's latest comment (for example what is missing)
    private String decidedOn;         // date of the Benefactor's latest decision, yyyy-MM-dd
    private String submittedOn;       // date the student submitted it, yyyy-MM-dd


    // ---------- Constructor ----------

    // Purpose: save a new application.
    // A new application always starts as Submitted, with no comment and no decision date.
    public Application(int applicationId, int studentId, int scholarshipId,
                       String credential, int cycleNo, String submittedOn) {
        this.applicationId = applicationId;
        this.studentId = studentId;
        this.scholarshipId = scholarshipId;
        this.credential = credential;
        this.cycleNo = cycleNo;
        this.submittedOn = submittedOn;

        this.status = "Submitted";
        this.benefactorComment = "";
        this.decidedOn = "";
    }


    // ---------- Status rules ----------
    // The status changes only through the methods below, and each method
    // changes it only when the current status allows it.
    // That is why none of them can be used to put the application in a wrong status.

    // Purpose: tell if the application is still waiting for a final answer.
    // Returns true when the status is Submitted, Shortlisted or Waitlisted.
    // A student cannot have two pending applications to the same scholarship.
    public boolean isPending() {
        boolean isSubmitted = status.equals("Submitted");
        boolean isShortlisted = status.equals("Shortlisted");
        boolean isWaitlisted = status.equals("Waitlisted");
        return isSubmitted || isShortlisted || isWaitlisted;
    }

    // Purpose: save the decision of the Benefactor.
    // newStatus is Returned, Approved, Rejected or Waitlisted.
    // The comment and the decision date (today) are saved too.
    // Returns true if the decision was saved, and false if:
    // - newStatus is not one of the four decisions,
    // - the application is not pending, or
    // - the decision is Returned and the application is not Submitted
    //   (so a shortlisted or waitlisted application cannot be returned).
    public boolean decide(String newStatus, String comment, String today) {
        // 1. Only four decisions are allowed.
        boolean isAllowedDecision = newStatus.equals("Returned") || newStatus.equals("Approved")
                || newStatus.equals("Rejected") || newStatus.equals("Waitlisted");
        if (isAllowedDecision == false) {
            return false;
        }

        // 2. A decision can be made only on a pending application.
        if (isPending() == false) {
            return false;
        }

        // 3. "Returned" works only on a Submitted application.
        boolean wantsToReturn = newStatus.equals("Returned");
        boolean isSubmitted = status.equals("Submitted");
        if (wantsToReturn && isSubmitted == false) {
            return false;
        }

        // All checks passed, so save the decision.
        status = newStatus;
        benefactorComment = comment;
        decidedOn = today;
        return true;
    }

    // Purpose: the Benefactor shortlists the application.
    // It stays in the same record and can later be approved, rejected or waitlisted,
    // but it cannot be returned.
    // Returns true if it was shortlisted, and false if it is not Submitted.
    public boolean shortlist() {
        boolean isSubmitted = status.equals("Submitted");
        if (isSubmitted == false) {
            return false;
        }

        status = "Shortlisted";
        return true;
    }

    // Purpose: the student cancels the application.
    // Returns true if it was cancelled, and false if it is not pending.
    public boolean cancel() {
        if (isPending() == false) {
            return false;
        }

        status = "Cancelled";
        return true;
    }

    // Purpose: the system withdraws the application by itself.
    // This happens when the student is approved for another scholarship,
    // or when the scholarship is renewed or closed.
    // Returns true if it was withdrawn, and false if it is not pending.
    public boolean withdraw() {
        if (isPending() == false) {
            return false;
        }

        status = "Withdrawn";
        return true;
    }


    // ---------- Text ----------

    // Purpose: give a short text of the application for lists and printing.
    // Example: "[3] Scholarship 1, cycle 1 - Submitted - ana.pdf (submitted 2026-10-20)"
    // toString() replaces (overrides) the toString() that every Java object has.
    @Override
    public String toString() {
        return "[" + applicationId + "] Scholarship " + scholarshipId + ", cycle " + cycleNo
                + " - " + status + " - " + credential + " (submitted " + submittedOn + ")";
    }


    // ---------- Getters ----------
    // They let other classes read the private attributes.
    // There are no setters, because the status, the comment and the decision date
    // change only through the methods above.

    public int getApplicationId() {
        return applicationId;
    }

    public int getStudentId() {
        return studentId;
    }

    public int getScholarshipId() {
        return scholarshipId;
    }

    public String getCredential() {
        return credential;
    }

    public int getCycleNo() {
        return cycleNo;
    }

    public String getStatus() {
        return status;
    }

    public String getBenefactorComment() {
        return benefactorComment;
    }

    public String getDecidedOn() {
        return decidedOn;
    }

    public String getSubmittedOn() {
        return submittedOn;
    }
}