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

package com.mikenimer.familycloud.services;

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
import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
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
        @Property(name="service.vendor", value="The FamilyCloud Project"),
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

            //Session session = request.getResourceResolver().adaptTo(Session.class);
            //Node parentNode = session.getNode(request.getResource().getPath());
            //Node node = session.getNode(parentNode.getPrimaryItem().getPath());

        Resource resource = request.getResource();

        try
        {
            ResourceMetadata meta = resource.getResourceMetadata();
            String hash = new Integer(request.getPathInfo().hashCode()).toString();
            String eTag = request.getHeader(HttpConstants.HEADER_ETAG);
            if( hash.equals(eTag) )
            {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
            else
            {
                response.setHeader(HttpConstants.HEADER_ETAG, hash);
                response.setHeader("Cache-Control", "600");
            }

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
            BufferedImage cachedImage = (BufferedImage)imageCache.get(request.getPathInfo());
            if( cachedImage != null)
            {
                response.setContentType("image/png");
                //response.setContentLength( new Long(resource.getResourceMetadata().getContentLength()).intValue() );
                //write bytes
                ImageIO.write(cachedImage, "png", response.getOutputStream());
                response.getOutputStream().flush();
                return;
            }


            //resize image
            //InputStream stream = node.getProperty("jcr:data").getBinary().getStream();
            InputStream stream = resource.adaptTo(InputStream.class);
            if (stream != null)
            {
                BufferedImage bi = ImageIO.read(stream);
                //BufferedImage bi = ImageIO.read( new URL("http://localhost:8080/content/dam/photos/2013/2013-10-13/IMG_4830.JPG") );
                //BufferedImage bi = ImageIO.read(resource.adaptTo(URL.class));
                BufferedImage scaledImage = getScaledImage(bi, width, height);

                // cache the image
                long modTime = System.currentTimeMillis();
                imageCache.put(request.getPathInfo(), scaledImage);
                timeGeneratedCache.put(request.getPathInfo(), modTime);


                response.setContentType("image/png");
                response.setHeader(HttpConstants.HEADER_LAST_MODIFIED, new Long(modTime).toString());
                //response.setContentLength( new Long(resource.getResourceMetadata().getContentLength()).intValue() );
                //write bytes
                ImageIO.write(scaledImage, "png", response.getOutputStream());

                stream.close();
                response.getOutputStream().flush();
            }
        }catch( Exception ex ){

            // return original file.
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
     * Resizes an image using a Graphics2D object backed by a BufferedImage.
     * @param src - source image to scale
     * @param w - desired width
     * @param h - desired height
     * @return - the new resized image
     */
    private BufferedImage getScaledImage(BufferedImage src, int w, int h){
        int finalw = w;
        int finalh = h;

        if( h == -1 ) finalh = w;
        if( w == -1 ) finalw = h;

        double factor = 1.0d;
        if(src.getWidth() > src.getHeight()){
            factor = ((double)src.getHeight()/(double)src.getWidth());
            finalh = (int)(finalw * factor);
        }else{
            factor = ((double)src.getWidth()/(double)src.getHeight());
            finalw = (int)(finalh * factor);
        }

        BufferedImage scaledImage = new BufferedImage(finalw, finalh, src.getType());
        Graphics2D g = scaledImage.createGraphics();

        try
        {
            AffineTransform at = AffineTransform.getScaleInstance((double) finalw / src.getWidth(), (double) finalh/ src.getHeight());
            g.drawRenderedImage(src, at);
            return scaledImage;
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
     * copied from: org.apache.sling.servlets.get.impl.helpers.StreamRendererServlet
     *
     * Returns <code>true</code> if the request has a
     * <code>If-Modified-Since</code> header whose date value is later than the
     * last modification time given as <code>modifTime</code>.
     *
     * @param request The <code>ComponentRequest</code> checked for the
     *            <code>If-Modified-Since</code> header.
     * @param modifTime The last modification time to compare the header to.
     * @return <code>true</code> if the <code>modifTime</code> is less than or
     *         equal to the time of the <code>If-Modified-Since</code> header.
     */
    private boolean unmodified(HttpServletRequest request, Long modIfTime) {
        if (modIfTime > 0) {
            long modTime = modIfTime / 1000; // seconds
            long ims = request.getDateHeader(HttpConstants.HEADER_IF_MODIFIED_SINCE) / 1000;
            return modTime <= ims;
        }

        // we have no modification time value, assume modified
        return false;
    }



    // Look at http://chunter.tistory.com/143 for information
    public static AffineTransform getExifTransformation(int orientation, int width, int height) {

        AffineTransform t = new AffineTransform();

        switch (orientation) {
            case 1:
                break;
            case 2: // Flip X
                t.scale(-1.0, 1.0);
                t.translate(-width, 0);
                break;
            case 3: // PI rotation
                t.translate(width, height);
                t.rotate(Math.PI);
                break;
            case 4: // Flip Y
                t.scale(1.0, -1.0);
                t.translate(0, -height);
                break;
            case 5: // - PI/2 and Flip X
                t.rotate(-Math.PI / 2);
                t.scale(-1.0, 1.0);
                break;
            case 6: // -PI/2 and -width
                t.translate(height, 0);
                t.rotate(Math.PI / 2);
                break;
            case 7: // PI/2 and Flip
                t.scale(-1.0, 1.0);
                t.translate(-height, 0);
                t.translate(0, width);
                t.rotate(  3 * Math.PI / 2);
                break;
            case 8: // PI / 2
                t.translate(0, width);
                t.rotate(  3 * Math.PI / 2);
                break;
        }

        return t;
    }


    public static BufferedImage transformImage(BufferedImage image, AffineTransform transform) throws Exception {

        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);

        BufferedImage destinationImage = op.createCompatibleDestImage(image,  (image.getType() == BufferedImage.TYPE_BYTE_GRAY)? image.getColorModel() : null );
        Graphics2D g = destinationImage.createGraphics();
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, destinationImage.getWidth(), destinationImage.getHeight());
        destinationImage = op.filter(image, destinationImage);;
        return destinationImage;
    }

}
