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

package com.mikenimer.familycloud.filehandlers.observers.images;

import com.mikenimer.familycloud.filehandlers.jobs.images.MetadataJob;
import com.mikenimer.familycloud.filehandlers.jobs.images.SizeJob;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;
import java.util.HashMap;
import java.util.Map;

/**
 * User: mikenimer
 * Date: 11/13/13
 * todo: why didn't this trap any events, need to research and use this instead of making the UploadObserver do both.
 */
//Component(enabled = true, immediate = true, metatype = false)
//Property(name = "service.description", value = "Process new files, under /content/dam/photos")
public class NewImageObserver implements EventListener
{
    private final Logger log = LoggerFactory.getLogger(NewImageObserver.class);

    private Session session;
    private ObservationManager observationManager;


    @Reference
    private SlingRepository repository;
    //Reference
    //private VersionManager versionManager;

    @Property(value = "/content/dam/photos")
    private static final String UPLOAD_PATH = "/content/dam/photos";


    protected void activate(ComponentContext context) throws Exception
    {

        String contentPath = (String) context.getProperties().get(UPLOAD_PATH);

        session = repository.loginAdministrative(null);
        if (repository.getDescriptor(Repository.OPTION_OBSERVATION_SUPPORTED).equals("true"))
        {
            observationManager = session.getWorkspace().getObservationManager();
            String[] types = {"nt:file"};
            observationManager.addEventListener(this, Event.NODE_ADDED |  Event.NODE_MOVED, contentPath, true, null, types, true);

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


                    // check for inner nodes, like metadata that we are adding to this node.
                    if( node.getPath().contains("metadata") )
                    {
                        return;
                    }

                    if( !node.getName().startsWith(".") )
                    {
                        // extract and save the metadata for this node.
                        new MetadataJob().process(node, true);
                        new SizeJob().process(node);
                        session.save();

                        // Parse Metadata job
                        //Map metadataJob = new HashMap();
                        //metadataJob.put("path", node.getPath());
                        //Job job1 = jobManager.addJob(Constants.JOB_IMAGE_METADATA, metadataJob);

                        // parse sizing information
                        //Map sizeJob = new HashMap();
                        //sizeJob.put("path", node.getPath());
                        //Job job2 = jobManager.addJob(Constants.JOB_IMAGE_SIZE, sizeJob);

                    }
                    log.info("finished processing of {}", event.getPath());
                }
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }
    }

}
