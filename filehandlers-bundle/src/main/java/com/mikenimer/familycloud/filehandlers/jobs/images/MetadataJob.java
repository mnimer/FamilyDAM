package com.mikenimer.familycloud.filehandlers.jobs.images;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.mikenimer.familycloud.filehandlers.metadata.MetadataExtractor;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.jcr.resource.JcrModifiablePropertyMap;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * User: mikenimer
 * Date: 11/17/13
 */
@Component(enabled = true, immediate = true, metatype = true)
@Service(value=JobConsumer.class)
@Property(name=JobConsumer.PROPERTY_TOPICS, value="familycloud/photos/metadata")
public class MetadataJob implements JobConsumer
{
    private final Logger log = LoggerFactory.getLogger(MetadataJob.class);

    @Reference
    private SlingRepository repository;

    @Activate
    protected void activate(ComponentContext context) throws Exception
    {
        log.debug("Activate Metadata Job");
    }


    @Deactivate
    protected void deactivate(ComponentContext componentContext) throws RepositoryException
    {
        log.debug("Deactivate Metadata Job");
    }


    public JobConsumer.JobResult process(final Job job)
    {
        String path = (String)job.getProperty("path");

        try
        {
            Session session = repository.loginAdministrative(null);
            Node node = session.getRootNode().getNode(path);

            Map<String, Object> metadata = extractMetadata(node);

            Node metaNode = node.addNode("metadata");
            JcrModifiablePropertyMap propMap = new JcrModifiablePropertyMap(metaNode);
            propMap.putAll(metadata);
            propMap.save();
            // process the job and return the result
            return JobConsumer.JobResult.OK;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            return JobResult.FAILED;
        }

    }



    private Map<String, Object> extractMetadata(Node node) throws RepositoryException, IOException, ImageProcessingException
    {
        InputStream fileStream = node.getProperty("jcr:data").getBinary().getStream();
        MetadataExtractor metadataExtractor = new MetadataExtractor(fileStream);
        Map<String, Object> md = null;

        try
        {
            // first try with the drew metadata library
            md = metadataExtractor.getMetadata();
        }
        catch (ImageProcessingException ipe)
        {
            //alternative approach
            ImageInputStream iis = ImageIO.createImageInputStream(fileStream);
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (readers.hasNext())
            {
                // pick the first available ImageReader
                ImageReader reader = readers.next();
                // attach source to the reader
                reader.setInput(iis, true);
                // read metadata of first image
                IIOMetadata metadata = reader.getImageMetadata(0);
                String[] names = metadata.getMetadataFormatNames();
                int length = names.length;
                md = new HashMap<String, Object>();
                for (int i = 0; i < length; i++)
                {
                    md.put(names[i], (metadata.getAsTree(names[i])).getNodeValue());
                }
            }

        }

        return md;
    }
}