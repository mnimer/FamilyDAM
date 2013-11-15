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
                title:'@'
            },
            replace: true,
            transclude: false,
            templateUrl: "directives/fileUpload/fileUpload.tpl.html",
            link:function(scope, element, attrs, controller) {}
        };
    })


    .controller('FileUploadController', function ($scope, $fileUploader) {
        'use strict';

        // create a uploader with options
        var uploader = $scope.uploader = $fileUploader.create({
            scope: $scope,                          // to automatically update the html. Default: $rootScope
            url: '/content/dam/upload/queue',
            alias: './*',
            formData: [{}],
            filters: []
        });


        // REGISTER HANDLERS
        uploader.bind('afteraddingfile', function (event, item) {
            console.log('After adding a file', item);
        });

        uploader.bind('afteraddingall', function (event, items) {
            console.log('After adding all files', items);
        });

        uploader.bind('changedqueue', function (event, items) {
            console.log('Changed queue', items);
        });

        uploader.bind('beforeupload', function (event, item) {
            console.log('Before upload', item);
        });

        uploader.bind('progress', function (event, item, progress) {
            console.log('Progress: ' + progress, item);
        });

        uploader.bind('success', function (event, xhr, item) {
            console.log('Success: ' + xhr.response, item);
        });

        uploader.bind('complete', function (event, xhr, item) {
            console.log('Complete: ' + xhr.response, item);
        });

        uploader.bind('progressall', function (event, progress) {
            console.log('Total progress: ' + progress);
        });

        uploader.bind('completeall', function (event, items) {
            console.log('All files are transferred', items);
        });

    });




