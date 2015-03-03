package nl.tkp.opleveringen.representation.search.assignee;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by eekhout.l on 03-03-2015.
 * class Result
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchAssigneeResult {
    private int startAt;
    private int maxResults;
    private int total;
    private List<Issue> issues;

    public int getStartAt() {
        return startAt;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public int getTotal() {
        return total;
    }

    public List<Issue> getIssues() {
        return issues;
    }

    @Override
    public String toString() {
        return "SearchAssigneeResult{" +
                "startAt=" + startAt +
                ", maxResults=" + maxResults +
                ", total=" + total +
                ", issues=" + issues +
                '}';
    }
}
