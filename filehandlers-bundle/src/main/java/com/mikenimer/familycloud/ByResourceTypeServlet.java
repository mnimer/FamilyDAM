/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.mikenimer.familycloud;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello World Servlet registered by resource type
 */
@Component
@Service(Servlet.class)
@Properties({ @Property(name="service.description", value="Add Hateoas Links to a resource"),
    @Property(name="service.vendor", value="The FamilyCloud Project"),
    @Property(name = "sling.servlet.resourceTypes", value = "nt:file"),
    @Property(name = "sling.servlet.selectors", value = "links"),
    @Property(name = "sling.servlet.extensions", value = "json")
})
public class ByResourceTypeServlet extends SlingSafeMethodsServlet {
    
    private final Logger log = LoggerFactory.getLogger(ByResourceTypeServlet.class);

    @Override
    protected void doGet(SlingHttpServletRequest request,
            SlingHttpServletResponse response) throws ServletException,
            IOException {

        Resource resource = request.getResource();

        //todo

        // forward along
        //request.getRequestDispatcher(resource).forward(request, response);
        return;
    }

}

