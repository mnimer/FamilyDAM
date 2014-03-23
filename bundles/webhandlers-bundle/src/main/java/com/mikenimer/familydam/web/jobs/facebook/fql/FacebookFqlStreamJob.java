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

package com.mikenimer.familydam.web.jobs.facebook.fql;

import com.mikenimer.familydam.Constants;
import com.mikenimer.familydam.mappers.JsonToNode;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeType;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Created by mnimer on 2/6/14.
 */
@Component(enabled = true, immediate = true)
@Service(value = JobConsumer.class)
@Property(name = JobConsumer.PROPERTY_TOPICS, value = "familydam/web/facebook/fql/stream")
public class FacebookFqlStreamJob extends FacebookFqlJob
{
    public static String TOPIC = "familydam/web/facebook/fql/stream";
    public static String FACEBOOKFOLDERPATH = "/content/dam/web/facebook/{1}/stream/{2}";
    public static String FACEBOOKNODEPATH = "/content/dam/web/facebook/{1}/stream/{2}/{3}";

    private final Logger log = LoggerFactory.getLogger(FacebookFqlStreamJob.class);
    private Session session;

    @Reference
    protected JobManager jobManager;


    @Reference
    public SlingRepository repository;



    @Activate
    protected void activate(ComponentContext context) throws Exception
    {
        log.debug("Activate FacebookFqlStreamJob Job");
    }


    @Deactivate
    protected void deactivate(ComponentContext componentContext) throws RepositoryException
    {
        log.debug("Deactivate FacebookFqlStreamJob Job");
    }


    @Override
    protected JobResult queryFacebook(Job job, Node facebookData, String username, String nodePath, String nextUrl) throws RepositoryException, IOException, JSONException
    {
        String accessToken = facebookData.getProperty("accessToken").getString();
        String expiresIn = facebookData.getProperty("expiresIn").getString();
        String signedRequest = facebookData.getProperty("signedRequest").getString();

        if (!facebookData.hasProperty("userID"))
        {
            return JobResult.FAILED;
        }

        String created_time = null;
        try
        {
            created_time = job.getProperty("created_time").toString();
        }catch(Exception pe ){
            //swallow, we need to find a better way to do a contains.
        }

        String userId = "'" + facebookData.getProperty("userID").getString() + "'"; //with escapes
        String _url = nextUrl;

        if (nextUrl == null)
        {
            String fql = "SELECT type,post_id,actor_id,source_id,target_id,message,message_tags,place,created_time,description,call_to_action,action_links,attachment\n" +
                    " FROM stream " +
                    " WHERE source_id = " + userId +" ";
            if( created_time != null )
            {
                fql += " AND created_time < '" +created_time +"'";
            }
                fql += " limit 100";

            _url = "https://graph.facebook.com/fql?q=" + URLEncoder.encode(fql, "UTF-8") + "&access_token=" + accessToken;
        }
        URL url = new URL(_url);

        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(_url);
        int statusCode = client.executeMethod(method);

        if (statusCode != HttpStatus.SC_OK)
        {
            return JobResult.FAILED;
        }

        // Create default Albums Node as Sling:Folder, if it doesn't exist
        String facebookFolderPath = FACEBOOKFOLDERPATH.replace("{1}", username);
        Session session = repository.loginAdministrative(null);
        JcrUtils.getOrCreateByPath(facebookFolderPath, "sling:Folder", session);


        // Read the response body.
        String jsonStr = method.getResponseBodyAsString();
        //System.out.println(jsonStr);

        JSONObject jsonObj = new JSONObject(jsonStr);
        return saveData(job, username, jsonObj);
    }


    private JobResult saveData(Job job, String username, JSONObject jsonObject)
    {

        try
        {
            session = repository.loginAdministrative(null);

            // TODO add support for calling the next set in the history, based on created_time

            if (jsonObject.has("data"))
            {
                JSONArray statusList = jsonObject.getJSONArray("data");
                Integer oldest_created_time = null;

                for (int i = 0; i < statusList.length(); i++)
                {
                    JSONObject post = (JSONObject) statusList.get(i);
                    Integer ctDate = post.getInt("created_time");

                    if( oldest_created_time == null || oldest_created_time > ctDate)
                    {
                        oldest_created_time = ctDate;
                    }

                    if( post.get("type") != JSONObject.NULL )
                    {
                        if (post.getInt("type") == 46) //56 write on a wall
                        {
                            persistSingleStatusNode(username, "status", post);
                        }
                        else if (post.getInt("type") == 128)
                        {
                            persistSingleStatusNode(username, "video", post);
                        }
                        else if (post.getInt("type") == 247)
                        {
                            persistSingleStatusNode(username, "photo", post);
                        }
                        else if (post.getInt("type") == 285)
                        {
                            persistSingleStatusNode(username, "checkin", post);
                        }
                        else
                        { //65 - tagged in a photo
                            log.trace("skip facebook type:" + post.getInt("type"));
                        }
                    }
                }


                if( oldest_created_time != null )
                {
                    Map props = extractJobProperties(job);
                    props.put("created_time", oldest_created_time);
                    // Run immediately
                    Job streamJob = jobManager.addJob("familydam/web/facebook/fql/stream", props);
                }
            }

            return JobResult.OK;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return JobResult.FAILED;
        }
    }



    public synchronized void persistSingleStatusNode(String username, String type, JSONObject post) throws JSONException, ParseException, IOException, RepositoryException
    {
        // Not thread safe, so we'll recreate
        //SimpleDateFormat facebookDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+SSSS");
        //SimpleDateFormat facebookDateFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+Z");

        // Delete nodes we don't need to store
        cleanPacket(post);

        // pull out keys
        String id = post.getString("post_id");
        String timestamp = post.getString("created_time");

        Date dateCreated = new Date();
        if (timestamp != null && timestamp.length() > 0)
        {
            try
            {
                dateCreated = new Date( new Long(timestamp).longValue()*1000 );//facebookDateFormat.parse(timestamp);
            }
            catch (NumberFormatException nfe)
            {
                nfe.getMessage();
            }
        }

        //pull out the year so we can group posts by year
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateCreated);
        String year = new Integer(calendar.get(Calendar.YEAR)).toString();


        // default path
        String folderPath = FACEBOOKFOLDERPATH.replace("{1}", username).replace("{2}", year);
        String nodePath = FACEBOOKNODEPATH.replace("{1}", username).replace("{2}", year).replace("{3}", id);
        if (true)// !session.nodeExists(nodePath) )
        {
            // first check folders to node
            Node folder = JcrUtils.getOrCreateByPath(folderPath, "sling:Folder", session);

            // delete the old node if it exists.
            if( folder.hasNode(id) )
            {
                folder.getNode(id).remove();
                session.save();
            }

            // create a new node
            Node node = JcrUtils.getOrCreateByPath(nodePath, NodeType.NT_UNSTRUCTURED, session);

            // add some FamilyDam specific properties
            node.addMixin(Constants.NODE_CONTENT);
            node.addMixin(Constants.NODE_FACEBOOK);

            // convert the json to a node properties
            // AND load any media binary data
            new JsonToNode().convert(node, post, type);

            node.setProperty("type", type);
            try
            {
                // override the unix timestamp string
                node.setProperty(Constants.DATETIME, calendar);
            }
            catch (Exception e)
            {
                e.getMessage();
            }

            Map location = null;//todo, query by place id - checkForLocation(post);
            if (location != null)
            {
                node.addMixin(Constants.NODE_GEOSTAMP);
                node.setProperty("latitude", (Double) location.get("latitude"));
                node.setProperty("longitude", (Double) location.get("longitude"));
            }



            // Save the new session, before we check for the photo
            try
            {
                session.save();
                //versionManager.checkin(node.getPath());
            }
            catch (ConstraintViolationException cve)
            {
                cve.getMessage();
                try
                { // hack to avoid this error: mandatory property {http://www.jcp.org/jcr/1.0}data does not exist
                    Thread.sleep(1000);
                    session.save();
                }catch (ConstraintViolationException cve2){
                    // still can't save it
                    cve2.printStackTrace();
                }catch (Exception ex){
                    // do nothing
                    ex.printStackTrace();
                }
            }

            //checkForPhoto(node, post);
            //versionManager.checkin(node.getPath());


        }
    }



    /**
     * TODO: Use this FQL
     * @see : https://developers.facebook.com/docs/reference/fql/photo/

    SELECT type, post_id,actor_id,source_id,target_id,message,message_tags, place,created_time,description, call_to_action, action_links, attachment
    FROM stream
    WHERE source_id = '562978141' and created_time <= 1391713049

    =========
    The type of this story. Possible values are:
    null - tagged in other people post or message on my wall (b-day wishes, etc)
    8 - now friends
    11 - Group created
    12 - Event created
    46 - Status update
    56 - Post on wall from another user
    66 - Note created
    80 - Link posted
    128 -Video posted
    247 - Photos posted
    237 - App story
    257 - Comment created
    272 - App story
    285 - Checkin to a place
    308 - Post in Group
    =======
    {
    "data": [
    {
    "type": 46,
    "post_id": "562978141_10152307380293142",
    "actor_id": 562978141,
    "source_id": 562978141,
    "target_id": null,
    "message": "Kayden the movie maker hard at work!",
    "message_tags": [
    ],
    "place": 111629348854125,
    "created_time": 1394932841,
    "description": null,
    "call_to_action": null,
    "action_links": null,
    "attachment": {
    "media": [
    {
    "href": "https://www.facebook.com/photo.php?fbid=10152307379933142&set=pcb.10152307380293142&type=1&relevant_count=3",
    "alt": "",
    "type": "photo",
    "src": "https://fbcdn-photos-c-a.akamaihd.net/hphotos-ak-frc3/t1.0-0/1891175_10152307379933142_812163758_s.jpg",
    "photo": {
    "aid": "2417972703958011745",
    "pid": "2417972705031618602",
    "fbid": "10152307379933142",
    "owner": 562978141,
    "index": 1,
    "width": 2048,
    "height": 1536,
    "images": [
    {
    "src": "https://fbcdn-photos-c-a.akamaihd.net/hphotos-ak-frc3/t1.0-0/1891175_10152307379933142_812163758_s.jpg",
    "width": 130,
    "height": 97
    },
    {
    "src": "https://scontent-a.xx.fbcdn.net/hphotos-frc3/t1.0-9/s720x720/1891175_10152307379933142_812163758_n.jpg",
    "width": 720,
    "height": 540
    }
    ]
    }
    },
    {
    "href": "https://www.facebook.com/photo.php?fbid=10152307379938142&set=pcb.10152307380293142&type=1&relevant_count=2",
    "alt": "",
    "type": "photo",
    "src": "https://fbcdn-photos-e-a.akamaihd.net/hphotos-ak-prn2/t1/1557740_10152307379938142_1599928954_s.jpg",
    "photo": {
    "aid": "2417972703958011745",
    "pid": "2417972705031618603",
    "fbid": "10152307379938142",
    "owner": 562978141,
    "index": 2,
    "width": 2048,
    "height": 1536,
    "images": [
    {
    "src": "https://fbcdn-photos-e-a.akamaihd.net/hphotos-ak-prn2/t1/1557740_10152307379938142_1599928954_s.jpg",
    "width": 130,
    "height": 97
    },
    {
    "src": "https://scontent-a.xx.fbcdn.net/hphotos-prn2/t1.0-9/s720x720/1557740_10152307379938142_1599928954_n.jpg",
    "width": 720,
    "height": 540
    }
    ]
    }
    },
    {
    "href": "https://www.facebook.com/photo.php?fbid=10152307379928142&set=pcb.10152307380293142&type=1&relevant_count=1",
    "alt": "",
    "type": "photo",
    "src": "https://scontent-a.xx.fbcdn.net/hphotos-frc3/l/t1.0-0/10006538_10152307379928142_295270631_s.jpg",
    "photo": {
    "aid": "2417972703958011745",
    "pid": "2417972705031618601",
    "fbid": "10152307379928142",
    "owner": 562978141,
    "index": 3,
    "width": 2048,
    "height": 1536,
    "images": [
    {
    "src": "https://scontent-a.xx.fbcdn.net/hphotos-frc3/l/t1.0-0/10006538_10152307379928142_295270631_s.jpg",
    "width": 130,
    "height": 97
    },
    {
    "src": "https://scontent-a.xx.fbcdn.net/hphotos-frc3/l/t1.0-9/s720x720/10006538_10152307379928142_295270631_n.jpg",
    "width": 720,
    "height": 540
    }
    ]
    }
    }
    ],
    "name": "",
    "caption": "",
    "description": "",
    "properties": [
    ],
    "icon": "https://fbstatic-a.akamaihd.net/rsrc.php/v2/yz/r/StEh3RhPvjk.gif",
    "fb_object_type": "album",
    "fb_object_id": "2417972703958011745"
    }
    },
    {
    "type": 128,
    "post_id": "562978141_10152307376993142",
    "actor_id": 562978141,
    "source_id": 562978141,
    "target_id": null,
    "message": "After watching hundreds of hours of stop motion lego movies on YouTube Kayden has created his first stop motion lego movie! Look out Tim Burton.",
    "message_tags": [
    ],
    "place": null,
    "created_time": 1394932714,
    "description": null,
    "call_to_action": null,
    "action_links": null,
    "attachment": {
    "media": [
    {
    "href": "https://www.facebook.com/photo.php?v=10152307376993142",
    "alt": "",
    "type": "video",
    "src": "https://fbcdn-vthumb-a.akamaihd.net/hvthumb-ak-prn2/t15/1095766_10152307377348142_10152307376993142_53668_1238_b.jpg",
    "video": {
    "display_url": "https://www.facebook.com/photo.php?v=10152307376993142",
    "source_url": "https://fbcdn-video-a.akamaihd.net/hvideo-ak-frc1/v/t42/1970148_10152307377268142_1355779978_n.mp4?oh=22ebee32f9d2f758bdefdb5c971b5007&oe=5328CB68&__gda__=1395145410_7e94cc4230dc9507fd1e49a3baf955f2",
    "owner": 562978141,
    "source_type": "raw",
    "created_time": 1394932714
    }
    }
    ],
    "name": "",
    "href": "https://www.facebook.com/photo.php?v=10152307376993142",
    "caption": "",
    "description": "",
    "properties": [
    {
    "name": "Length",
    "text": "0:16"
    }
    ],
    "icon": "https://fbstatic-a.akamaihd.net/rsrc.php/v2/yD/r/DggDhA4z4tO.gif",
    "fb_object_type": "video",
    "fb_object_id": "10152307376993142"
    }
    },
    {
    "type": 247,
    "post_id": "562978141_10152296008963142",
    "actor_id": 562978141,
    "source_id": 562978141,
    "target_id": null,
    "message": "My daughter is already putting umbrellas in her drinks, hmmmmm",
    "message_tags": [
    ],
    "place": null,
    "created_time": 1394409986,
    "description": null,
    "call_to_action": null,
    "action_links": null,
    "attachment": {
    "media": [
    {
    "href": "https://www.facebook.com/photo.php?fbid=10152296008938142&set=a.10151333004843142.526744.562978141&type=1&relevant_count=1",
    "alt": "My daughter is already putting umbrellas in her drinks, hmmmmm",
    "type": "photo",
    "src": "https://fbcdn-photos-h-a.akamaihd.net/hphotos-ak-ash3/t1/1932260_10152296008938142_1008671693_s.jpg",
    "photo": {
    "aid": "2417972703958403480",
    "pid": "2417972705031618596",
    "fbid": "10152296008938142",
    "owner": 562978141,
    "index": 1,
    "width": 852,
    "height": 1136
    }
    }
    ],
    "name": "iOS Photos",
    "href": "https://www.facebook.com/album.php?fbid=10151333004843142&id=562978141&aid=526744",
    "caption": "",
    "description": "",
    "properties": [
    ],
    "icon": "https://fbstatic-a.akamaihd.net/rsrc.php/v2/yz/r/StEh3RhPvjk.gif",
    "fb_object_type": "photo",
    "fb_object_id": "2417972705031618596"
    }
    },
    {
    "type": 80,
    "post_id": "562978141_10152288456348142",
    "actor_id": 562978141,
    "source_id": 562978141,
    "target_id": null,
    "message": "",
    "message_tags": [
    ],
    "place": null,
    "created_time": 1394074930,
    "description": "Mike Nimer shared a link.",
    "call_to_action": null,
    "action_links": null,
    "attachment": {
    "media": [
    {
    "href": "http://elitedaily.com/news/technology/this-insane-new-app-will-allow-you-to-read-novels-in-under-90-minutes/",
    "alt": "",
    "type": "link",
    "src": "https://fbexternal-a.akamaihd.net/safe_image.php?d=AQCQuZnZt4Zr6TqD&w=154&h=154&url=http%3A%2F%2Fcdn29.elitedaily.com%2Fwp-content%2Fuploads%2F2014%2F03%2Flarge-13.jpg"
    }
    ],
    "name": "This Insane New App Will Allow You To Read Novels In Under 90 Minutes | Elite Daily",
    "href": "http://elitedaily.com/news/technology/this-insane-new-app-will-allow-you-to-read-novels-in-under-90-minutes/",
    "caption": "elitedaily.com",
    "description": "The reading game is about to change forever. Boston-based software developer Spritz has been in \"stealth mode\" for three years, tinkering with their program and leasing it out to different ebooks, apps, and other platforms.",
    "properties": [
    ],
    "icon": "https://fbstatic-a.akamaihd.net/rsrc.php/v2/yD/r/aS8ecmYRys0.gif"
    }
    },
    {
    "type": 80,
    "post_id": "562978141_10152283107623142",
    "actor_id": 562978141,
    "source_id": 562978141,
    "target_id": null,
    "message": "",
    "message_tags": [
    ],
    "place": null,
    "created_time": 1393852060,
    "description": "Mike Nimer shared Veerendra Chandrappa's video.",
    "call_to_action": null,
    "action_links": null,
    "attachment": {
    "media": [
    {
    "href": "https://www.facebook.com/photo.php?v=10201369851507559",
    "alt": "",
    "type": "video",
    "src": "https://fbcdn-vthumb-a.akamaihd.net/hvthumb-ak-prn2/t15/1394580_10201369852867593_10201369851507559_10096_1521_t.jpg",
    "video": {
    "display_url": "https://www.facebook.com/photo.php?v=10201369851507559",
    "source_url": "https://fbcdn-video-a.akamaihd.net/hvideo-ak-prn2/v/t42.1790-2/1518376_10201369852347580_1330767221_n.mp4?oh=7bc021d1e83ffa4af9fde706410b44f5&oe=53284DF2&__gda__=1395151854_1e8d9f41f48192fed402f12e0f0d685f",
    "owner": 1212033702,
    "source_type": "raw",
    "created_time": 1391185905
    }
    }
    ],
    "name": "",
    "href": "https://www.facebook.com/photo.php?v=10201369851507559",
    "caption": "",
    "description": "Must watch awesome chemical reactions.. don't miss it at any chance!",
    "properties": [
    {
    "name": "Length",
    "text": "3:47"
    }
    ],
    "icon": "https://fbstatic-a.akamaihd.net/rsrc.php/v2/yD/r/aS8ecmYRys0.gif",
    "fb_object_type": "video",
    "fb_object_id": "10201369851507559"
    }
    },
    {
    "type": 80,
    "post_id": "562978141_10152273966393142",
    "actor_id": 562978141,
    "source_id": 562978141,
    "target_id": null,
    "message": "",
    "message_tags": [
    ],
    "place": null,
    "created_time": 1393510636,
    "description": "Mike Nimer shared a link.",
    "call_to_action": null,
    "action_links": null,
    "attachment": {
    "media": [
    {
    "href": "http://m.huffpost.com/us/entry/4832325",
    "alt": "",
    "type": "link",
    "src": "https://fbexternal-a.akamaihd.net/safe_image.php?d=AQCpuWUlAyfVWM3U&w=154&h=154&url=http%3A%2F%2Fi.huffpost.com%2Fgen%2F1645862%2Fthumbs%2Fo-AA023506-facebook.jpg"
    }
    ],
    "name": "8 Surprising Historical Facts That Will Change Your Concept Of Time Forever",
    "href": "http://m.huffpost.com/us/entry/4832325",
    "caption": "m.huffpost.com",
    "description": "You probably should know these things didn't happen anywhere near when you thought they did... Not everyone can be a world history master, especially when we tend to learn about it in specifically segmented classes like \"European History&quot...",
    "properties": [
    ],
    "icon": "https://fbstatic-a.akamaihd.net/rsrc.php/v2/yD/r/aS8ecmYRys0.gif"
    }
    },
    {
    "type": 80,
    "post_id": "562978141_10152271260988142",
    "actor_id": 562978141,
    "source_id": 562978141,
    "target_id": null,
    "message": "",
    "message_tags": [
    ],
    "place": null,
    "created_time": 1393384421,
    "description": "Mike Nimer shared a link.",
    "call_to_action": null,
    "action_links": null,
    "attachment": {
    "media": [
    ],
    "name": "http://zachholman.com/posts/only-90s-developers/",
    "href": "http://zachholman.com/posts/only-90s-developers/",
    "caption": "zachholman.com",
    "description": "http://zachholman.com/posts/only-90s-developers/",
    "properties": [
    ],
    "icon": "https://fbstatic-a.akamaihd.net/rsrc.php/v2/yD/r/aS8ecmYRys0.gif",
    "fb_object_type": ""
    }
    }
    ]
    }
     ***/
}
