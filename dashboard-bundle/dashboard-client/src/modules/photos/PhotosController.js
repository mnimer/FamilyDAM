/**
 * Controller for the all customers screen
 * @param $scope
 * @param $location
 * @param customerModel
 * @param productModel
 * @constructor
 */
var PhotosController = function($scope, $location, photoService) {

    $scope.layout = "grid";
    $scope.assets = [];

    $scope.selectFolder = function(path)
    {
        $scope.photos = photoService.list(path, listCallback);
    };

    /**
     * Invoked on startup, like a constructor.
     */
    $scope.$on('$viewContentLoaded', function() {
        console.log("Photo controller loaded");
    });

    $scope.$on('$stateChangeStart',
        function(evt, toState, toParams, fromState, fromParams){

        }
    );

    var listCallback = function(data, status, headers, config)
    {
        var contents = [];
        var pos = config.url.indexOf(".1.json");
        var basePath = config.url.substring(0, pos);

        for(var key in data)
        {
            var item = data[key];
            if( item instanceof Object )
            {
                item.name = key;
                item.path = basePath +"/" +key;

                if( item['jcr.primaryType'] == "nt:folder")
                {
                    item.children = [];
                }

                contents.push(item);
            }
        }

        $scope.assets = contents;
    };



    var init = function(){
        $scope.photos = photoService.list('/content/dam/photos', listCallback);
    };
    init();
};

PhotosController.$inject = ['$scope', '$location', 'photoService'];
module.exports = PhotosController;