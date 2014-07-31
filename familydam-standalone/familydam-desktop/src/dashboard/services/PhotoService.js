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
    this.invokeLink = function(path) {

        // make sure the path starts with /
        if( path.substring(0,1) != "/"){
            path = "/" +path;
        }

        var method =  $http.get(path);
        return method;
    };



    /**
     * Search all photos with limit/offset paging support, used by the grid view.
     * @param path
     * @param successCallback
     * @param errorCallback
     * @returns {*|Array|Object|Mixed|promise|HTMLElement}
     */
    this.search = function(type, limit, offset, filterPath, filterDateFrom, filterDateTo, filterTags )
    {
        var searchPath = "/dashboard-api/search?type=" +type +"&limit=" +limit +"&offset=" +offset +"&filterPath=" +filterPath +"&dateFrom=" +filterDateFrom +"&dateTo=" +filterDateTo +"&tags=" +filterTags;

        var method =  $http.get(searchPath);
        return method.then(function(data){
            return data.data;
        });
    };


};


PhotoService.$inject = ['$http'];
module.exports = PhotoService;
