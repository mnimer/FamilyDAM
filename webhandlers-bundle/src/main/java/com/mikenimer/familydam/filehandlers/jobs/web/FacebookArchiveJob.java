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

package com.mikenimer.familydam.filehandlers.jobs.web;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Created by mnimer on 2/6/14.
 */
@Component(enabled = true, immediate = true)
@Service(value=JobConsumer.class)
@Property(name= JobConsumer.PROPERTY_TOPICS, value="familydam/web/facebook/archive")
public class FacebookArchiveJob implements JobConsumer
{

    private final Logger log = LoggerFactory.getLogger(FacebookArchiveJob.class);

    @Reference
    private SlingRepository repository;

    @Activate
    protected void activate(ComponentContext context) throws Exception
    {
        log.debug("Activate ID3 Job");
    }


    @Deactivate
    protected void deactivate(ComponentContext componentContext) throws RepositoryException
    {
        log.debug("Deactivate ID3 Job");
    }


    @Override
    public JobResult process(Job job)
    {
        String nodePath = (String)job.getProperty("nodePath");
        return process(nodePath);
    }


    public JobResult process(String path)
    {
        try
        {
            Session session = repository.loginAdministrative(null);
            Node node = session.getNode(path);
            return JobResult.OK;
        }catch( RepositoryException re ){
            return JobResult.FAILED;
        }
    }


}
