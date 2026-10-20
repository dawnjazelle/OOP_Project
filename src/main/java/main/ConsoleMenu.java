package main;

import java.util.LinkedList;
import java.util.Scanner;

// The console screens.
// PART 1 of 2: start screen, sign-in, the shared menu, and the student screens.
// PART 2 goes right after this, in the same class: Benefactor screens, office screens,
// list helpers, input helpers, and the closing brace of the class.
public class ConsoleMenu {

    private DataStore db;
    private Scanner scanner;
    private int maxAttempts;
    private String adminUsername;
    private String adminPassword;

    public ConsoleMenu(DataStore db) {
        this.db = db;
        this.scanner = new Scanner(System.in);
        this.maxAttempts = 3;
        this.adminUsername = "admin";
        this.adminPassword = "admin1234";
    }

    // ------------------------------------------------------------ start and sign-in

    // Starts the program
    public void run() {
        boolean running = true;
        while (running) {
            printHeader("SCHOLARSHIP MANAGEMENT SYSTEM");
            System.out.println("1. Sign in as a student or Benefactor");
            System.out.println("2. Sign in as the office");
            System.out.println("0. Exit");
            int choice = readInt("Choose: ", 0, 1, 2);

            if (choice == 0) {
                running = false;
            } else if (choice == 1) {
                UserAccount account = signIn();
                if (account == null) {
                    System.out.println("Too many wrong tries.");
                } else if (account instanceof Student) {
                    showStudentMenu((Student) account);
                } else if (account instanceof Benefactor) {
                    showBenefactorMenu((Benefactor) account);
                }
            } else {
                if (signInOffice()) {
                    showOfficeMenu();
                } else {
                    System.out.println("Too many wrong tries.");
                }
            }
        }
        System.out.println("Goodbye.");
    }

    // Asks for the username and password. Gives null after too many wrong tries.
    private UserAccount signIn() {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            String username = readLine("Username: ");
            String password = readLine("Password: ");
            UserAccount account = db.findAccountByUsername(username);
            if (account != null && account.signIn(username, password, db.getToday())) {
                System.out.println("Welcome! You are signed in as " + account.getRole() + ".");
                return account;
            }
            System.out.println("Wrong username or password, or the account is disabled.");
        }
        return null;
    }

    // The office has no account record. It uses a separate admin login.
    private boolean signInOffice() {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            String username = readLine("Admin username: ");
            String password = readLine("Admin password: ");
            if (username.equals(adminUsername) && password.equals(adminPassword)) {
                System.out.println("Welcome, office.");
                return true;
            }
            System.out.println("Wrong admin username or password.");
        }
        return false;
    }

    // ------------------------------------------------------------ shared menu

    // Shows the menu of the signed-in user. Gives 0 for sign out.
    // The menu comes from getMenuOptions, so each kind of user gives its own menu (polymorphism).
    private int askMenuChoice(UserAccount account) {
        printHeader(account.getRole().toUpperCase() + " MENU - " + account.getUsername());
        String[] options = account.getMenuOptions();
        for (int i = 0; i < options.length; i++) {
            System.out.println((i + 1) + ". " + options[i]);
        }
        System.out.println((options.length + 1) + ". Sign out");

        int choice = readInt("Choose: ", 1, options.length + 1);
        if (choice == options.length + 1) {
            return 0;
        }
        return choice;
    }

    // ------------------------------------------------------------ student menu

    private void showStudentMenu(Student student) {
        boolean running = true;
        while (running) {
            int choice = askMenuChoice(student);

            if (choice == 1) {
                browseAndApply(student);
            } else if (choice == 2) {
                showMyApplications(student);
            } else if (choice == 3) {
                showMyScholarship(student);
            } else if (choice == 4) {
                showMyGrants(student);
            } else if (choice == 5) {
                showMyProfile(student);
            } else {
                running = false;
            }
        }
    }

    // ------------------------------------------------------------ student: browse and apply

    private void browseAndApply(Student student) {
        printHeader("BROWSE SCHOLARSHIPS");
        System.out.println("1. Show all open scholarships");
        System.out.println("2. Search by title or Benefactor name");
        System.out.println("3. Filter by Benefactor type, academic year or deadline");
        System.out.println("0. Back");
        int choice = readInt("Choose: ", 0, 3);
        if (choice == 0) {
            return;
        }

        LinkedList<Scholarship> list = student.browseScholarships(db);
        if (choice == 2) {
            String keyword = readLine("Keyword: ");
            list = db.searchScholarships(keyword);
        } else if (choice == 3) {
            String type = readLine("Benefactor type (Enter = any): ");
            String year = readLine("Academic year (Enter = any): ");
            String deadline = readLine("Deadline on or before yyyy-MM-dd (Enter = any): ");
            list = db.filterScholarships(list, type, year, deadline);
        }

        if (list.size() == 0) {
            System.out.println("No scholarships found.");
            return;
        }
        showOpenScholarships(list);

        int pick = readInt("Number to read and apply (0 = back): ", 0, list.size());
        if (pick == 0) {
            return;
        }
        Scholarship scholarship = list.get(pick - 1);
        showScholarshipDetails(scholarship);

        if (readYesNo("Apply to this scholarship?")) {
            applyTo(student, scholarship);
        }
    }

    // The list the students see. It shows the Benefactor of each scholarship.
    private void showOpenScholarships(LinkedList<Scholarship> list) {
        for (int i = 0; i < list.size(); i++) {
            Scholarship s = list.get(i);
            Benefactor owner = db.findBenefactorById(s.getBenefactorId());
            String ownerName = "Unknown";
            if (owner != null) {
                ownerName = owner.getBenefactorName();
            }
            System.out.println((i + 1) + ". " + s + " | by " + ownerName);
        }
    }

    // The details the student reads before applying, including the Benefactor page
    private void showScholarshipDetails(Scholarship s) {
        Benefactor owner = db.findBenefactorById(s.getBenefactorId());
        System.out.println();
        System.out.println("Title: " + s.getTitle());
        System.out.println("Academic year: " + s.getAcademicYear());
        System.out.println("Description: " + s.getDescription());
        System.out.println("Qualification: " + s.getQualification());
        System.out.println("Requirement: " + s.getRequirement());
        System.out.println("Slots: " + s.getSlots());
        System.out.println("Deadline: " + s.getDeadline());
        System.out.println("Grant amount: " + s.getGrantAmount());
        if (owner != null) {
            System.out.println("Benefactor: " + owner.getBenefactorName() + " (" + owner.getBenefactorType() + ")");
            System.out.println("About the Benefactor: " + owner.getDescription());
            System.out.println("Contact: " + owner.getContactEmail() + " | " + owner.getContactNumber());
        }
        System.out.println();
    }

    private void applyTo(Student student, Scholarship scholarship) {
        String problem = student.getApplyProblem(db, scholarship);
        if (!problem.equals("")) {
            System.out.println("Cannot apply: " + problem);
            return;
        }

        String file = readLine("File name of your one combined PDF: ");
        if (!file.toLowerCase().endsWith(".pdf")) {
            System.out.println("The file must be one combined PDF. The name must end with .pdf");
            return;
        }

        Application application = student.apply(db, scholarship, file);
        if (application == null) {
            System.out.println("Not submitted. Please try again.");
        } else {
            System.out.println("Submitted. Application ID " + application.getApplicationId()
                    + ", status " + application.getStatus() + ".");
        }
    }

    // ------------------------------------------------------------ student: applied scholarships

    private void showMyApplications(Student student) {
        printHeader("APPLIED SCHOLARSHIPS");
        LinkedList<Application> list = student.viewMyApplications(db);
        if (list.size() == 0) {
            System.out.println("You have not applied to any scholarship yet.");
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            Application a = list.get(i);
            Scholarship s = db.findScholarshipById(a.getScholarshipId());
            System.out.println((i + 1) + ". " + s.getTitle() + " | cycle " + a.getCycleNo()
                    + " | " + a.getStatus() + " | submitted " + a.getSubmittedOn());
            if (!a.getBenefactorComment().equals("")) {
                System.out.println("   Comment: " + a.getBenefactorComment());
            }
            if (a.getStatus().equals(Application.RETURNED)) {
                System.out.println("   Go to Browse scholarships and apply again.");
            }
        }

        int pick = readInt("Number to cancel (0 = back): ", 0, list.size());
        if (pick == 0) {
            return;
        }
        if (!readYesNo("Cancel this application?")) {
            return;
        }
        if (student.cancelApplication(list.get(pick - 1))) {
            System.out.println("Application cancelled.");
        } else {
            System.out.println("Only a pending application (Submitted, Shortlisted or Waitlisted) can be cancelled.");
        }
    }

    // ------------------------------------------------------------ student: my scholarship

    private void showMyScholarship(Student student) {
        printHeader("MY SCHOLARSHIP");
        Scholar scholar = student.viewMyScholarship(db);
        if (scholar == null) {
            System.out.println("You do not hold a scholarship now.");
            return;
        }

        Scholarship s = db.findScholarshipById(scholar.getScholarshipId());
        System.out.println("Scholarship: " + s.getTitle());
        System.out.println("Status: " + scholar.getStatus());
        System.out.println("Started: " + scholar.getStartDate());
        System.out.println("Ends: " + scholar.getEndDate());
        System.out.println("Renewals: " + scholar.getRenewalCount());
        System.out.println();
        System.out.println("To renew, go to Browse scholarships and apply to the renewed scholarship before its deadline.");
        System.out.println("You cannot apply to another scholarship while you hold this one.");
    }

    // ------------------------------------------------------------ student: my grants

    private void showMyGrants(Student student) {
        printHeader("MY GRANTS");
        LinkedList<Grant> grants = student.viewMyGrants(db);
        if (grants.size() == 0) {
            System.out.println("You have no grants yet.");
            return;
        }

        for (int i = 0; i < grants.size(); i++) {
            Grant g = grants.get(i);
            System.out.println((i + 1) + ". " + g + " | ref " + g.getReferenceNo());
            if (g.getStatus().equals(Grant.VOIDED)) {
                System.out.println("   Voided. Reason: " + g.getVoidReason());
            }
        }

        int pick = readInt("Number of the grant to answer (0 = back): ", 0, grants.size());
        if (pick == 0) {
            return;
        }
        Grant grant = grants.get(pick - 1);
        if (!grant.getStatus().equals(Grant.SENT)) {
            System.out.println("This grant is already " + grant.getStatus() + ". It cannot be answered again.");
            return;
        }

        System.out.println("1. I received it");
        System.out.println("2. I did not receive it");
        System.out.println("0. Back");
        int answer = readInt("Choose: ", 0, 2);
        if (answer == 1) {
            if (student.confirmGrant(grant, db.getToday())) {
                System.out.println("Confirmed. Thank you.");
            } else {
                System.out.println("This grant cannot be confirmed.");
            }
        } else if (answer == 2) {
            if (student.disputeGrant(grant)) {
                System.out.println("Disputed. Please contact the Benefactor by email.");
            } else {
                System.out.println("This grant cannot be disputed.");
            }
        }
    }

    // ------------------------------------------------------------ student: my profile

    // Works for any signed-in account
    private void showMyProfile(UserAccount account) {
        printHeader("MY PROFILE");
        System.out.println("Username: " + account.getUsername());
        System.out.println("Role: " + account.getRole());
        System.out.println("Status: " + account.getStatus());
        System.out.println("Last login: " + account.getLastLogin());
        System.out.println("Account created: " + account.getCreatedOn());

        if (account instanceof Student) {
            Student student = (Student) account;
            System.out.println("Student number: " + student.getStudentNumber());
            System.out.println("Name: " + student.getFullName());
            System.out.println("Course: " + student.getCourse());
            System.out.println("Email: " + student.getEmail());
            System.out.println("Contact number: " + student.getContactNumber());
        }

        if (!readYesNo("Change your password?")) {
            return;
        }
        String newPassword = readLine("New password: ");
        if (account.changePassword(newPassword)) {
            System.out.println("Password changed.");
        } else {
            System.out.println("Too short. Use at least " + UserAccount.MIN_PASSWORD_LENGTH + " characters.");
        }
    }

    // ------------------------------------------------------------ END OF PART 1
    // PART 2 continues here.