package nl.tkp.opleveringen;

import com.cedarsoftware.util.io.JsonReader;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Jacob on 12-11-2014.
 */
public class FileTypes {
    ArrayList<FileType> fileTypes;

    FileTypes(String configFolderName) {
        try {
            System.out.println("Laden filetypes (" + configFolderName + "\\filetypes.json).");
            // read in the object
            InputStream inputStream = new FileInputStream(configFolderName + "\\filetypes.json");
            JsonReader jr = new JsonReader(inputStream);

            this.fileTypes = (ArrayList<FileType>) jr.readObject();

            System.out.println("Er zijn " + +this.fileTypes.size() + " filetypes geladen.");
            jr.close();

        } catch (Exception ex) {
            System.out.println("error in FileTypes:" + ex.getMessage());
        }

    }

    FileTypes() {
        this.fileTypes.add(new FileType("seq", 1, "ddl", "Ja", true));
        this.fileTypes.add(new FileType("tab", 2, "ddl", "Ja", true));
        this.fileTypes.add(new FileType("ind", 2, "ddl", "Ja", true));
        this.fileTypes.add(new FileType("sql", 2, "ddl", "Ja", true));
        this.fileTypes.add(new FileType("prc", 2, "ddl", "Ja", true));
        this.fileTypes.add(new FileType("fnc", 2, "ddl", "Ja", true));
        this.fileTypes.add(new FileType("pks", 2, "ddl", "Ja", true));
        this.fileTypes.add(new FileType("pkb", 2, "ddl", "Ja", true));
        this.fileTypes.add(new FileType("fmb", 99, "fmb", "Nee", false));
        this.fileTypes.add(new FileType("fmx", 99, "fmb", "Nee", false));
        this.fileTypes.add(new FileType("rdf", 99, "fmb", "Nee", false));
        this.fileTypes.add(new FileType("pll", 99, "fmb", "Nee", false));
        this.fileTypes.add(new FileType("plx", 99, "fmb", "Nee", false));
    }

    public String toString() {
        String returnString = "FileTypes[";
        for (FileType ft : fileTypes) {
            returnString += ft.toString();
        }
        return returnString;
    }


}
