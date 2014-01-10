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

module.exports = function() {
    return {
        scope: {
            'event':"@",
            'label':"@"
        },
        replace: true,
        templateUrl: "modules/photos/directives/tagFilter/tagFilter.tpl.html",
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

            scope.selectTag = function(tag)
            {
                scope.$emit(this.event, tag);
            };

        }
    };
};
