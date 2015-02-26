package nl.tkp.opleveringen;

/**
 * Created by Jacob on 10-11-2014.
 */
public class FileType {
    public String name;
    public int sequenceNumber;
    public String folderName;
    public String filePrefix;
    public boolean inSetup;

    FileType(String name, int sequenceNumber) {
        this.name = name;
        this.folderName = "";
        this.sequenceNumber = sequenceNumber;
        this.filePrefix = "Nee";
        this.inSetup = inSetup;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FileType)) return false;
        FileType other = (FileType) o;
        return (this.name.toUpperCase().equals(other.name.toUpperCase()));
    }

    public String toString() {
        return "FileType(name=" + this.name + ", sequenceNumber=" + this.sequenceNumber + ", folderName=" + this.folderName
                + ", filePrefix=" + this.filePrefix + ", inSetup=" + this.inSetup + ")";
    }

}

