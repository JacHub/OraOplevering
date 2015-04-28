package nl.tkp.opleveringen;

import nl.tkp.opleveringen.clientCalls.JerseyClientJiraSearchLabelsCall;

import java.util.Map;

/**
 * Created by eekhout.l on 03-03-2015.
 * class TestFileTypes
 */

public class TestFileTypes {

    public static void main(String args[]) {
        FileTypes types = new FileTypes("X:\\ICT\\03 ICT Ontwikkeling\\05 ICT Algemeen\\Uitleveren\\OpleverJar");
        System.out.println(types);
    }
}
