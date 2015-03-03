package nl.tkp.opleveringen;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by Jacob on 12-11-2014.
 */
public final class FileHelper {

    private FileHelper() {
    }

    public static List<File> readFolder(String folderName) {
        File folder = new File(folderName);
        FileHelper fr = new FileHelper();
        List<File> fileList = new ArrayList<File>();
        fr.listFilesForFolder(folder, fileList);
        return fileList;
    }

    public void listFilesForFolder(final File folder, List<File> fileList) {
        File[] filesInFolder = folder.listFiles();
        if (filesInFolder != null) {
            for (final File fileEntry : filesInFolder) {
                if (!fileEntry.isDirectory()
                        && !fileEntry.getName().contains(".txt")
                        && !fileEntry.getName().contains(".json")
                        && !fileEntry.getName().contains(".cfg")
                        && !fileEntry.getName().contains("actionnotes")
                        && !fileEntry.getName().contains("releasenotes")
                        && !fileEntry.getName().contains("setup")
                        && fileEntry.getName().contains(".")
                        ) {
                    fileList.add(fileEntry);
                }
            }
        }
    }

    public static void createFolder(String folderName) {
        File theDir = new File(folderName);

        // if the directory does not exist, create it
        if (!theDir.exists()) {
            boolean result = false;

            try {
                theDir.mkdir();
                result = true;
            } catch (SecurityException se) {
                //handle it
            }
            if (!result) {
                System.out.println("DIR NOT created");
            }
        }
    }

    public static void moveFile(String from, String to) {
        try {
            File afile = new File(from);
            if (!afile.renameTo(new File(to))) {
                System.out.println("Het verplaatsen van het bestand "+from+" naar "+to+" is fout gegaan!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static boolean folderExists (String folderName) {
        if (Files.isDirectory(Paths.get(folderName))) {
            return true;
        } else {
            return false;
        }
    }
    public static String getCurrentFolderName(String folderName) {
        return folderName.substring(folderName.lastIndexOf('\\')+1);
    }

    public static void removeFilePrefixes(String folderName) {
        List<File> fl = FileHelper.readFolder(folderName);
        String filename = "";
        String newFilename = "";
        String versie = getCurrentFolderName(folderName);
        System.out.println("Verwijderen evt versie prefix van de bestanden in de oplevermap "+folderName);
        for (File file : fl) {
            // trim evt al aanwezige versienummer
            int positieVersieNummer = file.getName().indexOf(versie);
            filename = file.getName();
            newFilename = file.getName();
            if (positieVersieNummer >= 0) {
                int positieSeparator = filename.indexOf('_', 7);
                if (positieSeparator >= 0) {
                    newFilename = filename.substring(positieSeparator + 1);
                    try {
                        File afile = new File(folderName+"\\"+filename);
                        if (!afile.renameTo(new File(folderName+"\\"+newFilename))) {
                            System.out.println("Het hernoemen van het bestand " + filename + " naar " + newFilename + " is fout gegaan!");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static Set<String> readFile(String filename) {
        List<String> list = new ArrayList<String>();
        try {
            list = Files.readAllLines(new File(filename).toPath(), Charset.defaultCharset());

        } catch (IOException e) {
            e.printStackTrace();
        }
        // maak er een set van zodat je geen dubbele meer hebt
        Set<String> s = new HashSet<String>(list);

        return s;
    }

    public static void generateFile(String filename, ArrayList<String> regels) {
        FileWriter fw = null;
        File file = null;
        try {
            file = new File(filename);
            boolean fileBestaatAl = true;
            boolean appendFile = false;

            if (!file.exists()) {
                file.createNewFile();
                fileBestaatAl = false;
            }
            if (fileBestaatAl && (file.getName().contains("releasenotes")||file.getName().contains("GewijzigdeObjecten"))) {
                appendFile = true;
                System.out.println("Uitbreiden: " + filename);
            } else {
                if (fileBestaatAl && file.getName().contains("setup")) {
                    // het gaat hier om de setup dus bestaande file uitbreiden met nieuwe objecten
                    appendFile = true;
                    System.out.println("");
                    System.out.println("**************************** LET OP! ****************************");
                    System.out.println("      Het setup script is uitgebreid met nieuwe objecten");
                    System.out.println("                Pas het setup script aan!");
                    System.out.println("*****************************************************************");
                    System.out.println("");
                } else {
                    appendFile = false;
                    System.out.println("Aanmaken  : " + filename);
                }
            }
            /* Als de file nog niet bestaat of wel bestaat maar ongelijk is aan de config en de actionnotes dan
               schrijven regels
            */
            if (!(fileBestaatAl && (file.getName().contains("oplevering.cfg") || file.getName().contains("actionnotes")))) {
                fw = new FileWriter(file, appendFile);

                for (String regel : regels) {
                    fw.write(regel + "\r\n");
                }
                fw.flush();
                fw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void zip(String inputFolder, String targetZippedFolder) throws IOException {

        FileOutputStream fileOutputStream = null;

        fileOutputStream = new FileOutputStream(targetZippedFolder);
        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

        File inputFile = new File(inputFolder);

        if (inputFile.isFile())
            zipFile(inputFile, "", zipOutputStream);
        else if (inputFile.isDirectory())
            zipFolder(zipOutputStream, inputFile, "");

        zipOutputStream.close();
    }


    public static void zipFolder(ZipOutputStream zipOutputStream, File inputFolder, String parentName) throws IOException {

        String myname = parentName + inputFolder.getName() + "\\";

        ZipEntry folderZipEntry = new ZipEntry(myname);
        zipOutputStream.putNextEntry(folderZipEntry);

        File[] contents = inputFolder.listFiles();

        for (File f : contents) {
            if (f.isFile())
                zipFile(f, myname, zipOutputStream);
            else if (f.isDirectory())
                zipFolder(zipOutputStream, f, myname);
        }
        zipOutputStream.closeEntry();
    }


    public static void zipFile(File inputFile, String parentName, ZipOutputStream zipOutputStream) throws IOException {


        // A ZipEntry represents a file entry in the zip archive
        // We name the ZipEntry after the original file's name
        ZipEntry zipEntry = new ZipEntry(parentName);
        zipOutputStream.putNextEntry(zipEntry);

        FileInputStream fileInputStream = new FileInputStream(inputFile);
        byte[] buf = new byte[1024];
        int bytesRead;

        // Read the input file by chucks of 1024 bytes
        // and write the read bytes to the zip stream
        while ((bytesRead = fileInputStream.read(buf)) > 0) {
            zipOutputStream.write(buf, 0, bytesRead);
        }

        // close ZipEntry to store the stream to the file
        zipOutputStream.closeEntry();

        System.out.println("Regular file :" + parentName + " is zipped to archive :");
    }

}
