package main;

// UserAccount is the parent class of everyone who signs in to the system.
// It keeps the login data and has the rules for signing in and for passwords.
// It is abstract, so we can never create a plain UserAccount.
// We only create a Student or a Benefactor, because they extend (inherit from) this class.
public abstract class UserAccount {

    // ---------- Attributes ----------
    // They are private, so other classes cannot change them directly.
    // Other classes must use the methods below.

    private int accountId;        // the unique number of the account
    private String username;      // the name the user types to sign in
    private String password;      // the password the user types to sign in (plain text)
    private String accountType;   // "Student" or "Benefactor"
    private String status;        // "Active" or "Disabled"
    private String lastLogin;     // date of the latest sign-in, written as yyyy-MM-dd
    private String createdOn;     // date the office created the account, written as yyyy-MM-dd


    // ---------- Constructor ----------

    // Purpose: fill in the login data when a new account is created.
    // It is protected, so only the child classes (Student and Benefactor) can call it.
    protected UserAccount(int accountId, String username, String tempPassword,
                          String accountType, String createdOn) {
        // "this.accountId" is the attribute, "accountId" is the value we received.
        this.accountId = accountId;
        this.username = username;
        this.password = tempPassword;     // the office gives a temporary password first
        this.accountType = accountType;
        this.createdOn = createdOn;
        this.status = "Active";           // a new account starts as Active
        this.lastLogin = "";              // empty, because the user has not signed in yet
    }


    // ---------- Account rules ----------

    // Purpose: check the username and password when the user signs in.
    // Returns true if the sign-in is allowed, and false if it is not.
    // today is the current date, written as yyyy-MM-dd.
    public boolean signIn(String username, String password, String today) {
        // Check each rule and keep the answer (true or false) in a variable.
        // The variable names say what we are checking.
        boolean accountIsActive = isActive();                      // a disabled account cannot sign in
        boolean usernameIsCorrect = this.username.equals(username);  // same as the saved username?
        boolean passwordIsCorrect = this.password.equals(password);  // same as the saved password?

        // The sign-in works only when ALL three answers are true.
        if (accountIsActive && usernameIsCorrect && passwordIsCorrect) {
            lastLogin = today;    // save the date of this sign-in
            return true;
        }
        return false;
    }

    // Purpose: let the user replace the temporary password with a new one.
    // Changing the password is optional.
    // A password must have at least 4 characters (spaces at the start and end do not count).
    // Returns true if the new password is accepted, and false if it is too short.
    public boolean changePassword(String newPassword) {
        int length = newPassword.trim().length();   // the number of characters
        boolean longEnough = (length >= 4);

        if (longEnough) {
            password = newPassword;
            return true;
        }
        return false;
    }

    // Purpose: office action. Give the account a new temporary password
    // when the user forgot the old one.
    // The same rule applies: at least 4 characters (spaces at the start and end do not count).
    // Returns true if the new password is accepted, and false if it is too short.
    public boolean resetPassword(String tempPassword) {
        int length = tempPassword.trim().length();   // the number of characters
        boolean longEnough = (length >= 4);

        if (longEnough) {
            password = tempPassword;
            return true;
        }
        return false;
    }

    // Purpose: office action. Stop the user from signing in.
    // All the records of the user stay in the system.
    public void disable() {
        status = "Disabled";
    }

    // Purpose: office action. Let the user sign in again.
    public void reactivate() {
        status = "Active";
    }

    // Purpose: tell if the account can be used.
    // Returns true when the status is "Active".
    public boolean isActive() {
        return status.equals("Active");
    }


    // ---------- Abstract methods ----------
    // These have no body here. Each child class (Student, Benefactor) must write its own.
    // This is how each role can give a different answer (polymorphism).

    // Purpose: give the name of the role, for example "Student" or "Benefactor".
    public abstract String getRole();

    // Purpose: give the menu items that this role can use.
    public abstract String[] getMenuOptions();


    // ---------- Getters ----------
    // They let other classes read the private attributes.
    // There is no getPassword, so the password can never be read from outside.
    // There are no setters, because the attributes change only through the methods above.

    public int getAccountId() {
        return accountId;
    }

    public String getUsername() {
        return username;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getStatus() {
        return status;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public String getCreatedOn() {
        return createdOn;
    }
}