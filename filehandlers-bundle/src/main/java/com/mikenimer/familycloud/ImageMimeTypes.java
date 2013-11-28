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

/**
 * User: mikenimer
 * Date: 11/17/13
 */
public enum ImageMimeTypes
{
    JPG("jpg", "image/jpeg"),
    JPE("jpe", "image/jpeg"),
    JPEG("jpeg", "image/jpeg"),
    PNG("png", "image/png"),
    GIF("gif", "image/gif"),
    NEF("nef", "image/nef"),
    TIF("tif", "image/tiff"),
    TIFF("tiff", "image/tiff"),
    PSD("psd", "image/vnd.adobe.photoshop"),
    SVG("svg", "image/svg+xml");

    private String extension;
    private String mimeType;

    public static boolean isSupportedExtension(String extension)
    {
        return true;
    }

    public static boolean isSupportedMimeType(String extension)
    {
        for (ImageMimeTypes imageMimeTypes : ImageMimeTypes.values() )
        {
            if( imageMimeTypes.mimeType.equalsIgnoreCase(extension) || imageMimeTypes.extension.equalsIgnoreCase(extension) )
            {
                return true;
            }
        }
        return false;
    }

    ImageMimeTypes(String extension, String mimeType)
    {
        this.extension = extension;
        this.mimeType = mimeType;
    }
}
