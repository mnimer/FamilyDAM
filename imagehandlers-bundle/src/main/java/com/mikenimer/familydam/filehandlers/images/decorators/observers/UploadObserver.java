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

package com.mikenimer.familydam.filehandlers.images.decorators.observers;

import com.mikenimer.familydam.filehandlers.images.Constants;
import com.mikenimer.familydam.filehandlers.images.MimeTypeManager;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.InvalidItemStateException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
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
@Component(enabled = true, immediate = true, metatype = false)
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

    @Reference
    EventAdmin eventAdmin;

    @Property(value = "/content/dam")
    private static final String DAM_ROOT_PATH = "/content/dam/";


    @Activate
    protected void activate(ComponentContext context) throws Exception
    {
        String contentPath = (String) context.getProperties().get(DAM_ROOT_PATH);

        session = repository.loginAdministrative(null);
        if (repository.getDescriptor(Repository.OPTION_OBSERVATION_SUPPORTED).equals("true"))
        {
            observationManager = session.getWorkspace().getObservationManager();
            String[] types = {"nt:file"};
            observationManager.addEventListener(this, Event.NODE_ADDED | Event.NODE_MOVED, contentPath, true, null, types, false);
        }
    }


    @Deactivate
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
                if (event.getType() == Event.NODE_ADDED || event.getType() == Event.NODE_MOVED)
                {
                    Node parentNode = session.getNode(event.getPath()).getParent();

                    if (MimeTypeManager.isImage(event.getPath()) || parentNode.isNodeType("fd:image") )
                    {
                        processImageFile(event);
                    }
                } else
                {
                    log.debug(event.getType() + ":" + event.getPath());
                }

            } catch (PathNotFoundException e)
            {
                //swallow. temporary files trigger this.
            } catch (Exception e)
            {
                e.printStackTrace();
                log.error(e.getMessage(), e);
            }
        }
    }


    /**
     * Initialize any jobs that are required for Image Files in the system.
     * @param event
     * @throws RepositoryException
     * @throws InterruptedException
     * @throws IOException
     */
    private void processImageFile(Event event) throws RepositoryException, InterruptedException, IOException
    {
        log.info("new image upload: {}", event.getPath());

        Node node = session.getNode(event.getPath()).getParent();
        // skip hidden files, we'll delete them instead
        if (!node.getName().startsWith(".")  && event.getPath().endsWith("jcr:content"))
        {
            // First we'll spin in a loop to wait for the file upload to complete. This way none
            // of our Jobs will hit broken files.
            waitForFileUploadToComplete(node); //todo, does sling3 support this

            //reload the node reference
            node = session.getNode(event.getPath()).getParent();


            //Check jcr created & versionable nodes
            if (!node.isNodeType("fd:image"))
            {
                try
                {
                    //first assign the right mixin
                    node.addMixin("fd:image");
                    node.addNode(Constants.METADATA, "nt:unstructured");
                    session.save();

                    //Set some default properties
                    Node md = node.getNode(Constants.METADATA);
                    md.setProperty(Constants.KEYWORDS, "");
                    //md.setProperty(Constants.LATITUDE, 0.0);
                    //md.setProperty(Constants.LONGITUDE, 0.0);
                    session.save();
                } catch (InvalidItemStateException e)
                {
                    e.printStackTrace();
                }
                //node = session.getNode(node.getPath());
            }

            // The trigger the jobs for this type of content


            //todo, sling 2.6 way, remove when sling 3 jobs are working.
            //new MetadataJob().process(node, true);
            //new SizeJob().process(node);


            // Sling 3 jobs.

            // extract and save the metadata for this node.
            Map props = new HashMap();
            props.put("nodePath", node.getPath());

            Job metadataJob = jobManager.addJob(Constants.JOB_IMAGE_METADATA, props);
            log.debug("Create Job {} / {}", metadataJob.getTopic(), metadataJob.getId());

            Job sizeJob = jobManager.addJob(Constants.JOB_IMAGE_SIZE, props);
            log.debug("Create Job {} / {}", sizeJob.getTopic(), sizeJob.getId());

            session.save();

        }
        else
        {
            //todo delete hidden files
            log.debug("skipping hidden file {}", node.getPath());
        }
    }



    /**
     * Spin in a loop to wait for file uploads to complete, so we can hold off post-processing against corrupt files.
     * @param node
     * @throws RepositoryException
     * @throws IOException
     * @throws InterruptedException
     */
    private void waitForFileUploadToComplete(Node node) throws RepositoryException, IOException, InterruptedException
    {
        int size = 0;
        boolean bIsLocked = true;

        Node nodeContent = node.getSession().getNode(node.getPrimaryItem().getPath());
        InputStream stream = nodeContent.getProperty("jcr:data").getBinary().getStream();
        while (bIsLocked)// || stream.available() == 0 || stream.available() > size )
        {
            Thread.sleep(100);
            bIsLocked = node.isLocked();
            stream = nodeContent.getProperty("jcr:data").getBinary().getStream();
            size = stream.available();
        }

        log.debug("File has completed upload and is unlocked");
    }


}
