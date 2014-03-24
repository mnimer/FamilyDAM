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

import com.mikenimer.familydam.Constants;
import com.mikenimer.familydam.MimeTypeManager;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.value.BinaryImpl;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.commons.mime.MimeTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.IOUtils;

import javax.imageio.ImageIO;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeType;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by mnimer on 2/6/14.
 */
@Component(enabled = true, immediate = true)
public class JsonToNode
{
    private final Logger log = LoggerFactory.getLogger(JsonToNode.class);

    private List ignorableFields = new ArrayList();

    @Reference
    public MimeTypeManager mimeTypeManager;

    public JsonToNode()
    {
        ignorableFields.add("jcr:primaryType");
        ignorableFields.add("jcr:mixinTypes");
        ignorableFields.add("jcr:created");
        ignorableFields.add("jcr:createdBy");
        ignorableFields.add(":name");
    }


    public Node convert(Node node, JSONObject json, String type) throws JSONException
    {
        JSONArray jsonArray = json.names();
        for (int i = 0; i < jsonArray.length(); i++)
        {
            String key = jsonArray.getString(i);
            Object value = json.get(key);
            try
            {
                if (ignorableFields.indexOf(key) == -1)
                {
                    if (value instanceof JSONObject)
                    {
                        Node n = JcrUtils.getOrAddNode(node, key);
                        convert(n, (JSONObject) value, type);
                    }
                    else if (value instanceof JSONArray)
                    {
                        for (int j = 0; j < ((JSONArray) value).length(); j++)
                        {
                            JSONObject object = (JSONObject) ((JSONArray) value).get(j);

                            if (key.equalsIgnoreCase("media"))
                            {
                                Node media = node.addNode(key, NodeType.NT_FILE);
                                media.addMixin(Constants.NODE_CONTENT);
                                //convert(media, object, type);
                                String src = object.getString("src");
                                loadMedia(media, object, src, type);
                            }
                            else
                            {
                                Node aNode = node.addNode(key);
                                convert(aNode, object, type);
                            }
                        }

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
                log.error("unable to update node: " + key + "with value=" + value, ex);
            }

        }


        return node;
    }


    public Object[] convert(Node node, String key, JSONArray json, String type) throws JSONException
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
                    n = JcrUtils.getOrAddNode(node, key, NodeType.NT_UNSTRUCTURED);

                    convert(n, (JSONObject) value, type);
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


    private void loadMedia(Node media, JSONObject object, String src, String type) throws JSONException
    {
        if( object.getString("type").equalsIgnoreCase("photo") )
        {
            loadPhotos(media, object, src, type);
        }
        else if( object.getString("type").equalsIgnoreCase("video") )
        {
            loadVideo(media, object, src, type);
        }
    }


    private void loadVideo(Node media, JSONObject object, String src, String type)
    {
        // pull URL and save the source image and an embedded URL
        Binary videoBinary = null;

        try
        {
            String _originalSource = src.replace("_n.", "_o.").replace("_s.", "_o.");
            String _videoSource = object.getJSONObject("video").getString("source_url");

            URL url = new URL(_videoSource);


            // read video stream
            ByteArrayOutputStream bais = new ByteArrayOutputStream();
            InputStream is = null;
            try {
                is = url.openStream ();
                byte[] byteChunk = new byte[4096];
                int n;

                while ( (n = is.read(byteChunk)) > 0 ) {
                    bais.write(byteChunk, 0, n);
                }
            }
            catch (IOException e) {
                //System.err.printf ("Failed while reading bytes from %s: %s", url.toExternalForm(), e.getMessage());
            }
            finally {
                if (is != null) { is.close(); }
            }

            
            videoBinary = new BinaryImpl(bais.toByteArray());
            //ValueFactory valueFactory = session.getValueFactory();
            //Binary contentValue = valueFactory.createBinary(is);


            if (videoBinary != null)
            {
                // create file node
                //Node sourceNode = JcrUtils.getOrAddNode(media, "file", NodeType.NT_FILE);
                media.addMixin("mix:referenceable");
                media.addMixin(Constants.NODE_CONTENT);
                media.addMixin(Constants.NODE_MOVIE);

                //Try to figure out the mime type
                String _mimeType = MimeTypeManager.getMimeType(_videoSource);

                Node contentNode = JcrUtils.getOrAddNode(media, "jcr:content", NodeType.NT_RESOURCE);
                contentNode.setProperty("jcr:mimeType", _mimeType);
                contentNode.setProperty("jcr:data", videoBinary);
                Calendar lastModified = Calendar.getInstance();
                lastModified.setTimeInMillis(lastModified.getTimeInMillis());
                contentNode.setProperty("jcr:lastModified", lastModified);
                //contentNode.setProperty("jcr:lastModified", Calendar.getInstance());
                //contentNode.setProperty("jcr:uuid", UUID.randomUUID().toString() );

            }
            else
            {
                log.warn("unable to load: " + src);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }


    private void loadPhotos(Node media, JSONObject object, String src, String type)
    {
        // pull URL and save the source image and an embedded URL
        BufferedImage bufferedImage = null;
        Binary imageBinary = null;

        try
        {
            String _originalSource = src.replace("_n.", "_o.").replace("_s.", "_o.");

            URL url = new URL(_originalSource);
            bufferedImage = ImageIO.read(url);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", os);
            InputStream is = new ByteArrayInputStream(os.toByteArray());
            imageBinary = new BinaryImpl(os.toByteArray());
            //ValueFactory valueFactory = session.getValueFactory();
            //Binary contentValue = valueFactory.createBinary(is);


            if (imageBinary != null)
            {
                // create file node
                //Node sourceNode = JcrUtils.getOrAddNode(media, "file", NodeType.NT_FILE);
                media.addMixin("mix:referenceable");
                media.addMixin(Constants.NODE_CONTENT);
                media.addMixin(Constants.NODE_IMAGE);

                //Try to figure out the mime type
                String _mimeType = MimeTypeManager.getMimeType(_originalSource);

                Node contentNode = JcrUtils.getOrAddNode(media, "jcr:content", NodeType.NT_RESOURCE);
                contentNode.setProperty("jcr:mimeType", _mimeType);
                contentNode.setProperty("jcr:data", imageBinary);
                Calendar lastModified = Calendar.getInstance();
                lastModified.setTimeInMillis(lastModified.getTimeInMillis());
                contentNode.setProperty("jcr:lastModified", lastModified);
                //contentNode.setProperty("jcr:lastModified", Calendar.getInstance());
                //contentNode.setProperty("jcr:uuid", UUID.randomUUID().toString() );

            }
            else
            {
                log.warn("unable to load: " + src);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

}
