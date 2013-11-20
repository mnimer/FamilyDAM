package com.mikenimer.familycloud;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.SlingServletException;
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
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * User: mikenimer
 * Date: 11/19/13
 */
@Component(immediate=true, metatype=false)
@Service(Servlet.class)
@Properties({ @Property(name="service.description", value="Search Resource"),
        @Property(name="service.vendor", value="The FamilyCloud Project"),
        @Property(name="sling.servlet.resourceTypes", value="sling/servlet/default"),
        @Property(name = "sling.servlet.selectors", value = "s"),
        @Property(name = "sling.servlet.extensions", value = "json")
})
public class SearchResourcesServlet extends SlingSafeMethodsServlet
{
    private final Logger log = LoggerFactory.getLogger(SearchResourcesServlet.class);

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
        String basePath = request.getRequestParameter("basePath").getString();
        String keyword = request.getRequestParameter("keyword").getString();
        int limit = new Integer(request.getRequestParameter("limit").getString());
        int offset = new Integer(request.getRequestParameter("offset").getString());

        try
        {
            String stmt = "select * from [nt:file] where isdescendantnode('" +basePath +"')"; //order by jcr:created desc

            Session session = request.getResourceResolver().adaptTo(Session.class);
            Query query = session.getWorkspace().getQueryManager().createQuery(stmt, Query.JCR_SQL2);
            query.setLimit(limit);
            query.setOffset(offset);
            QueryResult results = query.execute();



            /********
            int maxRecursionLevels = 0;
            final String[] selectors = request.getRequestPathInfo().getSelectors();
            if (selectors != null && selectors.length > 0) {
                final String level = selectors[selectors.length - 1];
                if (level.equalsIgnoreCase("infinity")) {
                    maxRecursionLevels = -1;
                }
                else if( StringUtils.isNumeric(level) )
                {
                    maxRecursionLevels = Integer.parseInt(level);
                }
                else{
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            "Invalid recursion selector value '" + level + "'");
                    return;
                }
            }
             **********/

            response.setContentType(request.getResponseContentType());
            response.setCharacterEncoding("UTF-8");
            try {
                final JSONWriter w = new JSONWriter(response.getWriter());
                w.setTidy(false);
                w.array();

                NodeIterator nodeItr = results.getNodes();
                while( nodeItr.hasNext() )
                {
                    Node n = nodeItr.nextNode();
                    w.value(n);
                }
                w.endArray();
            } catch(JSONException je) {
                throw (IOException)new IOException("JSONException in doGet").initCause(je);
            }


        }catch(RepositoryException re){
            throw new SlingServletException(new javax.servlet.ServletException(re));
        }

        //request.getRequestDispatcher(request.getResource()).forward(request, response);
    }
}
