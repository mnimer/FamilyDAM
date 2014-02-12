/*
 * This file is part of FamilyDAM Project.
 *
 *     The FamilyDAM Project is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     The FamilyDAM Project is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with the FamilyDAM Project.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mikenimer.familydam.web.jobs;

import com.mikenimer.familydam.importers.NestedContentImporter;
import com.mikenimer.familydam.mappers.JsonToNode;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.jcr.contentloader.ContentImporter;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mnimer on 2/6/14.
 */
@Component(enabled = true, immediate = true)
@Service(value = JobConsumer.class)
@Property(name = JobConsumer.PROPERTY_TOPICS, value = "familydam/web/facebook/statuses")
public class FacebookStatusJob implements JobConsumer
{
    public static String TOPIC = "familydam/web/facebook/statuses";
    public static String FACEBOOKPATH = "/content/dam/web/facebook/{1}/statuses/{2}/{3}";
    public static String FACEBOOKPATHByUser = "/content/dam/web/facebook/{1}/statuses/{2}/{3}";

    private final Logger log = LoggerFactory.getLogger(FacebookStatusJob.class);

    private Session session;
    private ContentImporter contentImporter;
    private final SimpleDateFormat jsonDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");



    @Reference
    private JobManager jobManager;

    @Reference
    private SlingRepository repository;


    @Activate
    protected void activate(ComponentContext context) throws Exception
    {
        log.debug("Activate ID3 Job");

        this.contentImporter = new NestedContentImporter();
    }


    @Deactivate
    protected void deactivate(ComponentContext componentContext) throws RepositoryException
    {
        log.debug("Deactivate ID3 Job");
    }


    @Override
    public JobResult process(Job job)
    {
        String nodePath = (String) job.getProperty("nodePath");
        String url = (String) job.getProperty("url");
        String username = (String) job.getProperty("username");
        return process(username, nodePath, url);
    }


    public JobResult process(String username, String path, String url)
    {
        try
        {
            session = repository.loginAdministrative(null);
            Node node = session.getNode(path);

            Node facebookData = node.getNode("web/facebook");
            if (facebookData != null)
            {
                return pullFacebookData(facebookData, username, path, url);
            }
            return JobResult.OK;
        } catch (Exception re)
        {
            return JobResult.CANCEL;
        }
    }


    private JobResult pullFacebookData(Node facebookData, String username, String nodePath, String nextUrl) throws RepositoryException, IOException, JSONException
    {
        String accessToken = facebookData.getProperty("accessToken").getString();
        String expiresIn = facebookData.getProperty("expiresIn").getString();
        String signedRequest = facebookData.getProperty("signedRequest").getString();

        String userId = "me";
        if (facebookData.hasProperty("userId"))
        {
            userId = facebookData.getProperty("userId").getString();
        }

        String _url = nextUrl;
        if (nextUrl == null)
        {
            _url = "https://graph.facebook.com/" + userId + "/statuses?limit=5&access_token=" + accessToken;
        }
        URL url = new URL(_url);

        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(_url);
        int statusCode = client.executeMethod(method);

        if (statusCode != HttpStatus.SC_OK)
        {
            return JobResult.FAILED;
        }

        // Read the response body.
        String jsonStr = method.getResponseBodyAsString();
        try
        {
            JSONObject jsonObj = new JSONObject(jsonStr);
            JSONArray statusList = jsonObj.getJSONArray("data");
            nextUrl = jsonObj.getJSONObject("paging").getString("next");

            for (int i = 0; i < statusList.length(); i++)
            {
                JSONObject post = (JSONObject) statusList.get(i);

                // pull out keys
                String id = post.getString("id");
                String updated_time = post.getString("updated_time");
                //todo: parse updated_time and pull out the year
                String year = "2014";

                // add some FamilyDam specific properties
                post.put("type", "status");

                /***
                if( contentImporter != null )
                {
                    Node node = JcrUtils.getOrCreateByPath(FACEBOOKPATH.replace("{1}", username).replace("{2}", year).replace("{3}", id), NodeType.NT_UNSTRUCTURED, session);

                    InputStream jsonStream = new ByteArrayInputStream(post.toString().getBytes());
                    ImportOptions options = new ImportOptions(){

                        @Override
                        public boolean isCheckin() {
                            return false;
                        }

                        @Override
                        public boolean isAutoCheckout() {
                            return true;
                        }

                        @Override
                        public boolean isIgnoredImportProvider(String extension) {
                            return false;
                        }

                        @Override
                        public boolean isOverwrite() {
                            return false;
                        }

                        @Override
                        public boolean isPropertyOverwrite() {
                            return false;
                        }
                    };


                    contentImporter.importContent(node, ".json", jsonStream, options, null);
                }
                //ContentImporter importer = new DefaultContentImporter();
                ***/


                /**
                String statusUrl = "http://localhost:8888" +FACEBOOKPATH.replace("{1}", username).replace("{2}", year).replace("{3}", id);

                Credentials credentials = new UsernamePasswordCredentials("admin", "admin");
                HttpClient statusClient = new HttpClient();
                statusClient.getState().setCredentials(AuthScope.ANY, credentials);

                PostMethod statusMethod = new PostMethod(statusUrl);
                statusMethod.addParameter(":operation", "import");
                statusMethod.addParameter(":contentType", "json");
                statusMethod.addParameter(":content", post.toString());
                statusMethod.addParameter(":checkin", "true");
                statusMethod.addParameter(":replace", "true");
                statusMethod.addParameter(":replaceProperties", "true");
                int saveNodeStatusCode = client.executeMethod(statusMethod);

                if( saveNodeStatusCode != 200 )
                {
                    log.warn("Unable to save node:" +jsonStr);
                }
                 **/
                //Node node = getFBPostNode(username, id, year);
                Node node = JcrUtils.getOrCreateByPath(FACEBOOKPATH.replace("{1}", username).replace("{2}", year).replace("{3}", id), NodeType.NT_UNSTRUCTURED, session);
                node = new JsonToNode().convert(node, post);
                session.save();

            }


            // Follow the NEXT link with another job
            Map props = new HashMap();
            props.put("nodePath", nodePath);
            props.put("url", nextUrl);
            //Job metadataJob = jobManager.addJob(TOPIC, props);


            return JobResult.OK;
        } catch (JSONException je)
        {
            je.printStackTrace();
            return JobResult.FAILED;
        } catch (Exception ex)
        {
            ex.printStackTrace();
            return JobResult.FAILED;
        }
    }


    /**
     * Create the path and return the new node
     * @param username
     * @param id
     * @param year
     * @return
     * @throws RepositoryException
     */
    private Node getFBPostNode(String username, String id, String year) throws RepositoryException
    {
        String fbPath = FACEBOOKPATH.replace("{}", username);
        Node node = session.getNode(fbPath);
        if( !node.hasNode("status") )
        {
            node = node.addNode("status");
        }else{
            node = node.getNode("status");
        }


        if( !node.hasNode(year) )
        {
            node = node.addNode(year);
        }else{
            node = node.getNode(year);
        }

        if( !node.hasNode(id) )
        {
            node = node.addNode(id);
        }else{
            node = node.getNode(id);
        }
        return node;
    }
}
