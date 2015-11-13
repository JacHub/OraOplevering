package nl.tkp.opleveringen;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by Jacob on 10-11-2014.
 */
public class OracleObject implements Serializable {

    private String fileName;
    private String fileType;
    private String folderName;
    private int sequenceNumber;
    private String filePrefix;
    private boolean inSetup;

    public OracleObject(String name) {
        this.fileName = name;
        this.fileType = this.fileName.substring(this.fileName.lastIndexOf(".") + 1);
        this.folderName = "";
        this.sequenceNumber = 999;
        this.filePrefix = "Ja";
        this.inSetup = false;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return this.fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFolderName() {
        return this.folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public int getSequenceNumber() {
        return this.sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getFilePrefix() {
        return this.filePrefix;
    }

    public void setFilePrefix(String filePrefix) {
        this.filePrefix = filePrefix;
    }

    public boolean getInSetup() {
        return this.inSetup;
    }

    public void setInSetup(boolean inSetup) {
        this.inSetup = inSetup;
    }

    public String toString() {
        return "OracleObject(fileName=" + this.fileName + ", fileType=" + this.fileType + ", folderName=" + this.folderName + ", sequenceNumber=" + this.sequenceNumber + ")";
    }

    public String getNewFileName(String versie) {
        if (this.filePrefix.equals("Ja")) {
            return versie + "_" + this.fileName;
        } else {
            return this.fileName;
        }
    }

    public String getObjectName(String versie) {
        String objectName = this.fileName;
        int positiePunt = objectName.indexOf('.');
        // trim het filetype
        if (positiePunt >= 0) {
            objectName = objectName.substring(0, positiePunt);
        }
        return objectName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OracleObject that = (OracleObject) o;

        if (fileName != null ? !fileName.equals(that.fileName) : that.fileName != null) return false;
        return !(fileType != null ? !fileType.equals(that.fileType) : that.fileType != null);

    }

    @Override
    public int hashCode() {
        int result = fileName != null ? fileName.hashCode() : 0;
        result = 31 * result + (fileType != null ? fileType.hashCode() : 0);
        return result;
    }

    public static class OracleSequenceComparator implements Comparator<OracleObject> {
        //http://stackoverflow.com/questions/2784514/sort-arraylist-of-custom-objects-by-property
//        @Override
        public int compare(OracleObject oracleObject1, OracleObject oracleObject2) {
            return oracleObject1.getSequenceNumber() - oracleObject2.getSequenceNumber();
        }
    }
    public static class OracleObjectNameComparator implements Comparator<OracleObject> {
        //http://stackoverflow.com/questions/2784514/sort-arraylist-of-custom-objects-by-property
//        @Override
        public int compare(OracleObject oracleObject1, OracleObject oracleObject2) {
            String name1 = oracleObject1.getObjectName("")+"|"+oracleObject1.getFileType();
            String name2 = oracleObject2.getObjectName("")+"|"+oracleObject2.getFileType();
            return name1.compareTo(name2);
        }
    }

    public static class OracleFolderNameComparator implements Comparator<OracleObject> {
        //http://stackoverflow.com/questions/2784514/sort-arraylist-of-custom-objects-by-property
//        @Override
        public int compare(OracleObject oracleObject1, OracleObject oracleObject2) {
            return oracleObject1.getFolderName().compareTo(oracleObject2.getFolderName());
        }
    }

}
