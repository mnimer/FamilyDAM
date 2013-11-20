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
     * Callback for file list from JCR Service
     * @param data
     * @param status
     * @param headers
     * @param config
     */
    var listCallback = function(data, status, headers, config)
    {
        var contents = [];
        var pos = config.url.indexOf(".1.json");
        var pos2 = $scope.basePath.length;
        var basePath = config.url.substring(pos2, pos);

        for(var key in data)
        {
            var item = data[key];
            if( item instanceof Object )
            {
                item.name = key;
                item.path = basePath +"/" +key;

                contents.push(item);
            }
        }

        $scope.assets = contents;
    };


    var init = function(){
        if( $rootScope.user == null )
        {
            $state.go("login");
            return;
            // transitionTo() promise will be rejected with
            // a 'transition prevented' error
        }
        $scope.photos = photoService.list('/photos', listCallback);
    };
    init();

};

PhotosController.$inject = ['$scope', '$rootScope', '$location', '$modal', '$state', 'photoService'];
module.exports = PhotosController;