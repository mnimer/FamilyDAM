/*
 * This file is part of FamilyCloud Project.
 *
 *     The FamilyCloud Project is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     The FamilyCloud Project is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with the FamilyCloud Project.  If not, see <http://www.gnu.org/licenses/>.
 */
module.exports = angular.module('dashboard.photos.directives.slider', [])

.directive('columnSlider', function() {
    return {
        scope: {
            'columns':"@"
        },
        replace: true,
        link: function(scope, elem,attrs)
        {

            scope.$watch('columns', function(newValue, oldValue) {
                if( newValue.length > 0 )
                {
                    //scope.sliderInstance.attr("columns", newValue);
                    $(elem).slider({
                        value: newValue,
                        min: 2,
                        max: 12,
                        slide: function( event, ui ) {
                            //console.log(ui.value);
                            scope.$emit("photo:grid:columns", ui.value);
                        }
                    });


                }
            });
        }
    };
});