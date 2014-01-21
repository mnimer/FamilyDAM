/*
 * This file is part of FamilyCloud Project.
 *
 *     The FamilyCloud Project is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     The FamilyCloud Project is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with the FamilyCloud Project.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mikenimer.familycloud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: mikenimer
 * Date: 11/17/13
 */
public class Constants
{

    // JCR Paths
    public static final String JOB_MOVE = "familycloud/move";
    public static final String JOB_IMAGE_METADATA = "familydam/photos/metadata";
    public static final String JOB_IMAGE_SIZE = "familydam/photos/size";
    public static final String JOB_IMAGE_THUMBNAILS = "familydam/photos/thumbnails";

    // Map keys
    public static final String DATETIME = "dateTaken"; //todo change to datetime
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
}
