package nl.tkp.opleveringen.clientCalls;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import nl.tkp.opleveringen.representation.search.labels.Issue;
import nl.tkp.opleveringen.representation.search.labels.SearchLabelsResult;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.DatatypeConverter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by eekhout.l on 03-03-2015.
 * class JerseyClientJiraSearchLabelCall
 */

public class JerseyClientJiraSearchLabelsCall {

    private ClientConfig jerseyClientConfig = null;

    protected synchronized ClientConfig getClientConfig() {
        if (jerseyClientConfig == null) {
            jerseyClientConfig = new DefaultClientConfig();
            jerseyClientConfig.getClasses().add(JacksonJsonProvider.class);
        }
        return jerseyClientConfig;
    }

    private SearchLabelsResult jerseyClientCall(String search, SearchLabelsResult vorigeLabelsResult) {
        String qString = "";

        if (vorigeLabelsResult!=null) {
            qString += "&startAt=" + vorigeLabelsResult.getStartAt()+vorigeLabelsResult.getMaxResults();
            qString += "&maxResult=" + vorigeLabelsResult.getMaxResults();
        }
        // password even omzetten via https://www.base64decode.org/
        byte[] pwd = DatatypeConverter.parseBase64Binary("R3JvbmluZ2VuMjAxNQ==");
        HTTPBasicAuthFilter basicAuth = new HTTPBasicAuthFilter("scrumscherm", new String(pwd));
        Client client = Client.create(getClientConfig());
        client.addFilter(basicAuth);
        ClientResponse clientResponse = client
                .resource("http://jira/rest/api/latest/search?jql=labels=" + search + qString)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(ClientResponse.class);

        if (clientResponse.getStatus()==200) {
            return clientResponse.getEntity(SearchLabelsResult.class);
        } else
            throw new RuntimeException(clientResponse.toString());
    }

    public Map<String,String> haalJiraStoriesVanLabels(String label) {
        Map<String,String> stringMap = new HashMap<String,String>();
        SearchLabelsResult searchLabelsResult = null;

        do {
            searchLabelsResult = new JerseyClientJiraSearchLabelsCall().jerseyClientCall(label, searchLabelsResult);
            for (Issue issue : searchLabelsResult.getIssues()) {
                stringMap.put(issue.getKey(), issue.getFields().getSummary());
            }
        } while (searchLabelsResult.getStartAt()+searchLabelsResult.getMaxResults()<=searchLabelsResult.getTotal());

        return stringMap;
    }
}
