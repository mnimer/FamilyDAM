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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mnimer on 2/6/14.
 */
@Component(enabled = true, immediate = true)
@Service(value = JobConsumer.class)
@Property(name = JobConsumer.PROPERTY_TOPICS, value = "familydam/web/facebook/statuses")
public class FacebookPostsJob extends FacebookJob
{
    public static String TOPIC = "familydam/web/facebook/statuses";
    public static String FACEBOOKPATH = "/content/dam/web/facebook/{1}/statuses/{2}/{3}";
    public static String FACEBOOKPATHByUser = "/content/dam/web/facebook/{1}/statuses/{2}/{3}";

    private final Logger log = LoggerFactory.getLogger(FacebookPostsJob.class);


    @Activate
    protected void activate(ComponentContext context) throws Exception
    {
        log.debug("Activate FacebookStatusJob Job");
    }


    @Deactivate
    protected void deactivate(ComponentContext componentContext) throws RepositoryException
    {
        log.debug("Deactivate FacebookStatusJob Job");
    }


    @Override
    protected JobResult queryFacebook(Node facebookData, String username, String userPath, String nextUrl) throws RepositoryException, IOException, JSONException
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
            _url = "https://graph.facebook.com/" + userId + "/posts?access_token=" + accessToken;
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
        return saveData(username, jsonStr, FACEBOOKPATH, "status");
    }



    @Override
    protected void invokeNextJob(String username, String nodePath, String nextUrl)
    {
        Map props = new HashMap();
        props.put("nodePath", nodePath);
        props.put("url", nextUrl);
        props.put("username", username);
        Job metadataJob = jobManager.addJob(FacebookPostsJob.TOPIC, props);
    }
}
