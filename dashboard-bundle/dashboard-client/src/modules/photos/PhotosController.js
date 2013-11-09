/**
 * Controller for the all customers screen
 * @param $scope
 * @param $location
 * @param customerModel
 * @param productModel
 * @constructor
 */
var PhotosController = function($scope, $location) {

    $scope.layout = "grid";
    /**
     * Invoked on startup, like a constructor.
     */
    $scope.$on('$viewContentLoaded', function() {
        console.log("Photo controller loaded");
    });

    var init = function(){

    };
    init();
};

PhotosController.$inject = ['$scope',  '$location'];
module.exports = PhotosController;