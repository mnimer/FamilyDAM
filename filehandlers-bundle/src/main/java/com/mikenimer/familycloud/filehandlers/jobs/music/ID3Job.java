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

package com.mikenimer.familycloud.filehandlers.jobs.music;

import com.drew.imaging.ImageProcessingException;
import com.mikenimer.familycloud.Constants;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses out the ID3 metadata from uploaded mp3 files.
 *
 * Created by mnimer on 1/25/14.
 */

@Component(enabled = true, immediate = true)
@Service(value=JobConsumer.class)
@Property(name= JobConsumer.PROPERTY_TOPICS, value="familydam/music/metadata")
public class ID3Job implements JobConsumer
{
    private final Logger log = LoggerFactory.getLogger(ID3Job.class);

    @Reference
    private SlingRepository repository;

    @Activate
    protected void activate(ComponentContext context) throws Exception
    {
        log.debug("Activate ID3 Job");
    }


    @Deactivate
    protected void deactivate(ComponentContext componentContext) throws RepositoryException
    {
        log.debug("Deactivate ID3 Job");
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
        try
        {
            Map<String, Object> metadata = extractMetadata(node);

            if( metadata != null )
            {
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
        Mp3File mp3file = null;

        InputStream stream = node.getSession().getNode(node.getPrimaryItem().getPath()).getProperty("jcr:data").getBinary().getStream();
        Map<String, Object> md = new HashMap<String, Object>();

        //Create temp file for the mp3parser to open
        File tmpFile = File.createTempFile(node.getIdentifier(), ".mp3");
        // write the inputStream to a FileOutputStream
        OutputStream outputStream = new FileOutputStream(tmpFile);

        int read = 0;
        byte[] bytes = new byte[1024];

        while ((read = stream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, read);
        }

        tmpFile.deleteOnExit();

        try
        {
            mp3file = new Mp3File(tmpFile.getAbsolutePath());
            if (mp3file.hasId3v1Tag())
            {

                ID3v1 id3v1Tag = mp3file.getId3v1Tag();
                md.put(Constants.TITLE, id3v1Tag.getTitle());
                md.put(Constants.TRACK, id3v1Tag.getTrack());
                md.put(Constants.ARTIST, id3v1Tag.getArtist());
                md.put(Constants.ALBUM, id3v1Tag.getAlbum());
                md.put(Constants.YEAR, id3v1Tag.getYear());
                md.put(Constants.GENRE_CODE, id3v1Tag.getGenre());
                md.put(Constants.GENRE, id3v1Tag.getGenreDescription());
                md.put(Constants.COMMENT, id3v1Tag.getComment());
                md.put(Constants.VERSION, id3v1Tag.getVersion());
            }


            if (mp3file.hasId3v2Tag()) {
                ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                md.put(Constants.TITLE, id3v2Tag.getTitle());
                md.put(Constants.TRACK, id3v2Tag.getTrack());
                md.put(Constants.ARTIST, id3v2Tag.getArtist());
                md.put(Constants.ALBUM, id3v2Tag.getAlbum());
                md.put(Constants.ALBUM_ARTIST, id3v2Tag.getAlbumArtist());
                md.put(Constants.ALBUM_IMAGE, id3v2Tag.getAlbumImage());
                md.put(Constants.ALBUM_IMAGE_MIMETYPE, id3v2Tag.getAlbumImageMimeType());
                md.put(Constants.YEAR, id3v2Tag.getYear());
                md.put(Constants.GENRE_CODE, id3v2Tag.getGenre());
                md.put(Constants.GENRE, id3v2Tag.getGenreDescription());
                md.put(Constants.COMMENT, id3v2Tag.getComment());
                md.put(Constants.VERSION, id3v2Tag.getVersion());
                md.put(Constants.COMMENT, id3v2Tag.getComment());
                md.put(Constants.CHAPTERS, id3v2Tag.getChapters());
                md.put(Constants.CHAPTER_TOC, id3v2Tag.getChapterTOC());
                md.put(Constants.COMPOSER, id3v2Tag.getComposer());
                md.put(Constants.COPYRIGHT, id3v2Tag.getCopyright());
                md.put(Constants.ENCODER, id3v2Tag.getEncoder());
                md.put(Constants.COMMENT, id3v2Tag.getComment());
                md.put(Constants.ITUNES_COMMENT, id3v2Tag.getItunesComment());
                md.put(Constants.ORIGINAL_ARTIST, id3v2Tag.getOriginalArtist());
                md.put(Constants.PUBLISHER, id3v2Tag.getPublisher());
            }

            return md;
        }
        catch(UnsupportedTagException ute)
        {
            //todo
        }
        catch(InvalidDataException ide)
        {
            //todo
        }
        finally
        {
            if( tmpFile != null )
            {
                tmpFile.delete();
            }
        }

        return null;
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
