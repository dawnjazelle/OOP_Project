package main;

// One application of a student to a scholarship.
// Every try is its own record, so the history is kept.
public class Application {

    // Status values
    public static final String SUBMITTED = "Submitted";
    public static final String SHORTLISTED = "Shortlisted";
    public static final String RETURNED = "Returned";
    public static final String APPROVED = "Approved";
    public static final String REJECTED = "Rejected";
    public static final String WAITLISTED = "Waitlisted";
    public static final String CANCELLED = "Cancelled";
    public static final String WITHDRAWN = "Withdrawn";

    private int applicationId;
    private int studentId;
    private int scholarshipId;
    private String credential;
    private int cycleNo;
    private String status;
    private String benefactorComment;
    private String decidedOn;
    private String submittedOn;

    // credential is the file name of the one combined PDF
    // A new application starts as Submitted
    public Application(int applicationId, int studentId, int scholarshipId,
                       String credential, int cycleNo, String submittedOn) {
        this.applicationId = applicationId;
        this.studentId = studentId;
        this.scholarshipId = scholarshipId;
        this.credential = credential;
        this.cycleNo = cycleNo;
        this.submittedOn = submittedOn;
        this.status = SUBMITTED;
        this.benefactorComment = "";
        this.decidedOn = "None";
    }

    // True if the status is Submitted, Shortlisted or Waitlisted
    public boolean isPending() {
        if (status.equals(SUBMITTED)) {
            return true;
        }
        if (status.equals(SHORTLISTED)) {
            return true;
        }
        if (status.equals(WAITLISTED)) {
            return true;
        }
        return false;
    }

    // Saves the Benefactor's decision. Works only on a pending application.
    // Decisions: Returned, Approved, Rejected or Waitlisted
    public boolean decide(String newStatus, String comment, String today) {
        if (!isPending() || newStatus == null) {
            return false;
        }
        boolean validDecision = false;
        if (newStatus.equals(RETURNED)) {
            validDecision = true;
        }
        if (newStatus.equals(APPROVED)) {
            validDecision = true;
        }
        if (newStatus.equals(REJECTED)) {
            validDecision = true;
        }
        if (newStatus.equals(WAITLISTED)) {
            validDecision = true;
        }
        if (!validDecision) {
            return false;
        }

        this.status = newStatus;
        this.benefactorComment = "";
        if (comment != null) {
            this.benefactorComment = comment;
        }
        this.decidedOn = today;
        return true;
    }

    // Sets Shortlisted. Works only on a Submitted application.
    public boolean shortlist() {
        if (!status.equals(SUBMITTED)) {
            return false;
        }
        status = SHORTLISTED;
        return true;
    }

    // The student cancels a pending application
    public boolean cancel() {
        if (!isPending()) {
            return false;
        }
        status = CANCELLED;
        return true;
    }

    // Sets Withdrawn on a pending application (student approved elsewhere, or scholarship renewed or closed)
    public boolean withdraw() {
        if (!isPending()) {
            return false;
        }
        status = WITHDRAWN;
        return true;
    }

    // Text for printing an application
    @Override
    public String toString() {
        return applicationId + " | cycle " + cycleNo + " | " + status + " | submitted " + submittedOn;
    }

    // Getters
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