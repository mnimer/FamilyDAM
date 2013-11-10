package com.mikenimer.familycloud.filehandlers.observers;

import com.mikenimer.familycloud.filehandlers.metadata.MetadataExtractor;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * User: mikenimer
 * Date: 11/9/13
 */
@Component(immediate = true, metatype = false)
@Property(name = "service.description", value = "Listener for uploaded files. This observer will move items around for file management")
public class UploadObserver implements EventListener
{
    private final Logger log = LoggerFactory.getLogger(UploadObserver.class);

    private Session session;
    private ObservationManager observationManager;

    @Reference
    private SlingRepository repository;

    @Property(value = "/content/dam/upload-queue")
    private static final String CONTENT_PATH_PROPERTY = "content.path";

    @Property(value = "/content/dam/photos")
    private static final String CONTENT_PHOTOS_PATH_PROPERTY = "/content/dam/photos";//"content.photos.path";

    private Map<String, String> supportedMimeTypes = new HashMap<String, String>();


    protected void activate(ComponentContext context) throws Exception
    {
        supportedMimeTypes.put("image/jpeg", "jpg");
        supportedMimeTypes.put("image/jpeg", "jpeg");
        supportedMimeTypes.put("image/png", "png");
        supportedMimeTypes.put("image/gif", "gif");
        supportedMimeTypes.put("image/nef", "nef");

        String contentPath = (String) context.getProperties().get(CONTENT_PATH_PROPERTY);

        session = repository.loginAdministrative(null);
        if (repository.getDescriptor(Repository.OPTION_OBSERVATION_SUPPORTED).equals("true"))
        {
            observationManager = session.getWorkspace().getObservationManager();
            String[] types = {"nt:file"};
            observationManager.addEventListener(this, Event.NODE_ADDED, contentPath, true, null, types, false);
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
                        InputStream fileStream = node.getProperty("jcr:data").getBinary().getStream();
                        MetadataExtractor metadataExtractor = new MetadataExtractor(fileStream);
                        Map md = metadataExtractor.getMetadata();

                        Calendar dtCal = Calendar.getInstance();
                        dtCal.setTime((Date) md.get("DATETIME"));
                        if( md != null && dtCal != null )
                        {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            String folderPath = sdf.format(dtCal.getTime());

                            md.remove("DATETIME");// remove the extra property we passed back
                            //todo add mixin
                            //node.setProperty("metadata", md);

                            //todo Change these 3 calls to a single with a StringTokenizer.. See JCRUtils for an example.
                            String path = CONTENT_PHOTOS_PATH_PROPERTY;
                            verifyPathExists(node, path);

                            path = path +"/" +dtCal.get(Calendar.YEAR);
                            verifyPathExists(node, path);

                            path = path +"/" +folderPath;
                            verifyPathExists(node, path);

                            try
                            {
                                path = path +"/" +node.getParent().getName();
                                node.getSession().move(node.getParent().getPath(), path);
                                node.getSession().save();
                            }
                            catch(ItemExistsException iee)
                            {
                                //todo, make a version of the file, then update the jcr:Data & Metadata nodes
                            }
                        }
                        else
                        {
                            dtCal = node.getProperty(javax.jcr.Property.JCR_LAST_MODIFIED).getDate();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            String folderPath = sdf.format(dtCal.getTime());

                            //todo Change these 3 calls to a single with a StringTokenizer.. See JCRUtils for an example.
                            String path = CONTENT_PHOTOS_PATH_PROPERTY;
                            verifyPathExists(node, path);

                            path = path +"/" +dtCal.get(Calendar.YEAR);
                            verifyPathExists(node, path);

                            path = path +"/" +folderPath;
                            verifyPathExists(node, path);


                            try
                            {
                                path = path +"/" +node.getParent().getName();
                                node.getSession().move(node.getParent().getPath(), path);
                                node.getSession().save();
                            }
                            catch(ItemExistsException iee)
                            {
                                //todo, make a version of the file, then update the jcr:Data & Metadata nodes
                            }
                        }

                    }
                    log.info("finished processing of {}", event.getPath());
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
       
    private void verifyPathExists(Node node, String path) throws RepositoryException
    {
        try
        {
            Node n = node.getSession().getNode(path);
        }catch (PathNotFoundException pe){
            node.getSession().getRootNode().addNode(path.substring(1), "nt:folder");
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
