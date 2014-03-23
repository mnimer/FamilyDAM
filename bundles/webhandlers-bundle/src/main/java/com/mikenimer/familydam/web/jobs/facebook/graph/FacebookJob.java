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

package com.mikenimer.familydam.web.jobs.facebook.graph;

import com.mikenimer.familydam.Constants;
import com.mikenimer.familydam.mappers.JsonToNode;
import org.apache.commons.lang.NotImplementedException;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.value.BinaryImpl;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.apache.sling.jcr.api.SlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeType;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by mnimer on 2/12/14.
 */
@Component(enabled = true, immediate = true)
public class FacebookJob implements JobConsumer
{
    private Session session;
    private final Logger log = LoggerFactory.getLogger(FacebookJob.class);



    @Reference
    public SlingRepository repo;


    @Override
    public JobResult process(Job job)
    {
        String userNode = (String) job.getProperty("userNode");
        String url = (String) job.getProperty("url");
        String username = (String) job.getProperty("username");


        return process(job, username, userNode, url);
    }


    public JobResult process(Job job, String username, String path, String url)
    {
        try
        {
            session = repo.loginAdministrative(null);
            Node node = session.getNode(path);

            Node facebookData = node.getNode("web/facebook");
            if (facebookData != null)
            {
                return queryFacebook(job, facebookData, username, path, url);
            }
            return JobResult.OK;
        }
        catch (Exception re)
        {
            return JobResult.CANCEL;
        }
    }


    public Map<String, Object> extractJobProperties(Job job)
    {
        Map<String, Object> jobProperties = new HashMap<String, Object>();
        Set<String> names = job.getPropertyNames();
        for (String name : names)
        {
            if (name.indexOf('.') == -1 && name.indexOf(':') == -1) // ignore system names in props
            {
                jobProperties.put(name, job.getProperty(name));
            }
        }
        return jobProperties;
    }


    protected JobResult queryFacebook(Job job, Node facebookData, String username, String userNode, String nextUrl) throws RepositoryException, IOException, JSONException
    {
        return null;
    }


    /**
     * Take the JSON results from facebook and save them into the content/dam tree
     *
     * @param job
     * @param username
     * @param jsonStr
     * @param facebookPath
     * @param type
     * @return
     */
    protected JobResult saveData(Job job, String username, String jsonStr, String facebookPath, String type)
    {
        try
        {
            JSONObject jsonObj = new JSONObject(jsonStr);
            return saveData(job, username, jsonObj, facebookPath, type);
        }
        catch (JSONException ex)
        {
            ex.printStackTrace();
            return JobResult.FAILED;
        }
    }


    protected JobResult saveData(Job job, String username, JSONObject jsonObj, String facebookPath, String type)
    {
        String nextUrl = null;
        try
        {

            // pull out the next url for paging.
            if (jsonObj.has("paging") && jsonObj.getJSONObject("paging").has("next"))
            {
                nextUrl = jsonObj.getJSONObject("paging").getString("next");
            }

            if (jsonObj.has("data"))
            {
                JSONArray statusList = jsonObj.getJSONArray("data");

                for (int i = 0; i < statusList.length(); i++)
                {
                    JSONObject post = (JSONObject) statusList.get(i);
                    persistSingleNode(username, facebookPath, type, post);

                }
            }
            else
            {
                persistSingleNode(username, facebookPath, type, jsonObj);
            }


            // Follow the NEXT link with another job
            if (nextUrl != null)
            {
                invokeNextJob(job, username, facebookPath, nextUrl);
            }


            return JobResult.OK;
        }
        catch (JSONException je)
        {
            je.printStackTrace();
            return JobResult.FAILED;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return JobResult.FAILED;
        }
    }


    public synchronized void persistSingleNode(String username, String facebookPath, String type, JSONObject post) throws JSONException, ParseException, IOException, RepositoryException
    {
        // Not thread safe, so we'll recreate
        SimpleDateFormat facebookDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+SSSS");
        SimpleDateFormat facebookDateFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+Z");


        // Delete nodes we don't need to store
        cleanPacket(post);

        // pull out keys
        String id = post.getString("id");
        String timestamp = null;
        if (post.has("created_time"))
        {
            timestamp = post.getString("created_time");
        }
        else if (post.has("updated_time"))
        {
            timestamp = post.getString("updated_time");
        }

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
        String path = facebookPath.replace("{1}", username).replace("{2}", year).replace("{3}", id);
        if (true)// !session.nodeExists(path) )
        {
            Node node = JcrUtils.getOrCreateByPath(path, NodeType.NT_UNSTRUCTURED, session);

            // add some FamilyDam specific properties
            node.addMixin(Constants.NODE_CONTENT);
            node.addMixin(Constants.NODE_FACEBOOK);

            node.setProperty("type", type);
            try
            {
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


            new JsonToNode().convert(node, post, type);

            // Save the new session, before we check for the photo
            try
            {
                session.save();
                //versionManager.checkin(node.getPath());
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

            checkForPhoto(node, post);
            //versionManager.checkin(node.getPath());


        }
    }


    protected void invokeNextJob(Job job, String username, String nodePath, String nextUrl)
    {
        throw new NotImplementedException();
    }


    /**
     * Look for a nest "Place" node with a location
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


    /**
     * Look for a nest "Place" node with a location
     */
    private Map checkForPhoto(Node node, JSONObject post) throws JSONException, IOException, RepositoryException
    {
        Map photo = null;
        // Not thread safe so we need to create new instances
        SimpleDateFormat facebookDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+SSSS");
        SimpleDateFormat facebookDateFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+Z");


        if (post.has("source") )
        {
            //versionManager.checkout(node.getPath());
            String source = post.getString("source");
            String created_time = post.getString("created_time");

            // pull URL and save the source image and an embedded URL
            BufferedImage bufferedImage = null;
            Binary imageBinary = null;

            try
            {
                String _originalSource = source.replace("_n.", "_o.");

                URL url = new URL(_originalSource);
                bufferedImage = ImageIO.read(url);

                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "jpg", os);
                InputStream is = new ByteArrayInputStream(os.toByteArray());
                imageBinary = new BinaryImpl(os.toByteArray());
                //ValueFactory valueFactory = session.getValueFactory();
                //Binary contentValue = valueFactory.createBinary(is);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

            if (imageBinary != null )
            {
                // create file node
                Node sourceNode = JcrUtils.getOrAddNode(node, "file", NodeType.NT_FILE);
                sourceNode.addMixin("mix:referenceable");
                sourceNode.addMixin(Constants.NODE_CONTENT);
                sourceNode.addMixin(Constants.NODE_IMAGE);

                Node contentNode = JcrUtils.getOrAddNode(sourceNode, "jcr:content", NodeType.NT_RESOURCE);
                contentNode.setProperty("jcr:mimeType", "image/jpeg");
                contentNode.setProperty("jcr:data", imageBinary);
                Calendar lastModified = Calendar.getInstance();
                lastModified.setTimeInMillis(lastModified.getTimeInMillis());
                contentNode.setProperty("jcr:lastModified", lastModified);
                //contentNode.setProperty("jcr:lastModified", Calendar.getInstance());
                //contentNode.setProperty("jcr:uuid", UUID.randomUUID().toString() );


                Calendar cal = Calendar.getInstance();
                try
                {
                    if (created_time.length() > 9)
                    {
                        Date created = facebookDateFormat.parse(created_time);
                        cal.setTime(created);
                    }
                }
                catch (ParseException ex)
                {
                    // try alterative format before giving up
                    try
                    {
                        Date created = facebookDateFormat2.parse(created_time);
                        cal.setTime(created);
                    }
                    catch (ParseException pe)
                    {
                    }
                }
                if (sourceNode.isNodeType(Constants.NODE_CONTENT))
                {
                    sourceNode.setProperty("fd:date", cal);
                }


                try
                {
                    session.save();
                    //versionManager.checkin(node.getPath());
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
            else
            {
                log.warn("unable to load: " + source);
            }

            //session.save();

        }
        else
        {
            log.trace("Image file node already exists: {}", post.toString());
        }

        return photo;
    }


    /**
     * Delete nodes we don't need to store
     *
     * @param post
     * @throws org.apache.sling.commons.json.JSONException
     */
    private void cleanPacket(JSONObject post) throws JSONException
    {

        if (post.has("likes"))
        {
            JSONObject o = post.getJSONObject("likes");
            if (o.has("paging"))
            {
                o.remove("paging");
            }
        }

        if (post.has("comments"))
        {
            JSONObject o = post.getJSONObject("comments");
            if (o.has("paging"))
            {
                o.remove("paging");
            }
        }

        if (post.has("tags"))
        {
            JSONObject o = post.getJSONObject("tags");
            if (o.has("paging"))
            {
                o.remove("paging");
            }
        }

        // when we pull back facebook photos, they return an array of resized images (thumbnails, etc.)
        // that we don't need. So we'll strip these out too.
        if (post.has("images"))
        {
            post.remove("images");
        }

    }
}
