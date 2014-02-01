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


var UserService = function($http, $q, fileService)
{

    var systemPaths = [
        '/content/dam/photos',
        '/content/dam/music',
        '/content/dam/movies',
        '/content/dam/email',
        '/content/dam/documents',
        '/content/dam/web/facebook',
        '/content/dam/web/twitter',
        '/content/dam/web/dropbox',
    ];

    /**
     * Call the Sling API to list users.
     * @param path
     * @param successCallback
     * @param errorCallback
     * @returns {*|Array|Object|Mixed|promise|HTMLElement}
     */
    this.listUsers = function()
    {
        var method =  $http.get('/system/userManager/user.tidy.1.json',{ cache: false });
        return method;
    };

    /**
     * Invoke the Sling API to create a user
     * @param path
     * @param successCallback
     * @param errorCallback
     * @returns {*|Array|Object|Mixed|promise|HTMLElement}
     */
    this.createUser = function(data)
    {
        var username = data[':name'];
        var method =  $http.post('/system/userManager/user.create.json',
            $.param(data),
            {
                headers:
                {
                    'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
                }
            });

        return method.then(function()
        {
            var promises = [];
            var deferred = $q.defer();
            var promise = deferred.promise;

            for(var i=0; i < systemPaths.length; i++)
            {
                promises.push(fileService.createFolder(systemPaths[i], username));
            }

            $q.all(promises);
        });
    };



    /**
     * Call our API which will invoke the Sling API to create the user then
     * @param path
     * @param successCallback
     * @param errorCallback
     * @returns {*|Array|Object|Mixed|promise|HTMLElement}
     */
    this.updateUser = function(username, data)
    {
        var method =  $http.post("/system/userManager/user/" +username +".update.json",
            $.param(data),
            {
                headers:
                {
                    'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
                }
            });
        return method;
    };


    /**
     * Call our API which will invoke the Sling API to create the user then
     * @param path
     * @param successCallback
     * @param errorCallback
     * @returns {*|Array|Object|Mixed|promise|HTMLElement}
     */
    this.removeUser = function(username)
    {
        var method =  $http.post("/system/userManager/user/" +username +".delete.json");
        return method;
    };

};


UserService.$inject = ['$http', '$q', 'fileService'];
module.exports = UserService;



