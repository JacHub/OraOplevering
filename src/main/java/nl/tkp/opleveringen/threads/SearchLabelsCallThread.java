package nl.tkp.opleveringen.threads;

import nl.tkp.opleveringen.clientCalls.JerseyClientJiraSearchLabelsCall;

import java.util.Date;
import java.util.Map;

/**
 * Created by eekhout.l on 28-04-2015.
 * class SearchLabelsCallThread
 */
public class SearchLabelsCallThread extends Thread {

    private String searchLabel;
    private Map<String,String> stringMap;

    public SearchLabelsCallThread(String searchLabel) {
        super();
        this.searchLabel = searchLabel;
        start();
    }

    @Override
    public void run() {
        System.out.println("SearchLabelsCallThread.start()");
        stringMap = new JerseyClientJiraSearchLabelsCall().haalJiraStoriesVanLabels(searchLabel);
    }

    public Map<String,String> resultaat() {
        try {
            join();
            return stringMap;
        } catch (InterruptedException e) {
            return null;
        }
    }
}
