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
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.servlet.Servlet;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * User: mikenimer
 * Date: 11/19/13
 *
 Property(name="sling.servlet.resourceTypes", value="sling/servlet/default"),
 Property(name = "sling.servlet.selectors", value = "search"),
 Property(name = "sling.servlet.extensions", value = "json"),
 *
 *
 */
@Component(immediate=true, metatype=false)
@Service(Servlet.class)
@Properties({ @Property(name="service.description", value="Search Resource"),
        @Property(name="service.vendor", value="The FamilyCloud Project"),
        @Property(name="sling.servlet.paths", value="/dashboard-api/photos/search")
})
public class SearchResourcesServlet extends SlingSafeMethodsServlet
{
    private final Logger log = LoggerFactory.getLogger(SearchResourcesServlet.class);

    /** Used to format date values */
    private static final String ECMA_DATE_FORMAT = "EEE MMM dd yyyy HH:mm:ss 'GMT'Z";
    /** Used to format date values */
    private static final Locale DATE_FORMAT_LOCALE = Locale.US;

    private static final DateFormat CALENDAR_FORMAT = new SimpleDateFormat(ECMA_DATE_FORMAT, DATE_FORMAT_LOCALE);


    @Activate
    protected void activate(ComponentContext ctx) {
        //log.debug("Servlet started");
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) throws RepositoryException
    {
        //log.debug("Servlet Deactivated");
    }

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response) throws SlingServletException, IOException
    {
        String path = "/content/dam/photos";
        if( request.getRequestParameter("path") != null ){
            path = request.getRequestParameter("path").getString();
        }

        String keyword = null;
        if( request.getRequestParameter("keyword") != null )
        {
            keyword = request.getRequestParameter("keyword").getString();
        }

        int limit = 10;
        if( request.getRequestParameter("limit") != null ){
            limit = new Integer(request.getRequestParameter("limit").getString());
        }

        int offset = 1;
        if( request.getRequestParameter("offset") != null ){
            offset = new Integer(request.getRequestParameter("offset").getString());
        }

        boolean prettyJson = true;
        if( request.getRequestParameter("prettyJson") != null )
        {
            prettyJson = new Boolean(request.getRequestParameter("prettyJson").getString());
        }

        try
        {
            String stmt = "SELECT * FROM [fc:image] AS file INNER JOIN [nt:resource] as resource on ISCHILDNODE(resource, file)" +
                    " WHERE resource.[jcr:mimeType] like 'image/%'" +
                    " AND ISDESCENDANTNODE(file, '" +path +"')" +
                    " ORDER BY file.[fc:created] DESC";

            Session session = request.getResourceResolver().adaptTo(Session.class);
            Query query = session.getWorkspace().getQueryManager().createQuery(stmt, Query.JCR_SQL2);
            query.setLimit(limit);
            query.setOffset(offset);
            QueryResult results = query.execute();


            // build links
            String self = request.getPathInfo() +"?limit=" +limit +"&offset=" +offset;
            String next = request.getPathInfo() +"?limit=" +limit +"&offset=" +(offset + 1);
            String prev = null;
            if( offset > 1)
            {
                prev = request.getPathInfo() +"?keyword=" +keyword +"&limit=" +limit +"&offset=" +(offset - 1);
            }
            // set location header
            //response.setHeader("location", self);



            response.setContentType(request.getResponseContentType());
            response.setCharacterEncoding("UTF-8");
            try {
                final JSONWriter w = new JSONWriter(response.getWriter());

                w.setTidy(prettyJson);

                w.object();
                w.key("links");
                    w.object();
                        w.key("self").value(self);
                        w.key("next").value(next);
                        if( prev != null )
                        {
                            w.key("prev").value(prev);
                        }
                    w.endObject();
                w.key("data");
                    w.array();
                    NodeIterator nodeItr = results.getNodes();
                    while( nodeItr.hasNext() )
                    {
                        Node n = nodeItr.nextNode();
                        Resource resource = request.getResourceResolver().resolve(n.getPath());

                        //todo standardize this for all services that return an Image
                        w.object();
                        w.key("name").value(n.getName());
                        w.key("fc:created").value(n.getProperty("fc:created").getDate().getTime());
                        w.key("jcr:path").value(resource.getResourceMetadata().getResolutionPath());
                        w.key("jcr:uuid").value(n.getIdentifier());
                        w.key("jcr:primaryType").value(n.getPrimaryNodeType().getName());
                        w.key("jcr:contentType").value(resource.getResourceMetadata().getContentType());
                        w.key("jcr:contentLength").value(resource.getResourceMetadata().getContentLength());
                        w.key("jcr:created").value( CALENDAR_FORMAT.format(n.getProperty("jcr:created").getDate().getTime()) );
                        w.key("jcr:createdBy").value(n.getProperty("jcr:createdBy").getString());
                        w.key("links");
                            w.object();
                            w.key("self").value(n.getPath() + ".1.json");
                            w.key("image").value(n.getPath());
                            w.key("thumbnail").value(resource.getResourceMetadata().getResolutionPath() +".thumbnail.png");
                            w.endObject();
                        w.endObject();
                        //w.value(n);
                    }
                    w.endArray();
                w.endObject();
            } catch(JSONException je) {
                throw (IOException)new IOException("JSONException in doGet").initCause(je);
            } catch(Exception e) {
                throw (IOException)new IOException("General Exception in doGet").initCause(e);
            }


        }catch(RepositoryException re){
            re.printStackTrace();
            throw new SlingServletException(new javax.servlet.ServletException(re));
        }

        //request.getRequestDispatcher(request.getResource()).forward(request, response);
    }
}
