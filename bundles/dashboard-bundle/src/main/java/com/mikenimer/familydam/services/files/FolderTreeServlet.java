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


package com.mikenimer.familydam.services.files;

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
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.servlet.Servlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mnimer on 1/7/14.
 */
@Component(immediate = true, metatype = false)
@Service(Servlet.class)
@Properties({@Property(name = "service.description", value = "Search Resource"),
        @Property(name = "service.vendor", value = "The FamilyDAM Project"),
        @Property(name = "sling.servlet.paths", value = "/dashboard-api/files/foldertree")
})
public class FolderTreeServlet extends SlingSafeMethodsServlet
{
    private final Logger log = LoggerFactory.getLogger(FolderTreeServlet.class);
    private Session session;


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
        String path = "/content/dam";
        if (request.getRequestParameter("path") != null)
        {
            path = request.getRequestParameter("path").getString();
        }
        session = request.getResourceResolver().adaptTo(Session.class);

        //String stmt = "SELECT folders.* FROM [sling:Folder] AS folder WHERE ISDESCENDANTNODE(folder, '" +path +"') ";
        //Query query = session.getWorkspace().getQueryManager().createQuery(stmt, Query.JCR_SQL2);
        //QueryResult results = query.execute();

        response.setContentType(request.getResponseContentType());
        response.setCharacterEncoding("UTF-8");

        try
        {
            Map tree = loadNode(path);

            //JSONObject jsonObject = new JSONObject(tree);
            //response.getWriter().print(jsonObject.toString());
            //jsonObject.write(response.getWriter());

            final JSONWriter w = new JSONWriter(response.getWriter());
            w.setTidy(true);
            writeJsonObject(w, tree);

        } catch (Exception e)
        {
            throw (IOException) new IOException("General Exception in doGet").initCause(e);
        }

    }

    private void writeJsonObject(JSONWriter w, Map val) throws JSONException
    {
        w.object();
        for (Object key : val.keySet())
        {
            if( val.get(key) instanceof String )
            {
                w.key((String)key).value(val.get(key));
            }else if( val.get(key) instanceof List )
            {
                List children = (List)val.get(key);
                JSONWriter cObj = w.key("children");
                cObj.array();
                for (Object child : children)
                {
                    writeJsonObject(w, (Map)child);
                }
                cObj.endArray();
            }
        }
        w.endObject();
    }


    /**
     * recursively walk the tree and get each child folder.
     *
     * @param path
     * @return
     */
    private Map loadNode(Object path)
    {
        try
        {
            Node rootNode;
            if (path instanceof String)
            {
                rootNode = session.getNode((String) path);
            } else
            {
                rootNode = (Node) path;
            }


            Map paths = new HashMap();
            List children = new ArrayList();
            paths.put("name", rootNode.getName());
            paths.put("path", rootNode.getPath());
            paths.put("children", children);


            NodeIterator nodes = rootNode.getNodes();
            while (nodes.hasNext())
            {
                Node n = nodes.nextNode();
                if (n.getPrimaryNodeType().isNodeType(NodeType.NT_FOLDER))
                {
                    children.add(loadNode(n.getPath()));
                }
            }

            return paths;
        } catch (PathNotFoundException pe)
        {
        } catch (Exception e)
        {
        }
        return null;
    }
}
