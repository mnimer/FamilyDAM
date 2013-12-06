/*
 * This file is part of FamilyCloud Project.
 *
 *     The FamilyCloud Project is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     The FamilyCloud Project is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with the FamilyCloud Project.  If not, see <http://www.gnu.org/licenses/>.
 */

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


module.exports = angular.module('dashboard.photos.directives', ['angularFileUpload'])

    .directive('photoMetadata', function ()
    {
        return {
            scope: {
                'node':"@"
            },
            replace: true,
            transclude: false,
            templateUrl: "modules/photos/directives/metadata/metadata.tpl.html",
            link:function(scope, element, attrs, controller) {}
        };
    })


    .controller('FileUploadController', function ($scope, $fileUploader) {
        'use strict';


        uploader.bind('node', function (event, node) {
            console.log('All files are transferred' +node);
        });

    });




