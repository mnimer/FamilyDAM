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

var timelineDirective = function (searchService)
{

    return {
        scope: {
            'rootPath': "@"
        },
        replace: true,
        templateUrl: "modules/web/directives/timeline/timeline.tpl.html",
        controller: function ($scope, $element, $attrs, $transclude)
        {
            $scope.search = function()
            {
                if( this.rootPath === undefined )
                {
                    this.rootPath = "/content/dam";
                }

                //type, limit, offset, filterPath, filterDateFrom, filterDateTo, filterTags
                searchService.search("fd:content", 1000, 0, this.rootPath, undefined, undefined, undefined).then(function(result){
                    $scope.content = result;
                });
            };


            $scope.search();
        },
        link: function (scope, elem, attrs)
        {
            // Initial WATCHER for default property
            scope.$watch('rootPath', function (newVal, oldVal)
            {
                if (oldVal == newVal) return;

                scope.search();
            });
        }

    };
};

timelineDirective.$inject = ['searchService'];
module.exports = timelineDirective;