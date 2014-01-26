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

module.exports = function() {
        return {
            scope: {
                'event':"@",
                'label':"@",
                'dt':"@"
            },
            replace: true,
            templateUrl: "modules/photos/directives/dateFilter/dateFilter.tpl.html",
            link: function(scope, elem,attrs)
            {
                var _event = "filter:date";

                scope.$watch('label', function(value, oldValue, scope)
                {
                    scope.label = value;
                });
                scope.$watch('event', function(value, oldValue, scope)
                {
                    _event = value;
                });
                // model
                scope.$watch('dt', function(value) {
                    scope.$emit(_event, value);
                });


                scope.open = function($event) {
                    $event.preventDefault();
                    $event.stopPropagation();

                    scope.opened = true;
                };

            }
        };
    };
