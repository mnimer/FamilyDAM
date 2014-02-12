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

import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

import javax.jcr.Node;
import javax.jcr.Value;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mnimer on 2/6/14.
 */
public class JsonToNode
{


    public Node convert(Node node, JSONObject json) throws JSONException
    {
        JSONArray jsonArray = json.names();
        for (int i = 0; i < jsonArray.length(); i++)
        {
            String key = jsonArray.getString(i);
            Object value = json.get(key);
            try
            {
                if (value instanceof JSONObject)
                {
                    Node n = null;
                    if( node.hasNode(key) )
                    {
                        n = node.getNode(key);
                    }else{
                        n = node.addNode(key);
                    }

                    convert(n, (JSONObject)value);
                }
                else if (value instanceof JSONArray)
                {
                    convert(node, key, (JSONArray)value);
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
            }catch(Exception ex){
                ex.printStackTrace();
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
                    n = node.addNode(key);

                    convert(n, (JSONObject)value);
                }
                else if (value instanceof JSONArray)
                {
                    //node = convert(node, key, (JSONArray)value);
                }
                /***
                else if (value instanceof Integer)
                {
                    if( list == null )
                    {
                        list = new Integer[json.length()];
                    }
                    int v = ((Integer) value).intValue();
                    //node.setProperty(key, v);
                }
                else if (value instanceof Float)
                {
                    if( list == null )
                    {
                        list = new Float[json.length()];
                    }
                    float v = ((Float) value).floatValue();
                    //node.setProperty(key, v);
                }
                else if (value instanceof Double)
                {
                    if( list == null )
                    {
                        list = new Double[json.length()];
                    }
                    double v = ((Double) value).doubleValue();
                    list[i] = value;
                }
                else if (value instanceof String)
                {
                    if( list == null )
                    {
                        list = new String[json.length()];
                    }
                    String v = value.toString();
                    list[i] = value;
                    //node.setProperty(key, (String[])list);
                }
                ***/

            }catch(Exception ex){
                ex.printStackTrace();
            }


        }

        return list;
    }
}
