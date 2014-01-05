/**
 * Controller for the all customers screen
 * @param $scope
 * @param $location
 * @constructor
 */
var UserManagerController = function($scope, $rootScope, $state, userService) {

    $scope.isNewUser = false;
    $scope.isExistingUser = false;
    $scope.selectedUser = {};
    $scope.users = [];
    $scope.currentUser = undefined;


    var refreshUsers = function()
    {
        userService.listUsers().then(function(data, status, headers, config){
            var list = [];

            for(var user in data.data)
            {
                var u = {};
                u[':name'] = user; //special key defined by Sling
                // copy properties to
                for ( var prop in data.data[user] )
                {
                    u[prop] = data.data[user][prop];
                }

                list.push(u);
            }

            $scope.users = list;
        });
    };


    /**
     * UPDATE USER
     */
    $scope.selectUser = function()
    {
        $scope.isNewUser = false;
        $scope.isExistingUser = true;
        //todo call api to get user based on selectedUser id

        $scope.currentUser = $scope.selectedUser;

        //set value to currentUser
    };





    /***
     * NEW USER
     */
    $scope.createNewUser = function()
    {
        $scope.currentUser = {};
        $scope.isNewUser = true;
        $scope.isExistingUser = false;

    };

    $scope.createNewUserHandler = function()
    {
        userService.createUser($scope.currentUser).then(
            function(data, status, headers, config)
            {
                refreshUsers();

                $scope.isExistingUser = true;
                $scope.isNewUser = false;
            }, function(data, status, headers, config)
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

    $scope.updateUserHandler = function()
    {
        var username = $scope.currentUser[':name'];

        //Copy the updateable properties. If we pass all back we'll get a 500 error.
        var userProps = {};
        for( var prop in $scope.currentUser )
        {
            if( prop != "memberOf" && prop != "declaredMemberOf" && prop != "pwd" && prop != "pwdConfirm" )
            {
                userProps[prop] = $scope.currentUser[prop];
            }
        }


        userService.updateUser(username, userProps).then(
            function(data, status, headers, config)
            {
                $scope.isExistingUser = true;
                $scope.isNewUser = false;
            }, function(data, status, headers, config)
            {
                console.log(data);
                //todo : show error to client
            });
    };



    /**
     * REMOVE USER
     */
    $scope.removeUserHandler = function()
    {
        userService.removeUser($scope.selectedUser[':name']).then(
            function(data, status, headers, config)
            {
                refreshUsers();

                $scope.currentUser = undefined;
                $scope.isNewUser = false;
                $scope.isExistingUser = false;
            },
            function(data, status, headers, config)
            {
                console.log(data);
                //todo : show error to client
            });
    };




    /**
     * Invoked on startup, like a constructor.
     */
    var init = function(){

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

UserManagerController.$inject = ['$scope', '$rootScope',  '$state', 'userService'];
module.exports = UserManagerController;