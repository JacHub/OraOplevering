package nl.tkp.opleveringen.representation.search.labels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by eekhout.l on 03-03-2015.
 * class Field
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Field {
    private String summary;

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Override
    public String toString() {
        return "Field{" +
                "summary='" + summary + '\'' +
                '}';
    }
}
