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

package com.mikenimer.familydam.mappers;

import org.apache.felix.scr.annotations.Component;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mnimer on 2/6/14.
 */
@Component(enabled = true, immediate = true)
public class JsonToNode
{
    private final Logger log = LoggerFactory.getLogger(JsonToNode.class);

    private List ignorableFields = new ArrayList();


    public JsonToNode()
    {
        ignorableFields.add("jcr:primaryType");
        ignorableFields.add("jcr:mixinTypes");
        ignorableFields.add("jcr:created");
        ignorableFields.add("jcr:createdBy");
        ignorableFields.add(":name");
    }


    public Node convert(Node node, JSONObject json) throws JSONException
    {
        JSONArray jsonArray = json.names();
        for (int i = 0; i < jsonArray.length(); i++)
        {
            String key = jsonArray.getString(i);
            Object value = json.get(key);
            try
            {
                if( ignorableFields.indexOf(key) == -1 )
                {
                    if (value instanceof JSONObject)
                    {
                        Node n = JcrUtils.getOrAddNode(node, key);
                        convert(n, (JSONObject) value);
                    }
                    else if (value instanceof JSONArray)
                    {
                        convert(node, key, (JSONArray) value);
                    }
                    else if (value instanceof Integer)
                    {
                        int v = ((Integer) value).intValue();
                        node.setProperty(key, v);
                    }
                    else if (value instanceof Float)
                    {
                        float v = ((Float) value).floatValue();
                        node.setProperty(key, v);
                    }
                    else if (value instanceof Double)
                    {
                        double v = ((Double) value).doubleValue();
                        node.setProperty(key, v);
                    }
                    else if (value instanceof String)
                    {
                        String v = value.toString();
                        node.setProperty(key, v);
                    }
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                log.error("unable to update node: " +key +"with value=" +value, ex);
            }

        }


        return node;
    }


    public Object[] convert(Node node, String key, JSONArray json) throws JSONException
    {
        Object[] list = null;
        for (int i = 0; i < json.length(); i++)
        {
            // we'll use the 1st item to determine the array type

            Object value = json.get(i);
            try
            {
                if (value instanceof JSONObject)
                {

                    Node n = null;
                    n = JcrUtils.getOrAddNode(node, key);

                    convert(n, (JSONObject) value);
                }
                else if (value instanceof JSONArray)
                {
                    // multivalue
                    final JSONArray array = (JSONArray) value;

                    final String values[] = new String[array.length()];
                    for (int j = 0; j < array.length(); j++)
                    {
                        values[j] = array.get(j).toString();
                    }

                    node.setProperty(key, values);
                }
                else
                {
                    node.setProperty(key, value.toString());
                }


            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }


        }

        return list;
    }
}
