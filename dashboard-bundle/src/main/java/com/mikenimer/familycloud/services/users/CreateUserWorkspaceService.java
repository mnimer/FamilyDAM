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

package com.mikenimer.familycloud.services.users;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.post.SlingPostConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Created by mnimer on 12/19/13.
 */
@Component(immediate = true, metatype = false)
@Service(Servlet.class)
@Properties({@Property(name = "service.description", value = "Create new user and a workspace for the user"),
        @Property(name = "service.vendor", value = "The FamilyCloud Project"),
        @Property(name = "sling.servlet.paths", value = "/dashboard-api/user/workspace"),
        @Property(name="sling.servlet.methods",value={"POST"})
})
public class CreateUserWorkspaceService extends SlingAllMethodsServlet
{
    private final Logger log = LoggerFactory.getLogger(CreateUserWorkspaceService.class);


    @Activate
    protected void activate(ComponentContext ctx)
    {
        log.debug("Servlet started");
    }


    @Deactivate
    protected void deactivate(ComponentContext componentContext)
    {
        log.debug("Servlet Deactivated");
    }


    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse httpResponse) throws ServletException, IOException
    {
        // create Workspace
        String userName = request.getParameter(SlingPostConstants.RP_NODE_NAME);

        try
        {
            createWorkspace(request, userName);
        }catch(RepositoryException re ){
            throw new ServletException(re);
        }
    }


    protected void createWorkspace(SlingHttpServletRequest request, String username) throws RepositoryException
    {
        Session session = request.getResourceResolver().adaptTo(Session.class);
        session.getWorkspace().createWorkspace(username);
    }
}
