package com.mikenimer.familycloud.filehandlers.observers.images;

import com.drew.imaging.ImageProcessingException;
import com.mikenimer.familycloud.filehandlers.metadata.MetadataExtractor;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.jackrabbit.commons.json.JsonUtil;
import org.apache.sling.jcr.api.SlingRepository;
import org.mortbay.util.ajax.JSON;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;
import javax.jcr.version.VersionManager;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
    private SlingRepository repository;
    //Reference
    //private VersionManager versionManager;

    @Property(value = "/content/dam")
    private static final String CONTENT_PATH_PROPERTY = "/content/dam/photos";

    private Map<String, String> supportedMimeTypes = new HashMap<String, String>();


    protected void activate(ComponentContext context) throws Exception
    {
        supportedMimeTypes.put("jpg", "image/jpeg");
        supportedMimeTypes.put("jpe", "image/jpeg");
        supportedMimeTypes.put("jpeg", "image/jpeg");
        supportedMimeTypes.put("png", "image/png");
        supportedMimeTypes.put("gif", "image/gif");
        supportedMimeTypes.put("nef", "image/nef");
        supportedMimeTypes.put("tif", "image/tiff");
        supportedMimeTypes.put("tiff", "image/tiff");
        supportedMimeTypes.put("psd", "image/vnd.adobe.photoshop");
        supportedMimeTypes.put("svg", "image/svg+xml");

        String contentPath = (String) context.getProperties().get(CONTENT_PATH_PROPERTY);

        session = repository.loginAdministrative(null);
        if (repository.getDescriptor(Repository.OPTION_OBSERVATION_SUPPORTED).equals("true"))
        {
            observationManager = session.getWorkspace().getObservationManager();
            String[] types = {"nt:file"};
            observationManager.addEventListener(this, Event.NODE_ADDED, CONTENT_PATH_PROPERTY, true, null, types, false);
            observationManager.addEventListener(this, Event.NODE_MOVED, CONTENT_PATH_PROPERTY, true, null, types, false);
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
                            node.addMixin("fc:image");
                            node.addMixin("fc:geo");
                            node.addMixin("fc:taggable");
                            node.addMixin("fc:image");
                            node.addMixin("fc:geo");
                            node.addMixin("fc:taggable");
                            node.getSession().save();
                        }catch (Exception ex){}



                        // Before doing anything else, save a default version.
                        try
                        {
                            //versionManager.checkin(node.getPath());
                            node.getSession().save();
                        }catch( Exception ex){}



                        // Parse Metadata
                        try
                        {
                            extractMetadata(node);
                            node.getSession().save();
                        }
                        catch(ItemExistsException iee)
                        {
                            //todo, make a version of the file, then update the jcr:Data & Metadata nodes
                        }

                    }
                    log.info("finished processing of {}", event.getPath());
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }


    private void extractMetadata(Node node) throws RepositoryException, IOException, ImageProcessingException
    {
        InputStream fileStream = node.getProperty("jcr:data").getBinary().getStream();
        MetadataExtractor metadataExtractor = new MetadataExtractor(fileStream);
        Map md = metadataExtractor.getMetadata();
        if( md != null )
        {
            //node.setProperty("metadata", json);
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
