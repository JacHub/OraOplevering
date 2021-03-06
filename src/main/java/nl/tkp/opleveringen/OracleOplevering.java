package nl.tkp.opleveringen;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Jacob on 12-11-2014.
 */
class WrongVersionNameException extends Exception {
    WrongVersionNameException(String s) {
        super(s);
    }
}

public class OracleOplevering {
    String versie;
    String folder;
    ArrayList<FileType> fileTypes;
    ArrayList<OracleObject> oracleObjecten;


    OracleOplevering(List<File> fl, String foldername, String configFolderName) {
        this.versie = getVersionName(fl);
        this.folder = foldername;
        FileTypes fts = new FileTypes(configFolderName);
        this.fileTypes = fts.fileTypes;
        if (this.fileTypes.size() == 0) {
            System.out.println("fileType is leeg");
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
        } else {
            System.out.println("FileType "+oo.getFileType()+" NIET gevonden! Het bestand blijft in de huidige map staan.");
            oo.setFolderName("");
            oo.setSequenceNumber(999);
            oo.setFilePrefix("Nee");
            oo.setInSetup(false);
        }
        this.oracleObjecten.add(oo);
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
        File file = new File(filename);
        String scriptPrefix = "@@";
        ArrayList<String> regels = new ArrayList<String>();

        if (!file.exists()) {
            regels.add("set serveroutput on size 1000000");
            regels.add("");
            regels.add("spool " + this.versie + "_setup.lst");
        } else {
            scriptPrefix = "-- @@";
            regels.add("********************************************************");
            regels.add("************* LET OP setup uitgebreid! *****************");
            regels.add("********************************************************");
        }
        regels.add("");
 //       regels.add("prompt Controleren of er niet al een nieuwere versie van het object bestaat.");
 //       regels.add("@@" + this.versie + "_ins_versie_pre.sql");
 //       regels.add("");

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

    public void createVersiePre() throws WrongVersionNameException{
        /**
         * Aanmaken insertscript voor het versienummer
         */
        ArrayList<String> regels = new ArrayList<String>();
        Set<String> oracleObjecten = FileHelper.readFile(this.folder + "\\GewijzigdeObjecten.txt");
        for (String oracleObject : oracleObjecten) {
            if (!oracleObject.substring(0,1).equals("#")) {

                int eersteSeparator = oracleObject.indexOf('|')+1;
                int tweedeSeparator = oracleObject.indexOf('|',eersteSeparator)+1;
                int derdeSeparator = oracleObject.indexOf('|',tweedeSeparator);
                if (derdeSeparator == -1) {
                    System.out.println("Bestand is niet in het juiste formaat: "+ oracleObject);
                } else {
                    regels.add("prompt controleer of er niet al een nieuwere versie van het object staat.");
                    regels.add("-- select gen_f_controleer_object_versie('" + getApplicatieId() + "', '"
                            + getVersieNummer() + "', '" + oracleObject.substring(eersteSeparator, tweedeSeparator-1) + "', '"
                            + oracleObject.substring(tweedeSeparator, derdeSeparator)  + "') from dual;");
                    regels.add("");
                }
            }
        }
        FileHelper.generateFile(this.folder + "\\ddl\\"+this.versie+"_ins_versie_pre.sql", regels);
    }

    public void createVersiePost() throws WrongVersionNameException{
        /**
         * Aanmaken insertscript voor het versienummer
         */
        ArrayList<String> regels = new ArrayList<String>();
        regels.add("insert into gen_versies( deelapplicatie, versienummer) values ('" + getApplicatieId() + "','" + getVersieNummer() + "');");
        regels.add("");

        Set<String> oracleObjecten = FileHelper.readFile(this.folder + "\\GewijzigdeObjecten.txt");
        for (String oracleObject : oracleObjecten) {
            if (!oracleObject.substring(0,1).equals("#")) {
                int eersteSeparator = oracleObject.indexOf('|')+1;
                int tweedeSeparator = oracleObject.indexOf('|',eersteSeparator)+1;
                int derdeSeparator = oracleObject.indexOf('|',tweedeSeparator);
                if (derdeSeparator == -1) {
                    System.out.println("Bestand is niet in het juiste formaat: "+ oracleObject);
                } else {
                    regels.add("-- insert into gen_object_versies (deelapplicatie, versienummer, objectnaam, object_type) values ('"
                            + getApplicatieId() + "', '" + getVersieNummer() + "', '"
                            + oracleObject.substring(eersteSeparator, tweedeSeparator-1)+ "', '"
                            + oracleObject.substring(tweedeSeparator, derdeSeparator)  + "');");
                }
            }
        }

        regels.add("");
        regels.add("commit;");
        regels.add("");
        FileHelper.generateFile(this.folder + "\\ddl\\"+this.versie+"_ins_versie.sql", regels);
    }
    public void createConfig() {
        /**
         * Aanmaken configfile
         */
        ArrayList<String> regels = new ArrayList<String>();
        regels.add("#schema eigenaar|naam setup script|Toegestane omgevingen");
        regels.add("#");
        if (this.versie.substring(0,3).equals("RPE")) {
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
        regels.add( "Actionnotes");
        regels.add("");
        regels.add( "Auteur      : " + System.getProperty("user.name"));
        regels.add( "Versie      : " + this.versie);

        SimpleDateFormat formatter;
        Locale currentLocale = new Locale("nl");
        formatter = new SimpleDateFormat("EEEEEEEEEE dd-MM-yyyy H:mm:ss", currentLocale);
        regels.add( "Datum       : " + formatter.format(new Date()));

        regels.add( "Omschrijving: \n");
        regels.add("");
        regels.add( "Volg de normale procedure voor het uitvoeren van het setup scripts.");
        regels.add("");
        regels.add( "1 "+ this.versie+"_setup.sql onder PAS in de PPS database");
        regels.add("");
        FileHelper.generateFile(this.folder + "\\"+this.versie+"_actionnotes.txt", regels);
    }

    public void createReleasenotes() {
        /**
        / Aanmaken Releasenotes
        */
        String filename = this.folder + "\\" + this.versie + "_releasenotes.txt";
        File file = new File(filename);
        ArrayList<String> regels = new ArrayList<String>();

        if (!file.exists()) {
            regels.add("Releasenotes");
            regels.add("");
            regels.add("Auteur      : " + System.getProperty("user.name"));
            regels.add("Versie      : " + this.versie);

            SimpleDateFormat formatter;
            Locale currentLocale = new Locale("nl");
            formatter = new SimpleDateFormat("EEEEEEEEEE dd-MM-yyyy H:mm:ss", currentLocale);
            regels.add("Datum       : " + formatter.format(new Date()));

            regels.add("Omschrijving: ");
            regels.add("");
            regels.add("De release bestaat uit de volgende scripts en of objecten:");
        }
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

    public void createObjectenLijst() {
        /**
         / Aanmaken Releasenotes
         */
        String filename = this.folder + "\\GewijzigdeObjecten.txt";
        File file = new File(filename);
        ArrayList<String> regels = new ArrayList<String>();

        if (!file.exists()) {
            regels.add("#|Naam en type van de objecten welke opgeleverd worden.");
            regels.add("#|Naam|Type|");
        }

        Collections.sort(this.oracleObjecten, new OracleObject.OracleObjectNameComparator());
        String vorigeObjectNaam = "#";
        for (OracleObject o : this.oracleObjecten) {
            // objectnamen ontdubbelen
            if (!vorigeObjectNaam.equals(o.getObjectName(this.versie))) {
                regels.add("|"+ o.getObjectName(this.versie).toUpperCase() +"|"+o.getFileType().toUpperCase() +"|");
                vorigeObjectNaam = o.getObjectName(this.versie)+"|"+o.getFileType();
            }
        }
        FileHelper.generateFile(filename, regels);
    }
    public String getApplicatieId() throws WrongVersionNameException {
        int positieSeparator = this.versie.indexOf('_');
        if (positieSeparator < 0) {
            positieSeparator = this.versie.indexOf(' ');
        }
        if (positieSeparator < 0) {
            throw new WrongVersionNameException("De naam van de map moet gelijk zijn aan de naam van de versie! Huidige naam bevat geen _ tussen applcatie en nummer.");
        }
        return this.versie.substring(0,positieSeparator);
    }
    public String getVersieNummer() throws WrongVersionNameException {
        int positieSeparator = this.versie.indexOf('_');
        if (positieSeparator < 0) {
            positieSeparator = this.versie.indexOf(' ');
        }
        if (positieSeparator < 0) {
            throw new WrongVersionNameException("De naam van de map moet gelijk zijn aan de naam van de versie! Huidige naam bevat geen _ tussen applcatie en nummer.");
        }
        return this.versie.substring(positieSeparator+1);
    }

    public String toString() {
        String returnString = "OralceOplevering: versie:"+this.versie+" folder "+ folder+" ";
        for (OracleObject oo : oracleObjecten) {
            returnString = returnString + " OracleObject " +oo.toString();
        }
        return returnString;
    }
}
