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

import com.mikenimer.familydam.Constants;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyUnbounded;
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
import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
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
        @Property(name = "sling.servlet.paths", value = "/dashboard-api/search")
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
        System.out.println("SearchResourcesServlet Started");
        log.debug("SearchResourcesServlet started");
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
        Session session = request.getResourceResolver().adaptTo(Session.class);

        int limit = 20;
        if (request.getRequestParameter("limit") != null && request.getRequestParameter("limit").getSize() > 0)
        {
            limit = new Integer(request.getRequestParameter("limit").getString());
        }

        int offset = 1;
        if (request.getRequestParameter("offset") != null && request.getRequestParameter("offset").getSize() > 0)
        {
            offset = new Integer(request.getRequestParameter("offset").getString());
        }

        String filterPath = "/content/dam";
        if (request.getRequestParameter("filterPath") != null && request.getRequestParameter("filterPath").getSize() > 0)
        {
            filterPath = request.getRequestParameter("filterPath").getString();
        }

        String filterType = Constants.NODE_CONTENT;
        if (request.getRequestParameter("type") != null)
        {
            filterType = request.getRequestParameter("type").getString();
        }


        Date filterDateFrom = null;
        if (request.getRequestParameter("dateFrom") != null && request.getRequestParameter("dateFrom").getSize() > 0)
        {
            try
            {
                filterDateFrom = new Date(new Long(request.getRequestParameter("dateFrom").getString()).longValue());
            }
            catch (Exception ex)
            {
                try
                {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
                    filterDateFrom = formatter.parse(request.getRequestParameter("dateFrom").getString());
                }
                catch (ParseException pe)
                {
                }
            }

        }

        Date filterDateTo = null;
        if (request.getRequestParameter("dateTo") != null && request.getRequestParameter("dateTo").getSize() > 0)
        {
            try
            {
                filterDateTo = new Date(new Long(request.getRequestParameter("dateTo").getString()).longValue());
            }
            catch (Exception ex)
            {
                try
                {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
                    filterDateTo = formatter.parse(request.getRequestParameter("dateTo").getString());
                }
                catch (ParseException pe)
                {
                }
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
            /******************************
             * Build up JCR SQL
            ******************************/

            String stmt = "SELECT content.* FROM [" + filterType + "] AS content";

            if (filterType.equals(Constants.NODE_IMAGE) && filterTags != null && !filterTags.equals("undefined"))
            {
                stmt += " LeftOuter JOIN [nt:unstructured] as metadata on ISCHILDNODE(metadata, content) ";
                if (filterTags != null)
                {
                    stmt += " AND metadata.[keywords] like '%" + filterTags + "%'"; //todo support array of tags. with a "AND ( word or word )" statement
                }
            }

            stmt += " WHERE ISDESCENDANTNODE(content, '" + filterPath + "')";
            //stmt += " AND content.[type] = 'status'";

            if (filterDateFrom != null)
            {
                Calendar cal = Calendar.getInstance();
                cal.setTime(filterDateFrom);

                stmt += " AND content.[created_time] >= CAST('" + JCR_DATE_FORMATTER.format(cal.getTime()) + "' AS DATE)";
            }
            if (filterDateTo != null)
            {
                Calendar cal = Calendar.getInstance();
                cal.setTime(filterDateTo);

                stmt += " AND content.[created_time] <= CAST('" + JCR_DATE_FORMATTER.format(cal.getTime()) + "' AS DATE)";
            }

            stmt += " ORDER BY content.[fd:date] DESC";


            Query query = session.getWorkspace().getQueryManager().createQuery(stmt, Query.JCR_SQL2);
            query.setLimit(limit);
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

            // Convert the results to JSON and write it into the request output
            serializeJson(request, response, prettyJson, results, self, next, prev);


        }
        catch (RepositoryException re)
        {
            re.printStackTrace();
            throw new SlingServletException(new javax.servlet.ServletException(re));
        }

        //request.getRequestDispatcher(request.getResource()).forward(request, response);
    }


    private void serializeJson(SlingHttpServletRequest request, SlingHttpServletResponse response, boolean prettyJson, QueryResult results, String self, String next, String prev) throws IOException
    {
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
                Row row = nodeItr.nextRow();
                Node n = row.getNode();


                w.object();
                w.key("jcr:path").value(n.getPath());
                w.key("jcr:uuid").value(n.getIdentifier());

                try
                {
                    if (n.hasProperty(Constants.DATETIME))
                    {
                        w.key("jcr:created").value(CALENDAR_FORMATTER.format(n.getProperty(Constants.DATETIME).getDate().getTime()));
                    }
                    else if (n.hasProperty("jcr:created"))
                    {
                        w.key("jcr:created").value(CALENDAR_FORMATTER.format(n.getProperty("jcr:created").getDate().getTime()));
                    }
                    if (n.hasProperty("jcr:createdBy"))
                    {
                        w.key("jcr:createdBy").value(n.getProperty("jcr:createdBy").getString());
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                // Walk the NODE and serialize it into json node
                convertNodeToJson(w, n);


                // Add any Hateoas links
                w.key("links");
                    w.object();
                        w.key("self").value(n.getPath() + ".1.json");

                        if (n.isNodeType(Constants.NODE_IMAGE))
                        {
                            //n = row.getNode("file");
                            Resource resource = request.getResourceResolver().resolve(n.getPath());

                            w.key("image").value(resource.getPath());
                            w.key("thumbnail").value(resource.getResourceMetadata().getResolutionPath() + ".scale.w:200.png");
                        }
                        // for files with a nested image (like facebook photo)
                        else if( n.hasNode("file") && n.getNode("file").isNodeType(Constants.NODE_IMAGE))
                        {
                            Resource fileResource = request.getResourceResolver().resolve(n.getNode("file").getPath());
                            w.key("image").value(fileResource.getPath());
                            w.key("thumbnail").value(fileResource.getResourceMetadata().getResolutionPath() + ".scale.w:200.png");
                        }

                    w.endObject();
                w.endObject();
                //w.value(n);
            }
            w.endArray();
            w.endObject();
        }
        catch (JSONException je)
        {
            je.printStackTrace();
            throw (IOException) new IOException("JSONException in doGet").initCause(je);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw (IOException) new IOException("General Exception in doGet").initCause(e);
        }
    }


    /**
     * TODO move this into it's own class in Code
     * Recursively walk a node tree and convert it to a JSON object
     * @param w
     * @param n
     * @throws RepositoryException
     * @throws JSONException
     */
    private void convertNodeToJson(JSONWriter w, Node n) throws RepositoryException, JSONException
    {
        PropertyIterator propItr = n.getProperties();
        while (propItr.hasNext())
        {
            javax.jcr.Property prop = propItr.nextProperty();

            if (prop.getType() == PropertyType.DATE)
            {
                String dt = CALENDAR_FORMATTER.format(prop.getDate().getTime());
                w.key(prop.getName()).value(dt);
            }
            else if (prop.getType() == PropertyType.BOOLEAN)
            {
                w.key(prop.getName()).value(prop.getBoolean());
            }
            else if (prop.getType() == PropertyType.BINARY)
            {
                log.trace("binary data for {}", prop.getName());
                // todo: add flag to conditionally base64 encode this
                //w.key(prop.getName()).value(prop.getBoolean());
            }
            else if (prop.isMultiple())
            {
                w.key(prop.getName());
                w.array();
                Value[] values = prop.getValues();
                for (int i = 0; i < values.length; i++)
                {
                    Value _v = values[i];
                    w.value(_v.getString());
                }
                w.endArray();
            }
            else
            {
                w.key(prop.getName()).value(prop.getString());
            }
        }


        // follow child nodes
        NodeIterator nodeItr = n.getNodes();
        while (nodeItr.hasNext())
        {
            Node _node = nodeItr.nextNode();
            w.key(_node.getName());
            w.object();
            convertNodeToJson(w, _node);
            w.endObject();
        }
    }
}
