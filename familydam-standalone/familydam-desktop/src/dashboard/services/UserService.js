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
        '/apps/familydam/users',
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
     * return the users properties
     * @returns {*}
     */
    this.getUserProperties = function(user)
    {
        var data = {};
        data[':name'] = user[':name'];

        var method =  $http.get("/dashboard-api/users?:name=" +user[':name'], { cache: false });
        return method.then( function(user)
        {
            for( var prop in user.data )
            {
                user[prop] = user.data[prop];
            }
            return user;
        });
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
    this.updateUser = function(user)
    {
        var data = {};
        data[':name'] = user[':name'];
        data[':content'] = angular.toJson(user);

        var method =  $http.post("/dashboard-api/users",
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
    this.updateUserAuth = function(username, data)
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
     * Simple call to refresh the users data from facebook
     * @param username
     * @returns {*}
     */
    this.refreshUserFacebook = function(username)
    {
        var method =  $http.get("/dashboard-api/jobs/facebook?username=" +username);
        return method;
    };


    /**
     * Update the users authentication (OAUTH) data for users.
     * @param username
     * @param accessToken
     * @param expiresIn
     * @param signedRequest
     * @param userId
     * @returns {Promise|*}
     */
    this.updateUserFacebook = function(username, accessToken, expiresIn, signedRequest, userId)
    {
        var data = {};
        data.username = username;
        data.accessToken = accessToken;
        data.expiresIn = expiresIn;
        data.signedRequest = signedRequest;
        data.userId = userId;

        if( username !== undefined )
        {
            var method =  $http.post("/apps/familydam/users/" +username +"/web/facebook",
                $.param(data),
                {
                    headers:
                    {
                        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
                    }
                });

            return method.then(function()
            {
                // Now that we've updated the users facebook oauth values, we'll call a 2nd service to start
                // up the Facebook jobs
                    var method2 =  $http.post("/dashboard-api/jobs/facebook",$.param({"username":username}),
                        {
                            headers:
                            {
                                'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
                            }
                        });
                    method2.then(function(){
                        //todo
                    });
            });
        }

    };


    /**
     * delete the users data from facebook
     * @param username
     * @returns {*}
     */
    this.deleteUserFacebook = function(username)
    {
        var method =  $http.delete("/apps/familydam/users/" +username +"/web/facebook");
        return method.then(function(arg1,arg2)
        {
            return arg1;
        });
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



