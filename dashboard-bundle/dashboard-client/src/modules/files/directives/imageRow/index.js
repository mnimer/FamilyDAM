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

var imageRowDirective = function ($compile, $parse, $state)
{
    return {
        scope: true,
        templateUrl: 'modules/files/directives/imageRow/row-image.tpl.html',
        link: function (scope, element, attrs)
        {
            //console.log('row data: ', scope.data);
            var delay = 300, clicks = 0, timer = null;

            var _previewFile = function (item_)
            {
                //scope.$emit("photo:preview", path_);
                $state.go("files.image:preview", {'path':item_.path});
            };

            var _selectFile = function (path_)
            {
                scope.$emit("photo:select", path_);
            };


            scope.handleClick = function (path_)
            {
                clicks++;  //count clicks
                if (clicks === 1)
                {
                    timer = setTimeout(function ()
                    {
                        scope.$apply(function ()
                        {
                            _previewFile(path_);
                        });
                        clicks = 0;             //after action performed, reset counter
                    }, delay);
                }
                else
                {
                    clearTimeout(timer);    //prevent single-click action
                    _selectFile(path_);
                    clicks = 0;             //after action performed, reset counter
                }
            };
        }
    };
};


imageRowDirective.$inject = ['$compile', '$parse', '$state'];
module.exports = imageRowDirective;