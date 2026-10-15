import java.util.LinkedList;

// A scholarship Benefactor. Keeps the page details and what a Benefactor can do.
public class Benefactor extends UserAccount {

    private int benefactorId;
    private String benefactorName;
    private String benefactorType;
    private String description;
    private String contactEmail;
    private String contactNumber;
    private String address;

    // benefactorId is the same number as the accountId
    public Benefactor(int benefactorId, String username, String password, String createdOn,
                      String benefactorName, String benefactorType, String description,
                      String contactEmail, String contactNumber, String address) {
        super(benefactorId, username, password, TYPE_BENEFACTOR, createdOn);
        this.benefactorId = benefactorId;
        this.benefactorName = benefactorName;
        this.benefactorType = benefactorType;
        this.description = description;
        this.contactEmail = contactEmail;
        this.contactNumber = contactNumber;
        this.address = address;
    }

    // The role name
    @Override
    public String getRole() {
        return "Benefactor";
    }

    // The menu of a Benefactor
    @Override
    public String[] getMenuOptions() {
        String[] options = new String[6];
        options[0] = "My page";
        options[1] = "My scholarships";
        options[2] = "Applicants";
        options[3] = "Scholars";
        options[4] = "Grants";
        options[5] = "Summary";
        return options;
    }

    // ------------------------------------------------------------ page

    // Changes the page details. Gives false if a main detail is empty.
    public boolean updatePage(String name, String type, String description,
                              String contactEmail, String contactNumber, String address) {
        if (isBlank(name) || isBlank(type)) {
            return false;
        }
        if (isBlank(contactEmail) || isBlank(contactNumber)) {
            return false;
        }
        this.benefactorName = name.trim();
        this.benefactorType = type.trim();
        this.contactEmail = contactEmail.trim();
        this.contactNumber = contactNumber.trim();

        // Description and address can be empty
        this.description = "";
        if (!isBlank(description)) {
            this.description = description.trim();
        }
        this.address = "";
        if (!isBlank(address)) {
            this.address = address.trim();
        }
        return true;
    }

    // ------------------------------------------------------------ scholarships

    // Posts a scholarship. It starts as Pending. Gives null if a value is not valid.
    public Scholarship postScholarship(DataStore db, String title, String academicYear,
                                       String qualification, String requirement, String description,
                                       int slots, String deadline, double grantAmount) {
        if (isBlank(title) || isBlank(academicYear) || isBlank(description)) {
            return null;
        }
        if (isBlank(qualification) || isBlank(requirement)) {
            return null;
        }
        if (slots < 1 || grantAmount < 0) {
            return null;
        }
        if (!isDate(deadline)) {
            return null;
        }
        // The deadline cannot be before today
        if (deadline.trim().compareTo(db.getToday()) < 0) {
            return null;
        }
        return db.createScholarship(this, title.trim(), academicYear.trim(), qualification.trim(),
                requirement.trim(), description.trim(), slots, deadline.trim(), grantAmount);
    }

    // Changes one field and saves the change in the edit history. Gives false if not valid.
    // Fields: title, academicYear, qualification, requirement, description, slots, deadline, grantAmount
    public boolean editScholarship(DataStore db, Scholarship scholarship, String field, String value) {
        if (!owns(scholarship)) {
            return false;
        }
        if (isBlank(field) || isBlank(value)) {
            return false;
        }
        String oldValue = getFieldValue(scholarship, field);
        if (oldValue == null) {
            return false;
        }
        if (!setFieldValue(scholarship, field, value.trim())) {
            return false;
        }
        db.createEditHistory(scholarship, field, oldValue, value.trim());

        // A rejected post goes back to the office as Pending
        if (scholarship.getApprovalStatus().equals(Scholarship.REJECTED)) {
            scholarship.resubmit();
        }
        return true;
    }

    // Renew button. The office must approve the scholarship again.
    public boolean renewScholarship(DataStore db, Scholarship scholarship) {
        if (!owns(scholarship)) {
            return false;
        }
        if (!scholarship.isVisible() || scholarship.isRenewalPending()) {
            return false;
        }
        withdrawPending(db, scholarship);
        scholarship.renew();
        return true;
    }

    // Closes the scholarship. Active and Probation scholars become Completed.
    public boolean closeScholarship(DataStore db, Scholarship scholarship) {
        if (!owns(scholarship)) {
            return false;
        }
        if (scholarship.getStatus().equals(Scholarship.CLOSED)) {
            return false;
        }
        withdrawPending(db, scholarship);

        LinkedList<Scholar> scholars = db.scholarsOfScholarship(scholarship);
        for (int i = 0; i < scholars.size(); i++) {
            Scholar scholar = scholars.get(i);
            if (scholar.blocksApplying()) {
                scholar.complete(db.getToday());
            }
        }
        scholarship.close();
        return true;
    }

    // ------------------------------------------------------------ applications

    // Returns the application with a comment. Only a Submitted application can be returned.
    public boolean returnApplication(Application application, String comment, String today) {
        if (application == null || isBlank(comment)) {
            return false;
        }
        if (!application.getStatus().equals(Application.SUBMITTED)) {
            return false;
        }
        application.decide(Application.RETURNED, comment.trim(), today);
        return true;
    }

    // Shortlists the application. Only a Submitted application can be shortlisted.
    public boolean shortlistApplication(Application application) {
        if (application == null) {
            return false;
        }
        if (!application.getStatus().equals(Application.SUBMITTED)) {
            return false;
        }
        application.shortlist();
        return true;
    }

    // Approves the application. It uses a slot and creates the scholar record.
    // The student's other pending applications become Withdrawn.
    // Gives null if it cannot be approved.
    public Scholar approveApplication(DataStore db, Application application) {
        if (application == null || !application.isPending()) {
            return null;
        }
        Scholarship scholarship = db.findScholarshipById(application.getScholarshipId());
        Student student = db.findStudentById(application.getStudentId());
        if (!owns(scholarship) || student == null) {
            return null;
        }
        // A student who already holds a scholarship cannot be approved
        if (db.findBlockingScholar(student) != null) {
            return null;
        }
        if (!hasFreeSlot(db, scholarship)) {
            return null;
        }
        application.decide(Application.APPROVED, application.getBenefactorComment(), db.getToday());
        Scholar scholar = db.createScholar(application);

        LinkedList<Application> others = db.applicationsOf(student);
        for (int i = 0; i < others.size(); i++) {
            Application other = others.get(i);
            if (other != application && other.isPending()) {
                other.withdraw();
            }
        }
        return scholar;
    }

    // Rejects a pending application
    public boolean rejectApplication(Application application, String today) {
        if (application == null || !application.isPending()) {
            return false;
        }
        application.decide(Application.REJECTED, application.getBenefactorComment(), today);
        return true;
    }

    // Waitlists a Submitted or Shortlisted application
    public boolean waitlistApplication(Application application, String today) {
        if (application == null || !application.isPending()) {
            return false;
        }
        if (application.getStatus().equals(Application.WAITLISTED)) {
            return false;
        }
        application.decide(Application.WAITLISTED, application.getBenefactorComment(), today);
        return true;
    }

    // ------------------------------------------------------------ renewal

    // Approves a renewal. The scholar stays Active or goes on Probation (a comment is needed).
    public boolean approveRenewal(DataStore db, Application application, boolean probation, String comment) {
        Scholar scholar = findRenewalScholar(db, application);
        if (scholar == null) {
            return false;
        }
        if (probation && isBlank(comment)) {
            return false;
        }
        Scholarship scholarship = db.findScholarshipById(application.getScholarshipId());
        if (!hasFreeSlot(db, scholarship)) {
            return false;
        }

        String text = "";
        if (!isBlank(comment)) {
            text = comment.trim();
        }
        String today = db.getToday();
        application.decide(Application.APPROVED, text, today);
        scholar.addRenewal();
        if (probation) {
            scholar.setStatus(Scholar.PROBATION, today);
        } else {
            scholar.setStatus(Scholar.ACTIVE, today);
        }
        return true;
    }

    // Rejects a renewal. The scholar becomes Not renewed.
    public boolean rejectRenewal(DataStore db, Application application, String comment) {
        Scholar scholar = findRenewalScholar(db, application);
        if (scholar == null) {
            return false;
        }
        String text = "";
        if (!isBlank(comment)) {
            text = comment.trim();
        }
        String today = db.getToday();
        application.decide(Application.REJECTED, text, today);
        scholar.setStatus(Scholar.NOT_RENEWED, today);
        return true;
    }

    // ------------------------------------------------------------ scholars and grants

    // Sets Active, Probation, Suspended or Completed.
    // Lapsed and Not renewed are set by the system.
    public boolean setScholarStatus(Scholar scholar, String status, String today) {
        if (scholar == null || status == null) {
            return false;
        }
        boolean allowed = false;
        if (status.equals(Scholar.ACTIVE)) {
            allowed = true;
        }
        if (status.equals(Scholar.PROBATION)) {
            allowed = true;
        }
        if (status.equals(Scholar.SUSPENDED)) {
            allowed = true;
        }
        if (status.equals(Scholar.COMPLETED)) {
            allowed = true;
        }
        if (!allowed) {
            return false;
        }
        scholar.setStatus(status, today);
        return true;
    }

    // Records a grant sent outside the system. It starts as Sent. Gives null if not valid.
    public Grant recordGrant(DataStore db, Scholar scholar, double amount,
                             String method, String referenceNo) {
        if (scholar == null || amount <= 0 || isBlank(method)) {
            return null;
        }
        Scholarship scholarship = db.findScholarshipById(scholar.getScholarshipId());
        if (!owns(scholarship)) {
            return null;
        }

        // The method must be GCash, Bank transfer or Cash
        String m = method.trim();
        boolean isCash = m.equalsIgnoreCase("Cash");
        boolean isGcash = m.equalsIgnoreCase("GCash");
        boolean isBank = m.equalsIgnoreCase("Bank transfer");
        if (!isCash && !isGcash && !isBank) {
            return null;
        }

        // Only cash can have no reference number
        String ref = "None";
        if (!isBlank(referenceNo)) {
            ref = referenceNo.trim();
        } else if (!isCash) {
            return null;
        }
        return db.createGrant(scholar, this, amount, m, ref);
    }

    // Voids a grant with a reason. A recorded grant is never edited or deleted.
    public boolean voidGrant(Grant grant, String reason) {
        if (grant == null || isBlank(reason)) {
            return false;
        }
        if (grant.getBenefactorId() != benefactorId) {
            return false;
        }
        if (grant.getStatus().equals(Grant.VOIDED)) {
            return false;
        }
        grant.voidWith(reason.trim());
        return true;
    }

    // Text for printing a Benefactor
    @Override
    public String toString() {
        return benefactorName + " (" + benefactorType + ")";
    }

    // ------------------------------------------------------------ getters

    public int getBenefactorId() {
        return benefactorId;
    }

    public String getBenefactorName() {
        return benefactorName;
    }

    public String getBenefactorType() {
        return benefactorType;
    }

    public String getDescription() {
        return description;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public String getAddress() {
        return address;
    }

    // ------------------------------------------------------------ private helpers

    // True if this Benefactor posted the scholarship
    private boolean owns(Scholarship scholarship) {
        if (scholarship == null) {
            return false;
        }
        return scholarship.getBenefactorId() == benefactorId;
    }

    // True if the approved applications of this cycle are fewer than the slots
    private boolean hasFreeSlot(DataStore db, Scholarship scholarship) {
        int approved = db.countApprovedInCycle(scholarship, scholarship.getCycleNo());
        return approved < scholarship.getSlots();
    }

    // Pending applications become Withdrawn (used when renewing or closing)
    private void withdrawPending(DataStore db, Scholarship scholarship) {
        LinkedList<Application> applications = db.applicationsForScholarship(scholarship);
        for (int i = 0; i < applications.size(); i++) {
            Application application = applications.get(i);
            if (application.isPending()) {
                application.withdraw();
            }
        }
    }

    // The scholar behind a pending renewal application. Gives null if none.
    private Scholar findRenewalScholar(DataStore db, Application application) {
        if (application == null || !application.isPending()) {
            return null;
        }
        Scholarship scholarship = db.findScholarshipById(application.getScholarshipId());
        Student student = db.findStudentById(application.getStudentId());
        if (!owns(scholarship) || student == null) {
            return null;
        }
        return db.findScholar(student, scholarship);
    }

    // True if the text is null or empty
    private boolean isBlank(String text) {
        if (text == null) {
            return true;
        }
        return text.trim().equals("");
    }

    // True if the text is a date like 2026-10-15
    private boolean isDate(String text) {
        if (text == null) {
            return false;
        }
        text = text.trim();
        if (text.length() != 10) {
            return false;
        }
        for (int i = 0; i < 10; i++) {
            char c = text.charAt(i);
            if (i == 4 || i == 7) {
                if (c != '-') {
                    return false;
                }
            } else if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    // Gives the old value of a field as text. Gives null if the field name is not known.
    private String getFieldValue(Scholarship s, String field) {
        if (field.equals("title")) {
            return s.getTitle();
        }
        if (field.equals("academicYear")) {
            return s.getAcademicYear();
        }
        if (field.equals("qualification")) {
            return s.getQualification();
        }
        if (field.equals("requirement")) {
            return s.getRequirement();
        }
        if (field.equals("description")) {
            return s.getDescription();
        }
        if (field.equals("slots")) {
            return String.valueOf(s.getSlots());
        }
        if (field.equals("deadline")) {
            return s.getDeadline();
        }
        if (field.equals("grantAmount")) {
            return String.valueOf(s.getGrantAmount());
        }
        return null;
    }

    // Sets a field from text. Gives false if the value is not valid.
    private boolean setFieldValue(Scholarship s, String field, String value) {
        if (field.equals("title")) {
            s.setTitle(value);
            return true;
        }
        if (field.equals("academicYear")) {
            s.setAcademicYear(value);
            return true;
        }
        if (field.equals("qualification")) {
            s.setQualification(value);
            return true;
        }
        if (field.equals("requirement")) {
            s.setRequirement(value);
            return true;
        }
        if (field.equals("description")) {
            s.setDescription(value);
            return true;
        }
        if (field.equals("slots")) {
            try {
                int slots = Integer.parseInt(value);
                if (slots < 1) {
                    return false;
                }
                s.setSlots(slots);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        if (field.equals("deadline")) {
            if (!isDate(value)) {
                return false;
            }
            s.setDeadline(value);
            return true;
        }
        if (field.equals("grantAmount")) {
            try {
                double amount = Double.parseDouble(value);
                if (amount < 0) {
                    return false;
                }
                s.setGrantAmount(amount);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }
}