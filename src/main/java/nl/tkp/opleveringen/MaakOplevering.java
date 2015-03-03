package nl.tkp.opleveringen;
import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Created by Jacob on 12-11-2014.
 */
public class MaakOplevering {
    public static void main(String[] args) throws WrongVersionNameException, ConfigFileNotExistsException, ConfigFileNotValidException {
        if (args.length != 2) {
            System.out.println("MaakOplevering "+args.length);
            System.out.println("FOUT!!");
            System.out.println("");
            System.out.println("Om een oplevering aan te maken zijn er twee argumenten nodig!");
            System.out.println("De map waar het configuratie bestand filetyps.json staat en een map waar de oplevering staat");
            System.out.println("Bijvoorbeeld:");
            System.out.println("MaakOplevering g:\\werk\\config g:\\werk\\IBRRFE_1.02.003");
        } else {
            String configFolderName = "";
            String folderName = "";
            if (!args[0].isEmpty()) {
                configFolderName = args[0];
            }
            if (!args[0].isEmpty()) {
                folderName = args[1];
            }
            if (FileHelper.folderExists(folderName) && FileHelper.getCurrentFolderName(folderName).matches("\\S{3,7}_\\d.\\d\\d.\\d\\d\\d") ) {

                FileHelper.removeFilePrefixes(folderName);

                List<File> fl = FileHelper.readFolder(folderName);
                System.out.println("Huidige werkmap: " + folderName);
                if (fl.size() == 0) {
                    System.out.println("Geen bestanden in " + folderName + " aanwezig om een oplevering van te maken!");
                } else {

                    OracleOplevering opl = new OracleOplevering(fl, folderName, configFolderName);
                    for (File bestand : fl) {
                        opl.addOracleObject(new OracleObject(bestand.getName()));
                    }
                    //System.out.println( "Oracle oplevering= "+opl.toString());

                    Collections.sort(opl.oracleObjecten, new OracleObject.OracleFolderNameComparator());
                    String naarFolder = folderName + "\\ddl\\"; // map ddl altijd aanmaken!
                    FileHelper.createFolder(naarFolder);
                    for (OracleObject o : opl.oracleObjecten) {
                        //
                        // Aanmaken folders
                        //
                        if (!o.getFolderName().equals(naarFolder)) {
                            naarFolder = folderName + "\\" + o.getFolderName() + "\\";
                            FileHelper.createFolder(naarFolder);
                        }
                        //
                        // Verplaats de bestanden naar de juiste map
                        //
                        FileHelper.moveFile(folderName + "\\" + o.getFileName(), naarFolder + o.getNewFileName(opl.versie));
                    }
                    opl.createObjectenLijst();
                    opl.createSetup();
                    opl.createVersiePre();
                    opl.createVersiePost();
                    opl.createReleasenotes();
                    opl.createConfig();
                    opl.createActionnotes();
                    try {
                        System.out.println("te zippen foldernaam is "+ folderName);
                        FileHelper.zip(folderName, "G:\\werk\\jacob.zip");
                    }   catch (Exception se) {
                        System.out.println("Wegens fouten is de oplevering niet gezipt: "+se.getStackTrace());
                    }

                    System.out.println("De oplevering is aangemaakt!");
                }
            } else {
                System.out.println("De opgegeven map " + folderName + " bestaat niet of de naam voldoet niet aan de conventie van een versienummer. (XXXXXXX_9.99.999)");
            }
        }
    }

}
