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


var MetadataService = function($http, $q)
{
    this.updateTags = function(items, words)
    {
        var calls = [];
        var deferred = $q.defer();
        var _config = {};
        _config.headers = {};
        _config.headers['Content-Type'] = undefined;
        _config.transformRequest = function (data) {
            return data;
        };

        for(var i=0; i<items.length; i++)
        {
            var formData = new FormData();
            formData.append("metadata/keywords", words.join(","));

            var item = items[i];
            var invokePath = item['jcr:path'];
            _config.url = invokePath;
            var method =  $http.post( invokePath, formData, _config );
            calls.push(method);
        }

        return deferred.all(calls);
    };



    /**
     * Return distinct list off all /metadata/keywords attached to the nodes under a specific path.
     * @param path
     * @returns
     */
    this.keywordsByPath = function( path )
    {
        var invokePath = "/dashboard-api/metadata/keywords?path=" +path;

        var method =  $http.get(invokePath);
        return method.then(function(data){
            return data.data;
        });
    };

};


MetadataService.$inject = ['$http', '$q'];
module.exports = MetadataService;
