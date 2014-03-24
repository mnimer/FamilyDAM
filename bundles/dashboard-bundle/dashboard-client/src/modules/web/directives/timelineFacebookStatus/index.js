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

var timelineFacebookStatus = function($compile) {
    return {
        scope: {
            inverted: '='
        },
        replace: true,
        templateUrl: 'modules/web/directives/timelineFacebookStatus/row-facebook-status.tpl.html',
        controller: function($scope)
        {
            $scope.config = {
                autoHide: false,
                autoPlay: false,
                responsive: true,
                transclude: true,
                theme: {
                    url: "assets/css/videogular.css",
                    playIcon: "&#xe000;",
                    pauseIcon: "&#xe001;",
                    volumeLevel3Icon: "&#xe002;",
                    volumeLevel2Icon: "&#xe003;",
                    volumeLevel1Icon: "&#xe004;",
                    volumeLevel0Icon: "&#xe005;",
                    muteIcon: "&#xe006;",
                    enterFullScreenIcon: "&#xe007;",
                    exitFullScreenIcon: "&#xe008;"
                },
                plugins: {
                    poster: {
                        url: "assets/images/logo.png"
                    }
                }
            };
        },
        compile: function(tElement, tAttrs, transclude)
        {
            console.log('Compile()');

            return {
                pre: function(scope, iElement, iAttrs, controller) {
                    scope.item = scope.$parent.data;
                },
                post: function(scope, iElement, iAttrs, controller) {
                    //
                }
            };
        }
    };
};


timelineFacebookStatus.$inject = ['$compile'];
module.exports = timelineFacebookStatus;