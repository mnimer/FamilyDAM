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

var ImagePreviewDirective = function($rootScope, $compile, $state, $stateParams) {
    return {
        scope: {
            'node': '@'
        },
        template: "<img src='{{imagePath}}' style='margin: 0 auto; margin-top: 10px; display: block;'/>",
        link: function(scope, element, attrs)
        {
            scope.$watch("node", function(nv, ov){
                if( nv != "" && nv != undefined )
                {
                    var _item = angular.fromJson(nv);
                    scope.imagePath = _item.path +".scale.w:" +element.parent().width() +".png";
                    //attrs.src = attrs.path +".scale.w:200.png";
                }
            });
        }
    };
};


ImagePreviewDirective.$inject = ['$rootScope', '$compile', '$state', '$stateParams'];
module.exports = ImagePreviewDirective;