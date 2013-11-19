package com.mikenimer.familycloud.filehandlers.jobs.images;

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
@Component(enabled = true, immediate = true, metatype = true)
@Service(value=JobConsumer.class)
@Property(name=JobConsumer.PROPERTY_TOPICS, value="familycloud/photos/size")
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


    public JobResult process(final Job job)
    {
        String path = (String)job.getProperty("path");

        try
        {
            Session session = repository.loginAdministrative(null);
            Node node = session.getRootNode().getNode(path);

            InputStream fileStream = node.getProperty("jcr:data").getBinary().getStream();
            BufferedImage bi = ImageIO.read(fileStream);

            int w = bi.getWidth();
            int h = bi.getHeight();

            node.setProperty("width", w);
            node.setProperty("height", h);

            return JobResult.OK;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            return JobResult.FAILED;
        }
    }
}
