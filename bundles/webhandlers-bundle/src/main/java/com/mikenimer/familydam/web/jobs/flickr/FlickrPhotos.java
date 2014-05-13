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

package com.mikenimer.familydam.web.jobs.flickr;

import com.mikenimer.familydam.web.jobs.facebook.fql.FacebookFqlJob;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.apache.sling.jcr.api.SlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Session;


@Component(enabled = true, immediate = true)
@Service(value = JobConsumer.class)
@Property(name = JobConsumer.PROPERTY_TOPICS, value = "familydam/web/flickr/photos")
public class FlickrPhotos implements JobConsumer
{
    public static String TOPIC = "familydam/web/flickr/photos";
    public static String FOLDERPATH = "/content/dam/web/flickr/{1}/photos/{2}";
    public static String NODEPATH = "/content/dam/web/flickr/{1}/photos/{2}/{3}";

    private final Logger log = LoggerFactory.getLogger(FlickrPhotos.class);
    private Session session;

    @Reference
    protected JobManager jobManager;


    @Reference
    public SlingRepository repository;


    @Override
    public JobResult process(Job job)
    {
        String userNode = (String) job.getProperty("userNode");


        try
        {
            session = repository.loginAdministrative(null);
            Node node = session.getNode(userNode);

            Node _flickrData = node.getNode("web/flickr");
            if (_flickrData != null)
            {
                //return queryFlickr(job, _flickrData);
            }
            // No Data, but job was still invoked
            return JobResult.FAILED;
        }
        catch (Exception re)
        {
            return JobResult.CANCEL;
        }
    }


    private JobResult queryFlickr(Job job, Node flickrData, String username)
    {
        String _username = (String) job.getProperty("username");

        return JobResult.OK;
    }
}