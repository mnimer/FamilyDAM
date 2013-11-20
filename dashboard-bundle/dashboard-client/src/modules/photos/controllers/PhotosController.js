var PhotosController = function($scope, $rootScope, $location, $modal, $state, photoService) {

    $scope.layout = "grid";
    $scope.assets = [];
    // part of the path we hide in the breadrum
    $scope.basePath = "/content/dam";
    // path to show in breadcrumb
    $scope.currentPath = "/";
    $scope.breadcrumb = [{name:"photos", path:$scope.basePath +"/photos"}];


    $scope.selectFolder = function(path)
    {
        // this is called from child view, so we'll update the parent scope to get the breadcrumb binding
        $scope.breadcrumb = convertPathToBreadcrumb(path);
        $scope.currentPath = path;
        // update list of photos
        $scope.assets = [];
        $scope.photos = photoService.list(path, listCallback);
    };




    $scope.createFolder = function()
    {
        var modalInstance = $modal.open({
            templateUrl: 'FolderNameModal',
            controller: 'FolderNameModalCntrl',
            resolve:{
                photoService: function(){
                    return photoService;
                },
                currentPath: function(){
                    if( $scope.currentPath == "/" )
                    {
                        return "/photos";
                    }else{
                        return $scope.currentPath;
                    }
                }
            }
        });
    };


    /*************
     * Event Listeners
     **************/



    $scope.$on('$viewContentLoaded', function(event, toState, toParams, fromState, fromParams)
    {
        if( $rootScope.user == null )
        {
            event.preventDefault();
            // transitionTo() promise will be rejected with
            // a 'transition prevented' error
        }
    });


    /**
     * Parse a path into it's tokens for a valid breadcrumb array
     */
    var convertPathToBreadcrumb = function(path)
    {
        var breadcrumb = [];
        var nodes = path.split("/");
        var lastPath = "";
        for (var indx in nodes)
        {
            var obj = nodes[indx];
            if( obj.length > 1 )
            {
                lastPath = lastPath +"/" +obj;
                breadcrumb.push({name:obj, path:lastPath});
            }
        }
        return breadcrumb;
    };




    var init = function(){
        if( $rootScope.user == null )
        {
            $state.go("login");
            return;
            // transitionTo() promise will be rejected with
            // a 'transition prevented' error
        }
    };
    init();
};

PhotosController.$inject = ['$scope', '$rootScope', '$location', '$modal', '$state', 'photoService'];
module.exports = PhotosController;