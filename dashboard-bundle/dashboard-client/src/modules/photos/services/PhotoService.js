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

var PhotoService = function($http) {
    var basePath = "/content/dam";


    /**
     * This method is used to invoke hateoas links returned from the other services
     * @param path
     * @param successCallback
     * @param errorCallback
     * @returns {*|Array|Object|Mixed|promise|HTMLElement}
     */
    this.invokeLink = function(path, successCallback, errorCallback) {

        // make sure the path starts with /
        if( path.substring(0,1) != "/"){
            path = "/" +path;
        }

        var get =  $http.get(basePath +path +'.1.json',{ cache: false });

        return get;
    };


    /**
     * Load one layer at a time, used by the list view to show the contents of a folder.
     * @param path
     * @param successCallback
     * @param errorCallback
     * @returns {*|Array|Object|Mixed|promise|HTMLElement}
     */
    this.list = function(path, successCallback, errorCallback) {

        // make sure the path starts with /
        if( path.substring(0,1) != "/"){
            path = "/" +path;
        }

        var get =  $http.get(basePath +path +'.1.json',{ cache: false });

        return get;
    };


    /**
     * Search all photos with limit/offset paging support, used by the grid view.
     * @param path
     * @param successCallback
     * @param errorCallback
     * @returns {*|Array|Object|Mixed|promise|HTMLElement}
     */
    this.search = function( limit, offset, successCallback, errorCallback )
    {
        var searchPath = "/dashboard-api/photos/search?limit=" +limit +"&offset=" +offset;

        var get =  $http.get(searchPath);

        return get;
    };


    /**
     * Create new folder (name=title) under the path
     * @param path
     * @param title
     * @param successCallback
     * @param errorCallback
     * @returns {*|HttpPromise}
     */
    this.createFolder = function(path, title, successCallback, errorCallback) {
        // make sure the path starts with /
        if( path.substring(0,1) != "/"){
            path = "/" +path;
        }
        var _url = basePath +path +"/*";
        var _data = ":nameHint=" +title +"&jcr:primaryType=nt:folder";
            _data[":nameHint"] = title;
            _data["jcr:primaryType"] = "nt:folder";
        var _config = {};
            _config.headers = {};
            _config.headers['Content-Type'] = "application/x-www-form-urlencoded";

        var post =  $http.post(_url, _data, _config);

        return post;
    };
};


PhotoService.$inject = ['$http'];
module.exports = PhotoService;
