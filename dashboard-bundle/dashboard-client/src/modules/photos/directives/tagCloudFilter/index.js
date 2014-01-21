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

var tagCloudDirective = function(metadataService) {
    return {
        scope: {
            'event':"@",
            'label':"@",
            'root':"@"
        },
        replace: true,
        templateUrl: "modules/photos/directives/tagCloudFilter/tagCloud.tpl.html",
        controller: function($scope)
        {
            $scope.selectTag = function(tag)
            {
                $scope.$emit($scope.event, tag);
            };
        },
        link: function(scope, elem,attrs)
        {
            scope.event = "filter:date";
            scope.keywords = [];

            scope.$watch('label', function(value, oldValue, scope)
            {
                scope.label = value;
            });
            scope.$watch('event', function(value, oldValue, scope)
            {
                scope.event = value;
            });

            scope.$watch('root', function (value, oldValue, scope)
            {
                _path = value;
                if( value !== undefined )
                {
                    metadataService.keywordsByPath(value).then(function (data)
                    {
                        _keywords = data;
                        scope.keywords = data;
                    });
                }
            });



        }
    };
};


tagCloudDirective.$inject = ['metadataService'];
module.exports = tagCloudDirective;