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

package com.mikenimer.familydam.services.photos;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.SlingServletException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceNotFoundException;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * User: mikenimer
 * Date: 11/20/13
 */

@Component(immediate=true, metatype=false)
@Service(Servlet.class)
@Properties({ @Property(name="service.description", value="Search Resource"),
        @Property(name="service.vendor", value="The FamilyDAM Project"),
        @Property(name="sling.servlet.resourceTypes", value="sling/servlet/default"),
        @Property(name = "sling.servlet.selectors", value = "scale"),
        @Property(name = "sling.servlet.extensions", value = "png")
})
public class ThumbnailService  extends SlingSafeMethodsServlet
{
    private final Logger log = LoggerFactory.getLogger(ThumbnailService.class);

    //todo: replace with osgi friendly fixed size caches
    private Map<String, Object> timeGeneratedCache;
    private Map<String, Object> imageCache;

    @Activate
    protected void activate(ComponentContext ctx) {
        log.debug("ThumbnailService started");

        imageCache = new HashMap<String, Object>();//CacheBuilder.newBuilder().maximumSize(1000).build();
        timeGeneratedCache = new HashMap<String, Object>();//CacheBuilder.newBuilder().maximumSize(10000).build();
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) throws RepositoryException
    {
        log.debug("ThumbnailService Deactivated");
    }

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response) throws SlingServletException, IOException
    {
        if (ResourceUtil.isNonExistingResource(request.getResource()))
        {
            throw new ResourceNotFoundException("No data to render.");
        }



        try
        {
            Session session = request.getResourceResolver().adaptTo(Session.class);
            Node parentNode = session.getNode(request.getResource().getPath());
            Resource resource = request.getResource();

            // first see if the etag header exists and we can skip processing.
            if (checkAndSetCacheHeaders(request, response, resource)) return;


            // check for a size selector
            int width = -1;
            int height = -1; //-1 means ignore and only set the width to resize
            final String[] selectors = request.getRequestPathInfo().getSelectors();
            if (selectors != null && selectors.length > 0)
            {
                final String selector = selectors[selectors.length - 1].toLowerCase().trim();
                if( selector.startsWith("w:") )
                {
                    width = Integer.parseInt(selector.substring(2));
                }
                if( selector.startsWith("h:") )
                {
                    height = Integer.parseInt(selector.substring(2));
                }
            }


            // check cache first
            if (checkForCachedImage(request, response)) return;



            //resize image
            //InputStream stream = node.getProperty("jcr:data").getBinary().getStream();
            InputStream stream = resource.adaptTo(InputStream.class);
            int orientation = 1;
            if (stream != null)
            {
                try
                {
                    Node n = parentNode.getNode("metadata");
                    if( n != null )
                    {
                        String _orientation = n.getProperty("orientation").getString();
                        if(StringUtils.isNumeric(_orientation))
                        {
                            orientation = new Integer(_orientation);
                        }
                    }
                }catch(Exception ex){

                }


                BufferedImage bi = ImageIO.read(stream);
                BufferedImage scaledImage = getScaledImage(bi, orientation, width, height);

                // cache the image
                long modTime = System.currentTimeMillis();
                imageCache.put(request.getPathInfo(), scaledImage);
                timeGeneratedCache.put(request.getPathInfo(), modTime);


                //return the image
                response.setContentType("image/png");
                response.setHeader(HttpConstants.HEADER_LAST_MODIFIED, new Long(modTime).toString());
                //write bytes and png thumbnail
                ImageIO.write(scaledImage, "png", response.getOutputStream());

                stream.close();
                response.getOutputStream().flush();
            }
        }catch( Exception ex ){

            // return original file.
            Resource resource = request.getResource();
            InputStream stream = resource.adaptTo(InputStream.class);
            response.setContentType(resource.getResourceMetadata().getContentType());
            response.setContentLength( new Long(resource.getResourceMetadata().getContentLength()).intValue() );

            //write bytes
            OutputStream out = response.getOutputStream();
            byte[] buffer = new byte[1024];
            while(true) {
                int bytesRead = stream.read(buffer);
                if (bytesRead < 0)
                    break;
                out.write(buffer, 0, bytesRead);
            }
            stream.close();
            response.getOutputStream().flush();

        }
    }


    /**
     * First check for the etag and see if we can return a 302 not modified response. If not, set the etag for next time.
     * @param request
     * @param response
     * @param resource
     * @return
     */
    private boolean checkAndSetCacheHeaders(SlingHttpServletRequest request, SlingHttpServletResponse response, Resource resource) {
        ResourceMetadata meta = resource.getResourceMetadata();
        String hash = new Integer(request.getPathInfo().hashCode()).toString();
        String eTag = request.getHeader(HttpConstants.HEADER_ETAG);
        if( hash.equals(eTag) )
        {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return true;
        }
        else
        {
            response.setHeader(HttpConstants.HEADER_ETAG, hash);
            response.setHeader("Cache-Control", "600");
        }
        return false;
    }


    /**
     * Check our memory cache and see if we can return the thumbnail from there
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    private boolean checkForCachedImage(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        BufferedImage cachedImage = (BufferedImage)imageCache.get(request.getPathInfo());
        if( cachedImage != null)
        {
            response.setContentType("image/png");
            //response.setContentLength( new Long(resource.getResourceMetadata().getContentLength()).intValue() );
            //write bytes
            ImageIO.write(cachedImage, "png", response.getOutputStream());
            response.getOutputStream().flush();
            return true;
        }
        return false;
    }


    /**
     * Resizes an image using a Graphics2D object backed by a BufferedImage.
     * @param src - source image to scale
     * @param w - desired width
     * @param h - desired height
     * @return - the new resized image
     */
    private BufferedImage getScaledImage(BufferedImage src, int orientation, int w, int h) throws Exception
    {
        int finalW = w;
        int finalH = h;

        if( h == -1 ) finalH = w;
        if( w == -1 ) finalW = h;

        double factor = 1.0d;
        if(src.getWidth() > src.getHeight()){
            factor = ((double)src.getHeight()/(double)src.getWidth());
            finalH = (int)(finalW * factor);
        }else{
            factor = ((double)src.getWidth()/(double)src.getHeight());
            finalW = (int)(finalH * factor);
        }

        BufferedImage scaledImage = new BufferedImage(finalW, finalH, src.getType());
        Graphics2D g = scaledImage.createGraphics();

        try
        {
            //AffineTransform at = AffineTransform.getScaleInstance((double) finalw / src.getWidth(), (double) finalh/ src.getHeight());
            AffineTransform at = getExifTransformation(orientation, (double)finalW , (double)finalH, (double) finalW / src.getWidth(), (double) finalH/ src.getHeight() );
            return transformImage(src, at);
            //g.drawRenderedImage(src, at);
            //return scaledImage;
        }
        finally
        {
            g.dispose();
        }



        /**
        BufferedImage resizedImg = new BufferedImage(finalw, finalh, src.getType());
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(src, 0, 0, finalw, finalh, null);
        g2.dispose();
        return resizedImg;
        **/
    }


    /**
     * get the right transformation based on the orientation setting in the exif metadata. In case the physical image is actually
     * stored in a rotated state.
     *
     * @param orientation
     * @param width
     * @param height
     * @return
     */
    public static AffineTransform getExifTransformation(int orientation, double width, double height, double scaleW, double scaleH) {

        AffineTransform t = new AffineTransform();

        switch (orientation) {
            case 1:
                t.scale(scaleW, scaleH);
                break;
            case 2: // Flip X //todo: test & fix
                t.scale(-scaleW, scaleH);
                t.translate(-width, 0);
                break;
            case 3: // PI rotation
                t.translate(width, height);
                t.rotate(Math.PI);
                t.scale(scaleW, scaleH);
                break;
            case 4: // Flip Y //todo: test & fix
                t.scale(scaleW, -scaleH);
                t.translate(0, -height);
                break;
            case 5: // - PI/2 and Flip X
                t.rotate(-Math.PI / 2);
                t.scale(-scaleW, scaleH);
                break;
            case 6: // -PI/2 and -width
                t.translate(height, 0);
                t.rotate(Math.PI / 2);
                t.scale(scaleW, scaleH);
                break;
            case 7: // PI/2 and Flip //todo:test & fix
                t.scale(-scaleW, scaleH);
                t.translate(-height, 0);
                t.translate(0, width);
                t.rotate(  3 * Math.PI / 2);
                break;
            case 8: // PI / 2
                t.translate(0, width);
                t.rotate(  3 * Math.PI / 2);
                t.scale(scaleW, scaleH);
                break;
        }

        return t;
    }


    /**
     * using the metadata orientation transformation information rotate the image.
     * @param image
     * @param transform
     * @return
     * @throws Exception
     */
    public static BufferedImage transformImage(BufferedImage image, AffineTransform transform) throws Exception {

        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);

        BufferedImage destinationImage = op.createCompatibleDestImage(image,  (image.getType() == BufferedImage.TYPE_BYTE_GRAY)? image.getColorModel() : null );
        Graphics2D g = destinationImage.createGraphics();
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, destinationImage.getWidth(), destinationImage.getHeight());
        destinationImage = op.filter(image, destinationImage);
        return destinationImage;
    }

}
