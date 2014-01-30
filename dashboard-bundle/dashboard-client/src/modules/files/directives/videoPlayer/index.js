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

var MusicPreviewDirective = function($rootScope, $compile, $parse, $stateParams) {
    return {
        scope: {
            'node':'@'
        },
        replace:true,
        templateUrl:'modules/files/directives/videoPlayer/video-player.tpl.html',
        compile: function compile(element, attributes)
        {
            return {
                pre: function preLink(scope, element, attributes) {

                    scope.$watch("node", function(nv, ov){
                        if( nv != "" && nv != undefined )
                        {
                            var _item = angular.fromJson(nv);
                            scope.videoPath = _item.path;
                        }
                    });
                },
                post: function postLink(scope, element, attributes) {

                }
            };

        }
    };
};


MusicPreviewDirective.$inject = ['$rootScope', '$compile','$parse', '$stateParams'];
module.exports = MusicPreviewDirective;