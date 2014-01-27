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

var ImagePreviewDirective = function($compile) {
    return {
        scope: true,
        template: "<img src='{{imagePath}}' style='margin: 0 auto; display: block;'>",
        link: function(scope, element, attrs)
        {
            scope.imagePath = attrs.path +".scale.w:" +element.parent().width() +".png";
            //attrs.src = attrs.path +".scale.w:200.png";
        }
    };
};


ImagePreviewDirective.$inject = ['$compile'];
module.exports = ImagePreviewDirective;