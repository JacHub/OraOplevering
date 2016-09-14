package nl.tkp.opleveringen;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created on 14-09-2016.
 *
 * @author <a href="mailto:huizenga.j@tkppensioen.nl">Jacob Huizenga</a>
 */
public class TestCommand2 {
    public static void main(String[] args) throws Exception {

        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c", "cd \"C:\\users\" && dir");
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while (true) {
            line = r.readLine();
            if (line == null) { break; }
            System.out.println(line);
        }        }

}
