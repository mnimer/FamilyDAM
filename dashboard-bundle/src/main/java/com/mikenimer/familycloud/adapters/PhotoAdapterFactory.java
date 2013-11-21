package com.mikenimer.familycloud.adapters;

import com.mikenimer.familycloud.model.Photo;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.adapter.AdapterFactory;

@Component(metatype = false, immediate = true)
@Service(value=org.apache.sling.api.adapter.AdapterFactory.class)
@Properties({
        @Property(name = AdapterFactory.ADAPTABLE_CLASSES, value = { "org.apache.sling.api.resource.Resource" }),
        @Property(name = AdapterFactory.ADAPTER_CLASSES, value = { "com.mikenimer.familycloud.Photo" })
})
public class PhotoAdapterFactory implements AdapterFactory
{
    public PhotoAdapterFactory()
    {
    }


    public  <AdapterType> AdapterType getAdapter(final Object adaptable, Class<AdapterType> type)
    {
        return null;
    }
}