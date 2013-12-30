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

package com.mikenimer.familycloud.services.photos;

import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.SlingServletException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by mnimer on 12/5/13.
 */

@Component(immediate=true, metatype=false)
@Service(Servlet.class)
@Properties({ @Property(name="service.description", value="Get Resource By Id"),
        @Property(name="service.vendor", value="The FamilyCloud Project"),
        @Property(name="sling.servlet.paths", value="/dashboard-api/photo")
})
public class ImageDetailsServlet extends SlingSafeMethodsServlet
{
    private final Logger log = LoggerFactory.getLogger(ImageDetailsServlet.class);

    /** Used to format date values */
    private static final String ECMA_DATE_FORMAT = "EEE MMM dd yyyy HH:mm:ss 'GMT'Z";
    /** Used to format date values */
    private static final Locale DATE_FORMAT_LOCALE = Locale.US;

    private static final DateFormat CALENDAR_FORMAT = new SimpleDateFormat(ECMA_DATE_FORMAT, DATE_FORMAT_LOCALE);


    @Activate
    protected void activate(ComponentContext ctx) {
        log.debug("Servlet started");
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) throws RepositoryException
    {
        log.debug("Servlet Deactivated");
    }

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response) throws SlingServletException, IOException
    {
        if( request.getRequestParameter("uuid") == null ){
            throw new SlingServletException( new ServletException("Missing parameter UUID") );
        }
        String uuid = request.getRequestParameter("uuid").getString();


        boolean prettyJson = true;
        if( request.getRequestParameter("prettyJson") != null )
        {
            prettyJson = new Boolean(request.getRequestParameter("prettyJson").getString());
        }


        try
        {
            Session session = request.getResourceResolver().adaptTo(Session.class);
            Node node = session.getNodeByIdentifier(uuid);
            Resource resource = request.getResourceResolver().resolve(node.getPath());


            response.setHeader("location", resource.getPath());

            if( prettyJson )
            {
                //response.sendRedirect(resource.getPath() +".tidy.infinity.json");
                request.getRequestDispatcher(node.getPath() +".tidy.infinity.json").include(request, response);
            }else{
                //response.sendRedirect(resource.getPath() +".infinity.json");
                request.getRequestDispatcher(node.getPath() +".infinity.json").include(request, response);
            }



        }catch(Exception re){
            re.printStackTrace();
            throw new SlingServletException(new javax.servlet.ServletException(re));
        }

    }

}
