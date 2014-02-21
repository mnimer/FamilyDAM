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

package com.mikenimer.familydam.model;

import org.apache.felix.scr.annotations.Component;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.adapter.SlingAdaptable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: mikenimer
 * Date: 11/20/13
 */
@Component(metatype = false, immediate = true)
public class Photo extends SlingAdaptable implements Adaptable
{
    private final Logger log = LoggerFactory.getLogger(Photo.class);


    public Photo()
    {
        log.debug("default const.");
    }


    public Photo(Object adaptable)
    {
        log.debug(adaptable.toString());
    }


    @Override
    public <AdapterType> AdapterType adaptTo(Class<AdapterType> type)
    {
        return (AdapterType)new Photo();
    }
}
