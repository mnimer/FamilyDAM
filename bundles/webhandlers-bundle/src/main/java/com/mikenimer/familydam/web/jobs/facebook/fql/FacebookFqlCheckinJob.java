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

package com.mikenimer.familydam.web.jobs.facebook.fql;

import com.mikenimer.familydam.Constants;
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
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeType;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Query facebook for the checkin data, then load each checkin by ID to get the full data (likes, comments, place, etc.)
 *
 * Sample FQL
 select app_id, author_uid, checkin_id, post_id, message, target_type, coords
 from checkin
 where author_uid = me()
 *
 * Created by mnimer on 2/6/14.
 */
@Component(enabled = true, immediate = true)
@Service(value = JobConsumer.class)
@Property(name = JobConsumer.PROPERTY_TOPICS, value = "familydam/web/facebook/fql/checkin")
public class FacebookFqlCheckinJob extends FacebookFqlJob
{
    public static String TOPIC = "familydam/web/facebook/fql/checkin";
    public static String FACEBOOKFOLDERPATH = "/content/dam/web/facebook/{1}/checkin/{2}";
    public static String FACEBOOKNODEPATH = "/content/dam/web/facebook/{1}/checkin/{2}/{3}";

    private final Logger log = LoggerFactory.getLogger(FacebookFqlCheckinJob.class);
    private Session session;

    @Reference
    protected JobManager jobManager;


    @Reference
    public SlingRepository repository;



    @Activate
    protected void activate(ComponentContext context) throws Exception
    {
        log.debug("Activate FacebookFqlCheckinJob Job");
    }


    @Deactivate
    protected void deactivate(ComponentContext componentContext) throws RepositoryException
    {
        log.debug("Deactivate FacebookFqlCheckinJob Job");
    }


    /**
     * Call facebook with a FQL query to load the data
     * @param job
     * @param facebookData
     * @param username
     * @param nodePath
     * @param nextUrl
     * @return
     * @throws RepositoryException
     * @throws IOException
     * @throws JSONException
     */
    @Override
    protected JobResult queryFacebook(Job job, Node facebookData, String username, String nodePath, String nextUrl) throws RepositoryException, IOException, JSONException
    {
        String accessToken = facebookData.getProperty("accessToken").getString();
        String expiresIn = facebookData.getProperty("expiresIn").getString();
        String signedRequest = facebookData.getProperty("signedRequest").getString();

        if (!facebookData.hasProperty("userID"))
        {
            return JobResult.FAILED;
        }

        String max_timestamp = null;
        try
        {
            max_timestamp = job.getProperty("timestamp").toString();
        }catch(Exception pe ){
            //swallow, we need to find a better way to do a contains.
        }

        String userId = "'" + facebookData.getProperty("userID").getString() + "'"; //with escapes
        String _url = nextUrl;

        if (nextUrl == null)
        {
            String fql = "select app_id, author_uid, checkin_id, post_id, message, target_type, timestamp, coords\n" +
                    " from checkin " +
                    " where author_uid = " + userId +" ";
            if( max_timestamp != null )
            {
                fql += " AND timestamp < '" +max_timestamp +"'";
            }
                fql += " limit 25";

            _url = "https://graph.facebook.com/fql?q=" + URLEncoder.encode(fql, "UTF-8") + "&access_token=" + accessToken;
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
        //System.out.println(jsonStr);

        JSONObject jsonObj = new JSONObject(jsonStr);
        return saveData(job, username, jsonObj, accessToken);
    }


    /**
     * Parse the array of checkins and save each one.
     * @param job
     * @param username
     * @param jsonObject
     * @param accessToken
     * @return
     */
    private JobResult saveData(Job job, String username, JSONObject jsonObject, String accessToken)
    {

        try
        {
            session = repository.loginAdministrative(null);

            // TODO add support for calling the next set in the history, based on created_time

            if (jsonObject.has("data"))
            {
                JSONArray statusList = jsonObject.getJSONArray("data");
                Integer oldest_created_time = null;

                for (int i = 0; i < statusList.length(); i++)
                {
                    JSONObject post = (JSONObject) statusList.get(i);
                    Integer ctDate = post.getInt("timestamp");

                    if( oldest_created_time == null || oldest_created_time > ctDate)
                    {
                        oldest_created_time = ctDate;
                    }

                    // pull out the id then load the larger checkin data packet.
                    JSONObject checkInPost = loadSingleCheckIn(post.getString("checkin_id"), accessToken);

                    if( checkInPost != null )
                    {
                        // then save the data as a single JCR node.
                        persistSingleCheckInNode(username, "checkin", checkInPost);
                    }
                }


                if( oldest_created_time != null )
                {
                    Map props = extractJobProperties(job);
                    props.put("timestamp", oldest_created_time);
                    // Run immediately
                    Job streamJob = jobManager.addJob("familydam/web/facebook/fql/checkin", props);

                }
            }

            return JobResult.OK;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return JobResult.FAILED;
        }
    }


    /**
     * With the checkin_id from the FQL checkin table, we call the graph api to get the full detail for the checkin
     * which includes better place data, likes, and comments.
     * @param id
     * @param accessToken
     * @return
     */
    private JSONObject loadSingleCheckIn(String id, String accessToken)
    {
        try
        {
            String _url = "https://graph.facebook.com/" +id +"?method=GET&format=json&access_token=" +accessToken;

            HttpClient client = new HttpClient();
            GetMethod method = new GetMethod(_url);
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK)
            {
                return null;
            }


            // Read the response body.
            String jsonStr = method.getResponseBodyAsString();
            //System.out.println(jsonStr);

            JSONObject jsonObj = new JSONObject(jsonStr);
            return jsonObj;
        }
        catch( Exception je )
        {
            return null;
        }
    }


    /**
     * Save the facebook data as a JCR node.
     * @param username
     * @param type
     * @param post
     * @throws JSONException
     * @throws ParseException
     * @throws IOException
     * @throws RepositoryException
     */
    private synchronized void persistSingleCheckInNode(String username, String type, JSONObject post) throws JSONException, ParseException, IOException, RepositoryException
    {
        // Not thread safe, so we'll recreate
        SimpleDateFormat facebookDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+SSSS");
        //SimpleDateFormat facebookDateFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+Z");

        // Delete nodes we don't need to store
        cleanPacket(post);

        // pull out keys
        String id = post.getString("id");
        String timestamp = post.getString("created_time");

        Date dateCreated = new Date();
        if (timestamp != null && timestamp.length() > 0)
        {
            try
            {
                dateCreated = facebookDateFormat.parse(timestamp);
            }
            catch (NumberFormatException nfe)
            {
                nfe.getMessage();
            }
        }

        //pull out the year so we can group posts by year
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateCreated);
        String year = new Integer(calendar.get(Calendar.YEAR)).toString();


        // default path
        String folderPath = FACEBOOKFOLDERPATH.replace("{1}", username).replace("{2}", year);
        String nodePath = FACEBOOKNODEPATH.replace("{1}", username).replace("{2}", year).replace("{3}", id);
        if (true)// !session.nodeExists(nodePath) )
        {
            // first check folders to node
            Node folder = JcrUtils.getOrCreateByPath(folderPath, "sling:Folder", session);

            // delete the old node if it exists.
            if( folder.hasNode(id) )
            {
                folder.getNode(id).remove();
                session.save();
            }

            // create a new node
            Node node = JcrUtils.getOrCreateByPath(nodePath, NodeType.NT_UNSTRUCTURED, session);

            // add some FamilyDam specific properties
            node.addMixin(Constants.NODE_CONTENT);
            node.addMixin(Constants.NODE_FACEBOOK);

            // convert the json to a node properties
            // AND load any media binary data
            new JsonToNode().convert(node, post, type);

            node.setProperty("type", "checkin");
            try
            {
                // override the unix timestamp string
                node.setProperty(Constants.DATETIME, calendar);
            }
            catch (Exception e)
            {
                e.getMessage();
            }

            Map location = checkForLocation(post);
            if (location != null)
            {
                node.addMixin(Constants.NODE_GEOSTAMP);
                node.setProperty("latitude", (Double) location.get("latitude"));
                node.setProperty("longitude", (Double) location.get("longitude"));
            }



            // Save the new session, before we check for the photo
            try
            {
                session.save();
            }
            catch (ConstraintViolationException cve)
            {
                cve.getMessage();
                try
                { // hack to avoid this error: mandatory property {http://www.jcp.org/jcr/1.0}data does not exist
                    Thread.sleep(1000);
                    session.save();
                }catch (ConstraintViolationException cve2){
                    // still can't save it
                    cve2.printStackTrace();
                }catch (Exception ex){
                    // do nothing
                    ex.printStackTrace();
                }
            }

        }
    }



    /**
     * Look for a nested "Place" node with a location
     */
    private Map checkForLocation(JSONObject post) throws JSONException
    {
        Map locationMap = null;

        if (post.has("place"))
        {
            JSONObject place = post.getJSONObject("place");
            if (place.has("location"))
            {
                JSONObject location = place.getJSONObject("location");
                locationMap = new HashMap();
                locationMap.put("latitude", location.getDouble("latitude"));
                locationMap.put("longitude", location.getDouble("longitude"));
            }
        }

        return locationMap;
    }
}

