/*
 * This file is part of FamilyCloud Project.
 *
 *     The FamilyCloud Project is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     The FamilyCloud Project is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with the FamilyCloud Project.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * This file is part of FamilyCloud Project.
 *
 *     The FamilyCloud Project is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     The FamilyCloud Project is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with the FamilyCloud Project.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * This file is part of FamilyCloud Project.
 *
 *     The FamilyCloud Project is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     The FamilyCloud Project is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with the FamilyCloud Project.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mikenimer.familycloud.filehandlers.observers;

import com.mikenimer.familycloud.Constants;
import com.mikenimer.familycloud.filehandlers.jobs.MoveAssetJob;
import com.mikenimer.familycloud.filehandlers.jobs.images.MetadataJob;
import com.mikenimer.familycloud.filehandlers.jobs.images.SizeJob;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.event.EventPropertiesMap;
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
import java.util.Dictionary;
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
    private SlingRepository repository;

    @Reference
    EventAdmin eventAdmin;

    @Property(value = "/content/dam")
    private static final String DAM_ROOT_PATH = "/content/dam/";

    @Property(value = "/content/dam/upload/queue")
    private static final String UPLOAD_QUEUE_PATH = "/content/dam/upload/queue";

    @Property(value = "/content/dam/upload/errors")
    private static final String UPLOAD_ERROR_PATH = "/content/dam/upload/errors";


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
            //observationManager.addEventListener(this, Event.NODE_ADDED, contentPath, true, null, types, false);
            //observationManager.addEventListener(this, Event.NODE_MOVED, contentPath, true, null, types, false);
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
                    if( event.getPath().startsWith(UPLOAD_QUEUE_PATH) )
                    {
                        processUploadedFile(event);
                    }
                    else if( event.getPath().startsWith( DAM_ROOT_PATH +"photos" ) )
                    {
                        processImageFiles(event);
                    }
                }
                else
                {
                    log.debug(event.getType() +":" +event.getPath());
                }

            }
            catch (PathNotFoundException e)
            {
                //swallow. temporary files trigger this.
            }
            catch (Exception e)
            {
                e.printStackTrace();
                log.error(e.getMessage(), e);
            }
        }
    }


    private void processUploadedFile(Event event) throws RepositoryException
    {
        log.info("new upload: {}", event.getPath());
        if( !event.getPath().contains("fc:metadata") )
        {
            Node node = session.getNode(event.getPath()).getParent();
            if( !node.getName().startsWith(".") )
            {
                // parse sizing information
                Map moveJob = new HashMap();
                moveJob.put(Constants.PATH, node.getPath());

                //until the next version of sling is ready and we can register a proper job to do this
                // we'll invoke this directly.. Todo: remove after sling upgrade to 3.
                String newPath = new MoveAssetJob().process(node.getPath(), session, null);
                //log.debug(node.getPath());

                if( newPath != null )
                {
                    Node newNode = session.getNode(newPath);
                    new MetadataJob().process(newNode, true);
                    new SizeJob().process(newNode);
                    session.save();
                }
                else
                {
                    session.move(node.getParent().getPath(), UPLOAD_ERROR_PATH);
                }

                // new Sling3.0 jobs
                //JobBuilder jobBuilder = jobManager.createJob(Constants.JOB_MOVE);
                //jobBuilder.properties(moveJob);
                //Job job = jobBuilder.add();
                //Job job = jobManager.addJob(Constants.JOB_MOVE, moveJob);
                //log.debug("Create Job {} / {}", job.getTopic(), job.getId());

                // osgi events
                //http://experiencedelivers.adobe.com/cemblog/en/experiencedelivers/2012/04/event_handling_incq.html
                //EventPropertiesMap props = new EventPropertiesMap();
                //props.put("path", node.getPath());
                //org.osgi.service.event.Event moveEvent = new org.osgi.service.event.Event("dam/move", props);
                //eventAdmin.postEvent(moveEvent);

            }
            else{
                log.debug("skipping hidden file {}", node.getPath());
            }
        }
        else{
            log.debug("skipping metadata node {}", event.getPath());
        }
    }



    private void processImageFiles(Event event) throws RepositoryException, InterruptedException, IOException
    {
        log.info("new upload: {}", event.getPath());
        if( !event.getPath().contains("fc:metadata") )
        {
            Node node = session.getNode(event.getPath()).getParent();
            // skip hidden, we'll delete them instead
            if( !node.getName().startsWith(".") )
            {
                waitForFileUploadToComplete(node);

                //reload the node reference
                node = session.getNode(event.getPath()).getParent();
                //Check jcr created & versionable nodes
                if( !node.isNodeType("fc:image") )
                {
                    try
                    {
                        node.addMixin("fc:image");
                        session.save();
                    }catch (InvalidItemStateException e){
                        e.printStackTrace();
                    }
                    //node = session.getNode(node.getPath());
                }


                EventPropertiesMap props = new EventPropertiesMap();
                props.put("path", node.getPath());

                // extract and save the metadata for this node.
                new MetadataJob().process(node, true);
                new SizeJob().process(node);
                session.save();
            }
            else{
                //todo delete hidden files
                log.debug("skipping hidden file {}", node.getPath());
            }
        }
        else{
            log.debug("skipping metadata node {}", event.getPath());
        }
    }


    private void waitForFileUploadToComplete(Node node) throws RepositoryException, IOException, InterruptedException
    {
        int size = 0;
        boolean bIsLocked = true;

        Node nodeContent = node.getSession().getNode(node.getPrimaryItem().getPath());
        InputStream stream = nodeContent.getProperty("jcr:data").getBinary().getStream();
        while ( bIsLocked || stream.available() == 0 || stream.available() > size )
        {
            Thread.sleep(100);
            bIsLocked = node.isLocked();
            stream = nodeContent.getProperty("jcr:data").getBinary().getStream();
            size = stream.available();
        }

        log.debug("File has completed upload and is unlocked");
    }


}
