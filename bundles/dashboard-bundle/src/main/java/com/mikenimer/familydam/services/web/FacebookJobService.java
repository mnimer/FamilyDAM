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

package com.mikenimer.familydam.services.web;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobBuilder;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.event.jobs.ScheduledJobInfo;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mnimer on 2/5/14.
 */

@Component(immediate=true, metatype=false)
@Service(Servlet.class)
@Properties({ @Property(name="service.description", value="Start Facebook job to pull data"),
        @Property(name="service.vendor", value="The FamilyDAM Project"),
        @Property(name="sling.servlet.paths", value="/dashboard-api/jobs/facebook")
})
public class FacebookJobService extends SlingAllMethodsServlet
{
    private final Logger log = LoggerFactory.getLogger(FacebookJobService.class);
    private Session session;


    @Reference
    private SlingRepository repository;

    @Reference
    private JobManager jobManager;

    @Activate
    protected void activate(ComponentContext ctx)  throws Exception
    {
        log.debug("Facebook Update User Servlet started");
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) throws RepositoryException
    {
        log.debug("Facebook Update User Servlet Deactivated");
    }

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
    {
        String username = request.getParameter("username");

        triggerFacebookJobs(username);
    }



    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
    {
        String username = request.getParameter("username");

        if( username == null ){
            throw new RuntimeException("Invalid Username"); //todo use customer exception
        }

        // extract and save the metadata for this node.
        triggerFacebookJobs(username);

    }


    @Override
    protected void doDelete(SlingHttpServletRequest request, SlingHttpServletResponse response)
    {
        String username = request.getParameter("username");

        if( username == null ){
            throw new RuntimeException("Invalid Username"); //todo use customer exception
        }

        // todo
    }




    private void triggerFacebookJobs(String username)
    {
        //todo: make this user configurable (OSGI property maybe)
        String cronSchedule = "0 0 0/1 * * ?";

        Map props = new HashMap();
        props.put("refreshAll", true);
        props.put("username", username);
        props.put("nodePath", "/apps/familydam/users/" +username);

        try
        {
            // Run immediately
            //Job metadataJob = jobManager.addJob("familydam/web/facebook/statuses", props);
            Job checkInJob = jobManager.addJob("familydam/web/facebook/checkins", props);
            //Job albumsJob = jobManager.addJob("familydam/web/facebook/albums", props);


            boolean hasStatusJob = false;
            boolean hasCheckinJob = false;
            boolean hasAlbumsJob = false;
            // add a scheduled job, if not already added.
            for (ScheduledJobInfo scheduledJobInfo : jobManager.getScheduledJobs())
            {
                if (scheduledJobInfo.getJobTopic().equals("familydam/web/facebook/statuses"))
                {
                    hasStatusJob = true;
                }

                if (scheduledJobInfo.getJobTopic().equals("familydam/web/facebook/checkins"))
                {
                    hasCheckinJob = true;
                }

                if (scheduledJobInfo.getJobTopic().equals("familydam/web/facebook/albums"))
                {
                    hasAlbumsJob = true;
                }
            }


            // our scheduled tasks should only try to pull the newest items
            props.put("refreshAll", false);
            if (!hasStatusJob)
            {
                //JobBuilder.ScheduleBuilder job = jobManager.createJob("familydam/web/facebook/statuses").properties(props).schedule().cron(cronSchedule);
                //ScheduledJobInfo info = job.add();
            }

            if (!hasCheckinJob)
            {
                //JobBuilder.ScheduleBuilder job2 = jobManager.createJob("familydam/web/facebook/checkins").properties(props).schedule().cron(cronSchedule);
                //ScheduledJobInfo info2 = job2.add();
            }

            if (!hasAlbumsJob)
            {
                //JobBuilder.ScheduleBuilder job3 = jobManager.createJob("familydam/web/facebook/albums").properties(props).schedule().cron(cronSchedule);
                //ScheduledJobInfo info3 = job3.add();
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

}
