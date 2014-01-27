/*
 * This file is part of FamilyDAM Project.
 *
 *     The FamilyDAM Project is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     The FamilyDAM Project is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with the FamilyDAM Project.  If not, see <http://www.gnu.org/licenses/>.
 */

var FileListController = function ($scope, $rootScope, $location, $modal, $state, $stateParams, $q, fileService)
{
    $scope.assetCountLabel = "loading...";
    $scope.selectedPaths = [];
    var rootPath = "/content/dam";
    $scope.currentPath = rootPath;
    $scope.showUploadSidebar = false;

    //Toolbar button visible flags
    $scope.showPhotoGrid = false;


    $scope.$on("pathChange", function (event, path)
    {
        $scope.currentPath = path;
        // update list of photos
        $scope.assets = [];
        $scope.photos = fileService.list(path).then(listCallback);
    });

    $scope.$on("refresh", function (event, path)
    {
        $scope.selectFolder($scope.currentPath);
    });


    $scope.toggleFolder = function (event, path)
    {
        var pos = $scope.selectedPaths.indexOf(path);

        if (pos == -1)
        {
            $(event.target).closest("tr").addClass("success");
            $scope.selectedPaths.push(path);
        }
        else
        {
            $(event.target).closest("tr").removeClass("success");
            $scope.selectedPaths.splice(pos, 1);
        }
    };


    $scope.toggleUpload = function ()
    {
        $state.go("files.upload");
    };


    $scope.openCreateFolder = function ()
    {
        var modalInstance = $modal.open({
            templateUrl: 'FolderNameModal',
            controller: 'FolderNameModalCntrl',
            resolve: {
                fileService: function ()
                {
                    return fileService;
                },
                currentPath: function ()
                {
                    return $scope.currentPath;
                }
            }
        });
    };


    $scope.deletePaths = function ()
    {
        var dialog = $modal.open({
            templateUrl: 'DeleteConfirmationDialog',
            controller: function ($scope, $modalInstance, items)
            {
                $scope.items = items;

                $scope.ok = function ()
                {
                    $modalInstance.close($scope.items);
                };

                $scope.cancel = function ()
                {
                    $modalInstance.dismiss('cancel');
                };
            },
            resolve: {
                items: function ()
                {
                    return $scope.selectedPaths;
                }
            }
        });

        dialog.result.then(function (paths)
        {
            var promises = [];
            var deferred = $q.defer();
            var promise = deferred.promise;

            for (var i = 0; i < paths.length; i++)
            {
                promises.push(fileService.deletePath(paths[i]));
            }

            $q.all(promises).then(function ()
            {
                $scope.selectFolder($scope.currentPath);
            });
        });
    };


    /**
     * Parse a path into it's tokens for a valid breadcrumb array
     */
    $scope.selectFile = function (path)
    {
        //todo
    };


    $scope.selectFolder = function (path)
    {
        if (path == "/")
        {
            path = rootPath;
        }

        //reset loading label
        $scope.assetCountLabel = "loading...";//todo localize

        // clear out selected paths. We don't save while you are drilling down.
        $scope.selectedPaths = [];

        // this is called from child view, so we'll update the parent/child scopes to get the breadcrumb binding
        $scope.$broadcast("pathChange", path);
        //$scope. $emit("pathChange", path);

        $scope.currentPath = path;
        updateBreadcrumb(path);
    };


    $scope.sortPrimaryType = function (item, arg2, arg3)
    {
        var type = "bFile";
        if (item['jcr:primaryType'] == 'nt:folder' || item['jcr:primaryType'] == 'sling:Folder')
        {
            type = "aFolder";
        }

        return type + "|" + item['name'];
    };


    var updateBreadcrumb = function (path)
    {
        var hiddenNodes = rootPath.split("/");


        var breadcrumb = [];
        var nodes = path.split("/");
        var lastPath = "";
        for (var indx in nodes)
        {

            var obj = nodes[indx];
            if (obj.length > 1)
            {
                lastPath = lastPath + "/" + obj;
                if (indx >= hiddenNodes.length - 1)
                {
                    breadcrumb.push({name: obj, path: lastPath});
                }
            }

        }
        $scope.breadcrumb = breadcrumb;
    };


    /**
     * Callback for file list from JCR Service
     * @param data
     * @param status
     * @param headers
     * @param config
     */
    var listCallback = function (data)
    {
        var contents = [];
        var pos = data.config.url.indexOf(".1.json");
        //var pos2 = $scope.basePath.length;
        var basePath = data.config.url.substring(0, pos);


        for (var key in data.data)
        {
            var item = data.data[key];
            if (item instanceof Object && isSupportedType(item))
            {
                item.name = key;
                item.path = basePath + "/" + key;

                if (item.name.substring(0, 1) != ".") // hide hidden files and only support folders/files (not jcr properties)
                {
                    contents.push(item);
                }
            }
        }

        $scope.assetCountLabel = contents.length + " items";//todo localize
        $scope.assets = contents;
    };


    /**
     * We only want to let folders or files show up in the tree. Not JCR properties
     * @param item
     * @returns {boolean}
     */
    var isSupportedType = function (item)
    {
        var isOk = item['jcr:primaryType'] == "sling:Folder" || item['jcr:primaryType'] == "nt:Folder" || item['jcr:primaryType'] == "nt:file";
        return isOk;
    };



    var init = function ()
    {
        if ($rootScope.user == null)
        {
            $state.go("login");
            return;
            // transitionTo() promise will be rejected with
            // a 'transition prevented' error
        }


        /**
         * Copy state properties to the scope, if it's defined
         * This is used by some of the other modules who are reusing this view.
         * **/
        if( $state.current.data !== undefined )
        {
            for(var prop in $state.current.data )
            {
                $scope[prop] = $state.current.data[prop];

                // If we are reusing this in a section (photos, music, etc) we want to reset the rootPath, so users can't browse higher
                // then the section allows.
                if( prop == "currentPath" )
                {
                    rootPath = $state.current.data[prop];
                }
            }
        }


        $scope.showSidebar = true;
        $scope.showUploadSidebar = true;
        $scope.selectFolder($scope.currentPath);
    };
    init();

};

FileListController.$inject = ['$scope', '$rootScope', '$location', '$modal', '$state', '$stateParams', '$q', 'fileService'];
module.exports = FileListController;