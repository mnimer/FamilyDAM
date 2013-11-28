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

package com.mikenimer.familycloud.filehandlers.jobs.images;

import com.drew.imaging.ImageProcessingException;
import com.mikenimer.familycloud.Constants;
import com.mikenimer.familycloud.filehandlers.metadata.DrewMetadataExtractor;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.jcr.resource.JcrModifiablePropertyMap;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * User: mikenimer
 * Date: 11/17/13
 */
@Component(enabled = true, immediate = true)
//Service(value=JobConsumer.class)
//Property(name=JobConsumer.PROPERTY_TOPICS, value="familycloud/photos/metadata")
public class MetadataJob //implements JobConsumer
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


    /**
    public Boolean process(String path)
    {
        Session session = repository.loginAdministrative(null);
        Node node = session.getRootNode().getNode(path);
        Map<String, Object> md = process(node);
        return (md != null);  //JobConsumer.JobResult.OK;
    }
     **/



    public Map<String, Object> process(Node node, boolean persist)
    {
        Calendar dtCal = Calendar.getInstance();
        try
        {
            Map<String, Object> metadata = extractMetadata(node);


            // Pull out the date/time of the original photo from the EXIF data and update system date for the file.
            Date datetime = (Date) metadata.get(Constants.DATETIME);
            // if an original date  doesn't exist we'll set fc:Created to now.
            if( datetime != null )
            {
                dtCal.setTime(datetime);
            }
            node.setProperty("fc:created", dtCal);

            if( persist )
            {
                saveObjectToNode(node, "fc:metadata", metadata);
                node.getSession().save();
            }

            // after saving the metadata with the node, return the result
            return metadata;

            /***
             *  try
             {
             md.remove("__DATETIME");// remove the extra property we passed back
             md.remove("__ORIENTATION");// remove the extra property we passed back

             saveObjectToNode(node, "fc:metadata", md);
             }catch(Exception ex){
             // swallow
             ex.printStackTrace();
             }
             session.save();

             */
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            return null;
        }

    }



    private Map<String, Object> extractMetadata(Node node) throws RepositoryException, IOException, ImageProcessingException
    {
        InputStream fileStream = node.getSession().getNode(node.getPrimaryItem().getPath()).getProperty("jcr:data").getBinary().getStream();
        Map<String, Object> md = new HashMap<String, Object>();


        try
        {
            // first try with the drew metadata library
            DrewMetadataExtractor drewMetadataExtractor = new DrewMetadataExtractor(fileStream);
            md = drewMetadataExtractor.getMetadata();

            // todo add Apache Commons Image library, as a backup
        }
        catch (ImageProcessingException ipe)
        {
            //alternative approach
            ImageInputStream iis = ImageIO.createImageInputStream(fileStream);
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

            //todo, find the "Date/Time Original" property in this metdata
        }

        return md;
    }



    private void saveObjectToNode(Node node, String key, Object value) throws Exception
    {
        if( value instanceof Map )
        {
            key = key.replace("/","");
            Node n = node.addNode(key, "nt:unstructured");
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
        else if( value != null )
        {
            node.setProperty(key, value.toString() );
        }

    }


}