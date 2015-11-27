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
        ArrayList<FileType> fileTypes = new ArrayList<FileType>();
        fileTypes.add(new FileType("usr",10  ,""                 ,"ddl","Ja",true ));
        fileTypes.add(new FileType("rle",20  ,""                 ,"ddl","Ja",true ));
        fileTypes.add(new FileType("seq",30  ,"SEQUENCE"         ,"ddl","Ja",true ));
        fileTypes.add(new FileType("sqs",31  ,"SEQUENCE"         ,"ddl","Ja",true ));
        fileTypes.add(new FileType("tps",40  ,""                 ,"ddl","Ja",true ));
        fileTypes.add(new FileType("tab",50  ,"TABLE"            ,"ddl","Ja",true ));
        fileTypes.add(new FileType("aqt",60  ,""                 ,"ddl","Ja",true ));
        fileTypes.add(new FileType("aqq",70  ,""                 ,"ddl","Ja",true ));
        fileTypes.add(new FileType("fnc",80  ,"FUNCTION"         ,"ddl","Ja",true ));
        fileTypes.add(new FileType("syn",90  ,"SYNONYM"          ,"ddl","Ja",true ));
        fileTypes.add(new FileType("snp",100 ,""                 ,"ddl","Ja",true ));
        fileTypes.add(new FileType("mv" ,110 ,"MATERIALIZED VIEW","ddl","Ja",true ));
        fileTypes.add(new FileType("vw" ,120 ,"VIEW"             ,"ddl","Ja",true ));
        fileTypes.add(new FileType("prc",130 ,"PROCEDURE"        ,"ddl","Ja",true ));
        fileTypes.add(new FileType("pks",140 ,"PACKAGE"          ,"ddl","Ja",true ));
        fileTypes.add(new FileType("pkb",150 ,"PACKAGE BODY"     ,"ddl","Ja",true ));
        fileTypes.add(new FileType("trg",160 ,"TRIGGER"          ,"ddl","Ja",true ));
        fileTypes.add(new FileType("sql",170 ,""                 ,"ddl","Ja",true ));
        fileTypes.add(new FileType("ind",180 ,""                 ,"ddl","Ja",true ));
        fileTypes.add(new FileType("con",181 ,""                 ,"ddl","Ja",true ));
        fileTypes.add(new FileType("grt",190 ,""                 ,"ddl","Ja",true ));
        fileTypes.add(new FileType("ins",191 ,""                 ,"ddl","Ja",true ));
        fileTypes.add(new FileType("upd",192 ,""                 ,"ddl","Ja",true ));
        fileTypes.add(new FileType("fmb",200 ,""                 ,"fmb","Ja",false));
        fileTypes.add(new FileType("fmx",210 ,""                 ,"fmx","Ja",false));
        fileTypes.add(new FileType("pll",240 ,""                 ,"pll","Ja",false));
        fileTypes.add(new FileType("plx",250 ,""                 ,"plx","Ja",false));
        fileTypes.add(new FileType("txt",260 ,""                 ,"plx","Ja",false));
        fileTypes.add(new FileType("mmb",270 ,""                 ,"mmb","Ja",false));
        fileTypes.add(new FileType("mmx",280 ,""                 ,"mmx","Ja",false));
        fileTypes.add(new FileType("rdf",270 ,""                 ,"rdf","Ja",false));

        this.fileTypes = fileTypes;
    }

    public String toString() {
        String returnString = "FileTypes[";
        for (FileType ft : fileTypes) {
            returnString += ft.toString();
        }
        return returnString;
    }


}
