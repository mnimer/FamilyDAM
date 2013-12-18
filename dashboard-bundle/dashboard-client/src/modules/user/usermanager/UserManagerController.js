/**
 * Controller for the all customers screen
 * @param $scope
 * @param $location
 * @constructor
 */
var UserManagerController = function($scope, $location) {

    $scope.selectedUser = {};
    $scope.users = [
        {id:1, username:"Admin", firstName:"admin", lastName:""},
        {id:2, username:"Mike", firstName:"Mike", lastName:"Nimer"},
        {id:3, username:"Angie", firstName:"Angela", lastName:"Nimer"}
    ];
    $scope.currentUser = undefined;


    $scope.selectUser = function()
    {
        //todo call api to get user based on selectedUser id

        $scope.currentUser = $scope.selectedUser;

        //set value to currentUser
    };


    $scope.createUser = function()
    {
        $scope.currentUser = {};
    };


    $scope.removeUser = function()
    {

    };

    /**
     * Invoked on startup, like a constructor.
     */
    var init = function(){
        //todo: call sling to get users
    };
    init();
};

UserManagerController.$inject = ['$scope',  '$location'];
module.exports = UserManagerController;