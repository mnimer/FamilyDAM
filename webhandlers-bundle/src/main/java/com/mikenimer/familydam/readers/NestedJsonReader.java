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

package com.mikenimer.familydam.readers;

import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.jcr.contentloader.internal.ContentCreator;
import org.apache.sling.jcr.contentloader.internal.ContentReader;
import org.apache.sling.jcr.contentloader.internal.ImportProvider;
import org.apache.sling.jcr.contentloader.internal.readers.JsonReader;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

/**
 * Created by mnimer on 2/11/14.
 */
public class NestedJsonReader extends JsonReader
{

    public static final ImportProvider PROVIDER = new ImportProvider() {
        private NestedJsonReader jsonReader;

        public ContentReader getReader() {
            if (jsonReader == null) {
                jsonReader = new NestedJsonReader();
            }
            return jsonReader;
        }
    };

    @Override
    protected void createProperty(String name, Object value, ContentCreator contentCreator) throws JSONException, RepositoryException
    {
        // assume simple value
        if (value instanceof JSONArray) {
            // multivalue
            final JSONArray array = (JSONArray) value;
            if (array.length() > 0)
            {
                final String values[] = new String[array.length()];
                for (int i = 0; i < array.length(); i++)
                {
                    if( array.get(i) instanceof JSONObject )
                    {
                        contentCreator.createNode( name, NodeType.NT_UNSTRUCTURED, null);
                    }
                    else
                    {
                        values[i] = array.get(i).toString();
                    }
                }
                final int propertyType = getType(name, array.get(0));
                contentCreator.createProperty(getName(name), propertyType, values);
            }
            else {
                contentCreator.createProperty(getName(name), PropertyType.STRING, new String[0]);
            }

        } else {
            // single value
            final int propertyType = getType(name, value);
            contentCreator.createProperty(getName(name), propertyType, value.toString());
        }
    }
}
