package com.mikenimer.familycloud.filehandlers.jobs;

import com.drew.imaging.ImageProcessingException;
import com.mikenimer.familycloud.Constants;
import com.mikenimer.familycloud.ImageMimeTypes;
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
import javax.jcr.observation.ObservationManager;
import java.io.IOException;
import java.io.InputStream;
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
@Service(value=JobConsumer.class)
@Property(name=JobConsumer.PROPERTY_TOPICS, value=Constants.JOB_MOVE)
public class MoveAssetJob implements JobConsumer
{
    private final Logger log = LoggerFactory.getLogger(MoveAssetJob.class);

    private Session session;
    private ObservationManager observationManager;

    @Reference
    private SlingRepository repository;

    @Property(value = "/content/dam/upload/queue")
    private static final String UPLOAD_QUEUE_PATH = "/content/dam/upload/queue";

    @Property(value = "/content/dam/upload/errors")
    private static final String UPLOAD_ERROR_PATH = "/content/dam/upload/error";

    @Property(value = "/content/dam/photos")
    private static final String CONTENT_PHOTOS_PATH_PATH = "/content/dam/photos";//"content.photos.path";


    public MoveAssetJob()
    {
        super();
    }


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


    @Override
    public JobResult process(Job job)
    {
        try{
            Node node = session.getNode((String)job.getProperty(Constants.PATH));

            if (isSupportedMimeType(node))
            {
                return moveImageAsset(node);
            }
        }
        catch(RepositoryException re)
        {
            return JobResult.FAILED;
        }

        return JobResult.FAILED;
    }


    private JobResult moveImageAsset(Node node)
    {
        try
        {
            boolean bMetadataExists = false;
            Calendar dtCal = Calendar.getInstance();
            String path = CONTENT_PHOTOS_PATH_PATH;
            String folderPath = "";

            try
            {
                InputStream fileStream = node.getProperty("jcr:data").getBinary().getStream();
                MetadataExtractor metadataExtractor = new MetadataExtractor(fileStream);
                Map md = metadataExtractor.getMetadata();


                // move to folder based on exif metadata
                if (md != null && md.get("DATETIME") != null)
                {
                    Date datetime = (Date) md.get("DATETIME");
                    dtCal.setTime(datetime);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                    folderPath = sdf.format(dtCal.getTime());
                    path = path + "/" + dtCal.get(Calendar.YEAR) + "/" + folderPath;
                    verifyPathExists(path);

                    md.remove("DATETIME");// remove the extra property we passed back

                    session.move(node.getParent().getPath(), path);
                    session.save();

                    bMetadataExists = true;
                    return JobResult.OK;
                }
            }
            catch (ImageProcessingException ipe){
                bMetadataExists = false;
            }
            catch (IOException ipe){
                bMetadataExists = false;
            }


            if( !bMetadataExists )
            {
                // No date in the metadata so we'll use the uploaded date
                // comment out for now, instead we'll keep images with no metadata in the root folder of the year.
                //dtCal = node.getProperty(javax.jcr.Property.JCR_LAST_MODIFIED).getDate();
                //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                //folderPath = sdf.format(dtCal.getTime());

                // no metadata for date created, so we'll put the image in root path
                path = path + "/" + dtCal.get(Calendar.YEAR); // +"/" +folderPath
                verifyPathExists(path);
                session.move(node.getParent().getPath(), path);
                session.save();
                return JobResult.OK;
            }
        }
        catch (ItemExistsException iee)
        {
            //todo create a version, then override
            iee.printStackTrace();
        }
        catch (RepositoryException iee)
        {
            return JobResult.FAILED;
        }

        return JobResult.FAILED;
    }


    /**
     * create needed folders before we try to copy into it.
     *
     * @param path
     * @throws javax.jcr.RepositoryException
     */
    private void verifyPathExists(String path) throws RepositoryException
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
                lastNode = lastNode.addNode(pathElement, "nt:folder");
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
            final String mimeType = n.getProperty("jcr:mimeType").getString();

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
