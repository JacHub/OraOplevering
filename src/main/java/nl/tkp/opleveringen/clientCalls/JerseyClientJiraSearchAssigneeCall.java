package nl.tkp.opleveringen.clientCalls;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import nl.tkp.opleveringen.representation.search.assignee.SearchAssigneeResult;

import javax.ws.rs.core.MediaType;

/**
 * Created by eekhout.l on 03-03-2015.
 * class JerseyClientJiraSearchLabelCall
 */

public class JerseyClientJiraSearchAssigneeCall {

    private ClientConfig jerseyClientConfig = null;

    protected synchronized ClientConfig getClientConfig() {
        if (jerseyClientConfig == null) {
            jerseyClientConfig = new DefaultClientConfig();
            jerseyClientConfig.getClasses().add(JacksonJsonProvider.class);
        }
        return jerseyClientConfig;
    }

    public SearchAssigneeResult jerseyClientCall(String search, SearchAssigneeResult vorigeAssigneeResult) {
        String qString = "";

        if (vorigeAssigneeResult!=null) {
            qString += "&startAt=" + vorigeAssigneeResult.getStartAt()+vorigeAssigneeResult.getMaxResults();
            qString += "&maxResult=" + vorigeAssigneeResult.getMaxResults();
        }

        ClientResponse clientResponse = Client.create(getClientConfig())
                .resource("https://jira.intra.tkppensioen.nl/rest/api/latest/search?jql=assignee=" + search + qString)
                .header("Authorization", "Basic c2NydW1zY2hlcm06V2ludGVyMjAxNWE=")
                .header("Connection", "Close")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(ClientResponse.class);

        if (clientResponse.getStatus()==200) {
            return clientResponse.getEntity(SearchAssigneeResult.class);
        } else
            throw new RuntimeException(clientResponse.toString());
    }
}
