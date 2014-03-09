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

package com.mikenimer.familydam.filehandlers.images.jobs;

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

import javax.imageio.ImageIO;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.awt.image.BufferedImage;
import java.io.InputStream;

/**
 * User: mikenimer
 * Date: 11/17/13
 */
@Component(enabled = true, immediate = true)
@Service(value = JobConsumer.class)
@Property(name = JobConsumer.PROPERTY_TOPICS, value = "familydam/photos/size")
public class SizeJob implements JobConsumer
{
    private final Logger log = LoggerFactory.getLogger(SizeJob.class);

    @Reference
    private SlingRepository repository;


    @Activate
    protected void activate(ComponentContext context) throws Exception
    {
        log.debug("Activate Size Job");
    }


    @Deactivate
    protected void deactivate(ComponentContext componentContext) throws RepositoryException
    {
        log.debug("Deactivate Size Job");
    }


    @Override
    public JobResult process(Job job)
    {
        String nodePath = (String) job.getProperty("nodePath");
        return process(nodePath);
    }


    public JobResult process(String path)
    {
        try
        {
            Session session = repository.loginAdministrative(null);
            Node node = session.getNode(path);

            if( !node.isNodeType("fd:image") )
            {
                node.addMixin("fd:image");
                session.save();
            }

            return process(session, node);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();  //todo, log with sling logging
            return JobResult.FAILED;
        }
    }


    public JobResult process(Session session, Node node)
    {
        try
        {
            int size = 0;
            InputStream stream = node.getSession().getNode(node.getPrimaryItem().getPath()).getProperty("jcr:data").getBinary().getStream();
            size = stream.available();
            BufferedImage bi = ImageIO.read(stream);

            int w = bi.getWidth();
            int h = bi.getHeight();

            node.setProperty("width", w);
            node.setProperty("height", h);
            node.setProperty("length", size);
            session.save();

            return JobResult.OK;
        } catch (Exception ex)
        {
            ex.printStackTrace();
            return JobResult.FAILED;
        }
    }

}

