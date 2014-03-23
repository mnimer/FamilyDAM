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
 * Controller for the all customers screen
 * @param $scope
 * @param $location
 * @constructor
 */
var UserManagerController = function ($scope, $rootScope, $state, $window, userService)
{

    $scope.isNewUser = false;
    $scope.isExistingUser = false;
    $scope.selectedUser = {};
    $scope.users = [];



    /*****************************************************
     *** User Manager
     ****************************************************/
    var refreshUsers = function ()
    {
        userService.listUsers().then(function (data, status, headers, config)
        {
            var list = [];

            for (var user in data.data)
            {
                var u = data.data[user];
                if( typeof(u) == "object" && user != "anonymous" )
                {
                    u[':name'] = user; //special key defined by Sling
                    list.push(u);
                }
            }

            $scope.users = list;
        });
    };


    /**
     * UPDATE USER
     */
    $scope.selectUser = function ()
    {
        $scope.isNewUser = false;
        $scope.isExistingUser = true;

        var results = userService.getUserProperties($scope.selectedUser);
        results.then( function(user){

            //loop over user and add properties to $scope.selectedUser
            for(var prop in user.data)
            {
                $scope.selectedUser[prop] = user.data[prop];
            }

            initFacebookSdk(user);
        });
    };



    /***
     * NEW USER
     */
    $scope.createNewUser = function ()
    {
        $scope.selectedUser = {};
        $scope.isNewUser = true;
        $scope.isExistingUser = false;

    };

    $scope.createNewUserHandler = function ()
    {
        userService.createUser($scope.selectedUser).then(
            function (data, status, headers, config)
            {
                refreshUsers();

                $scope.isExistingUser = true;
                $scope.isNewUser = false;
            }, function (data, status, headers, config)
            {
                console.log(data);

                //todo : show error to client

                // error, stay on form
                $scope.isNewUser = true;
            });
    };



    /**
     * UPDATE USER
     */
    $scope.updateUserHandler = function ()
    {
        var username = $scope.selectedUser[':name'];

        //Copy the updateable properties. If we pass all back we'll get a 500 error.
        var userProps = {};
        for (var prop in $scope.selectedUser)
        {
            if (prop != "memberOf" && prop != "declaredMemberOf" && prop != "pwd" && prop != "pwdConfirm")
            {
                userProps[prop] = $scope.selectedUser[prop];
            }
        }


        userService.updateUser(username, userProps).then(
            function (data, status, headers, config)
            {
                $scope.isExistingUser = true;
                $scope.isNewUser = false;
            }, function (data, status, headers, config)
            {
                console.log(data);
                //todo : show error to client
            });
    };


    /**
     * REMOVE USER
     */
    $scope.removeUserHandler = function ()
    {
        userService.removeUser($scope.selectedUser[':name']).then(
            function (data, status, headers, config)
            {
                refreshUsers();

                $scope.selectedUser = undefined;
                $scope.isNewUser = false;
                $scope.isExistingUser = false;
            },
            function (data, status, headers, config)
            {
                console.log(data);
                //todo : show error to client
            });
    };


    /*****************************************************
    *** Facebook Services
     ****************************************************/
    $scope.activateFacebook = function ()
    {
        // Logout first to force a reauthentication since it's possible that this could be
        // on a families shared computer.
        //FB.logout();

        //todo: make this dynamic
        var _redirectUrl = "http://localhost.familydam.com:8888/dashboard-api/facebook/callback?";
        //window.open("https://www.facebook.com/dialog/oauth?state=mnimer&response_type=token&client_id=1459016164310867&redirect_uri=" +_redirectUrl);
        FB.login(function(response){
            if (response.authResponse)
            {
                if( $scope.selectedUser.web === undefined )
                {
                    $scope.selectedUser.web = {};
                }
                if( $scope.selectedUser.web.facebook === undefined )
                {
                    $scope.selectedUser.web.facebook = {};
                }

                $scope.selectedUser.web.facebook.accessToken = response.authResponse.accessToken;
                $scope.selectedUser.web.facebook.expiresIn = response.authResponse.expiresIn;
                $scope.selectedUser.web.facebook.signedRequest = response.authResponse.signedRequest;
                $scope.selectedUser.web.facebook.userID = response.authResponse.userID;
                $scope.selectedUser.web.facebook.isAuthorized = "true";

                var updateResults = userService.updateUser($scope.selectedUser);

                refreshFacebook();

                $scope.$apply();
            }else{
                $scope.deactivateFacebook($scope.selectedUser);
            }

            //user_status,user_photos,user_videos,user_checkins,user_likes,user_notes
        }, {scope: 'read_stream,read_insights', enable_profile_selector:true });
        //var response = FB.getAuthResponse();
        //console.log(response);
    };


    $scope.deactivateFacebook = function (user)
    {
        if( user === undefined)
        {
            user = $scope.selectedUser;
        }
        if( user.web !== undefined && user.web.facebook !== undefined ){
            user.web.facebook.isAuthorized = "false";
        }

        userService.deleteUserFacebook( user[':name'] );
        FB.logout();
    };


    $scope.refreshFacebook = function ()
    {
        userService.refreshUserFacebook( $scope.selectedUser[':name'] );
    };


    var initFacebookSdk = function ()
    {

        FB.init({
            appId: '1459016164310867',
            status: false, // check login status
            cookie: false, // enable cookies to allow the server to access the session
            xfbml: true  // parse XFBML
        });

        // Here we subscribe to the auth.authResponseChange JavaScript event. This event is fired
        // for any authentication related change, such as login, logout or session refresh. This means that
        // whenever someone who was previously logged out tries to log in again, the correct case below
        // will be handled.
        FB.Event.subscribe('auth.authResponseChange', function (response)
        {
            // Here we specify what we do with the response anytime this event occurs.
            if (response.status === 'connected')
            {

            }
            else if (response.status === 'not_authorized')
            {
                // In this case, the person is logged into Facebook, but not into the app, so we call
                // FB.login() to prompt them to do so.
                // In real-life usage, you wouldn't want to immediately prompt someone to login
                // like this, for two reasons:
                // (1) JavaScript created popup windows are blocked by most browsers unless they
                // result from direct interaction from people using the app (such as a mouse click)
                // (2) it is a bad experience to be continually prompted to login upon page load.
                user.web.facebook.isAuthorized = false;
                $scope.$apply();
            }
            else
            {
                // In this case, the person is not logged into Facebook, so we call the login()
                // function to prompt them to do so. Note that at this stage there is no indication
                // of whether they are logged into the app. If they aren't then they'll see the Login
                // dialog right after they log in to Facebook.
                // The same caveats as above apply to the FB.login() call here.
                user.web.facebook.isAuthorized = false;
                $scope.$apply();
            }
        });

    };


    /**
     * Invoked on startup, like a constructor.
     */
    var init = function ()
    {


        if ($rootScope.user == null)
        {
            $state.go("login");
            return;
            // transitionTo() promise will be rejected with
            // a 'transition prevented' error
        }

        //call sling to get users
        refreshUsers();

    };
    init();

};

UserManagerController.$inject = ['$scope', '$rootScope', '$state', '$window', 'userService'];
module.exports = UserManagerController;