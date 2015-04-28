package nl.tkp.opleveringen;

import nl.tkp.opleveringen.clientCalls.JerseyClientJiraSearchLabelsCall;
import java.util.Map;

/**
 * Created by eekhout.l on 03-03-2015.
 * class TestJerseyClientJiraSearchLabelsCall
 */

public class TestJerseyClientJiraSearchLabelsCall {

    public static void main(String args[]) {
        Map<String,String> stringMap = new JerseyClientJiraSearchLabelsCall().haalJiraStoriesVanLabels("TEST_1.23.456");

        for (Map.Entry<String,String> entry : stringMap.entrySet()) {
            System.out.println("[" + entry.getKey() + "] " + entry.getValue());
        }
    }
}
