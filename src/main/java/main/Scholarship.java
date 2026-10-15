package main;

import java.util.LinkedList;

// A scholarship posted by a Benefactor
public class Scholarship {

    // Status values
    public static final String OPEN = "Open";
    public static final String CLOSED = "Closed";

    // Approval status values (the office sets these)
    public static final String PENDING = "Pending";
    public static final String APPROVED = "Approved";
    public static final String REJECTED = "Rejected";

    private int scholarshipId;
    private int benefactorId;
    private String title;
    private String academicYear;
    private String qualification;
    private String requirement;
    private String description;
    private int slots;
    private String deadline;
    private double grantAmount;
    private int cycleNo;
    private String status;
    private String approvalStatus;
    private String createdOn;
    private boolean renewalPending;
    private LinkedList<ScholarshipEditHistory> editHistory;

    // A new scholarship is Open, Pending and in cycle 1
    public Scholarship(int scholarshipId, int benefactorId, String title, String academicYear,
                       String qualification, String requirement, String description,
                       int slots, String deadline, double grantAmount, String createdOn) {
        this.scholarshipId = scholarshipId;
        this.benefactorId = benefactorId;
        this.title = title;
        this.academicYear = academicYear;
        this.qualification = qualification;
        this.requirement = requirement;
        this.description = description;
        this.slots = slots;
        this.deadline = deadline;
        this.grantAmount = grantAmount;
        this.createdOn = createdOn;
        this.cycleNo = 1;
        this.status = OPEN;
        this.approvalStatus = PENDING;
        this.renewalPending = false;
        this.editHistory = new LinkedList<ScholarshipEditHistory>();
    }

    // True if students can see it (approved and open)
    public boolean isVisible() {
        if (!approvalStatus.equals(APPROVED)) {
            return false;
        }
        if (!status.equals(OPEN)) {
            return false;
        }
        return true;
    }

    // True if today is after the deadline (dates are yyyy-MM-dd)
    public boolean isDeadlinePassed(String today) {
        return today.compareTo(deadline) > 0;
    }

    // True if this student can apply now
    public boolean canAccept(DataStore db, Student student) {
        if (!isVisible()) {
            return false;
        }
        if (isDeadlinePassed(db.getToday())) {
            return false;
        }

        // A renewed scholarship takes only its own scholars
        if (cycleNo > 1) {
            Scholar scholar = db.findScholar(student, this);
            if (scholar == null) {
                return false;
            }
            if (!scholar.blocksApplying()) {
                return false;
            }
        }
        return true;
    }

    // The office approves a Pending post. After a renewal the cycle number goes up.
    public boolean approve() {
        if (!approvalStatus.equals(PENDING)) {
            return false;
        }
        approvalStatus = APPROVED;
        if (renewalPending) {
            cycleNo = cycleNo + 1;
            renewalPending = false;
        }
        return true;
    }

    // The office rejects a Pending post
    public boolean reject() {
        if (!approvalStatus.equals(PENDING)) {
            return false;
        }
        approvalStatus = REJECTED;
        return true;
    }

    // The Benefactor sends a Rejected post again. It goes back to Pending.
    public boolean resubmit() {
        if (!approvalStatus.equals(REJECTED)) {
            return false;
        }
        approvalStatus = PENDING;
        return true;
    }

    // Renew button. The post goes back to the office as Pending.
    public boolean renew() {
        if (!isVisible()) {
            return false;
        }
        if (renewalPending) {
            return false;
        }
        renewalPending = true;
        approvalStatus = PENDING;
        return true;
    }

    // Closes the scholarship
    public boolean close() {
        if (status.equals(CLOSED)) {
            return false;
        }
        status = CLOSED;
        return true;
    }

    // Adds one edit record to the history of this scholarship
    public void addEdit(ScholarshipEditHistory edit) {
        if (edit != null) {
            editHistory.add(edit);
        }
    }

    // The edit records of this scholarship
    public LinkedList<ScholarshipEditHistory> getEditHistory() {
        return editHistory;
    }

    // Text for printing a scholarship
    @Override
    public String toString() {
        return title + " (" + academicYear + ") | deadline " + deadline;
    }

    // Getters
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

    public boolean isRenewalPending() {
        return renewalPending;
    }

    // Setters (the Benefactor class checks the values first)
    public void setTitle(String title) {
        this.title = title;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSlots(int slots) {
        this.slots = slots;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public void setGrantAmount(double grantAmount) {
        this.grantAmount = grantAmount;
    }
}