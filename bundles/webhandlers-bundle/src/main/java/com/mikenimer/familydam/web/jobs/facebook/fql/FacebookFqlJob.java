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

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.apache.sling.jcr.api.SlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by mnimer on 3/22/14.
 */
@Component(enabled = true, immediate = true)
public class FacebookFqlJob implements JobConsumer
{
    private Session session;
    private final Logger log = LoggerFactory.getLogger(FacebookFqlJob.class);



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
     * Delete nodes we don't need to store
     *
     * @param post
     * @throws org.apache.sling.commons.json.JSONException
     */
    protected void cleanPacket(JSONObject post) throws JSONException
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
