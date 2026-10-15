// A student who was approved for a scholarship.
// A student can be a scholar of the same scholarship only once.
public class Scholar {

    // Status values
    public static final String ACTIVE = "Active";
    public static final String PROBATION = "Probation";
    public static final String SUSPENDED = "Suspended";
    public static final String LAPSED = "Lapsed";
    public static final String NOT_RENEWED = "Not renewed";
    public static final String COMPLETED = "Completed";

    private int scholarId;
    private int studentId;
    private int scholarshipId;
    private int applicationId;
    private String status;
    private String startDate;
    private String endDate;
    private int renewalCount;

    // applicationId is the approved application this record came from
    // A new scholar starts Active with no end date
    public Scholar(int scholarId, int studentId, int scholarshipId,
                   int applicationId, String startDate) {
        this.scholarId = scholarId;
        this.studentId = studentId;
        this.scholarshipId = scholarshipId;
        this.applicationId = applicationId;
        this.startDate = startDate;
        this.status = ACTIVE;
        this.endDate = "None";
        this.renewalCount = 0;
    }

    // True if the student still holds the scholarship (Active or Probation)
    public boolean blocksApplying() {
        if (status.equals(ACTIVE)) {
            return true;
        }
        if (status.equals(PROBATION)) {
            return true;
        }
        return false;
    }

    // Sets a new status. Gives false if the status is not one of the six.
    // When the scholarship ends for the student, the end date is set to today.
    public boolean setStatus(String newStatus, String today) {
        if (newStatus == null) {
            return false;
        }
        boolean validStatus = false;
        if (newStatus.equals(ACTIVE)) {
            validStatus = true;
        }
        if (newStatus.equals(PROBATION)) {
            validStatus = true;
        }
        if (newStatus.equals(SUSPENDED)) {
            validStatus = true;
        }
        if (newStatus.equals(LAPSED)) {
            validStatus = true;
        }
        if (newStatus.equals(NOT_RENEWED)) {
            validStatus = true;
        }
        if (newStatus.equals(COMPLETED)) {
            validStatus = true;
        }
        if (!validStatus) {
            return false;
        }

        this.status = newStatus;
        if (blocksApplying()) {
            this.endDate = "None";
        } else {
            this.endDate = today;
        }
        return true;
    }

    // Adds 1 when a renewal is approved
    public void addRenewal() {
        renewalCount = renewalCount + 1;
    }

    // The scholar did not apply by the deadline of the renewed scholarship
    public boolean markLapsed(String today) {
        if (!blocksApplying()) {
            return false;
        }
        status = LAPSED;
        endDate = today;
        return true;
    }

    // The scholarship was closed or the scholar graduated
    public boolean complete(String today) {
        if (!blocksApplying()) {
            return false;
        }
        status = COMPLETED;
        endDate = today;
        return true;
    }

    // Text for printing a scholar
    @Override
    public String toString() {
        return scholarId + " | student " + studentId + " | " + status + " | renewals " + renewalCount;
    }

    // Getters
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