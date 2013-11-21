var PhotosController = function($scope, $rootScope, $location, $modal, $state, photoService) {

    /*************
     * Event Listeners
     **************/

    $scope.$on('refresh', $scope.refresh);


    $scope.refresh = function()
    {
        if( $scope.currentPath == "/" )
        {
            $scope.selectFolder("/photos");
        }else{
            $scope.selectFolder($scope.currentPath);
        }
    };

    /**
     * Callback for search query from JCR Service
     * @param data
     * @param status
     * @param headers
     * @param config
     */
    var searchCallback = function(data, status, headers, config)
    {
        $scope.assets = data.data.data;
        // hateoas links
        $scope.self = data.data.links.self;
        $scope.next = data.data.links.next;
        $scope.prev = data.data.links.prev;
    };



    var init = function(){
        if( $rootScope.user == null )
        {
            $state.go("login");
            return;
            // transitionTo() promise will be rejected with
            // a 'transition prevented' error
        }
        var request = photoService.search(20, 1);
        request.then( searchCallback );
    };
    init();

};

PhotosController.$inject = ['$scope', '$rootScope', '$location', '$modal', '$state', 'photoService'];
module.exports = PhotosController;