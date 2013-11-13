var FolderNameModalCntrl = function ($scope, $rootScope,  $modalInstance, photoService, currentPath) {

    $scope.formModel = {};
    $scope.currentPath = currentPath;
    $scope.photoService = photoService;

    $scope.createFolder = function ()
    {
        $scope.photoService.createFolder($scope.currentPath, $scope.formModel.path, function()
        {
            $rootScope.$broadcast('refresh');
            $modalInstance.close();
        }, function(err){
            //todo error handler
        });

    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
};

FolderNameModalCntrl.$inject = ['$scope', '$rootScope', '$modalInstance', 'photoService', 'currentPath'];
module.exports = FolderNameModalCntrl;