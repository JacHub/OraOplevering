package nl.tkp.opleveringen;

import nl.tkp.opleveringen.threads.SearchLabelsCallThread;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Jacob on 12-11-2014.
 */
public class OracleOplevering {

    public String versie;
    public String folder;
    public ArrayList<OracleObject> oracleObjecten;
    private ArrayList<FileType> fileTypes;
    private SearchLabelsCallThread jiraCall;
    private Map<String, String> jiraMeldingen = null;

    OracleOplevering(List<File> fl, String foldername, String configFolderName) throws ConfigFileNotExistsException, ConfigFileNotValidException {
        this.versie = getVersionName(fl);
        jiraCall = new SearchLabelsCallThread(this.versie); // 28-04-2015 Lveekhout: zo snel al het kan jira REST call afvuren.
        this.folder = foldername;
        FileTypes fts = new FileTypes(configFolderName);
        if (fts == null) {
            throw new ConfigFileNotExistsException("Configuratie bestand filetypes.json niet gevonden in de map " + foldername);
        }
        this.fileTypes = fts.fileTypes;
        if (this.fileTypes == null || this.fileTypes.size() == 0) {
            throw new ConfigFileNotValidException("Inhoud van het config bestand niet juist.");
        }
        this.oracleObjecten = new ArrayList<OracleObject>();
    }

    public void addOracleObject(OracleObject oo) {
        /**
         * Voeg een nieuw object toe aan de oplevering
         */
        //System.out.println("addOracleObject this file type "+oo.getFileType());
        int i = this.fileTypes.indexOf(new FileType(oo.getFileType(), 0));
        if (i >= 0) {
            oo.setFolderName(this.fileTypes.get(i).folderName);
            oo.setSequenceNumber(this.fileTypes.get(i).sequenceNumber);
            oo.setFilePrefix(this.fileTypes.get(i).filePrefix);
            oo.setInSetup(this.fileTypes.get(i).inSetup);
            if (!this.oracleObjecten.contains(oo)) {
                this.oracleObjecten.add(oo);
            }
        } else {
            System.out.println("********************************************************************************************");
            System.out.println("FileType '" + oo.getFileType() + "' NIET gevonden! Het bestand blijft in de huidige map staan.");
            System.out.println("********************************************************************************************");
            oo.setFolderName("");
            oo.setSequenceNumber(999);
            oo.setFilePrefix("Nee");
            oo.setInSetup(false);
        }
    }

    private String getVersionName(List<File> fl) {
        /**
         * Haal het versienummer uit de naam van de huidige map
         */
        String versie = "";
        if (fl.size() > 0) {
            versie = fl.get(0).getPath();
            int laatsteSlash = versie.lastIndexOf("\\");
            int eenNaLaatsteSlash = versie.lastIndexOf("\\", laatsteSlash - 1) + 1;
            versie = versie.substring(eenNaLaatsteSlash, laatsteSlash);
        }
        return versie;
    }

    public void createSetup() {
        /**
         * Aanmaken setup script
         */
        String filename = this.folder + "\\ddl\\" + this.versie + "_setup.sql";
        String scriptPrefix = "@@";
        ArrayList<String> regels = new ArrayList<String>();
        regels.add(("WHENEVER SQLERROR EXIT SQL.SQLCODE"));
        regels.add("set serveroutput on size 1000000");
        regels.add("");
        regels.add("spool " + this.versie + "_setup.lst");

        regels.add("");
        regels.add("prompt Controleren of er niet al een nieuwere versie van het object bestaat.");
        regels.add("@@" + this.versie + "_ins_versie_pre.sql");
        regels.add("");
        Collections.sort(this.oracleObjecten, new OracleObject.OracleSequenceComparator());
        String vorigeFiletype = "xx";
        for (OracleObject o : this.oracleObjecten) {
            if (o.getInSetup()) {
                if (!vorigeFiletype.equals(o.getFileType())) {
                    regels.add("");
                    regels.add("prompt Aanmaken of wijzigen objecten van het type " + o.getFileType());
                    vorigeFiletype = o.getFileType();
                }
                regels.add(scriptPrefix + o.getNewFileName(this.versie));
            }
        }
        regels.add("");
        regels.add("prompt inserten versienummer");
        regels.add("@@" + this.versie + "_ins_versie.sql");
        regels.add("");
        regels.add("spool off");
        regels.add("");
        FileHelper.generateFile(filename, regels);
    }

    public void createVersiePre() throws WrongVersionNameException {
        /**
         * Aanmaken insertscript voor het versienummer
         */
        ArrayList<String> regels = new ArrayList<String>();
        regels.add("prompt controleer of er niet al een nieuwere versie van het object staat.");
        regels.add("");
        for (OracleObject oracleObject : this.oracleObjecten) {
            regels.add("declare l_ok varchar2(10);err EXCEPTION;PRAGMA EXCEPTION_INIT( err, -20000 ); begin execute immediate 'select gen_f_controleer_object_versie(''"
                    + getApplicatieId() + "'', ''"
                    + getVersieNummer() + "'', ''" + oracleObject.getFileName() + "'', ''"
                    + oracleObject.getFileType() + "'') from dual' into l_ok; exception when err then raise; when others then null; end;");
            regels.add("/");
        }
        FileHelper.generateFile(this.folder + "\\ddl\\" + this.versie + "_ins_versie_pre.sql", regels);
    }

    public void createVersiePost() throws WrongVersionNameException {
        /**
         * Aanmaken insertscript voor het versienummer
         */
        ArrayList<String> regels = new ArrayList<String>();
        regels.add("insert into gen_versies( deelapplicatie, versienummer) values ('" + getApplicatieId() + "','" + getVersieNummer() + "');");
        regels.add("");
        for (OracleObject oracleObject : this.oracleObjecten) {
            regels.add("begin execute immediate 'insert into gen_object_versies (deelapplicatie, versienummer, objectnaam, objecttype) values (''"
                    + getApplicatieId() + "'', ''"
                    + getVersieNummer() + "'', ''"
                    + oracleObject.getFileName() + "'', ''"
                    + oracleObject.getFileType() + "'')'; exception when others then null; end;");
            regels.add("/");
        }
        regels.add("");
        regels.add("commit;");
        regels.add("");
        FileHelper.generateFile(this.folder + "\\ddl\\" + this.versie + "_ins_versie.sql", regels);
    }

    public void createConfig() {
        /**
         * Aanmaken configfile
         */
        ArrayList<String> regels = new ArrayList<String>();
        regels.add("#schema eigenaar|naam setup script|Toegestane omgevingen");
        regels.add("#");
        if (this.versie.substring(0, 3).equals("RPE")) {
            regels.add("RPE|" + this.versie + "_setup.sql|RPEDEV,RPETST,RPEACC,RPEPRD\n");
        } else {
            regels.add("PAS|" + this.versie + "_setup.sql|PPSDEV,PPSTST,PPSACC,PPSPRD\n");
        }
        FileHelper.generateFile(this.folder + "\\oplevering.cfg", regels);
    }

    public void createActionnotes() {
        /**
         * Aanmaken actionnotes
         */
        ArrayList<String> regels = new ArrayList<String>();
        regels.add("Actionnotes");
        regels.add("");
        regels.add("Auteur      : " + System.getProperty("user.name"));
        regels.add("Versie      : " + this.versie);
        SimpleDateFormat formatter;
        Locale currentLocale = new Locale("nl");
        formatter = new SimpleDateFormat("EEEEEEEEEE dd-MM-yyyy H:mm:ss", currentLocale);
        regels.add("Datum       : " + formatter.format(new Date()));
        vulJiraMeldingen(regels);
        regels.add("Omschrijving: \n");
        regels.add("");
        regels.add("Volg de normale procedure voor het uitvoeren van het setup scripts.");
        regels.add("");
        regels.add("1 " + this.versie + "_setup.sql onder PAS in de PPS database");
        regels.add("");
        FileHelper.generateFile(this.folder + "\\" + this.versie + "_actionnotes.txt", regels);
    }

    public void createReleasenotes() {
        /**
         / Aanmaken Releasenotes
         */
        String filename = this.folder + "\\" + this.versie + "_releasenotes.txt";
        ArrayList<String> regels = new ArrayList<String>();
        regels.add("Releasenotes");
        regels.add("");
        regels.add("Auteur      : " + System.getProperty("user.name"));
        regels.add("Versie      : " + this.versie);
        SimpleDateFormat formatter;
        Locale currentLocale = new Locale("nl");
        formatter = new SimpleDateFormat("EEEEEEEEEE dd-MM-yyyy H:mm:ss", currentLocale);
        regels.add("Datum       : " + formatter.format(new Date()));

        vulJiraMeldingen(regels);

        regels.add("Omschrijving: ");
        regels.add("");
        regels.add("De release bestaat uit de volgende scripts en of objecten:");
        regels.add("");

        Collections.sort(this.oracleObjecten, new OracleObject.OracleSequenceComparator());
        String vorigeType = "#";
        for (OracleObject o : this.oracleObjecten) {
            if (!o.getFileType().equals(vorigeType)) {
                regels.add(o.getFileType());
                vorigeType = o.getFileType();
            }
            regels.add(" - " + o.getObjectName(versie));
        }
        FileHelper.generateFile(filename, regels);
    }

    private void vulJiraMeldingen(ArrayList<String> regels) {
        // 03-03-2015 Lveekhout:
        try {
            if (jiraMeldingen == null) {
                jiraMeldingen = jiraCall.resultaat(); // 28-04-2015 Lveekhout: haal resultaat van de jira REST call thread.
            }
            if (jiraMeldingen.size() > 0) {
                regels.add("");
                regels.add("JIRA meldingen:");
                for (Map.Entry<String, String> entry : jiraMeldingen.entrySet()) {
                    regels.add("- [" + entry.getKey() + "] " + entry.getValue());
                }
                regels.add("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getApplicatieId() throws WrongVersionNameException {
        int positieSeparator = this.versie.indexOf('_');
        if (positieSeparator < 0) {
            positieSeparator = this.versie.indexOf(' ');
        }
        if (positieSeparator < 0) {
            throw new WrongVersionNameException("De naam van de map moet gelijk zijn aan de naam van de versie! Huidige naam bevat geen _ tussen applcatie en nummer.");
        }
        return this.versie.substring(0, positieSeparator);
    }

    public String getVersieNummer() throws WrongVersionNameException {
        int positieSeparator = this.versie.indexOf('_');
        if (positieSeparator < 0) {
            positieSeparator = this.versie.indexOf(' ');
        }
        if (positieSeparator < 0) {
            throw new WrongVersionNameException("De naam van de map moet gelijk zijn aan de naam van de versie! Huidige naam bevat geen _ tussen applcatie en nummer.");
        }
        return this.versie.substring(positieSeparator + 1);
    }

    void saveOracleOplevering() {
        try {
            FileOutputStream fileOut = new FileOutputStream(this.folder + "/oracleObjecten.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(this.oracleObjecten);
            out.close();
            fileOut.close();
            System.out.println("Serialized data is opgeslagen in " + this.folder + "/oracleObjecten.ser");
        } catch (IOException i) {
            System.out.println("Oplevering kan niet opgeslagen worden!");
            i.printStackTrace();
        }
    }

    ArrayList<OracleObject> loadOracleOplevering() {
        try {
            FileInputStream fileIn = new FileInputStream(this.folder + "/oracleObjecten.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            ArrayList<OracleObject> oracleObjecten = (ArrayList<OracleObject>) in.readObject();
            in.close();
            fileIn.close();
            return oracleObjecten;
        } catch (IOException i) {
            System.out.println("Bestand oracleObjecten.ser niet gevonden, waarschijnlijk gaat het hier om een nieuwe oplevering!");
            return null;
        } catch (ClassNotFoundException c) {
            System.out.println("oracleObjecten.ser gevonden welke niet gedeserialized kan worden naar de class OracleObject.");
            c.printStackTrace();
            throw new RuntimeException("oracleObjecten.ser gevonden welke niet gedeserialized kan worden naar de class OracleObject. Verwijder dit bestand en verplaats alle bestanden weer naar de oplevermap en genereer de oplevering opnieuw!");
        }
    }

    public String toString() {
        String returnString = "OralceOplevering: versie:" + this.versie + " folder " + folder + " ";
        for (OracleObject oo : oracleObjecten) {
            returnString = returnString + " OracleObject " + oo.toString();
        }
        return returnString;
    }

    class WrongVersionNameException extends Exception {
        WrongVersionNameException(String s) {
            super(s);
        }
    }

    class ConfigFileNotExistsException extends Exception {
        ConfigFileNotExistsException(String s) {
            super(s);
        }
    }

    class ConfigFileNotValidException extends Exception {
        ConfigFileNotValidException(String s) {
            super(s);
        }
    }

}
