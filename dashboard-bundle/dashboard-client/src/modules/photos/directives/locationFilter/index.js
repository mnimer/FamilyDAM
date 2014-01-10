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

var locationDirective = function(fileService) {
        return {
            scope: {
                'event':"@",
                'label':"@",
                'root':"@",
                'path':"@"
            },
            replace: true,
            templateUrl: "modules/photos/directives/locationFilter/locationFilter.tpl.html",
            link: function(scope, elem,attrs)
            {
                var _event = "filter:date";
                var _basePath = "/content/dam";

                // default options for tree control
                scope.treeOptions = {
                    nodeChildren: "children",
                    dirSelectable: true,
                    injectClasses: {
                        ul: "",
                        li: "",
                        iExpanded: "glyphicon glyphicon-minus ",
                        iCollapsed: "glyphicon glyphicon-plus ",
                        iLeaf: "glyphicon glyphicon-minus",
                        label: ""
                    }
                };


                scope.$watch('label', function(value, oldValue, scope)
                {
                    scope.label = value;
                });
                scope.$watch('event', function(value, oldValue, scope)
                {
                    _event = value;
                });
                scope.$watch('root', function(value, oldValue, scope)
                {
                    _basePath = value;

                    if( value !== undefined )
                    {
                        fileService.listFolders(value).then(function(paths){
                            console.log(paths);
                            scope.assets = paths.children;
                        });
                    }
                });


                scope.$watch('path', function(value, oldValue, scope)
                {
                    scope.$emit(_event, value);
                });

                scope.selectFolder = function(value) {
                    scope.$emit(this.event, value.path);
                };

            }

        };
    };


locationDirective.$inject = ['fileService'];
module.exports = locationDirective;