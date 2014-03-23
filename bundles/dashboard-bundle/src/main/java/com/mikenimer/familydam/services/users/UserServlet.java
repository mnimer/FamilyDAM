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

package com.mikenimer.familydam.services.users;

import com.mikenimer.familydam.mappers.JsonToNode;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;

/**
 * Created by mnimer on 2/15/14.
 */

@Component(immediate=true, metatype=false)
@Service(Servlet.class)
@Properties({ @Property(name="service.description", value=""),
        @Property(name="service.vendor", value="The FamilyDAM Project"),
        @Property(name="sling.servlet.paths", value="/dashboard-api/users")
})
public class UserServlet extends SlingAllMethodsServlet
{

    private final Logger log = LoggerFactory.getLogger(UserServlet.class);
    private String USERPATH = "/apps/familydam/users/";
    private Session session;

    @Reference
    private SlingRepository repository;

    @Activate
    protected void activate(ComponentContext ctx)  throws Exception
    {
        log.debug("User Servlet started");
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) throws RepositoryException
    {
        log.debug("User Servlet Deactivated");
    }


    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
    {
        String username = request.getParameter(":name");

        try
        {
            String nodePath = USERPATH +username;
            session = request.getResourceResolver().adaptTo(Session.class);

            Node node = node = session.getNode(nodePath);
            request.getRequestDispatcher(node.getPath() +".infinity.json").include(request, response);
        }
        catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }


    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
    {
        String jsonStr = request.getParameter(":content");
        String username = request.getParameter(":name");
        String nodePath = USERPATH +username;

        try
        {
            if( username!=null && username.length() > 0)
            {
                session = request.getResourceResolver().adaptTo(Session.class);
                Node node = session.getNode(nodePath);

                JSONObject jsonObj = new JSONObject(jsonStr);
                new JsonToNode().convert(node, jsonObj, null);
                session.save();
            }
        }
        catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
