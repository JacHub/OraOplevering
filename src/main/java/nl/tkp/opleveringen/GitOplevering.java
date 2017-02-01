package nl.tkp.opleveringen;

import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static nl.tkp.opleveringen.MaakOplevering.opleveringUitbreiden;
import static nl.tkp.opleveringen.MaakOplevering.verplaatsBestanden;

/**
 * Created on 14-09-2016.
 *
 * @author <a href="mailto:huizenga.j@tkppensioen.nl">Jacob Huizenga</a>
 */
public class GitOplevering {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GitOplevering.class);
        public static void main(String[] args) throws Exception {

        LOGGER.info("Start maken oplevering.");
        if (args.length != 3) {
            LOGGER.error ("GitOplevering onvoldoende argumenten aantal is " + args.length);
            LOGGER.error("FOUT!!");
            LOGGER.error("");
            LOGGER.error("Om een oplevering aan te maken zijn er drie argumenten nodig:");
            LOGGER.error("  De root map waar de database sources van de applicatie staan.");
            LOGGER.error("  De versie van de applicatie.");
            LOGGER.error("  De map waar de oplevering aangemaakt moet worden.");
            LOGGER.error("Bijvoorbeeld:");
            LOGGER.error("  GitOplevering g:\\werk\\CAE CAE_1.01.001 g:\\werk\\oplevermap");
        } else {

            String folderGitProject = args[0]; //"D:\\dev\\oracletest";
            String applicatieVersie = args[1]; // "CAE_1.01.111";
            String folderOplevering = args[2]; //"d:\\temp\\" + versie;
            folderOplevering = folderOplevering +"\\"+ applicatieVersie;

            Set<String> gewijzigdeFiles = bepaalGewijzigdeBestanden();

            if (gewijzigdeFiles.size() > 0 && !FileHelper.folderExists(folderOplevering)) {
                FileHelper.createFolder(folderOplevering);
                System.out.println("folder " + folderOplevering + " aangemaakt");
            }
            kopieerGewijzigdeBestandenNaarOpleverMap(folderGitProject, folderOplevering, gewijzigdeFiles);

            FileHelper.removeFilePrefixes(folderOplevering);

            List<File> fl = FileHelper.readFolder(folderOplevering);
            LOGGER.info("Huidige werkmap: " + folderOplevering);
            if (fl.size() == 0) {
                LOGGER.error("Geen bestanden in '" + folderOplevering + "' aanwezig om een oplevering van te maken!");
            } else {
                OracleOplevering opl = new OracleOplevering(fl, folderOplevering, null);

                for (File bestand : fl) {
                    opl.addOracleObject(new OracleObject(bestand.getName()));
                }
                Collections.sort(opl.oracleObjecten, new OracleObject.OracleFolderNameComparator());
                verplaatsBestanden(folderOplevering, opl);
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
        }
    }

    private static void kopieerGewijzigdeBestandenNaarOpleverMap(String folderGitProject, String folderOplevering, Set<String> gewijzigdeFiles) {
        for (String filenaam : gewijzigdeFiles) {
            String objectNaam = bepaalBestandsnaam(filenaam);
            LOGGER.info("copy file van " + folderGitProject + "\\" + filenaam + " naar " + folderOplevering + "\\" + objectNaam);
            FileHelper.copyFile(folderGitProject + "\\" + filenaam, folderOplevering + "\\" + objectNaam);
        }
    }

    private static String bepaalBestandsnaam(String filenaam) {
        String[] bestandsnaamMetPath = filenaam.split("/");
        String objectnaam = bestandsnaamMetPath[bestandsnaamMetPath.length - 1];
        String objectType = bestandsnaamMetPath[bestandsnaamMetPath.length - 2];
        String oudeFileType = objectnaam.substring(objectnaam.lastIndexOf("."));
        String nieuweFileType = bepaalFileType(objectType);
        if (nieuweFileType == null) {
            nieuweFileType = oudeFileType;
        }
        objectnaam = objectnaam.replace(oudeFileType, "." + nieuweFileType);
        return objectnaam;
    }

    private static String bepaalFileType(String bestandsnaam) {
        // bepaal nieuwe filetype
        FileTypes fileTypes = new FileTypes();
        List<FileType> fileTypeList = fileTypes.getFileTypes();
        FileType fileType = fileTypeList.stream()
                .filter(f -> bestandsnaam.contains(f.objectType))
                .findAny()
                .orElse(null);

        LOGGER.info("bestandsnaam to type bestandsnaam: " + bestandsnaam+ " filetype: " + fileType);
        return fileType.name;
    }

    private static Set<String> bepaalGewijzigdeBestanden() {
        try {
            Process process = Runtime
                    .getRuntime()
                    .exec("git log develop.. --name-status --pretty=format:");
//                    .exec("git log develop.. --name-status --pretty=format: | awk 'NF' | sort -k2,2 -u");
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            String line = reader.readLine();
            Set<String> gewijzigdeFiles = new TreeSet<>();
            while (line != null) {
                if (!line.isEmpty()) {
                    gewijzigdeFiles.add(line.substring(2));
                }
                line = reader.readLine();
            }
            return gewijzigdeFiles;
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return null;
    }
}
