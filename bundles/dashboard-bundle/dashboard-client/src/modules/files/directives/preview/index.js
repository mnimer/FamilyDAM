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

var previewDirective = function ($compile)
{


    var getImageHtml = function(src, element)
    {
        return "<img src='" +src +".scale.w:" +Math.max(300, element.width()) +".png' style='width:100%'/>";
    };

    var getMusicHtml = function(src)
    {
        return "<audio controls src='" +src +"' style='width:100%'/>";
    };

    var getVideoHtml = function(src)
    {
        return "<video controls src='" +src +"' style='width:100%'/>";
    };

    return {
        scope:{
            'path':'@',
            'node':'@'
        },
        priority: 1,
        replace:true,
        link: function (scope, element, attrs)
        {

            scope.$watch("node", function(newValue, oldValue )
            {
                // default to generic file
                var template = "<b>No Preview Available</b>";

                if( newValue !== undefined && newValue !== "" )
                {
                    var node = angular.fromJson(newValue);
                    var mixins = node['jcr:mixinTypes']; //get from scope.data

                    if (mixins !== undefined && mixins.indexOf("fd:image") != -1)
                    {
                        template = getImageHtml(node.path, element);
                    }
                    else if (mixins !== undefined && mixins.indexOf("fd:song") != -1)
                    {
                        template = getMusicHtml(node.path, element);
                    }
                    else if (mixins !== undefined && mixins.indexOf("fd:movie") != -1)
                    {
                        template = getVideoHtml(node.path, element);
                    }
                }

                element.empty();
                element.html(template);
                $compile(element.contents())(scope);
            });
        }
    };
};


previewDirective.$inject = ['$compile'];
module.exports = previewDirective;