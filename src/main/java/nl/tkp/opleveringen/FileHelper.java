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
                        && !fileEntry.getName().contains(".ser")
                        && !fileEntry.getName().contains(".cmd")
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
            File fromFile = new File(from);
            File toFile = new File(to);
            if (toFile.exists()) {
                System.out.println("Bestand '" + fromFile.getName() + "' bestaat al en wordt overschreven!");
                toFile.delete();
            }
            if (!fromFile.renameTo(new File(to))) {
                System.out.println("Het verplaatsen van het bestand " + from + " naar " + to + " is fout gegaan!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteFile(String filename) {
        try {
            File file = new File(filename);
            if (file.exists()) {
                System.out.println("Bestand '" + file.getName() + "' bestaat en wordt verwijderd!");
                file.delete();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean folderExists(String folderName) {
        if (Files.isDirectory(Paths.get(folderName))) {
            return true;
        } else {
            return false;
        }
    }

    public static String getCurrentFolderName(String folderName) {
        return folderName.substring(folderName.lastIndexOf('\\') + 1);
    }

    public static String getPeviousFolderName(String folderName) {
        return folderName.substring(0, folderName.lastIndexOf('\\'));
    }

    public static void removeFilePrefixes(String folderName) {
        List<File> fl = FileHelper.readFolder(folderName);
        String filename = "";
        String newFilename = "";
        String versie = getCurrentFolderName(folderName);
        System.out.println("Verwijderen evt versie prefix van de bestanden in de oplevermap " + folderName);
        for (File file : fl) {
            // trim evt al aanwezige versienummer
            int positieVersieNummer = file.getName().indexOf(versie);
            filename = file.getName();
            if (positieVersieNummer >= 0) {
                int positieSeparator = filename.indexOf('_', 7);
                if (positieSeparator >= 0) {
                    newFilename = filename.substring(positieSeparator + 1);
                    try {
                        File afile = new File(folderName + "\\" + filename);
                        if (!afile.renameTo(new File(folderName + "\\" + newFilename))) {
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
            boolean appendFile = false;

            if (file.exists()) {
                file.delete();
            }

            file.createNewFile();
            appendFile = false;
            System.out.println("Aanmaken  : " + filename);
            fw = new FileWriter(file, appendFile);

            for (String regel : regels) {
                fw.write(regel + "\r\n");
            }
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void zip(String teZippenFolder) {
        try {
            System.out.println("te zippen foldernaam is " + teZippenFolder + " naar " + getPeviousFolderName(teZippenFolder));

            zip(teZippenFolder);
        } catch (Exception se) {
            System.out.println("Wegens fouten is de oplevering niet gezipt: " + se.getStackTrace());
        }

    }

    public static void zipFolder(String teZippenFolder) throws IOException

    {
        File directoryToZip = new File(teZippenFolder);

        List<File> fileList = new ArrayList<File>();
        System.out.println("---Getting references to all files in: " + directoryToZip.getCanonicalPath());
        getAllFiles(directoryToZip, fileList);
        System.out.println("---Creating zip file");
        writeZipFile(directoryToZip, fileList);
        System.out.println("---Done");
    }

    public static void getAllFiles(File dir, List<File> fileList) {
        try {
            File[] files = dir.listFiles();
            for (File file : files) {
                fileList.add(file);
                if (file.isDirectory()) {
                    System.out.println("directory:" + file.getCanonicalPath());
                    getAllFiles(file, fileList);
                } else {
                    System.out.println("     file:" + file.getCanonicalPath());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeZipFile(File directoryToZip, List<File> fileList) {

        try {
            System.out.println("schrijf bestand naar " + getPeviousFolderName(directoryToZip.getName()) + directoryToZip.getName() + ".zip");
            FileOutputStream fos = new FileOutputStream(getPeviousFolderName(directoryToZip.getName()) + directoryToZip.getName() + ".zip");
            ZipOutputStream zos = new ZipOutputStream(fos);

            for (File file : fileList) {
                if (!file.isDirectory()) { // we only zip files, not directories
                    addToZip(directoryToZip, file, zos);
                }
            }
            zos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addToZip(File directoryToZip, File file, ZipOutputStream zos) throws FileNotFoundException,
            IOException {

        FileInputStream fis = new FileInputStream(file);

        // we want the zipEntry's path to be a relative path that is relative
        // to the directory being zipped, so chop off the rest of the path
        String zipFilePath = file.getCanonicalPath().substring(directoryToZip.getCanonicalPath().length() + 1,
                file.getCanonicalPath().length());
        System.out.println("Writing '" + zipFilePath + "' to zip file");
        ZipEntry zipEntry = new ZipEntry(zipFilePath);
        zos.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }

        zos.closeEntry();
        fis.close();
    }

}


