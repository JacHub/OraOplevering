package nl.tkp.opleveringen;

import nl.tkp.opleveringen.clientCalls.JerseyClientJiraSearchLabelsCall;
import nl.tkp.opleveringen.representation.search.labels.Issue;
import nl.tkp.opleveringen.representation.search.labels.SearchLabelsResult;

/**
 * Created by eekhout.l on 03-03-2015.
 * class TestJerseyClientJiraSearchLabelsCall
 */

public class TestJerseyClientJiraSearchLabelsCall {

    public static void main(String args[]) {
        try {
            SearchLabelsResult searchLabelsResult = null;
            do {
                searchLabelsResult = new JerseyClientJiraSearchLabelsCall().jerseyClientCall("RGR_3.02.012hf1", searchLabelsResult);
                for (Issue issue : searchLabelsResult.getIssues()) {
                    System.out.println(issue);
                }
            } while (searchLabelsResult.getStartAt()+searchLabelsResult.getMaxResults()<=searchLabelsResult.getTotal());
        } catch (Exception e) {
            System.out.println(e.getClass() + ": " + e.getMessage());
        }
    }
}
