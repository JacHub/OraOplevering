package nl.tkp.opleveringen;

import nl.tkp.opleveringen.threads.SearchLabelsCallThread;

import java.util.Map;

/**
 * Created by eekhout.l on 28-04-2015.
 * class TestSearchLabelsCallThread
 */
public class TestSearchLabelsCallThread {

    public static void main(String[] args) throws InterruptedException {
        SearchLabelsCallThread labelsCallThread = new SearchLabelsCallThread("TEST");
        for (int i=0;i<10;i++) { System.out.println(i); Thread.sleep(300); }
        Map<String,String> stringMap = labelsCallThread.resultaat();
        System.out.println(stringMap);
    }
}
