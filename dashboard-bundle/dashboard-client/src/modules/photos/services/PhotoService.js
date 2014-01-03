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
    var basePath = "/content/dam/photos";



    /**
     * Using the jcr:uuid get a json packet for the node
     * @param path
     * @param successCallback
     * @param errorCallback
     * @returns {*|Array|Object|Mixed|promise|HTMLElement}
     */
    this.getById = function(uuid, successCallback, errorCallback) {

        // make sure the path starts with /
        var get =  $http.get("/dashboard-api/photo?uuid=" +uuid,{ cache: false });
        return get;
    };


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


};


PhotoService.$inject = ['$http'];
module.exports = PhotoService;
