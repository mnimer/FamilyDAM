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

module.exports = function($compile) {
    return {
        scope: {
            inverted: '='
        },
        replace: true,
        templateUrl: 'modules/web/directives/timelineFacebookCheckin/row-facebook-checkin.tpl.html',
        controller: function ($scope, $element, $attrs, $transclude)
        {
            $scope.width = 300;//default
        },
        link: function(scope, element, attrs)
        {
            scope.item = scope.$parent.data;
            //scope.displayDate = moment("2011-01-01").fromNow();
            scope.width = element.width();
            //console.log("isInverted:" +inverted);
        }
    };
};
