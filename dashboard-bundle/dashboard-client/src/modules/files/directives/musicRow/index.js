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

var musicRowDirective = function($rootScope, $compile, $state) {
    return {
        scope: true,
        replace: true,
        templateUrl: 'modules/files/directives/musicRow/row-music.tpl.html',
        link: function(scope, element, attrs) {
            //console.log('row data: ', scope.data);
            var delay = 300, clicks = 0, timer = null;

            var _previewFile = function (item_)
            {
                //scope.$emit("photo:preview", path_);
                $rootScope.selectedNode = item_;
                //$state.go("files.music:preview", {'node':item_});
            };

            var _selectFile = function (item_)
            {
                scope.$emit("music:select",  {'path':item_.path, 'item':item_});
            };

            scope.handleClick = function (item_)
            {
                clicks++;  //count clicks
                if (clicks === 1)
                {
                    timer = setTimeout(function ()
                    {
                        scope.$apply(function ()
                        {
                            _previewFile(item_);
                        });
                        clicks = 0;             //after action performed, reset counter
                    }, delay);
                }
                else
                {
                    clearTimeout(timer);    //prevent single-click action
                    _selectFile(item_);
                    clicks = 0;             //after action performed, reset counter
                }
            };

            /**
            scope.play = function(item_, event)
            {
                $state.go("files.music:preview", {'path':item_.path});
                event.stopPropagation();
            };
             **/

        }
    };
};


musicRowDirective.$inject = ['$rootScope', '$compile', '$state'];
module.exports = musicRowDirective;