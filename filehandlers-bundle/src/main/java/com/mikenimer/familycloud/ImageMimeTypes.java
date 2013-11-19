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
        return true;
    }

    ImageMimeTypes(String extension, String mimeType)
    {
        this.extension = extension;
        this.mimeType = mimeType;
    }
}
