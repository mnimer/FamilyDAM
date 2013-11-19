/**
 * Module definition for the Add To WaitList popup.  This class will also define the url path that triggers this screen.
 */
module.exports = angular.module('familycloud.FileUploadModal', ['$scope', '$modalInstance'])
    .controller('fileUploadModalController', function ($scope, $modalInstance)
    {
        $scope.close = function ()
        {
            $modalInstance.dismiss('cancel');
        };
    });

