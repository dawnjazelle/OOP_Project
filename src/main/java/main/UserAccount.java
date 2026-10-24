package main;

// UserAccount is the parent class of everyone who signs in.
// It keeps the login data and the contact details, and has the rules for signing in and for passwords.
// It is abstract: only a Student or a Benefactor can be created.
public abstract class UserAccount {

    // ---------- Attributes ----------

    private int accountId;        // unique number of the account
    private String username;      // name used to sign in
    private String password;      // password used to sign in (plain text)
    private String accountType;   // "Student" or "Benefactor"
    private String status;        // "Active" or "Disabled"
    private String lastLogin;     // date of the latest sign-in, yyyy-MM-dd
    private String createdOn;     // date the office created the account, yyyy-MM-dd
    private String email;         // email of the user
    private String contactNumber; // phone number of the user


    // ---------- Constructor ----------

    // Purpose: fill in the login data and the contact details of a new account.
    // Only Student and Benefactor can call it (protected).
    protected UserAccount(int accountId, String username, String tempPassword,
                          String accountType, String createdOn,
                          String email, String contactNumber) {
        this.accountId = accountId;
        this.username = username;
        this.password = tempPassword;     // temporary password from the office
        this.accountType = accountType;
        this.createdOn = createdOn;
        this.email = email;
        this.contactNumber = contactNumber;
        this.status = "Active";           // a new account starts as Active
        this.lastLogin = "";              // empty until the first sign-in
    }


    // ---------- Account rules ----------

    // Purpose: check the username and password when the user signs in.
    // today is the current date, yyyy-MM-dd.
    // Returns true and saves today as lastLogin if the account is active
    // and the username and password are correct. Returns false if not.
    public boolean signIn(String username, String password, String today) {
        boolean accountIsActive = isActive();
        boolean usernameIsCorrect = this.username.equals(username);
        boolean passwordIsCorrect = this.password.equals(password);

        if (accountIsActive && usernameIsCorrect && passwordIsCorrect) {
            lastLogin = today;
            return true;
        }
        return false;
    }

    // Purpose: replace the password with a new one.
    // The new password needs at least 4 characters (spaces at the start and end do not count).
    // Returns true if it is accepted, and false if it is too short.
    public boolean changePassword(String newPassword) {
        int length = newPassword.trim().length();
        boolean longEnough = (length >= 4);

        if (longEnough) {
            password = newPassword;
            return true;
        }
        return false;
    }

    // Purpose: office action. Set a new temporary password on the account.
    // The new password needs at least 4 characters (spaces at the start and end do not count).
    // Returns true if it is accepted, and false if it is too short.
    public boolean resetPassword(String tempPassword) {
        int length = tempPassword.trim().length();
        boolean longEnough = (length >= 4);

        if (longEnough) {
            password = tempPassword;
            return true;
        }
        return false;
    }

    // Purpose: change the email and the contact number of the user.
    // Both values are required.
    // Returns true if they were changed, and false if one of them is empty (nothing is changed).
    public boolean updateContact(String newEmail, String newContactNumber) {
        boolean emailIsEmpty = newEmail.trim().equals("");
        boolean numberIsEmpty = newContactNumber.trim().equals("");
        if (emailIsEmpty || numberIsEmpty) {
            return false;
        }

        email = newEmail.trim();
        contactNumber = newContactNumber.trim();
        return true;
    }

    // Purpose: office action. Set the status to Disabled, so the user cannot sign in.
    // The records of the user stay.
    public void disable() {
        status = "Disabled";
    }

    // Purpose: office action. Set the status to Active, so the user can sign in again.
    public void reactivate() {
        status = "Active";
    }

    // Purpose: tell if the account can be used.
    // Returns true when the status is "Active".
    public boolean isActive() {
        return status.equals("Active");
    }


    // ---------- Abstract methods ----------
    // Each child class (Student, Benefactor) writes its own body.

    // Purpose: give the name of the role, "Student" or "Benefactor".
    public abstract String getRole();

    // Purpose: give the menu items of this role.
    public abstract String[] getMenuOptions();


    // ---------- Getters ----------
    // They let other classes read the private attributes.
    // There is no getPassword, so the password cannot be read from outside.
    // The attributes change only through the methods above.

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

    public String getEmail() {
        return email;
    }

    public String getContactNumber() {
        return contactNumber;
    }
}