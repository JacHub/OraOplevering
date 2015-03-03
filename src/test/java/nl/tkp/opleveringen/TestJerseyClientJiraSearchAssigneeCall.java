package nl.tkp.opleveringen;

import nl.tkp.opleveringen.clientCalls.JerseyClientJiraSearchAssigneeCall;
import nl.tkp.opleveringen.representation.search.assignee.Issue;
import nl.tkp.opleveringen.representation.search.assignee.SearchAssigneeResult;

/**
 * Created by eekhout.l on 03-03-2015.
 * class TestJerseyClientJiraSearchAssigneeCall
 */

public class TestJerseyClientJiraSearchAssigneeCall {

    public static void main(String args[]) {
        try {
            SearchAssigneeResult searchAssigneeResult = null;
            do {
                searchAssigneeResult = new JerseyClientJiraSearchAssigneeCall().jerseyClientCall("huizenga.j", searchAssigneeResult);
                for (Issue issue : searchAssigneeResult.getIssues()) {
                    System.out.println(issue);
                }
            } while (searchAssigneeResult.getStartAt()+searchAssigneeResult.getMaxResults()<=searchAssigneeResult.getTotal());
        } catch (Exception e) {
            System.out.println(e.getClass() + ": " + e.getMessage());
        }
    }
}
