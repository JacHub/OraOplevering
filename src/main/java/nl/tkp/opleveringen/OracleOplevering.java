package nl.tkp.opleveringen;

import nl.tkp.opleveringen.threads.SearchLabelsCallThread;
import oracle.jdbc.OracleTypes;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

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
    private Connection connection = null;

    OracleOplevering(List<File> fl, String foldername, String configFolderName) throws ConfigFileNotExistsException, ConfigFileNotValidException {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection("jdbc:oracle:thin:@pps-oradbs01.dev.tkp:1521:ppsdev", "alg", "alg");
            System.out.println("[INFO] Oracle connectie met ALG@PPSDEV!");
        } catch (ClassNotFoundException e) {
            System.out.println("[WARNING] Geen Oracle JDBC driver gevonden: [" + e.getMessage() + "]");
        } catch (SQLException se) {
            System.out.println("[WARNING] Kan geen Oracle connection maken: [" + se.getMessage() + "]");
        }

        this.versie = getVersionName(fl);
        jiraCall = new SearchLabelsCallThread(this.versie); // 28-04-2015 Lveekhout: zo snel al het kan jira REST call afvuren.
        this.folder = foldername;
        FileTypes fts = new FileTypes(configFolderName);
//        FileTypes fts = new FileTypes();
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
            oo.setDbEquivalent(this.fileTypes.get(i).dbEquivalent);
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
//        regels.add(("WHENEVER SQLERROR EXIT SQL.SQLCODE"));
        regels.add(("WHENEVER SQLERROR EXIT FAILURE"));
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
        Set<String> uniekObjects = new HashSet<>();
        for (OracleObject oracleObject : this.oracleObjecten) {
            if (oracleObject.getDbEquivalent()!=null&&oracleObject.getDbEquivalent().length()>0) {
                regels.add("declare l_ok varchar2(10);err EXCEPTION;PRAGMA EXCEPTION_INIT( err, -20000 ); begin execute immediate 'select gen_check_pck.gen_f_controleer_object_versie(''"
                        + getApplicatieId() + "'', ''"
                        + getVersieNummer() + "'', ''"
                        + oracleObject.getObjectName(this.versie).toUpperCase() + "'', ''"
                        + oracleObject.getDbEquivalent() + "'')" +
                        " from dual' into l_ok; exception when err then raise; when others then null; end;");
                regels.add("/");
            }

            if (connection!=null) {
                String upperObjectName = oracleObject.getObjectName(this.versie).toUpperCase();
                if (!uniekObjects.contains(upperObjectName)) {
                    uniekObjects.add(upperObjectName);
                    String upperFileType = oracleObject.getFileType().toUpperCase();
                    if (oracleObject.getDbEquivalent()!=null&&oracleObject.getDbEquivalent().length()>0) {
                        haalDependencies(regels, upperObjectName, oracleObject.getDbEquivalent());
                    }
                }
            }
        }
        FileHelper.generateFile(this.folder + "\\ddl\\" + this.versie + "_ins_versie_pre.sql", regels);
    }

    private void haalDependencies(List<String> regels, String upperObjectName, String oracleType) {
            System.out.println("[INFO] Ophalen dependencies database object: [" + upperObjectName + "][" + oracleType + "]");
            try (CallableStatement stmt = connection.prepareCall("{? = call chk_vsn_pck.dependencies_unknown_owner(?,?)}")) {
                stmt.registerOutParameter(1, OracleTypes.CURSOR);
                stmt.setString(2, upperObjectName);
                stmt.setString(3, oracleType);
                stmt.execute();

                List<String> wholes = new ArrayList<>();
                try (ResultSet resultSet = (ResultSet) stmt.getObject(1)) {
                    while (resultSet.next()) {
                        wholes.add(resultSet.getString("DEPENDENCY"));
                    }
                } catch (SQLException se) {
                    System.out.printf("[ERROR] %s\r\n", se.getMessage());
                }

                if (wholes.size()>0) {
                    regels.add(String.format("prompt Controleren dependencies van %s %s", oracleType, upperObjectName));
                    regels.add("declare");
                    regels.add("   procedure gen_p_controleer_object_versie(p_owner in varchar2,p_name in varchar2,p_type in varchar2,p_versie in varchar2 default null) is");
                    regels.add("   l_aantal   number;");
                    regels.add("   l_deelapplicatie   varchar2(32767);");
                    regels.add("   l_versie           varchar2(32767);");
                    regels.add("      function getContainer(p_regel in varchar2, p_versie out varchar2) return varchar2 is");
                    regels.add("      l_index            integer := 1;");
                    regels.add("      l_karakter         pls_integer;");
                    regels.add("         function is_number(p_char in pls_integer) return boolean is");
                    regels.add("         begin");
                    regels.add("            return p_char >= 48 and p_char <= 57;");
                    regels.add("         end;");
                    regels.add("      begin");
                    regels.add("         l_karakter := ascii(substr(p_regel, l_index, 1));");
                    regels.add("         while not is_number(l_karakter) and l_index <= length(p_regel) loop");
                    regels.add("            l_index := l_index + 1;");
                    regels.add("            l_karakter := ascii(substr(p_regel, l_index, 1));");
                    regels.add("         end loop;");
                    regels.add("         if l_index <= length(p_regel) then");
                    regels.add("            p_versie := substr(p_regel, l_index);");
                    regels.add("            return upper(substr(p_regel, 1, l_index-2));");
                    regels.add("         else");
                    regels.add("            p_versie := p_regel;");
                    regels.add("            return '';");
                    regels.add("         end if;");
                    regels.add("      end;");
                    regels.add("      function getVersieVanObject(p_object in varchar2, p_type in varchar2, p_versie out varchar2) return varchar2 is");
                    regels.add("      l_regel   varchar2 (32767);");
                    regels.add("      begin");
                    regels.add("         if p_type in ( 'PACKAGE', 'PACKAGE BODY' ) then");
                    regels.add("            execute immediate 'select ' || p_object || '.revision from dual' into l_regel;");
                    regels.add("            return getContainer(l_regel, p_versie);");
                    regels.add("         elsif p_type in ( 'TABLE', 'VIEW' ) then");
                    regels.add("            for regel in ( select * from all_tab_comments where table_name = p_object )");
                    regels.add("            loop");
                    regels.add("               return getContainer(regel.comments, p_versie);");
                    regels.add("            end loop;");
                    regels.add("         end if;");
                    regels.add("         return null;");
                    regels.add("      end;");
                    regels.add("   begin");
                    regels.add("      select count(*) into l_aantal");
                    regels.add("      from all_objects");
                    regels.add("      where owner=p_owner");
                    regels.add("      and   object_name = p_name");
                    regels.add("      and   object_type=p_type;");
                    regels.add("      if l_aantal = 0 then");
                    regels.add("         raise_application_error (-20000, 'Dependency niet bekend: [' || p_owner || '][' || p_name || '][' || p_type || ']. **** De oplevering wordt afgebroken! ****');");
                    regels.add("      end if;");
                    regels.add("      if p_versie is not null then");
                    regels.add("         l_deelapplicatie := getContainer(p_versie, l_versie);");
                    regels.add("         declare");
                    regels.add("         l_current_deelapplicatie   varchar2(32767);");
                    regels.add("         l_current_versie           varchar2(32767);");
                    regels.add("         begin");
                    regels.add("            l_current_deelapplicatie := getVersieVanObject(p_name, p_type, l_current_versie);");
                    regels.add("            if l_current_deelapplicatie != l_deelapplicatie then");
                    regels.add("               raise_application_error (-20001, 'Deelapplicaties verschillend: [' || p_owner || '][' || p_name || '][' || p_type || ']. Gevonden deelapplicatie: [' || l_current_deelapplicatie || '], verwacht: [' || l_deelapplicatie || ']. **** De oplevering wordt afgebroken! ****');");
                    regels.add("            end if;");
                    regels.add("            if l_current_versie < l_versie then");
                    regels.add("               raise_application_error (-20002, 'Lager versienummer gevonden bij dependency: [' || p_owner || '][' || p_name || '][' || p_type || ']. Gevonden versie: [' || l_current_versie || '], verwacht minimaal: [' || l_versie || ']. **** De oplevering wordt afgebroken! ****');");
                    regels.add("            end if;");
                    regels.add("         end;");
                    regels.add("      end if;");
                    regels.add("   end;");
                    regels.add("begin");

                    for (String whole : wholes) {
                        if (whole!=null) {
                            String[] parts = whole.split("\t");
                            if (parts.length==4) {
                                regels.add(String.format("   gen_p_controleer_object_versie('%s', '%s', '%s', '%s');", parts[0], parts[1], parts[2], parts[3]));
                            } else if (parts.length==3) {
                                regels.add(String.format("   gen_p_controleer_object_versie('%s', '%s', '%s');", parts[0], parts[1], parts[2]));
                            } else {
                                regels.add(String.format("-- [ERROR] %s: [%s]", "parts.lengte ongelijk aan 3 of 4", parts.length));
                            }
                        } else {
                            regels.add(String.format("-- [INFO] %-48s", whole));
                        }
                    }

                    regels.add("end;");
                    regels.add("/");
                }
            } catch (SQLException e) {
                System.out.println("Error bij uitvoeren query: [" + e.getMessage() + "]");
                e.printStackTrace();
                return;
            }
    }
    public void createVersiePost() throws WrongVersionNameException {
        /**
         * Aanmaken insertscript voor het versienummer
         */
        ArrayList<String> regels = new ArrayList<String>();
        Set<String> uniekObjects = new HashSet<>();
        regels.add("insert into gen_versies( deelapplicatie, versienummer) values ('" + getApplicatieId() + "','" + getVersieNummer() + "');");
        regels.add("");
//        for (OracleObject oracleObject : this.oracleObjecten) {
//            if (oracleObject.getDbEquivalent()!=null&&oracleObject.getDbEquivalent().length()>0) {
//                String upperObjectName = oracleObject.getObjectName(this.versie).toUpperCase();
//                if (!uniekObjects.contains(upperObjectName)) {
//                    uniekObjects.add(upperObjectName);
//                    regels.add("begin execute immediate 'insert into gen_object_versies (deelapplicatie, versienummer, objectnaam, objecttype, aanmaakdatum) values (''"
//                            + getApplicatieId() + "'', ''"
//                            + getVersieNummer() + "'', ''"
//                            + oracleObject.getObjectName(this.versie).toUpperCase() + "'', ''"
//                            + oracleObject.getDbEquivalent() + "'', "
//                            + "sysdate" + ")'; exception when others then null; end;");
//                    regels.add("/");
//                }
//            }
//        }
//        regels.add("");
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
