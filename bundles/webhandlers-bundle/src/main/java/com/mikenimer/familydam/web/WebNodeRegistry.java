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

package com.mikenimer.familydam.web;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;

/**
 * Created by mnimer on 2/25/14.
 */
@Component(enabled = true, immediate = true)
@Service(value = WebNodeRegistry.class)
public class WebNodeRegistry
{
    private final Logger log = LoggerFactory.getLogger(WebNodeRegistry.class);

    @Reference
    private Session session;

    @Activate
    protected void activate(ComponentContext context) throws Exception
    {
        log.debug("Activate WebNodeRegistry");


        NodeTypeManager manager = session.getWorkspace().getNodeTypeManager();

        /* Create node type */
        NodeTypeTemplate nodeType = manager.createNodeTypeTemplate();
        nodeType.setName("fd:facebook");
        nodeType.setMixin(true);
        nodeType.setQueryable(true);
        nodeType.setDeclaredSuperTypeNames(new String[]{"nt:base"});
        manager.registerNodeType(nodeType, true);
        NodeTypeIterator itr = manager.getAllNodeTypes();
    }


    @Deactivate
    protected void deactivate(ComponentContext componentContext) throws RepositoryException
    {
        log.debug("Deactivate WebNodeRegistry");
    }
}
