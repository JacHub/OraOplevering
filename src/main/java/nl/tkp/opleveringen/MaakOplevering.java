package nl.tkp.opleveringen;

import org.slf4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Jacob on 12-11-2014.
 */
public class MaakOplevering {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MaakOplevering.class);

    public static void main(String[] args) throws Exception {
        LOGGER.info("***********************************************************************************************************************************");
        LOGGER.info("Maak een map met als naam de versie van de oplevering. bv ARS_1.02.003 of ARS_1.02.003_hf1 (let op er mogen geen spaties in staan!)");
        LOGGER.info("Zet in deze map de bestanden met of zonder de versie prefix.");
        LOGGER.info("Maak per object een bestand met de naam van het object en gebruik hierbij de bekende filetypes zoals tab, fnc en pks enz.");
        LOGGER.info("");
        LOGGER.info("Als er al een oplevering gegenereerd is kan er een nieuw object toegevoegd worden door dit in de root map te plaatsen en");
        LOGGER.info("opnieuw een oplevering te genereren.");
        LOGGER.info("Bestaat het object al dan wordt dit overschreven.");
        LOGGER.info("De oplevering wordt in zijn geheel opnieuw gegenereerd en achteraf gemaakte wijzigingen in b.v. de setup.sql worden overschreven.");
        LOGGER.info("***********************************************************************************************************************************");
        LOGGER.info("");

        if (args.length != 2) {
            LOGGER.error("MaakOplevering " + args.length);
            LOGGER.error("FOUT!!");
            LOGGER.error("");
            LOGGER.error("Om een oplevering aan te maken zijn er twee argumenten nodig!");
            LOGGER.error("De map waar het configuratie bestand filetyps.json staat en een map waar de oplevering staat");
            LOGGER.error("Bijvoorbeeld:");
            LOGGER.error("MaakOplevering g:\\werk\\config g:\\werk\\IBRRFE_1.02.003");
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
                LOGGER.info("Huidige werkmap: " + folderName);
                if (fl.size() == 0) {
                    LOGGER.error("Geen bestanden in '" + folderName + "' aanwezig om een oplevering van te maken!");
                } else {
                    OracleOplevering opl = new OracleOplevering(fl, folderName, configFolderName);

                    for (File bestand : fl) {
                        opl.addOracleObject(new OracleObject(bestand.getName()));
                    }
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
                    LOGGER.info("De oplevering is aangemaakt!");
                }
            } else {
                LOGGER.error("De opgegeven map '" + folderName + "' bestaat niet of de naam voldoet niet aan de conventie van een versienummer. (XXXXXXX_9.99.999)");
            }
        }
    }

    public static void verplaatsBestanden(String folderName, OracleOplevering opl) {
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

    public static void opleveringUitbreiden(OracleOplevering opl) {
        // evt. toevoegen al eerder opgeleverde objecten
        ArrayList<OracleObject> vorigeOracleObjectenInOplevering = opl.loadOracleOplevering();
        if (vorigeOracleObjectenInOplevering != null) {
            for (OracleObject vorigObject : vorigeOracleObjectenInOplevering) {
                opl.addOracleObject(vorigObject);
                LOGGER.info("Oplevering wordt uitgebreid met nieuwe objecten.");
            }
        }
    }
}
