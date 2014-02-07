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
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.SlingServletException;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import java.io.IOException;

/**
 * Created by mnimer on 2/5/14.
 */

@Component(immediate=true, metatype=false)
@Service(Servlet.class)
@Properties({ @Property(name="service.description", value="Save Facebook authentication data"),
        @Property(name="service.vendor", value="The FamilyDAM Project"),
        @Property(name="sling.servlet.paths", value="/dashboard-api/facebook/callback")
})
public class FacebookCallback  extends SlingSafeMethodsServlet
{
    private final Logger log = LoggerFactory.getLogger(FacebookCallback.class);

    @Activate
    protected void activate(ComponentContext ctx) {
        log.debug("Facebook Callback Servlet started");
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) throws RepositoryException
    {
        log.debug("Facebook Callback Servlet Deactivated");
    }

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response) throws SlingServletException, IOException
    {

        //String accessToken = request.getParameter("accessToken");
        //String expiresIn = request.getParameter("expiresIn");
        //String signedRequest = request.getParameter("signedRequest");
        //String userId = request.getParameter("userId");



    }

}
