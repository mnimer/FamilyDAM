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

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;

/**
 * User: mikenimer
 * Date: 11/17/13
 */
//@Component(enabled = true, immediate = true, metatype = true)
//@Service(value = JobConsumer.class)
//@Property(name = JobConsumer.PROPERTY_TOPICS, value = "familycloud/photos/thumbnails")
public class ThumbnailJob //implements JobConsumer
{
    private final Logger log = LoggerFactory.getLogger(ThumbnailJob.class);

    @Reference
    private SlingRepository repository;


    @Activate
    protected void activate(ComponentContext context) throws Exception
    {
        log.debug("Activate Thumbnail Job");
    }


    @Deactivate
    protected void deactivate(ComponentContext componentContext) throws RepositoryException
    {
        log.debug("Deactivate Thumbnail Job");
    }


    public Boolean process(String path)
    {
        //String path = (String) job.getProperty("path");

        try
        {
            Session session = repository.loginAdministrative(null);
            Node node = session.getRootNode().getNode(path);
            node.addMixin("fd:thumbnail");

            Node renditionFolder = node.addNode("renditions", "nt:folder");

            InputStream fileStream = node.getProperty("jcr:data").getBinary().getStream();
            BufferedImage bi = ImageIO.read(fileStream);

            BufferedImage scaledImage1 = progressiveResize(bi, 150, 150, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            BufferedImage scaledImage2 = progressiveResize(bi, 300, 300, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            // create thumbnail folder
            Node rNode = node.addNode("renditions");


            return true;//JobResult.OK;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return true;//JobResult.FAILED;
        }
    }


    private static BufferedImage progressiveResize(BufferedImage source,
                                                   int width, int height, Object hint)
    {
        int w = Math.max(source.getWidth() / 2, width);
        int h = Math.max(source.getHeight() / 2, height);
        BufferedImage img = commonResize(source, w, h, hint);
        while (w != width || h != height)
        {
            BufferedImage prev = img;
            w = Math.max(w / 2, width);
            h = Math.max(h / 2, height);
            img = commonResize(prev, w, h, hint);
            prev.flush();
        }
        return img;
    }


    private static BufferedImage commonResize(BufferedImage source,
                                              int width, int height, Object hint)
    {
        BufferedImage img = new BufferedImage(width, height, source.getType());
        Graphics2D g = img.createGraphics();
        try
        {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g.drawImage(source, 0, 0, width, height, null);
        }
        finally
        {
            g.dispose();
        }
        return img;
    }
}
