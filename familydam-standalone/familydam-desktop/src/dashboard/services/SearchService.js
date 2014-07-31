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


var SearchService = function($http) {
    var basePath = "/content/dam";

    /**
     * Search all content with limit/offset paging support, used by the grid view.
     * @param path
     * @param successCallback
     * @param errorCallback
     * @returns {*|Array|Object|Mixed|promise|HTMLElement}
     */
    this.search = function(type, limit, offset, filterPath, filterDateFrom, filterDateTo, filterTags )
    {
        var searchPath = "/dashboard-api/search?type=" +type +"&limit=" +limit +"&offset=" +offset +"&filterPath=" +filterPath +"&dateFrom=" +filterDateFrom +"&dateTo=" +filterDateTo +"&tags=" +filterTags;

        var method =  $http.get(searchPath);
        return method.then(function(result){
            return result.data.data;
        });
    };


};


SearchService.$inject = ['$http'];
module.exports = SearchService;
