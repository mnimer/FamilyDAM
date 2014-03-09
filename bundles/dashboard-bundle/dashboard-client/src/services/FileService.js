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
/*jshint -W083 */

var FileService = function ($http)
{

    /**
     * Load one layer at a time, used by the list view to show the contents of a folder.
     * @param path
     * @param successCallback
     * @param errorCallback
     * @returns {*|Array|Object|Mixed|promise|HTMLElement}
     */
    this.list = function (path, type)
    {

        // make sure the path starts with /
        if (path.substring(0, 1) != "/")
        {
            path = "/" + path;
        }

        // todo call new service with path & type filter
        var get = $http.get(path + '.1.json', { cache: false });

        return get;
    };

    /**
     * Load one layer at a time, used by the list view to show the contents of a folder.
     * @param path
     * @param successCallback
     * @param errorCallback
     * @returns {*|Array|Object|Mixed|promise|HTMLElement}
     */
    this.listFolders = function (path)
    {
        var _this = this;
        var _path = "/dashboard-api/files/foldertree?path=" +path;

        var get = $http.get(_path);
        return get.then(function(data){
            return data.data;
        });
    };

    /**
     * Load one layer at a time, used by the list view to show the contents of a folder.
     * @param path
     * @param successCallback
     * @param errorCallback
     * @returns {*|Array|Object|Mixed|promise|HTMLElement}
     */
    this.deletePath = function (path)
    {
        var method = $http.delete(path);
        return method;
    };


    /**
     * Create new folder (name=title) under the path
     * @param path
     * @param title
     * @param successCallback
     * @param errorCallback
     * @returns {*|HttpPromise}
     */
    this.createFolder = function (path, title)
    {
        // make sure the path starts with /
        if (path.substring(0, 1) != "/")
        {
            path = "/" + path;
        }
        var _url = path + "/*";
        var _data = ":name=" + title + "&:nameHint=" + title + "&jcr:primaryType=sling:Folder";
        var _config = {};
        _config.headers = {};
        _config.headers['Content-Type'] = "application/x-www-form-urlencoded";

        var post = $http.post(_url, _data, _config);

        return post;
    };
};


FileService.$inject = ['$http'];
module.exports = FileService;
