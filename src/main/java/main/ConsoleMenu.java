package main;

import java.util.Scanner;

// ConsoleMenu holds the text screens of the console program, and it also holds main,
// the place where the program starts.
// It reads the keyboard, shows the menu of the signed-in role, and calls the methods
// of the other classes. It keeps NO business rules: every rule is checked inside
// Student, Benefactor, Scholarship, Application, Scholar, Grant and DataStore.
// The screens only ask, call, and print what the classes answer.
//
public class ConsoleMenu {

    // ---------- Attributes ----------
    // They are private, so only this class can use them.

    private DataStore db;             // the DataStore the program uses
    private Scanner scanner;          // reads what the user types
    private int maxAttempts;          // how many sign-in tries are allowed
    private String adminUsername;     // username of the separate office admin login
    private String adminPassword;     // password of the separate office admin login


    // ---------- Constructor and start ----------

    // Purpose: make a ConsoleMenu that works on the given DataStore.
    // The office has no account record, so its admin login is kept here.
    public ConsoleMenu(DataStore db) {
        this.db = db;
        this.scanner = new Scanner(System.in);
        this.maxAttempts = 3;
        this.adminUsername = "admin";
        this.adminPassword = "admin1234";
    }

    // Purpose: start the program.
    // It makes the DataStore, loads the sample data, and shows the start screen.
    public static void main(String[] args) {
        DataStore db = new DataStore();
        db.loadSampleData();

        ConsoleMenu menu = new ConsoleMenu(db);
        menu.run();
    }

    // Purpose: show the start screen again and again until the user chooses Exit.
    // From here the user signs in as a student, a Benefactor or the office.
    // Each sign-in gets maxAttempts tries.
    public void run() {
        boolean running = true;
        while (running) {
            printHeader("SCHOLARSHIP APPLICATION AND BENEFICIARY MANAGEMENT SYSTEM");
            System.out.println("Date of the system: " + db.getToday());
            System.out.println("1. Sign in as a student or Benefactor");
            System.out.println("2. Sign in as the office");
            System.out.println("0. Exit");
            int choice = -1;
            while (choice < 0 || choice > 2) {
                System.out.print("Choose: ");
                try {
                    choice = Integer.parseInt(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    choice = -1;
                }
                boolean choiceOutOfRange = (choice < 0 || choice > 2);
                if (choiceOutOfRange) {
                    System.out.println("Please type a number from " + 0 + " to " + 2 + ".");
                }
            }

            if (choice == 0) {
                running = false;
            }

            if (choice == 1) {
                UserAccount account = signIn();
                boolean signedIn = (account != null);

                if (signedIn == false) {
                    System.out.println("Too many wrong tries. Back to the start screen.");
                } else {
                    // The account ID, the student ID and the Benefactor ID are the same number,
                    // so the ID of the account finds the Student or the Benefactor.
                    boolean isStudent = account.getRole().equals("Student");
                    if (isStudent) {
                        Student student = db.findStudentById(account.getAccountId());
                        showStudentMenu(student);
                    } else {
                        Benefactor benefactor = db.findBenefactorById(account.getAccountId());
                        showBenefactorMenu(benefactor);
                    }
                }
            }

            if (choice == 2) {
                // The office has no account record, so it is checked here against the admin login.
                boolean officeSignedIn = false;
                int attempts = 0;
                while (officeSignedIn == false && attempts < maxAttempts) {
                    System.out.print("Admin username: ");
                    String username = scanner.nextLine().trim();
                    System.out.print("Admin password: ");
                    String password = scanner.nextLine().trim();

                    boolean usernameIsCorrect = username.equals(adminUsername);
                    boolean passwordIsCorrect = password.equals(adminPassword);
                    if (usernameIsCorrect && passwordIsCorrect) {
                        officeSignedIn = true;
                    } else {
                        attempts = attempts + 1;
                        int triesLeft = maxAttempts - attempts;
                        System.out.println("Wrong username or password. Tries left: " + triesLeft);
                    }
                }

                if (officeSignedIn) {
                    showOfficeMenu();
                } else {
                    System.out.println("Too many wrong tries. Back to the start screen.");
                }
            }
        }
        System.out.println("Goodbye.");
    }

    // Purpose: sign in a student or a Benefactor.
    // The user gets maxAttempts tries. The UserAccount decides if the sign-in is allowed
    // (the account must be active, and the username and password must be correct).
    // Returns the account, or null after too many wrong tries.
    private UserAccount signIn() {
        printHeader("SIGN IN");

        int attempts = 0;
        while (attempts < maxAttempts) {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            UserAccount account = db.findAccountByUsername(username);
            boolean accountFound = (account != null);
            if (accountFound) {
                boolean allowed = account.signIn(username, password, db.getToday());
                if (allowed) {
                    return account;
                }
            }

            // The same message for every failure, so it does not tell which part was wrong.
            attempts = attempts + 1;
            int triesLeft = maxAttempts - attempts;
            System.out.println("Wrong username or password, or the account is disabled. Tries left: " + triesLeft);
        }
        return null;
    }


    // ---------- Student screens ----------

    // Purpose: show the menu of a signed-in student until the student signs out.
    // The menu items come from Student.getMenuOptions, in the same order:
    // 1 Browse scholarships, 2 My applied scholarships, 3 My scholarship, 4 My grants, 5 My profile.
    private void showStudentMenu(Student student) {
        boolean running = true;
        while (running) {
            printHeader("STUDENT MENU - " + student.getFullName());

            String[] options = student.getMenuOptions();
            for (int i = 0; i < options.length; i++) {
                System.out.println((i + 1) + ". " + options[i]);
            }
            System.out.println("0. Sign out");
            int choice = -1;
            while (choice < 0 || choice > options.length) {
                System.out.print("Choose: ");
                try {
                    choice = Integer.parseInt(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    choice = -1;
                }
                boolean choiceOutOfRange = (choice < 0 || choice > options.length);
                if (choiceOutOfRange) {
                    System.out.println("Please type a number from " + 0 + " to " + options.length + ".");
                }
            }

            if (choice == 0) {
                running = false;
            }
            if (choice == 1) {
                browseAndApply(student);
            }
            if (choice == 2) {
                showMyApplications(student);
            }
            if (choice == 3) {
                showMyScholarship(student);
            }
            if (choice == 4) {
                showMyGrants(student);
            }
            if (choice == 5) {
                showMyProfile(student);
            }
        }
    }

    // Purpose: show the open scholarships, let the student read one, and apply to it.
    // The student can also open the search and filter screen.
    // The rules of applying are checked by Student.getApplyProblem and Student.apply,
    // so this screen only shows the reason when the student cannot apply.
    private void browseAndApply(Student student) {
        boolean running = true;
        while (running) {
            printHeader("BROWSE SCHOLARSHIPS");

            Scholarship[] list = student.browseScholarships(db);
            if (list.length == 0) {
                System.out.println("There are no open scholarships now.");
                running = false;
            } else {
                for (int i = 0; i < list.length; i++) {
                    Scholarship s = list[i];
                    Benefactor owner = db.findBenefactorById(s.getBenefactorId());
                    String ownerName = "";
                    if (owner != null) {
                        ownerName = owner.getBenefactorName();
                    }
                    System.out.println("[" + (i + 1) + "] " + s.getTitle() + " - " + ownerName
                            + " (" + s.getAcademicYear() + ", cycle " + s.getCycleNo()
                            + ") - deadline " + s.getDeadline());
                }

                System.out.println();
                System.out.println("1. Read a scholarship and apply");
                System.out.println("2. Search and filter");
                System.out.println("0. Back");
                int action = -1;
                while (action < 0 || action > 2) {
                    System.out.print("Choose: ");
                    try {
                        action = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        action = -1;
                    }
                    boolean actionOutOfRange = (action < 0 || action > 2);
                    if (actionOutOfRange) {
                        System.out.println("Please type a number from " + 0 + " to " + 2 + ".");
                    }
                }

                if (action == 0) {
                    running = false;
                }

                if (action == 2) {
                    searchScholarships(student);
                }

                if (action == 1) {
                    int pick = -1;
                    while (pick < 0 || pick > list.length) {
                        System.out.print("Scholarship number (0 = cancel): ");
                        try {
                            pick = Integer.parseInt(scanner.nextLine().trim());
                        } catch (NumberFormatException e) {
                            pick = -1;
                        }
                        boolean pickOutOfRange = (pick < 0 || pick > list.length);
                        if (pickOutOfRange) {
                            System.out.println("Please type a number from " + 0 + " to " + list.length + ".");
                        }
                    }
                    if (pick > 0) {
                        Scholarship chosen = list[pick - 1];
                        Benefactor owner = db.findBenefactorById(chosen.getBenefactorId());
                        String ownerName = "";
                        String ownerCategory = "";
                        if (owner != null) {
                            ownerName = owner.getBenefactorName();
                            ownerCategory = owner.getCategory();
                        }

                        printHeader(chosen.getTitle());
                        System.out.println("Benefactor:    " + ownerName + " (" + ownerCategory + ")");
                        System.out.println("Academic year: " + chosen.getAcademicYear() + ", cycle " + chosen.getCycleNo());
                        System.out.println("Description:   " + chosen.getDescription());
                        System.out.println("Qualification: " + chosen.getQualification());
                        System.out.println("Requirement:   " + chosen.getRequirement());
                        System.out.println("Slots:         " + chosen.getSlots());
                        System.out.println("Grant amount:  " + String.format("%.2f", chosen.getGrantAmount()));
                        System.out.println("Deadline:      " + chosen.getDeadline());

                        // An empty problem text means the student can apply.
                        String problem = student.getApplyProblem(db, chosen);
                        boolean canApply = problem.equals("");
                        if (canApply == false) {
                            System.out.println();
                            System.out.println(problem);
                        } else {
                            boolean wantsToApply = false;
                            boolean answered = false;
                            while (answered == false) {
                                System.out.print("Apply to this scholarship? (y/n): ");
                                String text = scanner.nextLine().trim().toLowerCase();
                                boolean isYes = text.equals("y") || text.equals("yes");
                                boolean isNo = text.equals("n") || text.equals("no");
                                if (isYes) {
                                    wantsToApply = true;
                                    answered = true;
                                } else if (isNo) {
                                    answered = true;
                                } else {
                                    System.out.println("Please type y or n.");
                                }
                            }
                            if (wantsToApply) {
                                System.out.print("File name of your one combined PDF (example: ana.pdf): ");
                                String fileName = scanner.nextLine().trim();

                                boolean endsWithPdf = fileName.toLowerCase().endsWith(".pdf");
                                boolean hasAName = (fileName.length() > 4);
                                boolean isPdfName = endsWithPdf && hasAName;
                                if (isPdfName == false) {
                                    System.out.println("The file must be one PDF, so its name must end with .pdf.");
                                } else {
                                    int applicationId = student.apply(db, chosen, fileName);
                                    if (applicationId == 0) {
                                        System.out.println("The application could not be submitted.");
                                    } else {
                                        System.out.println("Application submitted. Its number is " + applicationId + ".");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Purpose: search the open scholarships by title or Benefactor name,
    // and filter them by Benefactor category, academic year or deadline.
    // A value left empty means "any". The deadline filter keeps the scholarships
    // that close on or before the date typed.
    // The student can pick a result to read it and apply, the same way as in the Browse list.
    // The rules of applying are checked by Student.getApplyProblem and Student.apply.
    private void searchScholarships(Student student) {
        printHeader("SEARCH AND FILTER SCHOLARSHIPS");
        System.out.println("Leave a value empty to skip it.");

        System.out.print("Title or Benefactor name: ");
        String keyword = scanner.nextLine().trim();
        System.out.print("Benefactor category (government, private company, foundation, alumni, LGU): ");
        String category = scanner.nextLine().trim();
        System.out.print("Academic year (example: 2026-2027): ");
        String year = scanner.nextLine().trim();
        System.out.print("Closing on or before (yyyy-MM-dd): ");
        String deadline = scanner.nextLine().trim();

        // A typed deadline must look like yyyy-MM-dd: 10 characters with a "-" after the year and the month.
        boolean deadlineIsEmpty = deadline.equals("");
        boolean deadlineLooksRight = false;
        if (deadlineIsEmpty == false) {
            boolean rightLength = (deadline.length() == 10);
            if (rightLength) {
                boolean dashAfterYear = (deadline.charAt(4) == '-');
                boolean dashAfterMonth = (deadline.charAt(7) == '-');
                deadlineLooksRight = dashAfterYear && dashAfterMonth;
            }
        }
        boolean deadlineIsOk = deadlineIsEmpty || deadlineLooksRight;
        if (deadlineIsOk == false) {
            System.out.println("The deadline must be written as yyyy-MM-dd, for example 2026-12-31.");
        } else {
            Scholarship[] found = db.searchScholarships(keyword);
            found = db.filterScholarships(found, category, year, deadline);

            System.out.println();
            if (found.length == 0) {
                System.out.println("No scholarship matches.");
            } else {
                System.out.println(found.length + " scholarship(s) found:");
                for (int i = 0; i < found.length; i++) {
                    Benefactor owner = db.findBenefactorById(found[i].getBenefactorId());
                    String ownerName = "";
                    if (owner != null) {
                        ownerName = owner.getBenefactorName();
                    }
                    System.out.println((i + 1) + ". " + found[i].getTitle() + " - " + ownerName
                            + " (" + found[i].getAcademicYear() + ") - deadline " + found[i].getDeadline());
                }

                boolean running = true;
                while (running) {
                    int pick = -1;
                    while (pick < 0 || pick > found.length) {
                        System.out.print("Scholarship number to read and apply (0 = back): ");
                        try {
                            pick = Integer.parseInt(scanner.nextLine().trim());
                        } catch (NumberFormatException e) {
                            pick = -1;
                        }
                        boolean pickOutOfRange = (pick < 0 || pick > found.length);
                        if (pickOutOfRange) {
                            System.out.println("Please type a number from 0 to " + found.length + ".");
                        }
                    }

                    if (pick == 0) {
                        running = false;
                    } else {
                        Scholarship chosen = found[pick - 1];
                        Benefactor chosenOwner = db.findBenefactorById(chosen.getBenefactorId());
                        String chosenOwnerName = "";
                        String chosenOwnerCategory = "";
                        if (chosenOwner != null) {
                            chosenOwnerName = chosenOwner.getBenefactorName();
                            chosenOwnerCategory = chosenOwner.getCategory();
                        }

                        printHeader(chosen.getTitle());
                        System.out.println("Benefactor:    " + chosenOwnerName + " (" + chosenOwnerCategory + ")");
                        System.out.println("Academic year: " + chosen.getAcademicYear() + ", cycle " + chosen.getCycleNo());
                        System.out.println("Description:   " + chosen.getDescription());
                        System.out.println("Qualification: " + chosen.getQualification());
                        System.out.println("Requirement:   " + chosen.getRequirement());
                        System.out.println("Slots:         " + chosen.getSlots());
                        System.out.println("Grant amount:  " + String.format("%.2f", chosen.getGrantAmount()));
                        System.out.println("Deadline:      " + chosen.getDeadline());

                        // An empty problem text means the student can apply.
                        String problem = student.getApplyProblem(db, chosen);
                        boolean canApply = problem.equals("");
                        if (canApply == false) {
                            System.out.println();
                            System.out.println(problem);
                        } else {
                            boolean wantsToApply = false;
                            boolean answered = false;
                            while (answered == false) {
                                System.out.print("Apply to this scholarship? (y/n): ");
                                String text = scanner.nextLine().trim().toLowerCase();
                                boolean isYes = text.equals("y") || text.equals("yes");
                                boolean isNo = text.equals("n") || text.equals("no");
                                if (isYes) {
                                    wantsToApply = true;
                                    answered = true;
                                } else if (isNo) {
                                    answered = true;
                                } else {
                                    System.out.println("Please type y or n.");
                                }
                            }

                            if (wantsToApply) {
                                System.out.print("File name of your one combined PDF (example: ana.pdf): ");
                                String fileName = scanner.nextLine().trim();

                                boolean endsWithPdf = fileName.toLowerCase().endsWith(".pdf");
                                boolean hasAName = (fileName.length() > 4);
                                boolean isPdfName = endsWithPdf && hasAName;
                                if (isPdfName == false) {
                                    System.out.println("The file must be one PDF, so its name must end with .pdf.");
                                } else {
                                    int applicationId = student.apply(db, chosen, fileName);
                                    if (applicationId == 0) {
                                        System.out.println("The application could not be submitted.");
                                    } else {
                                        System.out.println("Application submitted. Its number is " + applicationId + ".");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Purpose: show all the applications of the student, with their status and history.
    // Every attempt is its own record, so a returned application and the new one
    // after it are both shown. The student can cancel a pending application here.
    private void showMyApplications(Student student) {
        boolean running = true;
        while (running) {
            printHeader("MY APPLIED SCHOLARSHIPS");

            Application[] applications = student.viewMyApplications(db);
            if (applications.length == 0) {
                System.out.println("You have not applied to any scholarship yet.");
                running = false;
            } else {
                for (int i = 0; i < applications.length; i++) {
                    Application a = applications[i];

                    String title = "(scholarship not found)";
                    Scholarship scholarship = db.findScholarshipById(a.getScholarshipId());
                    if (scholarship != null) {
                        title = scholarship.getTitle();
                    }

                    System.out.println((i + 1) + ". " + title + ", cycle " + a.getCycleNo() + " - " + a.getStatus());
                    System.out.println("   File: " + a.getCredential() + ", submitted " + a.getSubmittedOn());

                    boolean wasDecided = (a.getDecidedOn().equals("") == false);
                    if (wasDecided) {
                        System.out.println("   Last decision: " + a.getDecidedOn());
                    }
                    boolean hasComment = (a.getBenefactorComment().equals("") == false);
                    if (hasComment) {
                        System.out.println("   Comment of the Benefactor: " + a.getBenefactorComment());
                    }
                    boolean wasReturned = a.getStatus().equals("Returned");
                    if (wasReturned) {
                        System.out.println("   This application is closed. To apply again, go to Browse scholarships.");
                    }
                }

                System.out.println();
                int pick = -1;
                while (pick < 0 || pick > applications.length) {
                    System.out.print("Number of the application to cancel (0 = back): ");
                    try {
                        pick = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        pick = -1;
                    }
                    boolean pickOutOfRange = (pick < 0 || pick > applications.length);
                    if (pickOutOfRange) {
                        System.out.println("Please type a number from " + 0 + " to " + applications.length + ".");
                    }
                }
                if (pick == 0) {
                    running = false;
                } else {
                    boolean cancelled = student.cancelApplication(applications[pick - 1]);
                    if (cancelled) {
                        System.out.println("The application was cancelled.");
                    } else {
                        System.out.println("Only a pending application (Submitted, Shortlisted or Waitlisted) can be cancelled.");
                    }
                }
            }
        }
    }

    // Purpose: show the scholarship the student holds now.
    // A student holds a scholarship while the scholar status is Active or Probation.
    private void showMyScholarship(Student student) {
        printHeader("MY SCHOLARSHIP");

        Scholar scholar = student.viewMyScholarship(db);
        boolean holdsOne = (scholar != null);
        if (holdsOne == false) {
            System.out.println("You do not hold a scholarship now.");
        } else {
            String title = "(scholarship not found)";
            String ownerName = "";
            Scholarship scholarship = db.findScholarshipById(scholar.getScholarshipId());
            if (scholarship != null) {
                title = scholarship.getTitle();
                Benefactor owner = db.findBenefactorById(scholarship.getBenefactorId());
                if (owner != null) {
                    ownerName = owner.getBenefactorName();
                }
            }

            System.out.println("Scholarship: " + title);
            System.out.println("Benefactor:  " + ownerName);
            System.out.println("Status:      " + scholar.getStatus());
            System.out.println("Start date:  " + scholar.getStartDate());
            System.out.println("Renewals:    " + scholar.getRenewalCount());

            boolean onProbation = scholar.getStatus().equals("Probation");
            if (onProbation) {
                System.out.println("You are on probation. The conditions are in the comment of your last renewal application.");
            }
        }
    }

    // Purpose: show the grants recorded for the student, and let the student
    // confirm that a grant was received, or dispute it.
    // Only a grant with the status Sent can be answered. The Grant class checks this.
    private void showMyGrants(Student student) {
        boolean running = true;
        while (running) {
            printHeader("MY GRANTS");

            Grant[] grants = student.viewMyGrants(db);
            if (grants.length == 0) {
                System.out.println("No grant was recorded for you yet.");
                running = false;
            } else {
                for (int i = 0; i < grants.length; i++) {
                    Grant g = grants[i];

                    String referenceText = "no reference";
                    boolean hasReference = (g.getReferenceNo().equals("") == false);
                    if (hasReference) {
                        referenceText = "ref " + g.getReferenceNo();
                    }

                    System.out.println((i + 1) + ". " + String.format("%.2f", g.getAmount()) + " by "
                            + g.getMethod() + " (" + referenceText + ") - " + g.getStatus());
                    System.out.println("   Sent on " + g.getDateSent());

                    boolean wasConfirmed = (g.getConfirmedOn().equals("") == false);
                    if (wasConfirmed) {
                        System.out.println("   Confirmed on " + g.getConfirmedOn());
                    }
                }

                System.out.println();
                int pick = -1;
                while (pick < 0 || pick > grants.length) {
                    System.out.print("Number of the grant to answer (0 = back): ");
                    try {
                        pick = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        pick = -1;
                    }
                    boolean pickOutOfRange = (pick < 0 || pick > grants.length);
                    if (pickOutOfRange) {
                        System.out.println("Please type a number from " + 0 + " to " + grants.length + ".");
                    }
                }
                if (pick == 0) {
                    running = false;
                } else {
                    Grant grant = grants[pick - 1];
                    boolean canAnswer = grant.getStatus().equals("Sent");
                    if (canAnswer == false) {
                        System.out.println("This grant is already " + grant.getStatus() + ", so it cannot be answered.");
                    } else {
                        System.out.println("1. I received it (confirm)");
                        System.out.println("2. I did not receive it, or it is wrong (dispute)");
                        System.out.println("0. Cancel");
                        int answer = -1;
                        while (answer < 0 || answer > 2) {
                            System.out.print("Choose: ");
                            try {
                                answer = Integer.parseInt(scanner.nextLine().trim());
                            } catch (NumberFormatException e) {
                                answer = -1;
                            }
                            boolean answerOutOfRange = (answer < 0 || answer > 2);
                            if (answerOutOfRange) {
                                System.out.println("Please type a number from " + 0 + " to " + 2 + ".");
                            }
                        }

                        if (answer == 1) {
                            boolean confirmed = student.confirmGrant(grant, db.getToday());
                            if (confirmed) {
                                System.out.println("The grant was confirmed.");
                            } else {
                                System.out.println("The grant could not be confirmed.");
                            }
                        }
                        if (answer == 2) {
                            boolean disputed = student.disputeGrant(grant);
                            if (disputed) {
                                System.out.println("The grant was marked as disputed.");
                            } else {
                                System.out.println("The grant could not be disputed.");
                            }
                        }
                    }
                }
            }
        }
    }

    // Purpose: show the profile of the signed-in account, and let the user change the password.
    // Changing the password is optional. The UserAccount decides if the new password is accepted.
    private void showMyProfile(UserAccount account) {
        boolean running = true;
        while (running) {
            printHeader("MY PROFILE");
            System.out.println("Username:   " + account.getUsername());
            System.out.println("Role:       " + account.getRole());
            System.out.println("Status:     " + account.getStatus());
            System.out.println("Created on: " + account.getCreatedOn());
            System.out.println("Last login: " + account.getLastLogin());

            // The account ID is the same number as the student ID, so it finds the student details.
            Student student = db.findStudentById(account.getAccountId());
            boolean isStudent = (student != null);
            if (isStudent) {
                System.out.println("Name:           " + student.getFullName());
                System.out.println("Student number: " + student.getStudentNumber());
                System.out.println("Email:          " + student.getEmail());
                System.out.println("Contact number: " + student.getContactNumber());
                System.out.println("Course:         " + student.getCourse());
            }

            System.out.println();
            System.out.println("1. Change my password");
            System.out.println("0. Back");
            int choice = -1;
            while (choice < 0 || choice > 1) {
                System.out.print("Choose: ");
                try {
                    choice = Integer.parseInt(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    choice = -1;
                }
                boolean choiceOutOfRange = (choice < 0 || choice > 1);
                if (choiceOutOfRange) {
                    System.out.println("Please type a number from " + 0 + " to " + 1 + ".");
                }
            }

            if (choice == 0) {
                running = false;
            }
            if (choice == 1) {
                System.out.print("New password (at least 4 characters): ");
                String newPassword = scanner.nextLine().trim();
                System.out.print("Type the new password again: ");
                String again = scanner.nextLine().trim();

                boolean sameTwice = newPassword.equals(again);
                if (sameTwice == false) {
                    System.out.println("The two passwords are not the same. The password was not changed.");
                } else {
                    boolean changed = account.changePassword(newPassword);
                    if (changed) {
                        System.out.println("Your password was changed.");
                    } else {
                        System.out.println("The password must have at least 4 characters. It was not changed.");
                    }
                }
            }
        }
    }


    // ---------- Benefactor screens ----------

    // Purpose: show the menu of a signed-in Benefactor until the Benefactor signs out.
    // The menu items come from Benefactor.getMenuOptions, in the same order:
    // 1 My page, 2 My scholarships, 3 Applicants, 4 My scholars, 5 Grants, 6 Summary.
    private void showBenefactorMenu(Benefactor benefactor) {
        boolean running = true;
        while (running) {
            printHeader("BENEFACTOR MENU - " + benefactor.getBenefactorName());

            String[] options = benefactor.getMenuOptions();
            for (int i = 0; i < options.length; i++) {
                System.out.println((i + 1) + ". " + options[i]);
            }
            System.out.println("0. Sign out");
            int choice = -1;
            while (choice < 0 || choice > options.length) {
                System.out.print("Choose: ");
                try {
                    choice = Integer.parseInt(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    choice = -1;
                }
                boolean choiceOutOfRange = (choice < 0 || choice > options.length);
                if (choiceOutOfRange) {
                    System.out.println("Please type a number from " + 0 + " to " + options.length + ".");
                }
            }

            if (choice == 0) {
                running = false;
            }
            if (choice == 1) {
                manageBenefactorPage(benefactor);
            }
            if (choice == 2) {
                manageScholarships(benefactor);
            }
            if (choice == 3) {
                reviewApplications(benefactor);
            }
            if (choice == 4) {
                manageScholars(benefactor);
            }
            if (choice == 5) {
                recordGrants(benefactor);
            }
            if (choice == 6) {
                showBenefactorSummary(benefactor);
            }
        }
    }

    // Purpose: show the page details of the Benefactor, and let the Benefactor change them.
    // A value left empty keeps the current one. The Benefactor class checks the new details.
    // From here the Benefactor can also open the account screen to change the password.
    private void manageBenefactorPage(Benefactor benefactor) {
        boolean running = true;
        while (running) {
            printHeader("MY PAGE");
            System.out.println("Name:           " + benefactor.getBenefactorName());
            System.out.println("Category:       " + benefactor.getCategory());
            System.out.println("Description:    " + benefactor.getDescription());
            System.out.println("Email:          " + benefactor.getEmail());
            System.out.println("Contact number: " + benefactor.getContactNumber());
            System.out.println("Address:        " + benefactor.getAddress());

            System.out.println();
            System.out.println("1. Change the page details");
            System.out.println("2. My account and password");
            System.out.println("0. Back");
            int choice = -1;
            while (choice < 0 || choice > 2) {
                System.out.print("Choose: ");
                try {
                    choice = Integer.parseInt(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    choice = -1;
                }
                boolean choiceOutOfRange = (choice < 0 || choice > 2);
                if (choiceOutOfRange) {
                    System.out.println("Please type a number from " + 0 + " to " + 2 + ".");
                }
            }

            if (choice == 0) {
                running = false;
            }
            if (choice == 2) {
                showMyProfile(benefactor);
            }
            if (choice == 1) {
                System.out.println("Leave a value empty to keep the current one.");
                System.out.print("Name: ");
                String newName = scanner.nextLine().trim();
                System.out.print("Description: ");
                String newDescription = scanner.nextLine().trim();
                System.out.print("Email: ");
                String newEmail = scanner.nextLine().trim();
                System.out.print("Contact number: ");
                String newContact = scanner.nextLine().trim();
                System.out.print("Address: ");
                String newAddress = scanner.nextLine().trim();

                String[] categories = {"government", "private company", "foundation", "alumni", "LGU"};
                System.out.println("Category:");
                System.out.println("0. Keep the current one");
                for (int i = 0; i < categories.length; i++) {
                    System.out.println((i + 1) + ". " + categories[i]);
                }
                int categoryPick = -1;
                while (categoryPick < 0 || categoryPick > categories.length) {
                    System.out.print("Choose: ");
                    try {
                        categoryPick = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        categoryPick = -1;
                    }
                    boolean categoryPickOutOfRange = (categoryPick < 0 || categoryPick > categories.length);
                    if (categoryPickOutOfRange) {
                        System.out.println("Please type a number from " + 0 + " to " + categories.length + ".");
                    }
                }

                // An empty value keeps the current one.
                String name = benefactor.getBenefactorName();
                if (newName.equals("") == false) {
                    name = newName;
                }
                String category = benefactor.getCategory();
                if (categoryPick > 0) {
                    category = categories[categoryPick - 1];
                }
                String description = benefactor.getDescription();
                if (newDescription.equals("") == false) {
                    description = newDescription;
                }
                String email = benefactor.getEmail();
                if (newEmail.equals("") == false) {
                    email = newEmail;
                }
                String contactNumber = benefactor.getContactNumber();
                if (newContact.equals("") == false) {
                    contactNumber = newContact;
                }
                String address = benefactor.getAddress();
                if (newAddress.equals("") == false) {
                    address = newAddress;
                }

                boolean updated = benefactor.updatePage(name, category, description, email, contactNumber, address);
                if (updated) {
                    System.out.println("The page was updated.");
                } else {
                    System.out.println("The page was not updated. The name, category, email, contact number and address cannot be empty.");
                }
            }
        }
    }

    // Purpose: list the scholarships of the Benefactor, and let the Benefactor post a new one,
    // edit one, renew one, close one, or see the edit history of one.
    // A new post waits for the office to approve it. The Benefactor class checks every value.
    private void manageScholarships(Benefactor benefactor) {
        boolean running = true;
        while (running) {
            printHeader("MY SCHOLARSHIPS");

            Scholarship[] list = db.scholarshipsOf(benefactor);
            if (list.length == 0) {
                System.out.println("You have not posted a scholarship yet.");
            }
            for (int i = 0; i < list.length; i++) {
                Scholarship s = list[i];
                System.out.println((i + 1) + ". " + s.getTitle() + " (" + s.getAcademicYear() + ", cycle " + s.getCycleNo()
                        + ") - deadline " + s.getDeadline() + " - " + s.getStatus() + ", " + s.getApprovalStatus());
                System.out.println("   Slots: " + s.getSlots() + ", grant amount " + String.format("%.2f", s.getGrantAmount()));
                if (s.isRenewalPending()) {
                    System.out.println("   Renewal waiting for the approval of the office.");
                }
            }

            System.out.println();
            System.out.println("1. Post a new scholarship");
            System.out.println("2. Edit a scholarship");
            System.out.println("3. Renew a scholarship");
            System.out.println("4. Close a scholarship");
            System.out.println("5. See the edit history of a scholarship");
            System.out.println("0. Back");
            int action = -1;
            while (action < 0 || action > 5) {
                System.out.print("Choose: ");
                try {
                    action = Integer.parseInt(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    action = -1;
                }
                boolean actionOutOfRange = (action < 0 || action > 5);
                if (actionOutOfRange) {
                    System.out.println("Please type a number from " + 0 + " to " + 5 + ".");
                }
            }

            if (action == 0) {
                running = false;
            }

            if (action == 1) {
                System.out.println("The new scholarship waits for the approval of the office before students can see it.");
                System.out.print("Title: ");
                String title = scanner.nextLine().trim();
                System.out.print("Academic year (example: 2026-2027): ");
                String academicYear = scanner.nextLine().trim();
                System.out.print("Qualification: ");
                String qualification = scanner.nextLine().trim();
                System.out.print("Requirement: ");
                String requirement = scanner.nextLine().trim();
                System.out.print("Description: ");
                String description = scanner.nextLine().trim();
                int slots = -1;
                while (slots < 1 || slots > 1000) {
                    System.out.print("Slots (at least 1): ");
                    try {
                        slots = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        slots = -1;
                    }
                    boolean slotsOutOfRange = (slots < 1 || slots > 1000);
                    if (slotsOutOfRange) {
                        System.out.println("Please type a number from " + 1 + " to " + 1000 + ".");
                    }
                }
                double grantAmount = 0;
                boolean grantAmountDone = false;
                while (grantAmountDone == false) {
                    System.out.print("Grant amount (0 if none): ");
                    try {
                        grantAmount = Double.parseDouble(scanner.nextLine().trim());
                        grantAmountDone = (grantAmount >= 0);
                    } catch (NumberFormatException e) {
                        grantAmountDone = false;
                    }
                    if (grantAmountDone == false) {
                        System.out.println("Please type a number of 0 or more.");
                    }
                }
                System.out.print("Deadline (yyyy-MM-dd): ");
                String deadline = scanner.nextLine().trim();

                int newId = benefactor.postScholarship(db, title, academicYear, qualification,
                        requirement, description, slots, grantAmount, deadline);
                if (newId == 0) {
                    System.out.println("The scholarship was not posted. No text can be empty, and the deadline must be a real date (yyyy-MM-dd) that is today or later.");
                } else {
                    System.out.println("The scholarship was posted with the number " + newId + ". It is Pending until the office approves it.");
                }
            }

            // Edit, renew, close and the edit history all work on one scholarship of the list.
            boolean needsScholarship = (action >= 2) && (action <= 5);
            if (needsScholarship && list.length == 0) {
                System.out.println("You have no scholarship yet.");
            }
            if (needsScholarship && list.length > 0) {
                int pick = -1;
                while (pick < 0 || pick > list.length) {
                    System.out.print("Scholarship number (0 = cancel): ");
                    try {
                        pick = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        pick = -1;
                    }
                    boolean pickOutOfRange = (pick < 0 || pick > list.length);
                    if (pickOutOfRange) {
                        System.out.println("Please type a number from " + 0 + " to " + list.length + ".");
                    }
                }

                if (pick > 0) {
                    Scholarship chosen = list[pick - 1];

                    if (action == 2) {
                        String[] fields = {"title", "academicYear", "qualification", "requirement",
                                "description", "slots", "deadline", "grantAmount"};
                        String[] labels = {"Title", "Academic year", "Qualification", "Requirement",
                                "Description", "Slots", "Deadline (yyyy-MM-dd)", "Grant amount"};
                        for (int i = 0; i < labels.length; i++) {
                            System.out.println((i + 1) + ". " + labels[i]);
                        }
                        int fieldPick = -1;
                        while (fieldPick < 0 || fieldPick > fields.length) {
                            System.out.print("Field to change (0 = cancel): ");
                            try {
                                fieldPick = Integer.parseInt(scanner.nextLine().trim());
                            } catch (NumberFormatException e) {
                                fieldPick = -1;
                            }
                            boolean fieldPickOutOfRange = (fieldPick < 0 || fieldPick > fields.length);
                            if (fieldPickOutOfRange) {
                                System.out.println("Please type a number from " + 0 + " to " + fields.length + ".");
                            }
                        }

                        if (fieldPick > 0) {
                            System.out.print("New value: ");
                            String newValue = scanner.nextLine().trim();
                            boolean changed = benefactor.editScholarship(db, chosen, fields[fieldPick - 1], newValue);
                            if (changed) {
                                System.out.println("The change was saved in the edit history.");
                            } else {
                                System.out.println("The change was not saved. The value cannot be empty, slots must be at least 1, the grant amount must be 0 or more, and a deadline must be a real date that is today or later.");
                            }
                        }
                    }

                    if (action == 3) {
                        boolean renewed = benefactor.renewScholarship(db, chosen);
                        if (renewed) {
                            System.out.println("The renewal started. Its pending applications were withdrawn, and the scholarship waits for the office to approve it again. You can edit its details now.");
                        } else {
                            System.out.println("It cannot be renewed. Only an approved and open scholarship can be renewed, one renewal at a time.");
                        }
                    }

                    if (action == 4) {
                        boolean sure = false;
                        boolean sureAnswered = false;
                        while (sureAnswered == false) {
                            System.out.print("Close this scholarship? Its pending applications are withdrawn and its Active and Probation scholars become Completed. (y/n): ");
                            String sureText = scanner.nextLine().trim().toLowerCase();
                            boolean sureIsYes = sureText.equals("y") || sureText.equals("yes");
                            boolean sureIsNo = sureText.equals("n") || sureText.equals("no");
                            if (sureIsYes) {
                                sure = true;
                                sureAnswered = true;
                            } else if (sureIsNo) {
                                sureAnswered = true;
                            } else {
                                System.out.println("Please type y or n.");
                            }
                        }
                        if (sure) {
                            boolean closed = benefactor.closeScholarship(db, chosen);
                            if (closed) {
                                System.out.println("The scholarship was closed.");
                            } else {
                                System.out.println("It could not be closed. It may be closed already.");
                            }
                        }
                    }

                    if (action == 5) {
                        ScholarshipEditHistory[] history = chosen.getEditHistory();
                        if (history.length == 0) {
                            System.out.println("This scholarship has no change yet.");
                        }
                        for (int i = 0; i < history.length; i++) {
                            System.out.println(history[i].getEditedOn() + ": " + history[i].getFieldName()
                                    + " changed from [" + history[i].getOldValue() + "] to [" + history[i].getNewValue() + "]");
                        }
                    }
                }
            }
        }
    }

    // Purpose: let the Benefactor review the applications of one of the own scholarships.
    // The Benefactor can return one with a comment, shortlist, approve, reject or waitlist it.
    // A renewal application of a current scholar is approved (with or without probation) or rejected here too.
    // The rules of each decision are checked by the Benefactor and Application classes.
    private void reviewApplications(Benefactor benefactor) {
        boolean running = true;
        while (running) {
            printHeader("APPLICANTS");
            System.out.println("1. Review the applications of a scholarship");
            System.out.println("2. Search and filter my applications");
            System.out.println("0. Back");
            int action = -1;
            while (action < 0 || action > 2) {
                System.out.print("Choose: ");
                try {
                    action = Integer.parseInt(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    action = -1;
                }
                boolean actionOutOfRange = (action < 0 || action > 2);
                if (actionOutOfRange) {
                    System.out.println("Please type a number from " + 0 + " to " + 2 + ".");
                }
            }

            if (action == 0) {
                running = false;
            }
            if (action == 2) {
                filterApplications(benefactor);
            }

            if (action == 1) {
                Scholarship[] list = db.scholarshipsOf(benefactor);
                if (list.length == 0) {
                    System.out.println("You have not posted a scholarship yet.");
                } else {
                    for (int i = 0; i < list.length; i++) {
                        int waiting = db.countApplicationsByStatus(list[i], "Submitted")
                                + db.countApplicationsByStatus(list[i], "Shortlisted")
                                + db.countApplicationsByStatus(list[i], "Waitlisted");
                        System.out.println((i + 1) + ". " + list[i].getTitle() + " (cycle " + list[i].getCycleNo()
                                + ") - " + waiting + " waiting for a decision");
                    }
                    int pick = -1;
                    while (pick < 0 || pick > list.length) {
                        System.out.print("Scholarship number (0 = back): ");
                        try {
                            pick = Integer.parseInt(scanner.nextLine().trim());
                        } catch (NumberFormatException e) {
                            pick = -1;
                        }
                        boolean pickOutOfRange = (pick < 0 || pick > list.length);
                        if (pickOutOfRange) {
                            System.out.println("Please type a number from " + 0 + " to " + list.length + ".");
                        }
                    }

                    if (pick > 0) {
                        Scholarship chosen = list[pick - 1];
                        boolean reviewing = true;
                        while (reviewing) {
                            printHeader("APPLICATIONS - " + chosen.getTitle());

                            Application[] apps = db.applicationsForScholarship(chosen);
                            if (apps.length == 0) {
                                System.out.println("There is no application yet.");
                                reviewing = false;
                            } else {
                                for (int i = 0; i < apps.length; i++) {
                                    Student student = db.findStudentById(apps[i].getStudentId());
                                    String studentText = "(student not found)";
                                    if (student != null) {
                                        studentText = student.toString();
                                    }
                                    System.out.println((i + 1) + ". " + studentText);
                                    System.out.println("   Cycle " + apps[i].getCycleNo() + " - " + apps[i].getStatus()
                                            + " - file " + apps[i].getCredential() + " - submitted " + apps[i].getSubmittedOn());
                                    boolean hasComment = (apps[i].getBenefactorComment().equals("") == false);
                                    if (hasComment) {
                                        System.out.println("   Your comment: " + apps[i].getBenefactorComment());
                                    }
                                }

                                System.out.println();
                                int appPick = -1;
                                while (appPick < 0 || appPick > apps.length) {
                                    System.out.print("Application number to decide (0 = back): ");
                                    try {
                                        appPick = Integer.parseInt(scanner.nextLine().trim());
                                    } catch (NumberFormatException e) {
                                        appPick = -1;
                                    }
                                    boolean appPickOutOfRange = (appPick < 0 || appPick > apps.length);
                                    if (appPickOutOfRange) {
                                        System.out.println("Please type a number from " + 0 + " to " + apps.length + ".");
                                    }
                                }

                                if (appPick == 0) {
                                    reviewing = false;
                                } else {
                                    Application application = apps[appPick - 1];
                                    Student applicant = db.findStudentById(application.getStudentId());

                                    boolean isPending = application.isPending();
                                    boolean recordIsMissing = (applicant == null);
                                    if (isPending == false) {
                                        System.out.println("This application is already " + application.getStatus() + ", so it cannot be decided again.");
                                    } else if (recordIsMissing) {
                                        System.out.println("The student of this application was not found.");
                                    } else {
                                        // A renewal is an application of a student who still holds this scholarship.
                                        Scholar holder = db.findScholar(applicant, chosen);
                                        boolean holdsIt = false;
                                        if (holder != null) {
                                            holdsIt = holder.blocksApplying();
                                        }
                                        boolean isRenewal = (application.getCycleNo() >= 2) && holdsIt;

                                        printHeader(applicant.getFullName());
                                        System.out.println("Student number: " + applicant.getStudentNumber() + ", " + applicant.getCourse());
                                        System.out.println("Email: " + applicant.getEmail() + ", contact number " + applicant.getContactNumber());
                                        System.out.println("File: " + application.getCredential() + " (" + application.getStatus() + ")");
                                        if (isRenewal) {
                                            System.out.println("This is a renewal application of a current scholar.");
                                        }
                                        System.out.println();
                                        System.out.println("1. Return for correction (with a comment)");
                                        System.out.println("2. Shortlist");
                                        System.out.println("3. Approve");
                                        System.out.println("4. Reject");
                                        System.out.println("5. Waitlist");
                                        System.out.println("0. Cancel");
                                        int decision = -1;
                                        while (decision < 0 || decision > 5) {
                                            System.out.print("Choose: ");
                                            try {
                                                decision = Integer.parseInt(scanner.nextLine().trim());
                                            } catch (NumberFormatException e) {
                                                decision = -1;
                                            }
                                            boolean decisionOutOfRange = (decision < 0 || decision > 5);
                                            if (decisionOutOfRange) {
                                                System.out.println("Please type a number from " + 0 + " to " + 5 + ".");
                                            }
                                        }

                                        if (decision == 1) {
                                            System.out.print("Comment (what is missing): ");
                                            String returnComment = scanner.nextLine().trim();
                                            boolean returned = benefactor.returnApplication(application, returnComment, db.getToday());
                                            if (returned) {
                                                System.out.println("The application was returned. The student can apply again.");
                                            } else {
                                                System.out.println("It was not returned. The comment cannot be empty, and only a Submitted application can be returned.");
                                            }
                                        }

                                        if (decision == 2) {
                                            boolean shortlisted = benefactor.shortlistApplication(application);
                                            if (shortlisted) {
                                                System.out.println("The application was shortlisted.");
                                            } else {
                                                System.out.println("It was not shortlisted. Only a Submitted application can be shortlisted.");
                                            }
                                        }

                                        if (decision == 3) {
                                            if (isRenewal) {
                                                boolean probation = false;
                                                boolean probationAnswered = false;
                                                while (probationAnswered == false) {
                                                    System.out.print("Place the scholar on probation? (y/n): ");
                                                    String probationText = scanner.nextLine().trim().toLowerCase();
                                                    boolean probationIsYes = probationText.equals("y") || probationText.equals("yes");
                                                    boolean probationIsNo = probationText.equals("n") || probationText.equals("no");
                                                    if (probationIsYes) {
                                                        probation = true;
                                                        probationAnswered = true;
                                                    } else if (probationIsNo) {
                                                        probationAnswered = true;
                                                    } else {
                                                        System.out.println("Please type y or n.");
                                                    }
                                                }
                                                String conditions = "";
                                                if (probation) {
                                                    System.out.print("Conditions of the probation: ");
                                                    conditions = scanner.nextLine().trim();
                                                }
                                                boolean renewalApproved = benefactor.approveRenewal(db, application, probation, conditions);
                                                if (renewalApproved) {
                                                    System.out.println("The renewal was approved.");
                                                } else {
                                                    System.out.println("The renewal was not approved. A probation needs its conditions, and a slot must be left.");
                                                }
                                            } else {
                                                int scholarId = benefactor.approveApplication(db, application);
                                                if (scholarId == 0) {
                                                    System.out.println("It was not approved. The student may already hold a scholarship, or no slot is left.");
                                                } else {
                                                    System.out.println("The application was approved. The scholar record number is " + scholarId + ".");
                                                }
                                            }
                                        }

                                        if (decision == 4) {
                                            if (isRenewal) {
                                                System.out.print("Comment (can be empty): ");
                                                String rejectComment = scanner.nextLine().trim();
                                                boolean renewalRejected = benefactor.rejectRenewal(db, application, rejectComment);
                                                if (renewalRejected) {
                                                    System.out.println("The renewal was rejected. The scholar is now Not renewed.");
                                                } else {
                                                    System.out.println("The renewal could not be rejected.");
                                                }
                                            } else {
                                                boolean rejected = benefactor.rejectApplication(application, db.getToday());
                                                if (rejected) {
                                                    System.out.println("The application was rejected.");
                                                } else {
                                                    System.out.println("The application could not be rejected.");
                                                }
                                            }
                                        }

                                        if (decision == 5) {
                                            boolean waitlisted = benefactor.waitlistApplication(application, db.getToday());
                                            if (waitlisted) {
                                                System.out.println("The application was put on the waiting list.");
                                            } else {
                                                System.out.println("It could not be waitlisted. Only a Submitted or Shortlisted application can be waitlisted.");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Purpose: search and filter the applications of the Benefactor.
    // The Benefactor types a student name or student number, and chooses a scholarship and a status.
    // Choosing 0 (Any) or leaving the text empty skips that filter.
    // This screen only shows the results. It never changes a record.
    private void filterApplications(Benefactor benefactor) {
        printHeader("SEARCH AND FILTER MY APPLICATIONS");
        System.out.println("Leave the text empty, or choose 0, to skip a filter.");
        System.out.print("Student name or student number: ");
        String keyword = scanner.nextLine().trim();

        Scholarship[] mine = db.scholarshipsOf(benefactor);
        System.out.println("Scholarship:");
        System.out.println("0. Any");
        for (int i = 0; i < mine.length; i++) {
            System.out.println((i + 1) + ". " + mine[i].getTitle() + " (cycle " + mine[i].getCycleNo() + ")");
        }
        int scholarshipPick = -1;
        while (scholarshipPick < 0 || scholarshipPick > mine.length) {
            System.out.print("Choose: ");
            try {
                scholarshipPick = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                scholarshipPick = -1;
            }
            boolean scholarshipPickOutOfRange = (scholarshipPick < 0 || scholarshipPick > mine.length);
            if (scholarshipPickOutOfRange) {
                System.out.println("Please type a number from " + 0 + " to " + mine.length + ".");
            }
        }
        Scholarship wantedScholarship = null;
        if (scholarshipPick > 0) {
            wantedScholarship = mine[scholarshipPick - 1];
        }

        String[] statuses = {"Submitted", "Shortlisted", "Returned", "Approved",
                "Rejected", "Waitlisted", "Cancelled", "Withdrawn"};
        System.out.println("Status:");
        System.out.println("0. Any");
        for (int i = 0; i < statuses.length; i++) {
            System.out.println((i + 1) + ". " + statuses[i]);
        }
        int statusPick = -1;
        while (statusPick < 0 || statusPick > statuses.length) {
            System.out.print("Choose: ");
            try {
                statusPick = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                statusPick = -1;
            }
            boolean statusPickOutOfRange = (statusPick < 0 || statusPick > statuses.length);
            if (statusPickOutOfRange) {
                System.out.println("Please type a number from " + 0 + " to " + statuses.length + ".");
            }
        }
        String wantedStatus = "";
        if (statusPick > 0) {
            wantedStatus = statuses[statusPick - 1];
        }

        Application[] found = db.filterAllApplications(benefactor, wantedScholarship, null, wantedStatus);
        found = db.searchApplications(found, keyword);

        System.out.println();
        System.out.println(found.length + " application(s) found.");
        for (int i = 0; i < found.length; i++) {
            Student student = db.findStudentById(found[i].getStudentId());
            String studentText = "(student not found)";
            if (student != null) {
                studentText = student.toString();
            }
            String title = "(scholarship not found)";
            Scholarship scholarship = db.findScholarshipById(found[i].getScholarshipId());
            if (scholarship != null) {
                title = scholarship.getTitle();
            }
            System.out.println((i + 1) + ". " + studentText + " - " + title + ", cycle " + found[i].getCycleNo()
                    + " - " + found[i].getStatus());
            System.out.println("   File: " + found[i].getCredential() + ", submitted " + found[i].getSubmittedOn());
            boolean hasComment = (found[i].getBenefactorComment().equals("") == false);
            if (hasComment) {
                System.out.println("   Comment: " + found[i].getBenefactorComment());
            }
        }
    }

    // Purpose: list and search the scholars of the Benefactor, and let the Benefactor set a status.
    // The Benefactor can set Active, Probation, Suspended or Completed.
    // Lapsed and Not renewed are set by the system. The Benefactor and Scholar classes check each change.
    private void manageScholars(Benefactor benefactor) {
        boolean running = true;
        while (running) {
            printHeader("MY SCHOLARS");
            System.out.println("1. Search, filter and list my scholars (and set a status)");
            System.out.println("2. Check for lapsed scholars");
            System.out.println("0. Back");
            int action = -1;
            while (action < 0 || action > 2) {
                System.out.print("Choose: ");
                try {
                    action = Integer.parseInt(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    action = -1;
                }
                boolean actionOutOfRange = (action < 0 || action > 2);
                if (actionOutOfRange) {
                    System.out.println("Please type a number from " + 0 + " to " + 2 + ".");
                }
            }

            if (action == 0) {
                running = false;
            }
            if (action == 2) {
                checkLapsedScholars(benefactor);
            }

            if (action == 1) {
                System.out.println("Leave the text empty, or choose 0, to skip a filter.");
                System.out.print("Student name or student number: ");
                String keyword = scanner.nextLine().trim();

                Scholarship[] mine = db.scholarshipsOf(benefactor);
                System.out.println("Scholarship:");
                System.out.println("0. Any");
                for (int i = 0; i < mine.length; i++) {
                    System.out.println((i + 1) + ". " + mine[i].getTitle() + " (cycle " + mine[i].getCycleNo() + ")");
                }
                int scholarshipPick = -1;
                while (scholarshipPick < 0 || scholarshipPick > mine.length) {
                    System.out.print("Choose: ");
                    try {
                        scholarshipPick = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        scholarshipPick = -1;
                    }
                    boolean scholarshipPickOutOfRange = (scholarshipPick < 0 || scholarshipPick > mine.length);
                    if (scholarshipPickOutOfRange) {
                        System.out.println("Please type a number from " + 0 + " to " + mine.length + ".");
                    }
                }
                Scholarship wantedScholarship = null;
                if (scholarshipPick > 0) {
                    wantedScholarship = mine[scholarshipPick - 1];
                }

                String[] statuses = {"Active", "Probation", "Suspended", "Lapsed", "Not renewed", "Completed"};
                System.out.println("Status:");
                System.out.println("0. Any");
                for (int i = 0; i < statuses.length; i++) {
                    System.out.println((i + 1) + ". " + statuses[i]);
                }
                int statusPick = -1;
                while (statusPick < 0 || statusPick > statuses.length) {
                    System.out.print("Choose: ");
                    try {
                        statusPick = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        statusPick = -1;
                    }
                    boolean statusPickOutOfRange = (statusPick < 0 || statusPick > statuses.length);
                    if (statusPickOutOfRange) {
                        System.out.println("Please type a number from " + 0 + " to " + statuses.length + ".");
                    }
                }
                String wantedStatus = "";
                if (statusPick > 0) {
                    wantedStatus = statuses[statusPick - 1];
                }

                boolean listing = true;
                while (listing) {
                    Scholar[] found = db.filterAllScholars(benefactor, wantedScholarship, null, wantedStatus);
                    found = db.searchScholars(found, keyword);

                    System.out.println();
                    if (found.length == 0) {
                        System.out.println("No scholar matches.");
                        listing = false;
                    } else {
                        System.out.println(found.length + " scholar(s) found:");
                        for (int i = 0; i < found.length; i++) {
                            Student student = db.findStudentById(found[i].getStudentId());
                            String studentText = "(student not found)";
                            if (student != null) {
                                studentText = student.toString();
                            }
                            String title = "(scholarship not found)";
                            Scholarship scholarship = db.findScholarshipById(found[i].getScholarshipId());
                            if (scholarship != null) {
                                title = scholarship.getTitle();
                            }
                            System.out.println((i + 1) + ". " + studentText + " - " + title);
                            String endText = "";
                            boolean hasEnded = (found[i].getEndDate().equals("") == false);
                            if (hasEnded) {
                                endText = ", ended " + found[i].getEndDate();
                            }
                            System.out.println("   " + found[i].getStatus() + ", " + found[i].getRenewalCount()
                                    + " renewal(s), since " + found[i].getStartDate() + endText);
                        }

                        int scholarPick = -1;
                        while (scholarPick < 0 || scholarPick > found.length) {
                            System.out.print("Scholar number to set a status (0 = back): ");
                            try {
                                scholarPick = Integer.parseInt(scanner.nextLine().trim());
                            } catch (NumberFormatException e) {
                                scholarPick = -1;
                            }
                            boolean scholarPickOutOfRange = (scholarPick < 0 || scholarPick > found.length);
                            if (scholarPickOutOfRange) {
                                System.out.println("Please type a number from " + 0 + " to " + found.length + ".");
                            }
                        }
                        if (scholarPick == 0) {
                            listing = false;
                        } else {
                            String[] newStatuses = {"Active", "Probation", "Suspended", "Completed"};
                            for (int i = 0; i < newStatuses.length; i++) {
                                System.out.println((i + 1) + ". " + newStatuses[i]);
                            }
                            System.out.println("0. Cancel");
                            int newStatusPick = -1;
                            while (newStatusPick < 0 || newStatusPick > newStatuses.length) {
                                System.out.print("New status: ");
                                try {
                                    newStatusPick = Integer.parseInt(scanner.nextLine().trim());
                                } catch (NumberFormatException e) {
                                    newStatusPick = -1;
                                }
                                boolean newStatusPickOutOfRange = (newStatusPick < 0 || newStatusPick > newStatuses.length);
                                if (newStatusPickOutOfRange) {
                                    System.out.println("Please type a number from " + 0 + " to " + newStatuses.length + ".");
                                }
                            }

                            if (newStatusPick > 0) {
                                boolean changed = benefactor.setScholarStatus(found[scholarPick - 1],
                                        newStatuses[newStatusPick - 1], db.getToday());
                                if (changed) {
                                    System.out.println("The status was changed to " + newStatuses[newStatusPick - 1] + ".");
                                } else {
                                    System.out.println("The status was not changed. The scholarship of this scholar already ended.");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Purpose: run the lapsed check for every scholarship of the Benefactor.
    // After the deadline of a renewed scholarship, a scholar who did not apply for the renewal
    // is marked Lapsed, and the student is free to apply to other scholarships.
    // The DataStore decides who is marked. This screen only shows how many.
    private void checkLapsedScholars(Benefactor benefactor) {
        printHeader("CHECK FOR LAPSED SCHOLARS");
        System.out.println("A scholar who did not apply by the deadline of the renewed scholarship is marked Lapsed.");

        Scholarship[] mine = db.scholarshipsOf(benefactor);
        int totalLapsed = 0;
        for (int i = 0; i < mine.length; i++) {
            int lapsed = db.markLapsedScholars(mine[i], db.getToday());
            if (lapsed > 0) {
                System.out.println(mine[i].getTitle() + ": " + lapsed + " scholar(s) marked as Lapsed.");
            }
            totalLapsed = totalLapsed + lapsed;
        }

        if (totalLapsed == 0) {
            System.out.println("No scholar was marked as Lapsed.");
        }
    }

    // Purpose: let the Benefactor record a grant that was sent to a scholar OUTSIDE the system,
    // and void a wrong grant record. The system never sends or holds money.
    // A recorded grant is never edited or deleted. The Benefactor class checks every value.
    private void recordGrants(Benefactor benefactor) {
        boolean running = true;
        while (running) {
            printHeader("GRANTS");
            System.out.println("The system does not send money. Send the grant outside the system, then record it here.");
            System.out.println("1. Record a grant");
            System.out.println("2. See my grants and void one");
            System.out.println("0. Back");
            int action = -1;
            while (action < 0 || action > 2) {
                System.out.print("Choose: ");
                try {
                    action = Integer.parseInt(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    action = -1;
                }
                boolean actionOutOfRange = (action < 0 || action > 2);
                if (actionOutOfRange) {
                    System.out.println("Please type a number from " + 0 + " to " + 2 + ".");
                }
            }

            if (action == 0) {
                running = false;
            }

            if (action == 1) {
                Scholar[] myScholars = db.filterAllScholars(benefactor, null, null, "");
                if (myScholars.length == 0) {
                    System.out.println("You have no scholar yet.");
                } else {
                    for (int i = 0; i < myScholars.length; i++) {
                        Student student = db.findStudentById(myScholars[i].getStudentId());
                        String studentText = "(student not found)";
                        if (student != null) {
                            studentText = student.toString();
                        }
                        String title = "(scholarship not found)";
                        Scholarship scholarship = db.findScholarshipById(myScholars[i].getScholarshipId());
                        if (scholarship != null) {
                            title = scholarship.getTitle();
                        }
                        System.out.println((i + 1) + ". " + studentText + " - " + title + " - " + myScholars[i].getStatus());
                    }
                    int pick = -1;
                    while (pick < 0 || pick > myScholars.length) {
                        System.out.print("Scholar number (0 = cancel): ");
                        try {
                            pick = Integer.parseInt(scanner.nextLine().trim());
                        } catch (NumberFormatException e) {
                            pick = -1;
                        }
                        boolean pickOutOfRange = (pick < 0 || pick > myScholars.length);
                        if (pickOutOfRange) {
                            System.out.println("Please type a number from " + 0 + " to " + myScholars.length + ".");
                        }
                    }

                    if (pick > 0) {
                        double amount = 0;
                        boolean amountDone = false;
                        while (amountDone == false) {
                            System.out.print("Amount sent: ");
                            try {
                                amount = Double.parseDouble(scanner.nextLine().trim());
                                amountDone = (amount >= 0.01);
                            } catch (NumberFormatException e) {
                                amountDone = false;
                            }
                            if (amountDone == false) {
                                System.out.println("Please type a number of 0.01 or more.");
                            }
                        }
                        String[] methods = {"GCash", "Bank transfer", "Cash"};
                        for (int i = 0; i < methods.length; i++) {
                            System.out.println((i + 1) + ". " + methods[i]);
                        }
                        int methodPick = -1;
                        while (methodPick < 1 || methodPick > methods.length) {
                            System.out.print("Method: ");
                            try {
                                methodPick = Integer.parseInt(scanner.nextLine().trim());
                            } catch (NumberFormatException e) {
                                methodPick = -1;
                            }
                            boolean methodPickOutOfRange = (methodPick < 1 || methodPick > methods.length);
                            if (methodPickOutOfRange) {
                                System.out.println("Please type a number from " + 1 + " to " + methods.length + ".");
                            }
                        }

                        // A reference number is needed unless the grant was given in cash.
                        String method = methods[methodPick - 1];
                        String referenceNo = "";
                        boolean isCash = method.equals("Cash");
                        if (isCash == false) {
                            System.out.print("Reference number: ");
                            referenceNo = scanner.nextLine().trim();
                        }

                        int grantId = benefactor.recordGrant(db, myScholars[pick - 1], amount, method, referenceNo);
                        if (grantId == 0) {
                            System.out.println("The grant was not recorded. A grant that is not in cash needs a reference number.");
                        } else {
                            System.out.println("The grant was recorded with the number " + grantId + ". Its status is Sent.");
                        }
                    }
                }
            }

            if (action == 2) {
                boolean voiding = true;
                while (voiding) {
                    printHeader("MY GRANTS");
                    Grant[] myGrants = db.filterAllGrants(benefactor, null, null, "");
                    if (myGrants.length == 0) {
                        System.out.println("You have not recorded a grant yet.");
                        voiding = false;
                    } else {
                        for (int i = 0; i < myGrants.length; i++) {
                            String studentText = "(student not found)";
                            Scholar scholar = db.findScholarById(myGrants[i].getScholarId());
                            if (scholar != null) {
                                Student student = db.findStudentById(scholar.getStudentId());
                                if (student != null) {
                                    studentText = student.toString();
                                }
                            }
                            String referenceText = "no reference";
                            boolean hasReference = (myGrants[i].getReferenceNo().equals("") == false);
                            if (hasReference) {
                                referenceText = "ref " + myGrants[i].getReferenceNo();
                            }
                            System.out.println((i + 1) + ". " + studentText);
                            System.out.println("   " + String.format("%.2f", myGrants[i].getAmount()) + " by "
                                    + myGrants[i].getMethod() + " (" + referenceText + ") - " + myGrants[i].getStatus()
                                    + ", sent " + myGrants[i].getDateSent());
                        }

                        int grantPick = -1;
                        while (grantPick < 0 || grantPick > myGrants.length) {
                            System.out.print("Grant number to void (0 = back): ");
                            try {
                                grantPick = Integer.parseInt(scanner.nextLine().trim());
                            } catch (NumberFormatException e) {
                                grantPick = -1;
                            }
                            boolean grantPickOutOfRange = (grantPick < 0 || grantPick > myGrants.length);
                            if (grantPickOutOfRange) {
                                System.out.println("Please type a number from " + 0 + " to " + myGrants.length + ".");
                            }
                        }
                        if (grantPick == 0) {
                            voiding = false;
                        } else {
                            boolean sure = false;
                            boolean sureAnswered = false;
                            while (sureAnswered == false) {
                                System.out.print("Void this grant? A voided grant cannot be changed again. (y/n): ");
                                String sureText = scanner.nextLine().trim().toLowerCase();
                                boolean sureIsYes = sureText.equals("y") || sureText.equals("yes");
                                boolean sureIsNo = sureText.equals("n") || sureText.equals("no");
                                if (sureIsYes) {
                                    sure = true;
                                    sureAnswered = true;
                                } else if (sureIsNo) {
                                    sureAnswered = true;
                                } else {
                                    System.out.println("Please type y or n.");
                                }
                            }
                            if (sure) {
                                boolean voided = benefactor.voidGrant(myGrants[grantPick - 1]);
                                if (voided) {
                                    System.out.println("The grant was voided. If needed, record a new grant to replace it.");
                                } else {
                                    System.out.println("The grant could not be voided. It may be voided already.");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Purpose: show the summary of the Benefactor.
    // For each scholarship: the number of applications by status and of scholars by status.
    // Then the number and the total amount of the grants by status.
    // The DataStore does the counting. This screen only shows the numbers.
    private void showBenefactorSummary(Benefactor benefactor) {
        printHeader("SUMMARY - " + benefactor.getBenefactorName());

        String[] applicationStatuses = {"Submitted", "Shortlisted", "Returned", "Approved",
                "Rejected", "Waitlisted", "Cancelled", "Withdrawn"};
        String[] scholarStatuses = {"Active", "Probation", "Suspended", "Lapsed", "Not renewed", "Completed"};
        String[] grantStatuses = {"Sent", "Confirmed", "Disputed", "Voided"};

        Scholarship[] mine = db.scholarshipsOf(benefactor);
        if (mine.length == 0) {
            System.out.println("You have not posted a scholarship yet.");
        }
        for (int i = 0; i < mine.length; i++) {
            System.out.println();
            System.out.println(mine[i].getTitle() + " (cycle " + mine[i].getCycleNo() + ", "
                    + mine[i].getStatus() + ", " + mine[i].getApprovalStatus() + ")");

            System.out.println("  Applications by status:");
            for (int j = 0; j < applicationStatuses.length; j++) {
                System.out.println("    " + applicationStatuses[j] + ": "
                        + db.countApplicationsByStatus(mine[i], applicationStatuses[j]));
            }

            System.out.println("  Scholars by status:");
            for (int j = 0; j < scholarStatuses.length; j++) {
                System.out.println("    " + scholarStatuses[j] + ": "
                        + db.countScholarsByStatus(mine[i], scholarStatuses[j]));
            }
        }

        System.out.println();
        System.out.println("Grants by status (all my scholarships):");
        for (int j = 0; j < grantStatuses.length; j++) {
            int howMany = db.countGrantsByStatus(benefactor, grantStatuses[j]);
            double total = db.totalGrantAmountByStatus(benefactor, grantStatuses[j]);
            System.out.println("  " + grantStatuses[j] + ": " + howMany + " grant(s), total " + String.format("%.2f", total));
        }
    }

    // ---------- Office screens ----------
    // The office has no class and no records of its own. It works through the methods of
    // DataStore, UserAccount and Scholarship. In the records it can only read, never decide.

    // Purpose: show the menu of the office after the admin sign-in, until the office signs out.
    // 1 Manage accounts, 2 Review scholarship posts, 3 View all records, 4 Summary.
    // Choice 5 changes the date of the system, so deadlines and the lapsed check can be tested.
    private void showOfficeMenu() {
        boolean running = true;
        while (running) {
            printHeader("OFFICE MENU");
            System.out.println("1. Manage accounts");
            System.out.println("2. Review scholarship posts");
            System.out.println("3. View all records (read only)");
            System.out.println("4. Summary");
            System.out.println("5. Change the date of the system (to test deadlines)");
            System.out.println("0. Sign out");
            System.out.println("Date of the system: " + db.getToday());
            int choice = -1;
            while (choice < 0 || choice > 5) {
                System.out.print("Choose: ");
                try {
                    choice = Integer.parseInt(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    choice = -1;
                }
                boolean choiceOutOfRange = (choice < 0 || choice > 5);
                if (choiceOutOfRange) {
                    System.out.println("Please type a number from " + 0 + " to " + 5 + ".");
                }
            }

            if (choice == 0) {
                running = false;
            }
            if (choice == 1) {
                manageAccounts();
            }
            if (choice == 2) {
                reviewScholarshipPosts();
            }
            if (choice == 3) {
                viewAllRecords();
            }
            if (choice == 4) {
                showOfficeSummary();
            }

            if (choice == 5) {
                System.out.print("New date (yyyy-MM-dd): ");
                String newDate = scanner.nextLine().trim();

                // The date must look like yyyy-MM-dd with a real month and a day from 1 to 31.
                boolean dateIsGood = false;
                if (newDate.length() == 10) {
                    boolean dashAfterYear = (newDate.charAt(4) == '-');
                    boolean dashAfterMonth = (newDate.charAt(7) == '-');
                    if (dashAfterYear && dashAfterMonth) {
                        int year = 0;
                        int month = 0;
                        int day = 0;
                        boolean numbersAreGood = true;
                        try {
                            year = Integer.parseInt(newDate.substring(0, 4));
                            month = Integer.parseInt(newDate.substring(5, 7));
                            day = Integer.parseInt(newDate.substring(8, 10));
                        } catch (NumberFormatException e) {
                            numbersAreGood = false;
                        }
                        boolean monthIsGood = (month >= 1) && (month <= 12);
                        boolean dayIsGood = (day >= 1) && (day <= 31);
                        boolean yearIsGood = (year >= 2000);
                        dateIsGood = numbersAreGood && monthIsGood && dayIsGood && yearIsGood;
                    }
                }

                if (dateIsGood) {
                    db.setToday(newDate);
                    System.out.println("The date of the system is now " + db.getToday() + ".");
                } else {
                    System.out.println("The date was not changed. Write it as yyyy-MM-dd, for example 2026-12-31.");
                }
            }
        }
    }

    // Purpose: let the office create student and Benefactor accounts, find an account,
    // reset a password, and disable or reactivate an account.
    // There is no self-registration, so every account is created here with a temporary password.
    // A disabled account cannot sign in, and its records stay in the system.
    // DataStore and UserAccount check the values and the rules.
    private void manageAccounts() {
        boolean running = true;
        while (running) {
            printHeader("MANAGE ACCOUNTS");
            System.out.println("1. Create a student account");
            System.out.println("2. Create a Benefactor account");
            System.out.println("3. Find an account");
            System.out.println("4. Reset a password");
            System.out.println("5. Disable an account");
            System.out.println("6. Reactivate an account");
            System.out.println("0. Back");
            int action = -1;
            while (action < 0 || action > 6) {
                System.out.print("Choose: ");
                try {
                    action = Integer.parseInt(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    action = -1;
                }
                boolean actionOutOfRange = (action < 0 || action > 6);
                if (actionOutOfRange) {
                    System.out.println("Please type a number from " + 0 + " to " + 6 + ".");
                }
            }

            if (action == 0) {
                running = false;
            }

            if (action == 1) {
                System.out.println("Leave the middle name empty if the student has none.");
                System.out.print("Username: ");
                String username = scanner.nextLine().trim();
                System.out.print("Temporary password (at least 4 characters): ");
                String tempPassword = scanner.nextLine().trim();
                System.out.print("Student number: ");
                String studentNumber = scanner.nextLine().trim();
                System.out.print("Last name: ");
                String lname = scanner.nextLine().trim();
                System.out.print("First name: ");
                String fname = scanner.nextLine().trim();
                System.out.print("Middle name: ");
                String mname = scanner.nextLine().trim();
                System.out.print("Email: ");
                String email = scanner.nextLine().trim();
                System.out.print("Contact number: ");
                String contactNumber = scanner.nextLine().trim();
                System.out.print("Course: ");
                String course = scanner.nextLine().trim();

                int newId = db.createStudent(username, tempPassword, studentNumber, lname, fname,
                        mname, email, contactNumber, course);
                if (newId == 0) {
                    System.out.println("The account was not created. Every value except the middle name is required, the password needs at least 4 characters, and the username and the student number must not be used already.");
                } else {
                    System.out.println("The student account was created. The student ID is " + newId + ".");
                }
            }

            if (action == 2) {
                System.out.println("Leave the description empty if there is none.");
                System.out.print("Username: ");
                String username = scanner.nextLine().trim();
                System.out.print("Temporary password (at least 4 characters): ");
                String tempPassword = scanner.nextLine().trim();
                System.out.print("Name of the organization: ");
                String benefactorName = scanner.nextLine().trim();

                String[] categories = {"government", "private company", "foundation", "alumni", "LGU"};
                System.out.println("Category:");
                for (int i = 0; i < categories.length; i++) {
                    System.out.println((i + 1) + ". " + categories[i]);
                }
                int categoryPick = -1;
                while (categoryPick < 1 || categoryPick > categories.length) {
                    System.out.print("Choose: ");
                    try {
                        categoryPick = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        categoryPick = -1;
                    }
                    boolean categoryPickOutOfRange = (categoryPick < 1 || categoryPick > categories.length);
                    if (categoryPickOutOfRange) {
                        System.out.println("Please type a number from " + 1 + " to " + categories.length + ".");
                    }
                }

                System.out.print("Description: ");
                String description = scanner.nextLine().trim();
                System.out.print("Email: ");
                String email = scanner.nextLine().trim();
                System.out.print("Contact number: ");
                String contactNumber = scanner.nextLine().trim();
                System.out.print("Address: ");
                String address = scanner.nextLine().trim();

                int newId = db.createBenefactor(username, tempPassword, benefactorName,
                        categories[categoryPick - 1], description, email, contactNumber, address);
                if (newId == 0) {
                    System.out.println("The account was not created. Every value except the description is required, the password needs at least 4 characters, and the username must not be used already.");
                } else {
                    System.out.println("The Benefactor account was created. The Benefactor ID is " + newId + ".");
                }
            }

            // Finding, resetting, disabling and reactivating all work on one account.
            boolean needsAccount = (action >= 3) && (action <= 6);
            if (needsAccount) {
                System.out.print("Username of the account: ");
                String findUsername = scanner.nextLine().trim();
                UserAccount account = db.findAccountByUsername(findUsername);
                boolean accountExists = (account != null);

                if (accountExists == false) {
                    System.out.println("No account has this username.");
                } else {
                    if (action == 3) {
                        System.out.println("Username:   " + account.getUsername());
                        System.out.println("Role:       " + account.getRole());
                        System.out.println("Status:     " + account.getStatus());
                        System.out.println("Created on: " + account.getCreatedOn());
                        System.out.println("Last login: " + account.getLastLogin());

                        // The account ID is the same number as the student ID and the Benefactor ID.
                        Student student = db.findStudentById(account.getAccountId());
                        if (student != null) {
                            System.out.println("Student:    " + student.toString());
                            System.out.println("Email:      " + student.getEmail() + ", contact number " + student.getContactNumber());
                        }
                        Benefactor owner = db.findBenefactorById(account.getAccountId());
                        if (owner != null) {
                            System.out.println("Benefactor: " + owner.toString());
                            System.out.println("Email:      " + owner.getEmail() + ", contact number " + owner.getContactNumber());
                        }
                    }

                    if (action == 4) {
                        System.out.print("New temporary password (at least 4 characters): ");
                        String newPassword = scanner.nextLine().trim();
                        boolean wasReset = account.resetPassword(newPassword);
                        if (wasReset) {
                            System.out.println("The password was reset. Give the new temporary password to the user.");
                        } else {
                            System.out.println("The password was not reset. It needs at least 4 characters.");
                        }
                    }

                    if (action == 5) {
                        boolean isDisabled = (account.isActive() == false);
                        if (isDisabled) {
                            System.out.println("This account is disabled already.");
                        } else {
                            account.disable();
                            System.out.println("The account was disabled. Its records stay in the system.");
                        }
                    }

                    if (action == 6) {
                        boolean isActive = account.isActive();
                        if (isActive) {
                            System.out.println("This account is active already.");
                        } else {
                            account.reactivate();
                            System.out.println("The account was reactivated.");
                        }
                    }
                }
            }
        }
    }

    // Purpose: list the scholarship posts that wait for the office, and approve or reject each one.
    // A renewed scholarship waits here too, and its cycle number goes up when it is approved.
    // A scholarship is visible to students only when it is approved and open.
    // The Scholarship class checks that the post is still Pending.
    private void reviewScholarshipPosts() {
        boolean running = true;
        while (running) {
            printHeader("SCHOLARSHIP POSTS WAITING FOR APPROVAL");

            Scholarship[] pending = db.pendingScholarships();
            if (pending.length == 0) {
                System.out.println("No scholarship post is waiting.");
                running = false;
            } else {
                for (int i = 0; i < pending.length; i++) {
                    Scholarship s = pending[i];
                    Benefactor owner = db.findBenefactorById(s.getBenefactorId());
                    String ownerText = "(Benefactor not found)";
                    if (owner != null) {
                        ownerText = owner.toString();
                    }

                    String kind = "New post";
                    if (s.isRenewalPending()) {
                        kind = "Renewal of cycle " + s.getCycleNo();
                    }

                    System.out.println((i + 1) + ". " + s.getTitle() + " - " + ownerText);
                    System.out.println("   " + kind + ", " + s.getAcademicYear() + ", deadline " + s.getDeadline()
                            + ", slots " + s.getSlots() + ", grant amount " + String.format("%.2f", s.getGrantAmount()));
                    System.out.println("   Description:   " + s.getDescription());
                    System.out.println("   Qualification: " + s.getQualification());
                    System.out.println("   Requirement:   " + s.getRequirement());
                }

                System.out.println();
                int pick = -1;
                while (pick < 0 || pick > pending.length) {
                    System.out.print("Post number to decide (0 = back): ");
                    try {
                        pick = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        pick = -1;
                    }
                    boolean pickOutOfRange = (pick < 0 || pick > pending.length);
                    if (pickOutOfRange) {
                        System.out.println("Please type a number from " + 0 + " to " + pending.length + ".");
                    }
                }
                if (pick == 0) {
                    running = false;
                } else {
                    Scholarship chosen = pending[pick - 1];
                    System.out.println("1. Approve");
                    System.out.println("2. Reject (tell the Benefactor the reason by official email, outside the system)");
                    System.out.println("0. Cancel");
                    int decision = -1;
                    while (decision < 0 || decision > 2) {
                        System.out.print("Choose: ");
                        try {
                            decision = Integer.parseInt(scanner.nextLine().trim());
                        } catch (NumberFormatException e) {
                            decision = -1;
                        }
                        boolean decisionOutOfRange = (decision < 0 || decision > 2);
                        if (decisionOutOfRange) {
                            System.out.println("Please type a number from " + 0 + " to " + 2 + ".");
                        }
                    }

                    if (decision == 1) {
                        boolean approved = chosen.approve();
                        if (approved) {
                            System.out.println("The post was approved. It is visible to students now.");
                        } else {
                            System.out.println("The post could not be approved. It is not Pending.");
                        }
                    }
                    if (decision == 2) {
                        boolean rejected = chosen.reject();
                        if (rejected) {
                            System.out.println("The post was rejected. The Benefactor can edit it and send it again.");
                        } else {
                            System.out.println("The post could not be rejected. It is not Pending.");
                        }
                    }
                }
            }
        }
    }

    // Purpose: show all the applications, scholars or grants of the system, read only.
    // The office can filter them by Benefactor, scholarship, student and status.
    // A filter is skipped when the text is empty or 0 (Any) is chosen. The Benefactor and the
    // student are found by their username. This screen never changes a record.
    private void viewAllRecords() {
        boolean running = true;
        while (running) {
            printHeader("ALL RECORDS (READ ONLY)");
            System.out.println("1. Applications");
            System.out.println("2. Scholars");
            System.out.println("3. Grants");
            System.out.println("0. Back");
            int choice = -1;
            while (choice < 0 || choice > 3) {
                System.out.print("Choose: ");
                try {
                    choice = Integer.parseInt(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    choice = -1;
                }
                boolean choiceOutOfRange = (choice < 0 || choice > 3);
                if (choiceOutOfRange) {
                    System.out.println("Please type a number from " + 0 + " to " + 3 + ".");
                }
            }

            if (choice == 0) {
                running = false;
            }

            if (choice >= 1) {
                System.out.println("Leave the username empty to skip a filter.");
                boolean filtersAreGood = true;

                System.out.print("Username of the Benefactor: ");
                String benefactorUsername = scanner.nextLine().trim();
                Benefactor wantedBenefactor = null;
                boolean askedForBenefactor = (benefactorUsername.equals("") == false);
                if (askedForBenefactor) {
                    UserAccount benefactorAccount = db.findAccountByUsername(benefactorUsername);
                    if (benefactorAccount != null) {
                        wantedBenefactor = db.findBenefactorById(benefactorAccount.getAccountId());
                    }
                    if (wantedBenefactor == null) {
                        System.out.println("No Benefactor has this username.");
                        filtersAreGood = false;
                    }
                }

                System.out.print("Username of the student: ");
                String studentUsername = scanner.nextLine().trim();
                Student wantedStudent = null;
                boolean askedForStudent = (studentUsername.equals("") == false);
                if (askedForStudent) {
                    UserAccount studentAccount = db.findAccountByUsername(studentUsername);
                    if (studentAccount != null) {
                        wantedStudent = db.findStudentById(studentAccount.getAccountId());
                    }
                    if (wantedStudent == null) {
                        System.out.println("No student has this username.");
                        filtersAreGood = false;
                    }
                }

                if (filtersAreGood) {
                    // A scholarship can be chosen after a Benefactor is chosen, from the scholarships of that Benefactor.
                    Scholarship wantedScholarship = null;
                    if (wantedBenefactor != null) {
                        Scholarship[] theirs = db.scholarshipsOf(wantedBenefactor);
                        System.out.println("Scholarship:");
                        System.out.println("0. Any");
                        for (int i = 0; i < theirs.length; i++) {
                            System.out.println((i + 1) + ". " + theirs[i].getTitle() + " (cycle " + theirs[i].getCycleNo() + ")");
                        }
                        int scholarshipPick = -1;
                        while (scholarshipPick < 0 || scholarshipPick > theirs.length) {
                            System.out.print("Choose: ");
                            try {
                                scholarshipPick = Integer.parseInt(scanner.nextLine().trim());
                            } catch (NumberFormatException e) {
                                scholarshipPick = -1;
                            }
                            boolean scholarshipPickOutOfRange = (scholarshipPick < 0 || scholarshipPick > theirs.length);
                            if (scholarshipPickOutOfRange) {
                                System.out.println("Please type a number from " + 0 + " to " + theirs.length + ".");
                            }
                        }
                        if (scholarshipPick > 0) {
                            wantedScholarship = theirs[scholarshipPick - 1];
                        }
                    }

                    String[] statuses = {"Sent", "Confirmed", "Disputed", "Voided"};
                    if (choice == 1) {
                        statuses = new String[] {"Submitted", "Shortlisted", "Returned", "Approved",
                                "Rejected", "Waitlisted", "Cancelled", "Withdrawn"};
                    }
                    if (choice == 2) {
                        statuses = new String[] {"Active", "Probation", "Suspended", "Lapsed", "Not renewed", "Completed"};
                    }
                    System.out.println("Status:");
                    System.out.println("0. Any");
                    for (int i = 0; i < statuses.length; i++) {
                        System.out.println((i + 1) + ". " + statuses[i]);
                    }
                    int statusPick = -1;
                    while (statusPick < 0 || statusPick > statuses.length) {
                        System.out.print("Choose: ");
                        try {
                            statusPick = Integer.parseInt(scanner.nextLine().trim());
                        } catch (NumberFormatException e) {
                            statusPick = -1;
                        }
                        boolean statusPickOutOfRange = (statusPick < 0 || statusPick > statuses.length);
                        if (statusPickOutOfRange) {
                            System.out.println("Please type a number from " + 0 + " to " + statuses.length + ".");
                        }
                    }
                    String wantedStatus = "";
                    if (statusPick > 0) {
                        wantedStatus = statuses[statusPick - 1];
                    }

                    System.out.println();

                    if (choice == 1) {
                        Application[] found = db.filterAllApplications(wantedBenefactor, wantedScholarship, wantedStudent, wantedStatus);
                        System.out.println(found.length + " application(s) found.");
                        for (int i = 0; i < found.length; i++) {
                            Student student = db.findStudentById(found[i].getStudentId());
                            String studentText = "(student not found)";
                            if (student != null) {
                                studentText = student.toString();
                            }
                            String title = "(scholarship not found)";
                            Scholarship scholarship = db.findScholarshipById(found[i].getScholarshipId());
                            if (scholarship != null) {
                                title = scholarship.getTitle();
                            }
                            System.out.println((i + 1) + ". " + studentText + " - " + title + ", cycle " + found[i].getCycleNo()
                                    + " - " + found[i].getStatus());
                            System.out.println("   File: " + found[i].getCredential() + ", submitted " + found[i].getSubmittedOn());
                            boolean wasDecided = (found[i].getDecidedOn().equals("") == false);
                            if (wasDecided) {
                                System.out.println("   Last decision: " + found[i].getDecidedOn());
                            }
                            boolean hasComment = (found[i].getBenefactorComment().equals("") == false);
                            if (hasComment) {
                                System.out.println("   Comment of the Benefactor: " + found[i].getBenefactorComment());
                            }
                        }
                    }

                    if (choice == 2) {
                        Scholar[] found = db.filterAllScholars(wantedBenefactor, wantedScholarship, wantedStudent, wantedStatus);
                        System.out.println(found.length + " scholar(s) found.");
                        for (int i = 0; i < found.length; i++) {
                            Student student = db.findStudentById(found[i].getStudentId());
                            String studentText = "(student not found)";
                            if (student != null) {
                                studentText = student.toString();
                            }
                            String title = "(scholarship not found)";
                            Scholarship scholarship = db.findScholarshipById(found[i].getScholarshipId());
                            if (scholarship != null) {
                                title = scholarship.getTitle();
                            }
                            System.out.println((i + 1) + ". " + studentText + " - " + title);
                            String endText = "";
                            boolean hasEnded = (found[i].getEndDate().equals("") == false);
                            if (hasEnded) {
                                endText = ", ended " + found[i].getEndDate();
                            }
                            System.out.println("   " + found[i].getStatus() + ", " + found[i].getRenewalCount()
                                    + " renewal(s), since " + found[i].getStartDate() + endText);
                        }
                    }

                    if (choice == 3) {
                        Grant[] found = db.filterAllGrants(wantedBenefactor, wantedScholarship, wantedStudent, wantedStatus);
                        System.out.println(found.length + " grant(s) found.");
                        for (int i = 0; i < found.length; i++) {
                            String studentText = "(student not found)";
                            Scholar scholar = db.findScholarById(found[i].getScholarId());
                            if (scholar != null) {
                                Student student = db.findStudentById(scholar.getStudentId());
                                if (student != null) {
                                    studentText = student.toString();
                                }
                            }
                            String ownerText = "(Benefactor not found)";
                            Benefactor owner = db.findBenefactorById(found[i].getBenefactorId());
                            if (owner != null) {
                                ownerText = owner.getBenefactorName();
                            }
                            String referenceText = "no reference";
                            boolean hasReference = (found[i].getReferenceNo().equals("") == false);
                            if (hasReference) {
                                referenceText = "ref " + found[i].getReferenceNo();
                            }
                            System.out.println((i + 1) + ". " + studentText + " - from " + ownerText);
                            System.out.println("   " + String.format("%.2f", found[i].getAmount()) + " by " + found[i].getMethod()
                                    + " (" + referenceText + ") - " + found[i].getStatus() + ", sent " + found[i].getDateSent());
                            boolean wasConfirmed = (found[i].getConfirmedOn().equals("") == false);
                            if (wasConfirmed) {
                                System.out.println("   Confirmed on " + found[i].getConfirmedOn());
                            }
                        }
                    }
                }
            }
        }
    }

    // Purpose: show the summary counts across all Benefactors:
    // the scholarships by approval status, and the applications, scholars and grants by status.
    // The DataStore does the counting. This screen only reads and shows the numbers.
    private void showOfficeSummary() {
        printHeader("OFFICE SUMMARY");

        String[] approvalStatuses = {"Pending", "Approved", "Rejected"};
        String[] applicationStatuses = {"Submitted", "Shortlisted", "Returned", "Approved",
                "Rejected", "Waitlisted", "Cancelled", "Withdrawn"};
        String[] scholarStatuses = {"Active", "Probation", "Suspended", "Lapsed", "Not renewed", "Completed"};
        String[] grantStatuses = {"Sent", "Confirmed", "Disputed", "Voided"};

        System.out.println("Scholarships by approval status:");
        for (int i = 0; i < approvalStatuses.length; i++) {
            System.out.println("  " + approvalStatuses[i] + ": " + db.countAllScholarshipsByApproval(approvalStatuses[i]));
        }

        System.out.println("Applications by status:");
        for (int i = 0; i < applicationStatuses.length; i++) {
            System.out.println("  " + applicationStatuses[i] + ": " + db.countAllApplicationsByStatus(applicationStatuses[i]));
        }

        System.out.println("Scholars by status:");
        for (int i = 0; i < scholarStatuses.length; i++) {
            System.out.println("  " + scholarStatuses[i] + ": " + db.countAllScholarsByStatus(scholarStatuses[i]));
        }

        System.out.println("Grants by status:");
        for (int i = 0; i < grantStatuses.length; i++) {
            System.out.println("  " + grantStatuses[i] + ": " + db.countAllGrantsByStatus(grantStatuses[i]));
        }
    }


    // ---------- Output helper ----------
    // All the input is read with the scanner directly, where it is needed.

    // Purpose: print a title between two lines, so each screen starts the same way.
    protected void printHeader(String title) {
        System.out.println();
        System.out.println("==================================================");
        System.out.println(title);
        System.out.println("==================================================");
    }
}