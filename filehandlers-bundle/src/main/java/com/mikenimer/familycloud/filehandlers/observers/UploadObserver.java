package com.mikenimer.familycloud.filehandlers.observers;

import com.mikenimer.familycloud.Constants;
import com.mikenimer.familycloud.ImageMimeTypes;
import com.mikenimer.familycloud.filehandlers.metadata.MetadataExtractor;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobBuilder;
import org.apache.sling.event.jobs.JobManager;
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
import java.util.StringTokenizer;

/**
 * Watch files put in the generic upload folder. If it's an Image files (based on supported images) we
 * will move it to the right location.
 * <p/>
 * If we can find the data taken in the metadata, we'll put it in a year -> date sub folder. If not we'll
 * use the date uploaded to pick a path.
 * <p/>
 * <p/>
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
    private JobManager jobManager;

    @Reference
    private SlingRepository repository;

    @Property(value = "/content/dam/upload/queue")
    private static final String UPLOAD_QUEUE_PATH = "/content/dam/upload/queue";



    protected void activate(ComponentContext context) throws Exception
    {
        String contentPath = (String) context.getProperties().get(UPLOAD_QUEUE_PATH);

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

        while (events.hasNext())
        {
            Event event = events.nextEvent();
            try
            {
                if (event.getType() == Event.NODE_ADDED)
                {
                    log.info("new upload: {}", event.getPath());
                    Node node = session.getNode(event.getPath()).getParent();
                    if( !node.getName().startsWith(".") )
                    {
                        // parse sizing information
                        Map moveJob = new HashMap();
                        moveJob.put(Constants.PATH, node.getPath());

                        JobBuilder jobBuilder = jobManager.createJob(Constants.JOB_MOVE);
                        jobBuilder.properties(moveJob);
                        Job job = jobBuilder.add();
                        //Job job = jobManager.addJob(Constants.JOB_MOVE, moveJob);
                        log.debug("Create Job {} / {}", job.getTopic(), job.getId());
                    }
                    else{
                        log.debug("skipping hidden file {}", node.getPath());
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                log.error(e.getMessage(), e);
            }
        }
    }


}
