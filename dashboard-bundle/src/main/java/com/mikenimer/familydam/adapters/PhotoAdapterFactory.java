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

package com.mikenimer.familydam.adapters;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.adapter.AdapterFactory;

@Component(metatype = false, immediate = true)
@Service(value=org.apache.sling.api.adapter.AdapterFactory.class)
@Properties({
        @Property(name = AdapterFactory.ADAPTABLE_CLASSES, value = { "org.apache.sling.api.resource.Resource" }),
        @Property(name = AdapterFactory.ADAPTER_CLASSES, value = { "com.mikenimer.familydam.Photo" })
})
public class PhotoAdapterFactory implements AdapterFactory
{
    public PhotoAdapterFactory()
    {
    }


    public  <AdapterType> AdapterType getAdapter(final Object adaptable, Class<AdapterType> type)
    {
        return null;
    }
}