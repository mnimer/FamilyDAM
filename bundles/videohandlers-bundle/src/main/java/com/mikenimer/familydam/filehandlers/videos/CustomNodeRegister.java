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


package com.mikenimer.familydam.filehandlers.videos;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * User: mikenimer
 * Date: 11/25/13
 */
@Component(immediate = true)
public class CustomNodeRegister
{
    private final Logger log = LoggerFactory.getLogger(CustomNodeRegister.class);

    @Reference
    private Session session;


    @Activate
    protected void activate(ComponentContext context) throws Exception
    {

    }


    @Deactivate
    protected void deactivate(ComponentContext componentContext) throws RepositoryException
    {
        if (session != null)
        {
            session.logout();
            session = null;
        }
    }

    public static void RegisterCustomNodeTypes(Session session, String cndFileName)
            throws Exception
    {
        // Get the JackrabbitNodeTypeManager from the Workspace.
        // Note that it must be cast from the generic JCR NodeTypeManager to the
        // Jackrabbit-specific implementation.
        //NodeTypeManager manager = (NodeTypeManager) session.getWorkspace().getNodeTypeManager();
        // Register the custom node types defined in the CND file
        //manager.registerNodeTypes(new FileInputStream(cndFileName), NodeTypeManager.TEXT_X_JCR_CND);
    }

}
