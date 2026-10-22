package main;

// ScholarshipEditHistory is ONE saved change to a scholarship.
// It is created when a Benefactor edits or renews a scholarship.
// It keeps what was changed, the old value, the new value and when,
// so the history of every scholarship can be read later.
// Each record belongs to one Scholarship and is kept in that scholarship's editHistory array.
// A record is never changed after it is saved, so it has getters but no setters.
public class ScholarshipEditHistory {

    // ---------- Attributes ----------
    // They are private, so other classes must use the getters below.

    private int editId;           // unique ID of this change
    private int scholarshipId;    // ID of the scholarship that was edited
    private String fieldName;     // which field was changed, for example "deadline"
    private String oldValue;      // the value before the change
    private String newValue;      // the value after the change
    private String editedOn;      // date and time of the change


    // ---------- Constructor ----------

    // Purpose: fill in all the details of one change.
    public ScholarshipEditHistory(int editId, int scholarshipId, String fieldName,
                                  String oldValue, String newValue, String editedOn) {
        this.editId = editId;
        this.scholarshipId = scholarshipId;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.editedOn = editedOn;
    }


    // ---------- Text ----------

    // Purpose: give a short text of the change for lists and printing.
    // Example: "2026-10-20 - deadline: 2026-12-31 -> 2027-01-15"
    // toString() replaces (overrides) the toString() that every Java object has.
    public String toString() {
        return editedOn + " - " + fieldName + ": " + oldValue + " -> " + newValue;
    }


    // ---------- Getters ----------
    // They let other classes read the private attributes.

    public int getEditId() {
        return editId;
    }

    public int getScholarshipId() {
        return scholarshipId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public String getEditedOn() {
        return editedOn;
    }
}
