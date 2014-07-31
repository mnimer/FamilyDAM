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

var rendererDirective = function ($compile)
{
    return {
        scope:true,
        priority: 1,
        replace:true,
        link: function (scope, element, attrs)
        {
            scope.data = scope.$eval(attrs.data);
            var type = scope.data['jcr:primaryType']; //get from scope.data
            var mixins = scope.data['jcr:mixinTypes']; //get from scope.data

            // default to generic file
            var compiled = "<div x-file-row />";
            // override for different file types with special handling
            if (type == "nt:Folder" || type == "sling:Folder")
            {
                compiled = "<div x-folder-row />";
            }
            else if (mixins !== undefined && mixins.indexOf("fd:image") != -1)
            {
                compiled = "<div x-image-row />";
            }
            else if (mixins !== undefined && mixins.indexOf("fd:song") != -1)
            {
                compiled = "<div x-music-row />";
            }
            else if (mixins !== undefined && mixins.indexOf("fd:movie") != -1)
            {
                compiled = "<div x-video-row />";
            }

            //console.log('compiled: ', $compile(compiled)(scope));
            element.append(  $compile(compiled)(scope) );
        }
    };
};


rendererDirective.$inject = ['$compile'];
module.exports = rendererDirective;