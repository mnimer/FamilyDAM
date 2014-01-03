/*
 * jQuery File Upload AngularJS Plugin 2.0.1
 * https://github.com/blueimp/jQuery-File-Upload
 *
 * Copyright 2013, Sebastian Tschan
 * https://blueimp.net
 *
 * Licensed under the MIT license:
 * http://www.opensource.org/licenses/MIT
 */

/*jslint nomen: true, unparam: true */
/*global define, angular */


module.exports = angular.module('FamilyCloud.FileUpload', ['angularFileUpload'])

    .directive('fileUploadForm', function ()
    {
        return {
            scope: {
                'url':"@",
                'path':"@",
                title:'@'
            },
            replace: true,
            transclude: false,
            templateUrl: "directives/fileUpload/fileUpload.tpl.html",
            link:function(scope, element, attrs, controller)
            {
                scope.$watch('path', function(newValue, oldValue) {
                    if( newValue.length > 0 )
                    {
                        controller.setUploadPath(newValue);
                    }
                });
            },
            controller:function ($scope, $fileUploader) {
                'use strict';

                $scope.uploadPath = "/content/dam";

                // create a uploader with options
                $scope.uploader = $scope.uploader = $fileUploader.create({
                    scope: $scope,                          // to automatically update the html. Default: $rootScope
                    url: $scope.uploadPath,
                    alias: './*',
                    formData: [{'author': $scope.$root.username}],
                    filters: [],
                    removeAfterUpload:true
                });

                this.setUploadPath = function(val)
                {
                    $scope.uploadPath = val;
                    $scope.uploader.url = val;
                };


                // REGISTER HANDLERS
                $scope.uploader.bind('afteraddingfile', function (event, item) {
                    console.log('After adding a file', item);
                });

                $scope.uploader.bind('afteraddingall', function (event, items) {
                    console.log('After adding all files', items);

                });

                $scope.uploader.bind('changedqueue', function (event, items) {
                    console.log('Changed queue', items);
                });

                $scope.uploader.bind('beforeupload', function (event, item) {
                    console.log('Before upload', item);
                });

                $scope.uploader.bind('progress', function (event, item, progress) {
                    console.log('Progress: ' + progress, item);
                });

                $scope.uploader.bind('success', function (event, xhr, item) {
                    console.log('Success: ' + xhr.response, item);
                });

                $scope.uploader.bind('complete', function (event, xhr, item) {
                    console.log('Complete: ' + xhr.response, item);
                });

                $scope.uploader.bind('progressall', function (event, progress) {
                    console.log('Total progress: ' + progress);
                });

                $scope.uploader.bind('completeall', function (event, items) {
                    console.log('All files are transferred', items);

                    // send refresh folder event
                    $scope.$emit("refresh");
                });

            }
        };
    });




