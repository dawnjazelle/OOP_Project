// Base class of every user who signs in (Student and Benefactor)
public abstract class UserAccount {

    // Status values
    public static final String ACTIVE = "Active";
    public static final String DISABLED = "Disabled";

    // Account types
    public static final String TYPE_STUDENT = "Student";
    public static final String TYPE_BENEFACTOR = "Benefactor";

    // Shortest password allowed
    public static final int MIN_PASSWORD_LENGTH = 4;

    private int accountId;
    private String username;
    private String password;
    private String accountType;
    private String status;
    private String lastLogin;
    private String createdOn;

    // Called by Student and Benefactor. A new account starts Active.
    protected UserAccount(int accountId, String username, String password,
                          String accountType, String createdOn) {
        this.accountId = accountId;
        this.username = username;
        this.password = password;
        this.accountType = accountType;
        this.createdOn = createdOn;
        this.status = ACTIVE;
        this.lastLogin = "Never";
    }

    // Gives true if the account is active and the login is correct
    public boolean signIn(String username, String password, String today) {
        if (username == null || password == null || today == null) {
            return false;
        }
        if (!isActive()) {
            return false;
        }
        if (!this.username.equals(username)) {
            return false;
        }
        if (!this.password.equals(password)) {
            return false;
        }
        this.lastLogin = today;
        return true;
    }

    // The user changes the password. Gives false if it is too short.
    public boolean changePassword(String newPassword) {
        if (!isValidPassword(newPassword)) {
            return false;
        }
        this.password = newPassword;
        return true;
    }

    // The office sets a new temporary password. Gives false if it is too short.
    public boolean resetPassword(String tempPassword) {
        if (!isValidPassword(tempPassword)) {
            return false;
        }
        this.password = tempPassword;
        return true;
    }

    // The office blocks the account from signing in
    public void disable() {
        this.status = DISABLED;
    }

    // The office lets the account sign in again
    public void reactivate() {
        this.status = ACTIVE;
    }

    // True if the account can sign in
    public boolean isActive() {
        return status.equals(ACTIVE);
    }

    // Each subclass gives its own role name
    public abstract String getRole();

    // Each subclass gives its own menu
    public abstract String[] getMenuOptions();

    // Getters (no getPassword on purpose)
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

    // Checks the password length
    private boolean isValidPassword(String value) {
        if (value == null) {
            return false;
        }
        if (value.length() < MIN_PASSWORD_LENGTH) {
            return false;
        }
        return true;
    }
}