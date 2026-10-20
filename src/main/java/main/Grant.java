package main;

// Grant is a grant that a Benefactor recorded for a scholar.
// The system NEVER sends or holds money. The Benefactor sends the money outside the system
// (GCash, bank transfer or cash), and the system only keeps this record.
// A recorded grant is never edited or deleted. A wrong one is only voided,
// and the Benefactor records a new grant to replace it if needed.
public class Grant {

    // ---------- Attributes ----------
    // They are private, so other classes must use the methods and getters below.

    private int grantId;          // unique ID of this grant record
    private int scholarId;        // ID of the scholar who receives the grant
    private int benefactorId;     // ID of the Benefactor who recorded it
    private double amount;        // how much was sent
    private String method;        // "GCash", "Bank transfer" or "Cash"
    private String dateSent;      // date the Benefactor sent it, yyyy-MM-dd
    private String referenceNo;   // reference number from the e-wallet or bank (can be empty for cash)
    private String status;        // "Sent", "Confirmed", "Disputed" or "Voided"
    private String confirmedOn;   // date the scholar confirmed receiving it (empty until then)


    // ---------- Constructor ----------

    // Purpose: save a new grant record.
    // A new grant always starts as Sent, and it is not confirmed yet.
    public Grant(int grantId, int scholarId, int benefactorId, double amount,
                 String method, String dateSent, String referenceNo) {
        this.grantId = grantId;
        this.scholarId = scholarId;
        this.benefactorId = benefactorId;
        this.amount = amount;
        this.method = method;
        this.dateSent = dateSent;
        this.referenceNo = referenceNo;

        this.status = "Sent";
        this.confirmedOn = "";
    }


    // ---------- Status rules ----------
    // The status changes only through the methods below, and each method
    // changes it only when the current status allows it.

    // Purpose: the scholar confirms that the grant was received.
    // The status becomes Confirmed, and the confirmation date (today) is saved.
    // Returns true if it was confirmed, and false if the grant is not "Sent".
    public boolean confirm(String today) {
        boolean isSent = status.equals("Sent");
        if (isSent == false) {
            return false;
        }

        status = "Confirmed";
        confirmedOn = today;
        return true;
    }

    // Purpose: the scholar says the grant was not received or is wrong.
    // The status becomes Disputed.
    // Returns true if it was disputed, and false if the grant is not "Sent".
    public boolean dispute() {
        boolean isSent = status.equals("Sent");
        if (isSent == false) {
            return false;
        }

        status = "Disputed";
        return true;
    }

    // Purpose: the Benefactor voids a wrong grant record.
    // A grant is never edited or deleted, it is only voided.
    // Returns true if it was voided, and false if it is already Voided.
    public boolean markVoided() {
        boolean isVoided = status.equals("Voided");
        if (isVoided) {
            return false;
        }

        status = "Voided";
        return true;
    }


    // ---------- Text ----------

    // Purpose: give a short text of the grant for lists and printing.
    // Example: "[1] Scholar 1 - 1000.00 by GCash (ref REF123) - Sent on 2026-10-20"
    // toString() replaces (overrides) the toString() that every Java object has.
    public String toString() {
        // String.format works like printf: %.2f means a number with 2 decimals.
        String amountText = String.format("%.2f", amount);

        // A cash grant may have no reference number.
        String referenceText = "no reference";
        boolean hasReference = (referenceNo.trim().equals("") == false);
        if (hasReference) {
            referenceText = "ref " + referenceNo;
        }

        return "[" + grantId + "] Scholar " + scholarId + " - " + amountText + " by " + method
                + " (" + referenceText + ") - " + status + " on " + dateSent;
    }


    // ---------- Getters ----------
    // They let other classes read the private attributes.
    // There are no setters, because a grant is never edited.

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
}