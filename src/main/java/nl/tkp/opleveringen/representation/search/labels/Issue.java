package nl.tkp.opleveringen.representation.search.labels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by eekhout.l on 03-03-2015.
 * class Issue
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Issue {
    private String id;
    private String key;
    private Field fields;

    public String getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "Issue{" +
                "id='" + id + '\'' +
                ", key='" + key + '\'' +
                ", fields=" + fields +
                '}';
    }

    public Field getFields() {
        return fields;
    }
}