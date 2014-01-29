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

var MusicPreviewDirective = function($compile, $parse, $stateParams) {
    return {
        scope: true,
        replace:true,
        templateUrl:'modules/files/directives/musicPlayer/music-player.tpl.html',
        compile: function compile(element, attributes)
        {
            return {
                pre: function preLink(scope, element, attributes) {
                    var _path = $stateParams["path"];
                    scope.songPath = _path;
                },
                post: function postLink(scope, element, attributes) {

                }
            };

        }
    };
};


MusicPreviewDirective.$inject = ['$compile','$parse', '$stateParams'];
module.exports = MusicPreviewDirective;