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
            System.out.println("Laden filetypes ("+configFolderName+"\\filetypes.json).");
            // read in the object
            InputStream inputStream = new FileInputStream(configFolderName+"\\filetypes.json");
            JsonReader jr = new JsonReader(inputStream);

            this.fileTypes = (ArrayList<FileType>) jr.readObject();

            System.out.println("Er zijn " + +this.fileTypes.size() + " filetypes geladen.");

//            for (FileType ft : this.fileTypes) {
//                System.out.println(ft.name +"-"+ ft.sequenceNumber);
//            }
            jr.close();

        } catch (Exception ex) {
            System.out.println("error in FileTypes:" + ex.getMessage());
        }

    }

    public String toString() {
        String returnString = "FileTypes[";
        for (FileType ft : fileTypes) {
            returnString += ft.toString();
        }
        return returnString;
    }


}
