
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

var FileListController = function($scope, $rootScope, $location, $modal, $state, fileService)
{
    var rootPath = "/content/dam";
    $scope.currentPath = rootPath;
    $scope.showUploadSidebar = false;


    $scope.$on("pathChange", function(event, path)
    {
        $scope.currentPath = path;
        // update list of photos
        $scope.assets = [];
        $scope.photos = fileService.list(path).then(listCallback);
    });

    $scope.$on("refresh", function(event, path)
    {
        $scope.selectFolder($scope.currentPath);
    });




    $scope.toggleUpload = function()
    {
        var b = !$scope.showUploadSidebar;
        //show or hide the sidebar div, based on toggle
        $scope.showSidebar = b;
        $scope.showUploadSidebar = b;
    };


    $scope.openCreateFolder = function()
    {
        var modalInstance = $modal.open({
            templateUrl: 'FolderNameModal',
            controller: 'FolderNameModalCntrl',
            resolve:{
                fileService: function(){
                    return fileService;
                },
                currentPath: function(){
                    return $scope.currentPath;
                }
            }
        });
    };



    /**
     * Parse a path into it's tokens for a valid breadcrumb array
     */
    $scope.selectFolder = function(path)
    {
        if( path == "/")
        {
            path = rootPath;
        }

        // this is called from child view, so we'll update the parent/child scopes to get the breadcrumb binding
        $scope. $broadcast("pathChange", path);
        //$scope. $emit("pathChange", path);

        $scope.currentPath = path;
        updateBreadcrumb(path);
    };


    $scope.sortPrimaryType = function(item, arg2, arg3)
    {
        var type = "bFile";
        if( item['jcr:primaryType'] == 'nt:folder' || item['jcr:primaryType'] == 'sling:Folder' )
        {
            type = "aFolder";
        }

        return type +"|" +item['name'];
    };



    var updateBreadcrumb = function(path)
    {
        var hiddenNodes = rootPath.split("/");


        var breadcrumb = [];
        var nodes = path.split("/");
        var lastPath = "";
        for (var indx in nodes)
        {

            var obj = nodes[indx];
            if (obj.length > 1) {
                lastPath = lastPath + "/" + obj;
                if( indx >= hiddenNodes.length-1 )
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
    var listCallback = function(data)
    {
        var contents = [];
        var pos = data.config.url.indexOf(".1.json");
        //var pos2 = $scope.basePath.length;
        var basePath = data.config.url.substring(0, pos);


        for(var key in data.data)
        {
            var item = data.data[key];
            if( item instanceof Object && isSupportedType(item))
            {
                item.name = key;
                item.path = basePath +"/" +key;

                if( item.name.substring(0,1) != ".") // hide hidden files and only support folders/files (not jcr properties)
                {
                    contents.push(item);
                }
            }
        }

        $scope.assets = contents;
    };


    /**
     * We only want to let folders or files show up in the tree. Not JCR properties
     * @param item
     * @returns {boolean}
     */
    var isSupportedType = function(item)
    {
        var isOk = item['jcr:primaryType'] == "sling:Folder" || item['jcr:primaryType'] == "nt:Folder"  || item['jcr:primaryType'] == "nt:file";
        return isOk;
    };


    var init = function()
    {
        if( $rootScope.user == null )
        {
            $state.go("login");
            return;
            // transitionTo() promise will be rejected with
            // a 'transition prevented' error
        }
        $scope.showSidebar = true;
        $scope.showUploadSidebar = true;
        $scope.photos = fileService.list(rootPath).then(listCallback);
    };
    init();

};

FileListController.$inject = ['$scope', '$rootScope', '$location', '$modal', '$state', 'fileService'];
module.exports = FileListController;