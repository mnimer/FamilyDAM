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


/**
 * Cache key/values in memory (until the browser is reloaded) or longer in the localStorage
 * @constructor
 */
var CacheService = function ()
{
    //in-memory cache
    var memoryCache = {};
    var _hasLocalCache = false;


    try
    {
        _hasLocalCache = ('localStorage' in window) && window['localStorage'] !== null;

        // Set helper functions on LocalStorage prototype
        if( _hasLocalCache )
        {
            Storage.prototype.setObject = function(key, value) {
                this.setItem(key, JSON.stringify(value));
            };


            Storage.prototype.getObject = function(key) {
                return JSON.parse(this.getItem(key));
            };
        }
    }
    catch (ex)
    {
        _hasLocalCache = false;
    }


    this.has = function (key)
    {
        if( this.memoryCache[key] !== undefined )
        {
            return true;
        }

        if( _hasLocalCache )
        {
            if( localStorage.getObject(key) !== undefined )
            {
                return true;
            }
        }

        return false;
    };


    /**
     * {String} key - the key
     */
    this.get = function (key)
    {
        // first check in memory
        var object = this.memoryCache[key] || undefined;

        // if we don't have it memory, and the browsers local cache is enabled, double check there.
        if( object === undefined && _hasLocalCache)
        {
            object = localStorage.getObject(key) || undefined;
        }
        return object;
    };


    /**
     * {String} key - the key
     * {Object} value - any kind of value you want to store
     * however only objects and strings are allowed in local storage
     * {Boolean} persist - put this in local storage(true) or memory(false)
     */
    this.put = function (key, value, persist)
    {
        if (persist && _hasLocalCache)
        {
            localStorage.setObject(key, value);
        }
        else
        {
            // put in our memory cache
            this.memoryCache[key] = value;
        }
        // return our newly cached item
        return value;
    };


    /**
     * {String} key - the key
     */
    this.clear = function (key)
    {
        if (_hasLocalCache)
        {
            localStorage.removeItem(key);
        }
        // delete in both caches - doesn't hurt.
        delete this.memoryCache[key];
    };
};


CacheService.$inject = [];
module.exports = CacheService;
