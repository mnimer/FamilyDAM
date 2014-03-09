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


package com.mikenimer.familydam;

import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.LoginException;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeDefinition;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.nodetype.PropertyDefinitionTemplate;

/**
 * User: mikenimer
 * Date: 11/25/13
 */
public class CustomNodeRegister implements BundleListener, BundleActivator
{
    private final Logger log = LoggerFactory.getLogger(CustomNodeRegister.class);

    @Reference
    private Session session;

    @Reference
    private SlingRepository repository;


    @Override
    public void start(BundleContext bundleContext) throws Exception
    {
        bundleContext.addBundleListener(this);
        repository = ((SlingRepository) bundleContext.getService(bundleContext.getServiceReference("org.apache.sling.jcr.api.SlingRepository")));
    }


    @Override
    public void stop(BundleContext bundleContext) throws Exception
    {
        bundleContext.removeBundleListener(this);
    }


    @Override
    public void bundleChanged(BundleEvent bundleEvent)
    {
        log.trace(bundleEvent.getBundle().toString());

        if (bundleEvent.getType() == BundleEvent.STARTED)
        {
            //log.trace("started");
            this.checkNodeTypes();
        }
    }


    protected void checkNodeTypes()
    {
        try
        {
            session = repository.loginAdministrative(null);
            if (repository.getDescriptor(Repository.OPTION_OBSERVATION_SUPPORTED).equals("true"))
            {
                NodeTypeManager manager = session.getWorkspace().getNodeTypeManager();


                /**
                 @see http://jackrabbit.510166.n4.nabble.com/How-to-add-ChildNodeDefinition-to-NodeTypeTemplate-while-creating-a-custom-NodeType-using-NodeTypeMa-td4657488.html
                 **/
                checkContentNode(manager);
                checkTwitterNode(manager);
                checkFacebookNode(manager);

            }
        }catch(LoginException le){
            log.error(le.getMessage(), le);
        }catch(RepositoryException re){
            log.error(re.getMessage(), re);
        }


    }


    private void checkContentNode(NodeTypeManager manager)
    {
        try
        {
            NodeTypeTemplate node = manager.createNodeTypeTemplate();
            node.setName(Constants.NODE_CONTENT);
            node.setMixin(true);
            node.setQueryable(true);

            /* Create a new property */
            PropertyDefinitionTemplate customProperty = manager.createPropertyDefinitionTemplate();
            customProperty.setName(Constants.DATETIME);
            customProperty.setRequiredType(PropertyType.DATE);
            /* Add property to node type */
            node.getPropertyDefinitionTemplates().add(customProperty);

            manager.registerNodeType(node, true);
            NodeTypeIterator nti = manager.getMixinNodeTypes();
        }
        catch (RepositoryException re){
            log.error("Unable to create node types", re);
        }
    }


    private void checkFacebookNode(NodeTypeManager manager)
    {
        try
        {
            NodeTypeTemplate node = manager.createNodeTypeTemplate();
            node.setName(Constants.NODE_FACEBOOK);
            node.setMixin(true);
            //node.setPrimaryItemName(NodeType.NT_UNSTRUCTURED);
            //fbNode.setQueryable(true);
            //node.setDeclaredSuperTypeNames(new String[]{"nt:base"});
            manager.registerNodeType(node, true);
            NodeTypeIterator nti = manager.getMixinNodeTypes();
        }
        catch (RepositoryException re){
            log.error("Unable to create node types", re);
        }
    }


    private void checkTwitterNode(NodeTypeManager manager)
    {
        try
        {
            NodeTypeTemplate node = manager.createNodeTypeTemplate();
            node.setName(Constants.NODE_TWITTER);
            node.setMixin(true);
            //node.setPrimaryItemName(NodeType.NT_UNSTRUCTURED);
            //fbNode.setQueryable(true);
            //node.setDeclaredSuperTypeNames(new String[]{"nt:base"});
            manager.registerNodeType(node, true);
            NodeTypeIterator nti = manager.getMixinNodeTypes();
        }
        catch (RepositoryException re){
            log.error("Unable to create node types", re);
        }
    }
}
