package nl.tkp.opleveringen;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created on 14-09-2016.
 *
 * @author <a href="mailto:huizenga.j@tkppensioen.nl">Jacob Huizenga</a>
 */
public class TestCommand3 {
    public static void main(String[] args) throws Exception {
        String folderGitProject = "D:\\dev\\oracletest";
        String Versie = "CAE_1.01.111";
        String folderOplevering = "d:\\temp\\"+Versie;

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

            for (String filenaam : gewijzigdeFiles) {
                System.out.println("file: "+filenaam);
                System.out.println("copy file van "+folderGitProject + "\\" + filenaam+" naar "+ folderOplevering +"\\"+filenaam);
                FileHelper.copyFile(folderGitProject + "\\" + filenaam, folderOplevering +"\\"+filenaam);

            }

            if (gewijzigdeFiles.size() > 0 && !FileHelper.folderExists(folderOplevering)) {
                FileHelper.createFolder(folderOplevering);
                System.out.println("map aangemaakt");
            }

        } catch (Exception e1) {
            e1.printStackTrace();
        }
        System.out.println("Finished");
    }
}