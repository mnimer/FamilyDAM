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

/**
 *
 */


module.exports = angular.module('dashboard.web', ['ui.bootstrap'])

    .directive('timeline', require('./directives/timeline'))
    .directive('timelineRenderer', require('./directives/timelineRenderer'))
    .directive('timelineFacebookCheckin', require('./directives/timelineFacebookCheckin'))
    .directive('timelineFacebookPhoto', require('./directives/timelineFacebookPhoto'))
    .directive('timelineFacebookStatus', require('./directives/timelineFacebookStatus'))

    .filter('fromNow', function() {
        return function(dateString) {
            return moment(dateString).fromNow();
        };
    })

    .config(['$stateProvider', function($stateProvider)
    {
        $stateProvider
            .state('web',
            {
                url:'/web',
                templateUrl: "modules/web/web.tpl.html"
            });
    }]);
