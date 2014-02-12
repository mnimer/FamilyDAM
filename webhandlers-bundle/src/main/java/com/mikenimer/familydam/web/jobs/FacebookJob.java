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

import com.mikenimer.familydam.mappers.JsonToNode;
import org.apache.commons.lang.NotImplementedException;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.apache.sling.jcr.api.SlingRepository;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mnimer on 2/12/14.
 */
@Component(enabled = true, immediate = true)
public class FacebookJob implements JobConsumer
{
    private Session session;
    private final SimpleDateFormat facebookDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+SSSS");


    @Reference
    protected JobManager jobManager;

    @Reference
    protected SlingRepository repository;


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
                return queryFacebook(facebookData, username, path, url);
            }
            return JobResult.OK;
        } catch (Exception re)
        {
            return JobResult.CANCEL;
        }
    }


    protected JobResult queryFacebook(Node facebookData, String username, String nodePath, String nextUrl) throws RepositoryException, IOException, JSONException
    {
        return null;
    }


    /**
     * Take the JSON results from facebook and save them into the content/dam tree
     * @param username
     * @param nodePath
     * @param jsonStr
     * @return
     */
    protected JobResult saveData(String username, String nodePath, String jsonStr, String type)
    {
        String nextUrl = null;
        try
        {
            JSONObject jsonObj = new JSONObject(jsonStr);
            JSONArray statusList = jsonObj.getJSONArray("data");

            // pull out the next url for paging.
            if( jsonObj.has("paging") && jsonObj.getJSONObject("paging").has("next") )
            {
                nextUrl = jsonObj.getJSONObject("paging").getString("next");
            }

            for (int i = 0; i < statusList.length(); i++)
            {
                JSONObject post = (JSONObject) statusList.get(i);

                // Delete nodes we don't need to store
                cleanPacket(post);

                // pull out keys
                String id = post.getString("id");
                String updated_time = post.getString("updated_time");
                Date date = facebookDateFormat.parse(updated_time);

                //pull out the year so we can group posts by year
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                String year = new Integer(calendar.get(Calendar.YEAR)).toString();




                Node node = JcrUtils.getOrCreateByPath(FacebookStatusJob.FACEBOOKPATH.replace("{1}", username).replace("{2}", year).replace("{3}", id), NodeType.NT_UNSTRUCTURED, session);

                // add some FamilyDam specific properties
                post.put("type", type);
                Map location = checkForLocation(post);
                if( location != null )
                {
                    node.addMixin("fd:geostamp");
                    node.setProperty("latitude", (Double) location.get("latitude"));
                    node.setProperty("longitude", (Double)location.get("longitude"));
                }

                new JsonToNode().convert(node, post);
                session.save();

            }


            // Follow the NEXT link with another job
            if( nextUrl != null )
            {
                invokeNextJob(username, nodePath, nextUrl);
            }


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


    protected void invokeNextJob(String username, String nodePath, String nextUrl)
    {
        throw new NotImplementedException();
    }


    /**
     * Look for a nest "Place" node with a location
     */
    private Map checkForLocation(JSONObject post) throws JSONException
    {
        Map locationMap = null;

        if( post.has("place") )
        {
            JSONObject place = post.getJSONObject("place");
            if( place.has("location") )
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
     * Delete nodes we don't need to store
     * @param post
     * @throws org.apache.sling.commons.json.JSONException
     */
    private void cleanPacket(JSONObject post) throws JSONException
    {

        if( post.has("likes") )
        {
            JSONObject o = post.getJSONObject("likes");
            if( o.has("paging") )
            {
                o.remove("paging");
            }
        }

        if( post.has("comments") )
        {
            JSONObject o = post.getJSONObject("comments");
            if( o.has("paging") )
            {
                o.remove("paging");
            }
        }

        if( post.has("tags") )
        {
            JSONObject o = post.getJSONObject("tags");
            if( o.has("paging") )
            {
                o.remove("paging");
            }
        }
    }
}
