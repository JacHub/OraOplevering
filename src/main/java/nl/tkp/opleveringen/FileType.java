package nl.tkp.opleveringen;

/**
 * Created by Jacob on 10-11-2014.
 */
public class FileType {
    public String name;
    public String objectType;
    public int sequenceNumber;
    public String dbEquivalent;
    public String folderName;
    public String filePrefix;
    public boolean inSetup;

    public FileType(String name, String objectType, int sequenceNumber, String dbEquivalent, String folderName, String filePrefix, boolean inSetup) {
        this.name = name;
        this.objectType = objectType;
        this.sequenceNumber = sequenceNumber;
        this.dbEquivalent = dbEquivalent;
        this.folderName = folderName;
        this.filePrefix = filePrefix;
        this.inSetup = inSetup;
    }

    FileType(String name, int sequenceNumber) {
        this.name = name;
        this.objectType = "";
        this.folderName = "";
        this.sequenceNumber = sequenceNumber;
        this.filePrefix = "Nee";
        this.inSetup = false;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FileType)) return false;
        FileType other = (FileType) o;
        return (this.name.toUpperCase().equals(other.name.toUpperCase()));
    }

    public String toString() {
        return "FileType(name=" + this.name + " objectType= "+ this.objectType +", sequenceNumber=" + this.sequenceNumber + ", dbEquivalent=" + this.dbEquivalent + ", folderName=" + this.folderName
                + ", filePrefix=" + this.filePrefix + ", inSetup=" + this.inSetup + ")";
    }

}

