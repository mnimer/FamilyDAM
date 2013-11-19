package com.mikenimer.familycloud.filehandlers.observers.images;

import com.drew.imaging.ImageProcessingException;
import com.mikenimer.familycloud.Constants;
import com.mikenimer.familycloud.filehandlers.metadata.MetadataExtractor;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * User: mikenimer
 * Date: 11/13/13
 */
@Component(immediate = true, metatype = false)
@Property(name = "service.description", value = "Process new files, under /content/dam/photos")
public class NewImageObserver  implements EventListener
{
    private final Logger log = LoggerFactory.getLogger(NewImageObserver.class);

    private Session session;
    private ObservationManager observationManager;

    @Reference
    private JobManager jobManager;

    @Reference
    private SlingRepository repository;
    //Reference
    //private VersionManager versionManager;

    @Property(value = "/content/dam/photos")
    private static final String CONTENT_PHOTOS_PATH_PROPERTY = "/content/dam/photos";

    @Property(
            label="Photo Mixins",
            value = "mix:created, mix:etag, mix:lastModified, mix:mimeType, mix:referenceable, mix:versionable, fc:image, fc:geo, fc:taggable",
            description="Default mixins applied to photos")
    private static final String PHOTO_MIXINS = "mix:created, mix:etag, mix:lastModified, mix:mimeType, mix:referenceable, mix:versionable, fc:image, fc:geo, fc:taggable";


    private Map<String, String> supportedMimeTypes = new HashMap<String, String>();


    protected void activate(ComponentContext context) throws Exception
    {

        String contentPath = (String) context.getProperties().get(CONTENT_PHOTOS_PATH_PROPERTY);

        session = repository.loginAdministrative(null);
        if (repository.getDescriptor(Repository.OPTION_OBSERVATION_SUPPORTED).equals("true"))
        {
            observationManager = session.getWorkspace().getObservationManager();
            String[] types = {"nt:file"};
            observationManager.addEventListener(this, Event.NODE_ADDED, CONTENT_PHOTOS_PATH_PROPERTY, true, null, types, false);
            observationManager.addEventListener(this, Event.NODE_MOVED, CONTENT_PHOTOS_PATH_PROPERTY, true, null, types, false);
        }
    }


    protected void deactivate(ComponentContext componentContext) throws RepositoryException
    {
        if (observationManager != null)
        {
            observationManager.removeEventListener(this);
        }
        if (session != null)
        {
            session.logout();
            session = null;
        }
    }


    @Override
    public void onEvent(EventIterator events)
    {
        log.debug(events.toString());

        while (events.hasNext()) {
            Event event = events.nextEvent();
            try {
                if (event.getType() == Event.NODE_ADDED )
                {
                    log.info("new upload: {}", event.getPath());
                    Node node = session.getRootNode().getNode(event.getPath().substring(1));


                    if( isSupportedMimeType(node) )
                    {
                        // Add default set of mixins
                        try
                        {
                            String[] mixinList = PHOTO_MIXINS.split(",");
                            for (String mixin : mixinList)
                            {
                                node.addMixin(mixin.trim());
                            }
                            node.getSession().save();
                        }catch (Exception ex){}



                        // Parse Metadata job
                        Map metadataJob = new HashMap();
                        metadataJob.put("path", node.getPath());
                        Job job1 = jobManager.addJob(Constants.JOB_IMAGE_METADATA, metadataJob);

                        // parse sizing information
                        Map sizeJob = new HashMap();
                        sizeJob.put("path", node.getPath());
                        Job job2 = jobManager.addJob(Constants.JOB_IMAGE_SIZE, sizeJob);

                        // create thumbnails of images
                        Map thumbnailJob = new HashMap();
                        thumbnailJob.put("path", node.getPath());
                        Job job3 = jobManager.addJob(Constants.JOB_IMAGE_THUMBNAILS, thumbnailJob);

                    }

                    log.info("finished processing of {}", event.getPath());
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }





    private boolean isSupportedMimeType(Node n) throws RepositoryException
    {
        boolean result = false;
        final String mimeType = n.getProperty("jcr:mimeType").getString();

        for(String key : supportedMimeTypes.keySet()) {
            if(mimeType!=null && mimeType.startsWith(key)) {
                //result = key;
                return true;
            }
        }

        if(!result)
        {
            String extension = n.getParent().getName().substring(n.getParent().getName().indexOf('.')+1).toLowerCase();
            if( supportedMimeTypes.containsValue( extension ))
            {
                return true;
            }

            log.info("Node {} rejected, unsupported mime-type {}", n.getPath(), mimeType);
        }


        return result;
    }

}
