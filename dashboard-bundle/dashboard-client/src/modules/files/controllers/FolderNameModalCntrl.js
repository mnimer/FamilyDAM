var FolderNameModalCntrl = function ($scope, $rootScope,  $modalInstance, fileService, currentPath) {

    $scope.formModel = {};
    $scope.currentPath = currentPath;
    $scope.fileService = fileService;

    $scope.createFolder = function ()
    {
        $scope.fileService.createFolder($scope.currentPath, $scope.formModel.path).then(function()
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

FolderNameModalCntrl.$inject = ['$scope', '$rootScope', '$modalInstance', 'fileService', 'currentPath'];
module.exports = FolderNameModalCntrl;