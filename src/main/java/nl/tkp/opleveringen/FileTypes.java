package nl.tkp.opleveringen;

import com.cedarsoftware.util.io.JsonReader;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
        fileTypes.add(new FileType("usr","Users",10  ,""                 ,"ddl","Ja",true ));
        fileTypes.add(new FileType("rle","Roles",20  ,""                 ,"ddl","Ja",true ));
        fileTypes.add(new FileType("seq","Sequences",30  ,"SEQUENCE"         ,"ddl","Ja",true ));
        fileTypes.add(new FileType("sqs","Sequences",31  ,"SEQUENCE"         ,"ddl","Ja",true ));
        fileTypes.add(new FileType("tps","",40  ,""                 ,"ddl","Ja",true ));
        fileTypes.add(new FileType("tab","Tables",50  ,"TABLE"            ,"ddl","Ja",true ));
        fileTypes.add(new FileType("aqt","QueueTables",60  ,""                 ,"ddl","Ja",true ));
        fileTypes.add(new FileType("aqq","Queues",70  ,""                 ,"ddl","Ja",true ));
        fileTypes.add(new FileType("fnc","Functions",80  ,"FUNCTION"         ,"ddl","Ja",true ));
        fileTypes.add(new FileType("syn","Synonyms",90  ,"SYNONYM"          ,"ddl","Ja",true ));
        fileTypes.add(new FileType("snp","MaterializedView",100 ,""                 ,"ddl","Ja",true ));
        fileTypes.add(new FileType("mv" ,"MaterializedView",110 ,"MATERIALIZED VIEW","ddl","Ja",true ));
        fileTypes.add(new FileType("vw" ,"View",120 ,"VIEW"             ,"ddl","Ja",true ));
        fileTypes.add(new FileType("prc","Procedures",130 ,"PROCEDURE"        ,"ddl","Ja",true ));
        fileTypes.add(new FileType("pks","PackageSpec",140 ,"PACKAGE"          ,"ddl","Ja",true ));
        fileTypes.add(new FileType("pkb","PackageBody",150 ,"PACKAGE BODY"     ,"ddl","Ja",true ));
        fileTypes.add(new FileType("trg","Triggers",160 ,"TRIGGER"          ,"ddl","Ja",true ));
        fileTypes.add(new FileType("sql","sql",170 ,""                 ,"ddl","Ja",true ));
        fileTypes.add(new FileType("ind","Index",180 ,""                 ,"ddl","Ja",true ));
        fileTypes.add(new FileType("con","Constraints",181 ,""                 ,"ddl","Ja",true ));
        fileTypes.add(new FileType("grt","Grants",190 ,""                 ,"ddl","Ja",true ));
        fileTypes.add(new FileType("ins","Inserts",191 ,""                 ,"ddl","Ja",true ));
        fileTypes.add(new FileType("upd","Updates",192 ,""                 ,"ddl","Ja",true ));
        fileTypes.add(new FileType("fmb","Fmb",200 ,""                 ,"fmb","Ja",false));
        fileTypes.add(new FileType("fmx","Fmx",210 ,""                 ,"fmx-linux","Ja",false));
        fileTypes.add(new FileType("pll","pll",240 ,""                 ,"pll","Ja",false));
        fileTypes.add(new FileType("plx","plx",250 ,""                 ,"plx-linux","Ja",false));
        fileTypes.add(new FileType("txt","txt",260 ,""                 ,"plx","Ja",false));
        fileTypes.add(new FileType("mmb","mmb",270 ,""                 ,"mmb","Ja",false));
        fileTypes.add(new FileType("mmx","mmx",280 ,""                 ,"mmx-linux","Ja",false));
        fileTypes.add(new FileType("rdf","rdf",270 ,""                 ,"rdf","Ja",false));

        this.fileTypes = fileTypes;
    }

    public ArrayList<FileType> getFileTypes() {
        return fileTypes;
    }

    public String toString() {
        String returnString = "FileTypes[";
        for (FileType ft : fileTypes) {
            returnString += ft.toString();
        }
        return returnString;
    }


}
