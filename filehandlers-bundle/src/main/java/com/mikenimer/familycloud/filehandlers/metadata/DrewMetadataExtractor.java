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

package com.mikenimer.familycloud.filehandlers.metadata;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.mikenimer.familycloud.Constants;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * User: mikenimer
 * Date: 11/9/13
 */
public class DrewMetadataExtractor
{
    private final Logger log = LoggerFactory.getLogger(DrewMetadataExtractor.class);

    File file = null;
    InputStream fileStream = null;


    public DrewMetadataExtractor(File file)
    {
        this.file = file;
    }


    public DrewMetadataExtractor(InputStream file)
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
            result.put(Constants.ORIENTATION, 1);
            for (Directory directory : md.getDirectories())
            {
                if( directory instanceof ExifIFD0Directory)
                {
                    Integer orientation = directory.getInteger(ExifIFD0Directory.TAG_ORIENTATION);
                    result.put(Constants.ORIENTATION, orientation);
                }

                if(directory instanceof ExifSubIFDDirectory)
                {
                    Date dt = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                    result.put(Constants.DATETIME, dt);
                }


                Map tags = new HashMap();
                result.put(directory.getName(), tags);
                for (Tag tag : directory.getTags())
                {
                    Map<String, Object> tagMap = new HashMap();
                    tagMap.put(Constants.NAME, tag.getTagName());
                    tagMap.put(Constants.TYPE, tag.getTagType());
                    tagMap.put(Constants.VALUE, directory.getObject(tag.getTagType()));
                    tagMap.put(Constants.DESCRIPTION, tag.getDescription());

                    tags.put(tag.getTagName(), tagMap);
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
