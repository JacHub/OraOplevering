package nl.tkp.opleveringen;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created on 14-09-2016.
 *
 * @author <a href="mailto:huizenga.j@tkppensioen.nl">Jacob Huizenga</a>
 */
public class TestCommand {
    public static void main(String[] args) throws Exception {
//        String cmd = "git log develop.. --name-status --pretty=format: | awk 'NF' | sort -k2,2 -u";
        String cmd = "git log develop.. --name-status --pretty=format: | sort -k2,2 -u";
        Runtime run = Runtime.getRuntime();
        Process pr = run.exec(cmd);
        //pr.waitFor();
        BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String line = "";
        while ((line = buf.readLine()) != null) {
//            System.out.println("REGEL");
            System.out.println(line);
        }
    }
}
