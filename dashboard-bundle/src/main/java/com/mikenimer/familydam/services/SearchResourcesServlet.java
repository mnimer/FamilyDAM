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


package com.mikenimer.familydam.services;

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
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;
import javax.servlet.Servlet;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * User: mikenimer
 * Date: 11/19/13
 * <p/>
 * Property(name="sling.servlet.resourceTypes", value="sling/servlet/default"),
 * Property(name = "sling.servlet.selectors", value = "search"),
 * Property(name = "sling.servlet.extensions", value = "json"),
 */
@Component(immediate = true, metatype = false)
@Service(Servlet.class)
@Properties({@Property(name = "service.description", value = "Search Resource"),
        @Property(name = "service.vendor", value = "The FamilyDAM Project"),
        @Property(name = "sling.servlet.paths", value = "/dashboard-api/photos/search")
})
public class SearchResourcesServlet extends SlingSafeMethodsServlet
{
    private final Logger log = LoggerFactory.getLogger(SearchResourcesServlet.class);

    /**
     * Used to format date values
     */
    private static final String ECMA_DATE_FORMAT = "EEE MMM dd yyyy HH:mm:ss 'GMT'Z";
    private static final String JCR_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    /**
     * Used to format date values
     */
    private static final Locale DATE_FORMAT_LOCALE = Locale.US;

    private static final DateFormat CALENDAR_FORMATTER = new SimpleDateFormat(ECMA_DATE_FORMAT, DATE_FORMAT_LOCALE);
    private static final DateFormat JCR_DATE_FORMATTER = new SimpleDateFormat(JCR_DATE_FORMAT, DATE_FORMAT_LOCALE);


    @Activate
    protected void activate(ComponentContext ctx)
    {
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
        int limit = 100;
        if (request.getRequestParameter("limit") != null && request.getRequestParameter("limit").getSize() > 0)
        {
            limit = new Integer(request.getRequestParameter("limit").getString());
        }

        int offset = 1;
        if (request.getRequestParameter("offset") != null && request.getRequestParameter("offset").getSize() > 0)
        {
            offset = new Integer(request.getRequestParameter("offset").getString());
        }

        String filterPath = "/content/dam/photos";
        if (request.getRequestParameter("filterPath") != null && request.getRequestParameter("filterPath").getSize() > 0)
        {
            filterPath = request.getRequestParameter("filterPath").getString();
        }

        Date filterDateFrom = null;
        if (request.getRequestParameter("dateFrom") != null && request.getRequestParameter("dateFrom").getSize() > 0)
        {
            try
            {
                filterDateFrom = new Date(  new Long(request.getRequestParameter("dateFrom").getString()).longValue()  );
            }catch(Exception ex){
                try
                {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
                    filterDateFrom = formatter.parse( request.getRequestParameter("dateFrom").getString() );
                }
                catch(ParseException pe){}
            }

        }

        Date filterDateTo = null;
        if (request.getRequestParameter("dateTo") != null && request.getRequestParameter("dateTo").getSize() > 0)
        {
            try
            {
                filterDateTo = new Date(  new Long(request.getRequestParameter("dateTo").getString()).longValue()  );
            }catch(Exception ex){
                try
                {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
                    filterDateTo = formatter.parse( request.getRequestParameter("dateTo").getString() );
                }
                catch(ParseException pe){}
            }
        }

        String filterTags = null;
        if (request.getRequestParameter("tags") != null && request.getRequestParameter("tags").getSize() > 0)
        {
            filterTags = request.getRequestParameter("tags").getString();
        }

        boolean prettyJson = true;
        if (request.getRequestParameter("prettyJson") != null)
        {
            prettyJson = new Boolean(request.getRequestParameter("prettyJson").getString());
        }

        try
        {
            String stmt = "SELECT file.* FROM [fd:image] AS file" +
                    " INNER JOIN [nt:resource] as resource on ISCHILDNODE(resource, file)" +
                    " INNER JOIN [nt:unstructured] as metadata on ISCHILDNODE(metadata, file)";
            stmt += " WHERE ISDESCENDANTNODE(file, '" + filterPath + "')";

            if (filterTags != null)
            {
                stmt += " AND metadata.[keywords] like '%" + filterTags + "%'"; //todo support array of tags. with a "AND ( word or word )" statement
            }
            if (filterDateFrom != null)
            {
                Calendar cal = Calendar.getInstance();
                cal.setTime(filterDateFrom);

                stmt += " AND file.[created] >= CAST('" + JCR_DATE_FORMATTER.format(cal.getTime()) + "' AS DATE)";
            }
            if (filterDateTo != null)
            {
                Calendar cal = Calendar.getInstance();
                cal.setTime(filterDateTo);

                stmt += " AND file.[created] <= CAST('" + JCR_DATE_FORMATTER.format(cal.getTime()) + "' AS DATE)";
            }

            stmt += " ORDER BY file.[created] DESC";

            Session session = request.getResourceResolver().adaptTo(Session.class);
            Query query = session.getWorkspace().getQueryManager().createQuery(stmt, Query.JCR_SQL2);
            //query.setLimit(limit);
            //query.setOffset(offset);
            QueryResult results = query.execute();


            // build links
            String self = request.getPathInfo() + "?limit=" + limit + "&offset=" + offset + "&filterPath=" + filterPath + "&dateFrom=" + filterDateFrom + "&dateTo=" + filterDateTo + "&tags=" + filterTags;
            String next = request.getPathInfo() + "?limit=" + limit + "&offset=" + (offset + 1) + "&filterPath=" + filterPath + "&dateFrom=" + filterDateFrom + "&dateTo=" + filterDateTo + "&tags=" + filterTags;
            String prev = null;
            if (offset > 1)
            {
                prev = request.getPathInfo() + "?limit=" + limit + "&offset=" + (offset - 1) + "&filterPath=" + filterPath + "&dateFrom=" + filterDateFrom + "&dateTo=" + filterDateTo + "&tags=" + filterTags;
            }
            // set location header
            response.setHeader("location", self);
            response.setContentType(request.getResponseContentType());
            response.setCharacterEncoding("UTF-8");


            try
            {
                final JSONWriter w = new JSONWriter(response.getWriter());

                w.setTidy(prettyJson);

                w.object();
                w.key("links");
                w.object();
                w.key("self").value(self);
                w.key("next").value(next);
                if (prev != null)
                {
                    w.key("prev").value(prev);
                }
                w.endObject();
                w.key("data");
                w.array();
                RowIterator nodeItr = results.getRows();
                while (nodeItr.hasNext())
                {
                    Node n = nodeItr.nextRow().getNode("file");
                    Resource resource = request.getResourceResolver().resolve(n.getPath());

                    //todo standardize this for all services that return an Image
                    w.object();
                    w.key("name").value(n.getName());
                    if (n.hasProperty("created"))
                    {
                        w.key("created").value(n.getProperty("created").getDate().getTime());
                    } else
                    {
                        w.key("created").value(n.getProperty("jcr:created").getDate().getTime());
                    }
                    w.key("jcr:path").value(resource.getResourceMetadata().getResolutionPath());
                    w.key("jcr:uuid").value(n.getIdentifier());
                    w.key("jcr:primaryType").value(n.getPrimaryNodeType().getName());
                    w.key("jcr:contentType").value(resource.getResourceMetadata().getContentType());
                    w.key("jcr:contentLength").value(resource.getResourceMetadata().getContentLength());
                    w.key("jcr:created").value(CALENDAR_FORMATTER.format(n.getProperty("jcr:created").getDate().getTime()));
                    w.key("jcr:createdBy").value(n.getProperty("jcr:createdBy").getString());
                    w.key("metadata");
                    w.object();
                            try
                            {
                                //todo remove
                                String dt = n.getNode("metadata").getProperty("__DATETIME").getString();
                                if( dt != null )
                                {
                                    w.key("datetime").value(dt);
                                }
                            } catch (Exception e1) {}
                    /**
                            try
                            {
                                //w.key("created").value(n.getNode("metadata").getProperty("dateTaken").getString());
                            } catch (Exception e1) {}
                     **/
                            try
                            {
                                String tags = n.getNode("metadata").getProperty("keywords").getString();
                                if( tags != null )
                                {
                                    w.key("keywords").value(tags);
                                }
                            } catch (Exception e1) {}
                    w.endObject();
                    w.key("links");
                    w.object();
                    w.key("self").value(n.getPath() + ".1.json");
                    w.key("image").value(n.getPath());
                    w.key("thumbnail").value(resource.getResourceMetadata().getResolutionPath() + ".scale.w:200.png");
                    w.endObject();
                    w.endObject();
                    //w.value(n);
                }
                w.endArray();
                w.endObject();
            } catch (JSONException je)
            {
                je.printStackTrace();
                throw (IOException) new IOException("JSONException in doGet").initCause(je);
            } catch (Exception e)
            {
                e.printStackTrace();
                throw (IOException) new IOException("General Exception in doGet").initCause(e);
            }


        } catch (RepositoryException re)
        {
            re.printStackTrace();
            throw new SlingServletException(new javax.servlet.ServletException(re));
        }

        //request.getRequestDispatcher(request.getResource()).forward(request, response);
    }
}
