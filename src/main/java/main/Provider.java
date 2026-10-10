package main;

public class Provider extends Account {

    private int providerId;
    private String providerName;
    private String providerType;
    private String description;
    private String contactPerson;
    private String contactEmail;
    private String contactNumber;
    private String address;

    // the first 5 values go up to Account, the rest belong to Provider
    public Provider(int accountId, String username, String tempPassword, String createdOn,
                    int providerId, String providerName, String providerType,
                    String description, String contactPerson, String contactEmail,
                    String contactNumber, String address) {
        super(accountId, username, tempPassword, "Provider", createdOn);
        this.providerId = providerId;
        this.providerName = providerName;
        this.providerType = providerType;
        this.description = description;
        this.contactPerson = contactPerson;
        this.contactEmail = contactEmail;
        this.contactNumber = contactNumber;
        this.address = address;
    }

    @Override
    public String getRole() {
        return "Provider";
    }

    @Override
    public String[] getMenuOptions() {
        String[] options = {
            "Manage scholarships",
            "Review applications",
            "Filter applications by status",
            "Manage scholars",
            "Record payments",
            "Summary",
            "My profile"
        };
        return options;
    }

    @Override
    public String toString() {
        return providerName + " (" + providerType + ")";
    }

    // getters (the UML does not draw these)
    public int getProviderId() { return providerId; }
    public String getProviderName() { return providerName; }
    public String getProviderType() { return providerType; }
    public String getDescription() { return description; }
    public String getContactPerson() { return contactPerson; }
    public String getContactEmail() { return contactEmail; }
    public String getContactNumber() { return contactNumber; }
    public String getAddress() { return address; }
}