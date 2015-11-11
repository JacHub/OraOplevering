package nl.tkp.opleveringen;

import com.sun.org.apache.xpath.internal.SourceTree;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Jacob on 12-11-2014.
 */
public class MaakOplevering {
    public static void main(String[] args) throws Exception {
        // TODO Om een oplevering uit te kunnen breiden met nieuwe objecten de Oplevering opslaan als JSON bestand in de oplevermap
        // TODO Bij start eerst kijken of er een JSON oplever bestand is zoja deze eerst inlezen en nieuwe objecten er aan toevoegen
        // TODO Dan obv van dit object een compleet nieuwe oplevering genereren.
        if (args.length != 2) {
            System.out.println("MaakOplevering " + args.length);
            System.out.println("FOUT!!");
            System.out.println("");
            System.out.println("Om een oplevering aan te maken zijn er twee argumenten nodig!");
            System.out.println("De map waar het configuratie bestand filetyps.json staat en een map waar de oplevering staat");
            System.out.println("Bijvoorbeeld:");
            System.out.println("MaakOplevering g:\\werk\\config g:\\werk\\IBRRFE_1.02.003");
            System.out.println("");
            System.out.println("Maak een map met als naam de versie van de oplevering. bv ARS_1.02.003 of ARS_1.02.003_hf1 (let op er mogen geen spaties in staan!)");
            System.out.println("Zet in deze map de bestanden met of zonder de versie prefix.");
            System.out.println("Maak per object een bestand met de naam van het object en gebruik hierbij de bekende filetypes zoals tab, fnc en pks enz.");
            System.out.println("");
            System.out.println("Als er al een oplevering gegenereerd is kan er een nieuw object toegevoegd worden door dit in de root map te plaatsen en");
            System.out.println("opnieuw een oplevering te genereren.");
            System.out.println("Bestaat het object al dan wordt dit overschreven.");
            System.out.println("De oplevering wordt in zijn geheel opnieuw gegenereerd en achteraf gemaakte wijzigingen in b.v. de setup.sql worden overschreven.");
        } else {
            String configFolderName = "";
            String folderName = "";
            if (!args[0].isEmpty()) {
                configFolderName = args[0];
            }
            if (!args[1].isEmpty()) {
                folderName = args[1];
            }

            if (FileHelper.folderExists(folderName) && FileHelper.getCurrentFolderName(folderName).matches("\\S{3,7}_\\d.\\d\\d.\\d\\d\\d(_\\w{0,4})*")) {

                FileHelper.removeFilePrefixes(folderName);

                List<File> fl = FileHelper.readFolder(folderName);
                System.out.println("Huidige werkmap: " + folderName);
                if (fl.size() == 0) {
                    System.out.println("Geen bestanden in '" + folderName + "' aanwezig om een oplevering van te maken!");
                } else {
                    OracleOplevering opl = new OracleOplevering(fl, folderName, configFolderName);

                    for (File bestand : fl) {
                        opl.addOracleObject(new OracleObject(bestand.getName()));
                    }
                    //System.out.println( "Oracle oplevering= "+opl.toString());

                    Collections.sort(opl.oracleObjecten, new OracleObject.OracleFolderNameComparator());
                    verplaatsBestanden(folderName, opl);
                    opleveringUitbreiden(opl);
                    opl.saveOracleOplevering();
                    opl.createSetup();
                    opl.createVersiePre();
                    opl.createVersiePost();
                    opl.createReleasenotes();
                    opl.createConfig();
                    opl.createActionnotes();
                    //                    FileHelper.zip(folderName);

                    System.out.println("De oplevering is aangemaakt!");
                }
            } else {
                System.out.println("De opgegeven map '" + folderName + "' bestaat niet of de naam voldoet niet aan de conventie van een versienummer. (XXXXXXX_9.99.999)");
            }
        }
    }

    private static void verplaatsBestanden(String folderName, OracleOplevering opl) {
        String naarFolder = folderName + "\\ddl\\"; // map ddl altijd aanmaken!
        FileHelper.createFolder(naarFolder);
        for (OracleObject o : opl.oracleObjecten) {
            // Aanmaken folders en verplaatsen bestanden
            if (!o.getFolderName().equals("")) {
                if (!o.getFolderName().equals(naarFolder)) {
                    naarFolder = folderName + "\\" + o.getFolderName() + "\\";
                    FileHelper.createFolder(naarFolder);
                }
                FileHelper.moveFile(folderName + "\\" + o.getFileName(), naarFolder + o.getNewFileName(opl.versie));
            }
        }
    }

    private static void opleveringUitbreiden(OracleOplevering opl) {
        // evt. toevoegen al eerder opgeleverde objecten
        ArrayList<OracleObject> vorigeOracleObjectenInOplevering = opl.loadOracleOplevering();
        if (vorigeOracleObjectenInOplevering != null) {
            for (OracleObject vorigObject : vorigeOracleObjectenInOplevering) {
                opl.addOracleObject(vorigObject);
                System.out.println("Oplevering wordt uitgebreid met nieuwe objecten.");
            }
        }
    }
}
