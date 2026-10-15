// One saved change to a scholarship (an edit or a renewal change).
// It is never changed after it is saved.
public class ScholarshipEditHistory {

    private int editId;
    private int scholarshipId;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String editedOn;

    // fieldName is the field that changed, for example deadline
    public ScholarshipEditHistory(int editId, int scholarshipId, String fieldName,
                                  String oldValue, String newValue, String editedOn) {
        this.editId = editId;
        this.scholarshipId = scholarshipId;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.editedOn = editedOn;
    }

    // Text for printing an edit
    @Override
    public String toString() {
        return editId + " | " + fieldName + ": " + oldValue + " -> " + newValue + " | " + editedOn;
    }

    // Getters
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