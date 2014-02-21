package com.mikenimer.familydam;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * User: mikenimer
 * Date: 11/13/13
 */
//@Service
//@Component(immediate = true)
public class TestDecorators implements ResourceDecorator
{
    private final Logger log = LoggerFactory.getLogger(TestDecorators.class);


    @Override
    public Resource decorate(Resource resource)
    {
        //Node node = resource.adaptTo(Node.class);

        return resource;
    }


    @Override
    public Resource decorate(Resource resource, HttpServletRequest request)
    {
        return decorate(resource);
    }
}
