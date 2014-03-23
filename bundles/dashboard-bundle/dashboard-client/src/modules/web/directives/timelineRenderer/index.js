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

var timelineRendererDirective = function ($compile)
{
    var inverted = true;
    var lastMixin = [];
    var lastType = "";

    var isInverted = function(mixins, data)
    {
        if (mixins !== undefined && mixins.indexOf("fd:facebook") != -1)
        {
            if( lastType != data.type )
            {
                lastType = data.type;
                inverted = !inverted;
                return inverted;
            }
        }

        // same type
        return inverted;
    };

    return {
        scope:true,
        priority: 1,
        replace:true,
        controller: function ($scope, $element, $attrs, $transclude)
        {
            //var data = $scope.$eval($attrs.data);
            //console.log(data);
            //console.log($attrs.data);
        },
        link: function (scope, element, attrs)
        {
            scope.data = scope.$eval(attrs.data);
            var type = scope.data['jcr:primaryType']; //get from scope.data
            var mixins = scope.data['jcr:mixinTypes']; //get from scope.data


            // default to generic file
            var compiled = ""; // unknown
             // override for different file types with special handling
            if (mixins !== undefined && mixins.indexOf("fd:facebook") != -1)
            {
                inverted = isInverted(mixins, scope.data);

                if( scope.data.type == "checkin" )
                {
                    compiled = "<li x-timeline-facebook-checkin inverted='" +inverted +"'/>";
                }else if( scope.data.type == "status" || scope.data.type == "photo" || scope.data.type == "video")
                {
                    compiled = "<li x-timeline-facebook-status inverted='" +inverted +"'/>";
                }

            }

            //console.log('compiled: ', $compile(compiled)(scope));
            element.replaceWith(  $compile(compiled)(scope) );
        }
    };
};


timelineRendererDirective.$inject = ['$compile'];
module.exports = timelineRendererDirective;