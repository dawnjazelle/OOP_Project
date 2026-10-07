public abstract class Account {

    // status values of an account
    public static final String ACTIVE = "Active";
    public static final String DISABLED = "Disabled";

    // fields are private, so other classes must use the methods below
    private int accountId;
    private String username;
    private String password;
    private String accountType;
    private String status;
    private String lastLogin;
    private String createdOn;

    // protected: only Student and Provider can call this constructor
    protected Account(int accountId, String username, String tempPassword,
                   String accountType, String createdOn) {
        this.accountId = accountId;
        this.username = username;
        this.password = tempPassword;
        this.accountType = accountType;
        this.status = ACTIVE;
        this.lastLogin = "";
        this.createdOn = createdOn;
    }

    // true only if the account is active and the username and password match
    public boolean signIn(String username, String password, String today) {
        if (!isActive()) {
            return false;
        }
        if (!this.username.equals(username)) {
            return false;
        }
        if (!this.password.equals(password)) {
            return false;
        }
        lastLogin = today;
        return true;
    }

    // optional, the user may change the temporary password
    public void changePassword(String newPassword) {
        password = newPassword;
    }

    // used by the office when a user forgets the password
    public void resetPassword(String tempPassword) {
        password = tempPassword;
    }

    public void disable() {
        status = DISABLED;
    }

    public void reactivate() {
        status = ACTIVE;
    }

    public boolean isActive() {
        return status.equals(ACTIVE);
    }

    // abstract: no body here, every subclass must write its own version
    public abstract String getRole();
    public abstract String[] getMenuOptions();

    // getters (the UML does not draw these)
    public int getAccountId() { return accountId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getAccountType() { return accountType; }
    public String getStatus() { return status; }
    public String getLastLogin() { return lastLogin; }
    public String getCreatedOn() { return createdOn; }
}