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

import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: mikenimer
 * Date: 11/17/13
 */
public class Constants
{

    // JCR Paths
    public static final String JOB_MOVE = "familydam/move";
    public static final String JOB_MUSIC_METADATA = "familydam/music/metadata";
    public static final String JOB_IMAGE_METADATA = "familydam/photos/metadata";
    public static final String JOB_IMAGE_SIZE = "familydam/photos/size";
    public static final String JOB_IMAGE_THUMBNAILS = "familydam/photos/thumbnails";

    // Map keys
    public static final String DATETIME = "fd:date"; //todo change to datetime
    public static final String ORIENTATION = "orientation"; //todo change to orientation
    public static final String KEYWORDS = "keywords";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";

    //EXIF String
    public static final String GPS = "GPS";
    public static final String GPS_LATITUDE = "GPS Latitude";
    public static final String GPS_LONGITUDE = "GPS Longitude";

    // simple strings
    public static final String PATH= "path";
    public static final String NAME= "name";
    public static final String TYPE= "type";
    public static final String VALUE= "value";
    public static final String DESCRIPTION= "description";
    public static final String METADATA= "metadata";
    public static final String TITLE = "title";
    public static final String TRACK = "track";
    public static final String ARTIST = "artist";
    public static final String ALBUM = "album";
    public static final String ALBUM_ARTIST = "album_artist";
    public static final String ALBUM_IMAGE = "album_image";
    public static final String ALBUM_IMAGE_MIMETYPE = "album_image_mimetype";
    public static final String YEAR = "year";
    public static final String GENRE = "genre";
    public static final String GENRE_CODE = "genre_code";
    public static final String COMMENT = "comment";
    public static final String VERSION = "version";
    public static final String CHAPTERS = "chapters";
    public static final String CHAPTER_TOC = "chapters_toc";
    public static final String COMPOSER = "composer";
    public static final String COPYRIGHT = "copyright";
    public static final String ENCODER = "encoder";
    public static final String ITUNES_COMMENT = "itunes_comment";
    public static final String ORIGINAL_ARTIST = "original_artist";
    public static final String PUBLISHER = "publisher";



    public static final String NODE_CONTENT = "fd:content";
    public static final String NODE_GEOSTAMP = "fd:geostamp";
    public static final String NODE_IMAGE = "fd:image";
    public static final String NODE_SONG = "fd:song";
    public static final String NODE_VIDEO = "fd:video";
    public static final String NODE_FACEBOOK = "fd:facebook";
    public static final String NODE_TWITTER = "fd:twittter";
}
