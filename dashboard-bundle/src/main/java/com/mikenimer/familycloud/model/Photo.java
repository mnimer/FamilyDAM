package com.mikenimer.familycloud.model;

import org.apache.felix.scr.annotations.Component;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.adapter.SlingAdaptable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: mikenimer
 * Date: 11/20/13
 */
@Component(metatype = false, immediate = true)
public class Photo extends SlingAdaptable implements Adaptable
{
    private final Logger log = LoggerFactory.getLogger(Photo.class);


    public Photo()
    {
        log.debug("default const.");
    }


    public Photo(Object adaptable)
    {
        log.debug(adaptable.toString());
    }


    @Override
    public <AdapterType> AdapterType adaptTo(Class<AdapterType> type)
    {
        return (AdapterType)new Photo();
    }
}
