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

var FileService = function($http) {

    /**
     * Load one layer at a time, used by the list view to show the contents of a folder.
     * @param path
     * @param successCallback
     * @param errorCallback
     * @returns {*|Array|Object|Mixed|promise|HTMLElement}
     */
    this.list = function(path) {

        // make sure the path starts with /
        if( path.substring(0,1) != "/"){
            path = "/" +path;
        }

        var get =  $http.get(path +'.1.json',{ cache: false });

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
    this.createFolder = function(path, title) {
        // make sure the path starts with /
        if( path.substring(0,1) != "/"){
            path = "/" +path;
        }
        var _url = path +"/*";
        var _data = ":name=" +title +"&:nameHint=" +title +"&jcr:primaryType=sling:Folder";
            //_data[":name"] = title;
            //_data[":nameHint"] = title;
            //_data["jcr:primaryType"] = "sling:Folder";
        var _config = {};
            _config.headers = {};
            _config.headers['Content-Type'] = "application/x-www-form-urlencoded";

        var post =  $http.post(_url, _data, _config);

        return post;
    };
};


FileService.$inject = ['$http'];
module.exports = FileService;
