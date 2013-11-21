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
            Long modIfTime = (Long)timeGeneratedCache.get(request.getPathInfo());
            if( modIfTime != null )
            {
                if (unmodified(request, modIfTime)) {
                    response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    return;
                }
            }


            // check for a size selector
            int width = 200;
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
                response.setContentLength( new Long(resource.getResourceMetadata().getContentLength()).intValue() );
                //write bytes
                ImageIO.write(cachedImage, "png", response.getOutputStream());
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
                imageCache.put(request.getPathInfo(), scaledImage);
                timeGeneratedCache.put(request.getPathInfo(), System.currentTimeMillis());


                response.setContentType("image/png");
                response.setContentLength( new Long(resource.getResourceMetadata().getContentLength()).intValue() );
                //write bytes
                ImageIO.write(scaledImage, "png", response.getOutputStream());
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

}
