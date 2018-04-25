package nl.tkp.opleveringen;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRegex {
    public static void main(String[] args) {
        System.out.println("PAS_DMN_1.00.001".matches("^.+_\\d\\.\\d{2}\\.\\d{3}.*$"));
        Pattern p = Pattern.compile("(^.+)_(\\d\\.\\d{2}\\.\\d{3}.*)$");
        Matcher m = p.matcher("PAS_DMN_1.00.001");
        if (m.matches()) {
            System.out.println(m.groupCount());
            System.out.println(m.group(2));
        } else throw new RuntimeException("Fout bij bepalen deelapplicatie. Geen match op pattern: ");
    }
}
