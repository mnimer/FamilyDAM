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

package com.mikenimer.familydam.filehandlers.jobs.images;

import com.drew.imaging.ImageProcessingException;
import com.mikenimer.familydam.Constants;
import com.mikenimer.familydam.filehandlers.metadata.DrewMetadataExtractor;
import org.apache.commons.lang.StringUtils;
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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * User: mikenimer
 * Date: 11/17/13
 * Component(enabled = true, immediate = true)
 */
@Component(enabled = true, immediate = true)
@Service(value=JobConsumer.class)
@Property(name= JobConsumer.PROPERTY_TOPICS, value="familydam/photos/metadata")
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


    @Override
    public JobResult process(Job job)
    {
        String nodePath = (String)job.getProperty("nodePath");
        return process(nodePath);
    }


    public JobResult process(String path)
    {
        try
        {
            Session session = repository.loginAdministrative(null);
            Node node = session.getNode(path);
            return process(node, true);
        }catch( RepositoryException re ){
            return JobResult.FAILED;
        }
    }



    public JobResult process(Node node, boolean persist)
    {
        Calendar dtCal = Calendar.getInstance();
        try
        {
            Map<String, Object> metadata = extractMetadata(node);

            if( metadata != null )
            {
                try
                {
                    // Pull out the date/time of the original photo from the EXIF data and update system date for the file.
                    Date datetime = (Date) metadata.get(Constants.DATETIME);
                    // if an original date  doesn't exist we'll set fc:Created to now.
                    if( datetime != null )
                    {
                        dtCal.setTime(datetime);
                        node.setProperty("created", dtCal);
                    }
                }catch (Exception ex){
                    //swallow
                }



                if( persist )
                {
                    saveObjectToNode(node, Constants.METADATA, metadata);
                    node.getSession().save();
                }
            }
            // after saving the metadata with the node, return the result
            return JobResult.OK;


        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            return JobResult.FAILED;
        }

    }



    private Map<String, Object> extractMetadata(Node node) throws RepositoryException, IOException, ImageProcessingException, InterruptedException
    {
        InputStream stream = node.getSession().getNode(node.getPrimaryItem().getPath()).getProperty("jcr:data").getBinary().getStream();
        Map<String, Object> md = new HashMap<String, Object>();

        try
        {
            // first try with the drew metadata library
            DrewMetadataExtractor drewMetadataExtractor = new DrewMetadataExtractor(stream);
            md = drewMetadataExtractor.getMetadata();

            // todo add Apache Commons Image library, as a backup
        }
        catch (ImageProcessingException ipe)
        {
            //alternative approach
            /***
            ImageInputStream iis = ImageIO.createImageInputStream(stream);
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

                for (int i = 0; i < length; i++)
                {
                    md.put(names[i], (metadata.getAsTree(names[i])).getNodeValue());
                }
            }
             ***/


        }

        return md;
    }




    private void saveObjectToNode(Node node, String key, Object value) throws Exception
    {
        if( value instanceof Map )
        {
            key = key.replace("/","");

            Node n = null;
            if( !node.hasNode(key) )
            {
                n = node.addNode(key, "nt:unstructured");
            }else{
                n = node.getNode(key);
            }

            Map m = (Map)value;
            for (Object mKey : m.keySet())
            {
                saveObjectToNode(n, mKey.toString(), m.get(mKey));
            }
        }
        else if( value instanceof Date)
        {
            Calendar c = Calendar.getInstance();
            c.setTime( (Date)value );
            node.setProperty(key, c );
        }
        else if( value instanceof Integer)
        {
            node.setProperty(key, ((Integer) value) );
        }
        else if( value instanceof Double)
        {
            node.setProperty(key, ((Double) value) );
        }
        else if( value instanceof Long)
        {
            node.setProperty(key, ((Long) value) );
        }
        else if( value instanceof Boolean)
        {
            node.setProperty(key, ((Boolean) value) );
        }
        else if( value instanceof String)
        {
            node.setProperty(key, ((String) value) );
        }
        else if( value instanceof String[])
        {
            String list = StringUtils.join(((String[]) value), ",");
            node.setProperty(key, list.toLowerCase() );
        }
        else if( value != null )
        {
            node.setProperty(key, value.toString() );
        }

    }


}