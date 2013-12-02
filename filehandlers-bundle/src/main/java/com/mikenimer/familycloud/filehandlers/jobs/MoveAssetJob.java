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

package com.mikenimer.familycloud.filehandlers.jobs;

import com.mikenimer.familycloud.Constants;
import com.mikenimer.familycloud.ImageMimeTypes;
import com.mikenimer.familycloud.filehandlers.jobs.images.MetadataJob;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.jcr.resource.JcrModifiablePropertyMap;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.ObservationManager;
import javax.jcr.version.VersionManager;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * User: mikenimer
 * Date: 11/17/13
 */
@Component(enabled = true, immediate = true, metatype = true)
//Service(value=JobConsumer.class)
//Property(name=JobConsumer.PROPERTY_TOPICS, value=Constants.JOB_MOVE)
public class MoveAssetJob //implements JobConsumer
{
    private final Logger log = LoggerFactory.getLogger(MoveAssetJob.class);

    private Session session;
    private ObservationManager observationManager;

    @Reference
    private SlingRepository repository;

    //Reference
    private VersionManager versionManager;

    @Property(value = "/content/dam/upload/queue")
    private static final String UPLOAD_QUEUE_PATH = "/content/dam/upload/queue";

    @Property(value = "/content/dam/upload/errors")
    private static final String UPLOAD_ERROR_PATH = "/content/dam/upload/error";

    @Property(value = "/content/dam/photos")
    private static final String CONTENT_PHOTOS_PATH_PATH = "/content/dam/photos";//"content.photos.path";


    @Activate
    protected void activate(ComponentContext context) throws Exception
    {
        log.debug("Active Move Asset Job");
    }


    @Deactivate
    protected void deactivate(ComponentContext componentContext) throws RepositoryException
    {
        if (session != null)
        {
            session.logout();
            session = null;
        }
        log.debug("Deactivate Move Job");
    }


    public String process(String path, Session session, VersionManager versionManager)
    {
        try{
            this.versionManager = versionManager;
            this.session = session;
            Node node = session.getNode(path);

            if (isSupportedMimeType(node))
            {
                return moveImageAsset(node);
            }
        }
        catch(RepositoryException re)
        {
            return null;//JobResult.FAILED;
        }

        return null;//JobResult.FAILED;
    }





    private String moveImageAsset(Node node)
    {
        try
        {
            boolean bMetadataExists = false;
            Calendar dtCal = Calendar.getInstance();
            String path = CONTENT_PHOTOS_PATH_PATH;
            String folderPath = "";

            //Check jcr created & versionable nodes
            if( !node.isNodeType("fc:image") )
            {
                node.addMixin("fc:image");
                if( !node.isLocked() )
                {
                    session.save();
                }
                //node = session.getNode(node.getPath());
            }

            // extract but don't save the metadata for this node. the newImageObserver will take care of that.
            Map md = new MetadataJob().process(node, false);

            // move to folder based on exif metadata
            String originalPath = node.getPath();
            String destinationNode = null;
            Date datetime = null;
            if (md != null )
            {
                datetime = (Date) md.get(Constants.DATETIME);
                if( datetime != null )
                {
                    bMetadataExists = true;

                    // set to original date when photo was taken.
                    dtCal.setTime(datetime);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                    folderPath = sdf.format(dtCal.getTime());
                    path = path + "/" + dtCal.get(Calendar.YEAR) + "/" + folderPath;
                    verifyDestFolderExists(path);

                    destinationNode = path +"/" +node.getName();
                }

            }


            if( !bMetadataExists )
            {
                // No date in the metadata so we'll use the current date and save it at the "year" level
                // comment out for now, instead we'll keep images with no metadata in the root folder of the current year.
                //dtCal = node.getProperty(javax.jcr.Property.JCR_LAST_MODIFIED).getDate();
                //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                //folderPath = sdf.format(dtCal.getTime());

                // no metadata for date created, so we'll put the image in root path
                path = path + "/" + dtCal.get(Calendar.YEAR); // +"/" +folderPath
                verifyDestFolderExists(path);
                destinationNode = path;
            }




            /// If it already exists, override.
            if( session.itemExists(destinationNode) )
            {
                // todo. we might want save the old as a version or give the user the option to skip or override (with or without a version).
                session.removeItem(destinationNode +"/" +node.getName());
                session.save();
                //node.checkout();
            }


            // Move
            session.move(originalPath, destinationNode);
            session.save();


            return destinationNode;//JobResult.OK;
        }
        catch (ItemExistsException iee)
        {
            //todo create a version, then override
            iee.printStackTrace();
        }
        catch (RepositoryException iee)
        {
            return null;//JobResult.FAILED;
        }

        return null;//JobResult.FAILED;
    }



    /**
     * create needed folders before we try to copy into it.
     *
     * @param path
     * @throws javax.jcr.RepositoryException
     */
    private void verifyDestFolderExists(String path) throws RepositoryException
    {
        StringTokenizer st = new StringTokenizer(path, "/");
        Node lastNode = null;
        String buildPath = "";
        while (st.hasMoreElements())
        {
            String pathElement = st.nextElement().toString();
            buildPath = buildPath +"/" +pathElement;
            // check for the existence of the folder, if it doesn't exists create it.
            try
            {
                lastNode = session.getNode(buildPath);
            }
            catch (PathNotFoundException pe)
            {
                lastNode = lastNode.addNode(pathElement, "sling:Folder");
                //session.save();
            }
        }

    }


    /**
     * Check to see if this is a file type we should process, or leave it for another mime type
     *
     * @param n Node to check
     * @return
     * @throws RepositoryException
     */
    private boolean isSupportedMimeType(Node n)
    {
        try
        {
            //final String mimeType = n.getProperty("jcr:mimeType").getString();
            final String mimeType = n.getProperty("jcr:content/jcr:mimeType").getString();

            boolean b = ImageMimeTypes.isSupportedMimeType(mimeType);

            if (!b)
            {
                log.info("Node {} rejected, unsupported mime-type {}", n.getPath(), mimeType);
            }

            return b;
        }catch( RepositoryException re ){
            return false;
        }
    }

}
