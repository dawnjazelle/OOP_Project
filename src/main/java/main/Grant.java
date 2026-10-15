// A grant that a Benefactor recorded for a scholar.
// The system does not send or hold money. It only keeps the record.
public class Grant {

    // Status values
    public static final String SENT = "Sent";
    public static final String CONFIRMED = "Confirmed";
    public static final String DISPUTED = "Disputed";
    public static final String VOIDED = "Voided";

    private int grantId;
    private int scholarId;
    private int benefactorId;
    private double amount;
    private String method;
    private String dateSent;
    private String referenceNo;
    private String status;
    private String confirmedOn;
    private String voidReason;

    // method is GCash, Bank transfer or Cash
    // A new grant starts as Sent
    public Grant(int grantId, int scholarId, int benefactorId, double amount,
                 String method, String dateSent, String referenceNo) {
        this.grantId = grantId;
        this.scholarId = scholarId;
        this.benefactorId = benefactorId;
        this.amount = amount;
        this.method = method;
        this.dateSent = dateSent;
        this.referenceNo = referenceNo;
        this.status = SENT;
        this.confirmedOn = "None";
        this.voidReason = "";
    }

    // The scholar says the grant was received. Works only on a Sent grant.
    public boolean confirm(String today) {
        if (!status.equals(SENT)) {
            return false;
        }
        status = CONFIRMED;
        confirmedOn = today;
        return true;
    }

    // The scholar says the grant was not received. Works only on a Sent grant.
    public boolean dispute() {
        if (!status.equals(SENT)) {
            return false;
        }
        status = DISPUTED;
        return true;
    }

    // The Benefactor voids the grant with a reason. A grant is never edited or deleted.
    public boolean voidWith(String reason) {
        if (status.equals(VOIDED)) {
            return false;
        }
        if (reason == null || reason.trim().equals("")) {
            return false;
        }
        status = VOIDED;
        voidReason = reason.trim();
        return true;
    }

    // Text for printing a grant
    @Override
    public String toString() {
        return grantId + " | " + amount + " | " + method + " | " + status + " | sent " + dateSent;
    }

    // Getters
    public int getGrantId() {
        return grantId;
    }

    public int getScholarId() {
        return scholarId;
    }

    public int getBenefactorId() {
        return benefactorId;
    }

    public double getAmount() {
        return amount;
    }

    public String getMethod() {
        return method;
    }

    public String getDateSent() {
        return dateSent;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public String getStatus() {
        return status;
    }

    public String getConfirmedOn() {
        return confirmedOn;
    }

    public String getVoidReason() {
        return voidReason;
    }
}