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

import com.mikenimer.familydam.Constants;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.HashBag;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.SlingServletException;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
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
import java.util.Set;

@Component(immediate = true, metatype = false)
@Service(Servlet.class)
@Properties({@Property(name = "service.description", value = "Get metadata for node"),
        @Property(name = "service.vendor", value = "The FamilyDAM Project"),
        @Property(name = "sling.servlet.paths", value = "/dashboard-api/metadata/keywords")
})
public class MetadataKeywordsListServlet extends SlingAllMethodsServlet
{
    private final Logger log = LoggerFactory.getLogger(ImageDetailsServlet.class);


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


        try
        {
            Bag bag = new HashBag();


            String stmt = "select * from [" + Constants.NODE_CONTENT +"] WHERE ISDESCENDANTNODE([" +path +"])";

            Session session = request.getResourceResolver().adaptTo(Session.class);
            Query query = session.getWorkspace().getQueryManager().createQuery(stmt, Query.JCR_SQL2);
            //query.setLimit(limit);
            //query.setOffset(offset);
            QueryResult results = query.execute();

            // Execute the query and get the results ...
            // (This is the same as before.)
            //javax.jcr.QueryResult result = query.execute();

            NodeIterator nodeIterator = results.getNodes();
            while(nodeIterator.hasNext())
            {
                Node n = nodeIterator.nextNode();
                if( n.hasNode("metadata") )
                {
                    Node metaNode = n.getNode("metadata");
                    if( metaNode.hasProperty("keywords") )
                    {
                        String keywords = metaNode.getProperty("keywords").getString();
                        String[] keys = keywords.split(",");
                        for (String key : keys)
                        {
                            bag.add(key);
                        }
                    }
                }
            }


            Set set = bag.uniqueSet();

            // find scale ratio, we need a 1-12 range

            final JSONWriter w = new JSONWriter(response.getWriter());
            w.setTidy(true);
            w.array();
            for (Object word : set)
            {
                w.object();
                w.key("word").value(word.toString().toLowerCase());
                w.key("count").value(bag.getCount(word));
                w.key("size").value(Math.max(1.5, Math.min(5, bag.getCount(word) * .05)) +"rem");

                // todo try this.
                // $size = min(max(round(( $size_max*($count-$count_min))/($count_max-$count_min),2), $size_min),$size_max);
                w.endObject();
            }
            w.endArray();

            //writeJsonObject(w, tree);


        } catch (Exception re)
        {
            re.printStackTrace();
            throw new SlingServletException(new javax.servlet.ServletException(re));
        }
    }


}
