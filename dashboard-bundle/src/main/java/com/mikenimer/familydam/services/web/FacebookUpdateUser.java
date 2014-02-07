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

package com.mikenimer.familydam.services.web;

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
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.jcr.JsonJcrNode;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.security.auth.login.LoginContext;
import javax.servlet.Servlet;
import java.io.IOException;

/**
 * Created by mnimer on 2/5/14.
 */

@Component(immediate=true, metatype=false)
@Service(Servlet.class)
@Properties({ @Property(name="service.description", value="Save Facebook authentication data"),
        @Property(name="service.vendor", value="The FamilyDAM Project"),
        @Property(name="sling.servlet.paths", value="/dashboard-api/jobs/facebook")
})
public class FacebookUpdateUser extends SlingAllMethodsServlet
{
    private final Logger log = LoggerFactory.getLogger(FacebookUpdateUser.class);
    private Session session;
    private String _userPathRoot = "/apps/familydam/users/";

    @Reference
    private SlingRepository repository;

    @Activate
    protected void activate(ComponentContext ctx)  throws Exception
    {
        log.debug("Facebook Update User Servlet started");
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) throws RepositoryException
    {
        log.debug("Facebook Update User Servlet Deactivated");
    }


    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
    {
        String username = request.getParameter("username");
        String _userPath = _userPathRoot +username ;
    }



    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
    {

        try
        {
            String username = request.getParameter("username");
            String accessToken = request.getParameter("accessToken");
            String expiresIn = request.getParameter("expiresIn");
            String signedRequest = request.getParameter("signedRequest");
            String userId = request.getParameter("userId");
            String _userPath = _userPathRoot +username ;


            Credentials credentials = new SimpleCredentials("admin", "admin".toCharArray());
            Session session = repository.loginAdministrative(null);

            Node userNode = null;
            if( !session.nodeExists(_userPath) )
            {
                userNode = session.getNode(_userPathRoot).addNode(username);
                //session.save();
            }else{
                userNode = session.getNode(_userPath);
            }


            Node webNode = null;
            if( !session.nodeExists(_userPath +"/web") )
            {
                webNode = userNode.addNode("web");
                //session.save();
            }else{
                webNode = userNode.getNode("web");
            }


            Node FBNode = null;
            if( !webNode.hasNode("facebook") )
            {
                FBNode = webNode.addNode("facebook");
            }else{
                FBNode = webNode.getNode("facebook");
            }
            FBNode.setProperty("accessToken", accessToken);
            FBNode.setProperty("expiresIn", expiresIn);
            FBNode.setProperty("signedRequest", signedRequest);
            FBNode.setProperty("userId", userId);
            session.save();
        }
        catch( Exception re ){  //IOException,JSONException,RepositoryException
            re.printStackTrace();
            throw new RuntimeException(re);//TODO Throw a custom exception
        }
    }

}
