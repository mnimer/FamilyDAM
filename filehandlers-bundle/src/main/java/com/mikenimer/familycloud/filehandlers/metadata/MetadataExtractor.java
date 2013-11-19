package com.mikenimer.familycloud.filehandlers.metadata;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * User: mikenimer
 * Date: 11/9/13
 */
public class MetadataExtractor
{
    private final Logger log = LoggerFactory.getLogger(MetadataExtractor.class);

    File file = null;
    InputStream fileStream = null;


    public MetadataExtractor(File file)
    {
        this.file = file;
    }


    public MetadataExtractor(InputStream file)
    {
        fileStream = new BufferedInputStream(file);
    }


    public Map getMetadata() throws IOException, ImageProcessingException
    {
        try
        {
            Metadata md = null;
            if (file != null)
            {
                try
                {
                    // first try with the drew metadata library
                    md = ImageMetadataReader.readMetadata(file);
                }
                catch (ImageProcessingException ipe)
                {
                    //alternative approach
                    ImageInputStream iis = ImageIO.createImageInputStream(file);
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
                        md = new Metadata();
                        for (int i = 0; i < length; i++)
                        {

                            System.out.println("Format name: " + names[i]);
                            System.out.println((metadata.getAsTree(names[i])).getNodeValue());
                        }

                    }

                }
            }
            else if (fileStream != null)
            {
                md = ImageMetadataReader.readMetadata(new BufferedInputStream(fileStream), true);
            }


            Map result = new HashMap();
            for (Directory directory : md.getDirectories())
            {
                Map tags = new HashMap();
                result.put(directory.getName(), tags);
                for (Tag tag : directory.getTags())
                {
                    tags.put(tag.getTagName(), tag.getDescription());
                    if (tag.getTagName().contains("Date/Time Original"))
                    {
                        try
                        {
                            SimpleDateFormat df = new SimpleDateFormat("yyyy:MM:ddd HH:mm:ss");
                            Date dt = df.parse(tag.getDescription());
                            result.put("DATETIME", dt);
                        }
                        catch (ParseException parseException)
                        {
                            //swallow
                        }
                    }
                }
            }

            return result;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }
}
