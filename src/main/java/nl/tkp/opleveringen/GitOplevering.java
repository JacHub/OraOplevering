package nl.tkp.opleveringen;

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
    public static void main(String[] args) throws Exception {


        if (args.length != 3) {
            System.out.println("GitOplevering onvoldoende argumenten aantal is " + args.length);
            System.out.println("FOUT!!");
            System.out.println("");
            System.out.println("Om een oplevering aan te maken zijn er drie argumenten nodig:");
            System.out.println("  De root map waar de database sources van de applicatie staan.");
            System.out.println("  De versie van de applicatie.");
            System.out.println("  De map waar de oplevering aangemaakt moet worden.");
            System.out.println("Bijvoorbeeld:");
            System.out.println("  GitOplevering g:\\werk\\CAE CAE_1.01.001 g:\\werk\\oplevermap");
        } else {

            String folderGitProject = args[0]; //"D:\\dev\\oracletest";
            String versie = args[1]; // "CAE_1.01.111";
            String folderOplevering = args[2]; //"d:\\temp\\" + versie;
            folderOplevering = folderOplevering +"\\"+ versie;

            Set<String> gewijzigdeFiles = bepaalGewijzigdeBestanden();

            if (gewijzigdeFiles.size() > 0 && !FileHelper.folderExists(folderOplevering)) {
                FileHelper.createFolder(folderOplevering);
                System.out.println("map " + folderOplevering + " aangemaakt");
            }
            kopieerGewijzigdeBestandenNaarOpleverMap(folderGitProject, folderOplevering, gewijzigdeFiles);

            FileHelper.removeFilePrefixes(folderOplevering);

            List<File> fl = FileHelper.readFolder(folderOplevering);
            System.out.println("Huidige werkmap: " + folderOplevering);
            if (fl.size() == 0) {
                System.out.println("Geen bestanden in '" + folderOplevering + "' aanwezig om een oplevering van te maken!");
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
                System.out.println("De oplevering is aangemaakt!");

            }
        }
    }

    private static void kopieerGewijzigdeBestandenNaarOpleverMap(String folderGitProject, String folderOplevering, Set<String> gewijzigdeFiles) {
        for (String filenaam : gewijzigdeFiles) {
            String objectNaam = bepaalBestandsnaam(filenaam);
            System.out.println("copy file van " + folderGitProject + "\\" + filenaam + " naar " + folderOplevering + "\\" + objectNaam);
            FileHelper.copyFile(folderGitProject + "\\" + filenaam, folderOplevering + "\\" + objectNaam);
        }
    }

    private static String bepaalBestandsnaam(String filenaam) {
        String[] output = filenaam.split("/");
        String objectNaam = output[output.length - 1];
        String objectType = output[output.length - 2];
        String oudeFileType = objectNaam.substring(objectNaam.lastIndexOf("."));
        String nieuweFileType = bepaalFileType(filenaam);
        if (nieuweFileType == null) {
            nieuweFileType = oudeFileType;
        }
        objectNaam = objectNaam.replace(oudeFileType, "." + nieuweFileType);
        return objectNaam;
    }

    private static String bepaalFileType(String bestandsNaam) {
        // bepaal filetype
        System.out.println("bestandsnaam to type " + bestandsNaam);
        FileTypes fileTypes = new FileTypes();
        List<FileType> fileTypeList = fileTypes.getFileTypes();
        FileType fileType = fileTypeList.stream()
                .filter(f -> bestandsNaam.contains(f.objectType))
                .findAny()
                .orElse(null);

        System.out.println("filetype " + fileType);
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
        System.out.println("Finished");
        return null;
    }
}
