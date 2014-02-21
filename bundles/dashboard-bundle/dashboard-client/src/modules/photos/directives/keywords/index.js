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


var keywordsDirective = function (metadataService)
{
    return {
        scope: {
            'items': "@",
            'tagList': "@",
            'label': "@",
            'path': "@"
        },
        replace: true,
        templateUrl: "modules/photos/directives/keywords/keywords.tpl.html",
        controller: function ($scope, $element, $attrs, $transclude)
        {
            $scope.query = function (query)
            {
                var words = {results: []};

                for (var indx in $scope.tags)
                {
                    var item = $scope.tags[indx];
                    if (item.word !== undefined && item.word.substring(0, query.term.length) == query.term)
                    {
                        words.results.push({'id': item.word, 'text': item.word});
                    }
                }
                query.callback(words);
            };

            $scope.selectedTags = [];
            $scope.select2Options = {
                'multiple': true,
                'simple_tags': true,
                'query': $scope.query
            };

        },
        link: function (scope, elem, attrs)
        {
            //console.debug(scope);
            scope.selectedItems = [];
            var _path = "/content/dam/photos";

            metadataService.keywordsByPath(_path).then(function (data)
            {
                scope.tags = data;
                this.tags = data;
            });


            scope.$watch('selectedTags', function (newVal, oldVal)
            {
                if (oldVal == newVal) return;

                if (typeof(newVal) == "string")
                {
                    newVal = newVal.split(",");
                }

                metadataService.updateTags(scope.selectedItems, newVal);

            }, true);


            scope.$watch('items', function (newVal, oldVal)
            {
                if (oldVal == newVal) return;

                if (typeof(newVal) == "string")
                {
                    newVal = angular.fromJson(newVal);
                }

                scope.selectedItems = newVal;
                scope.updateKeywordsArray();
            });


            scope.$watch('path', function (value, oldValue, scope)
            {
                if (value !== undefined)
                {
                    _path = value;


                    metadataService.keywordsByPath(_path).then(function (data)
                    {
                        scope.tags = data;
                    });
                }
            });

            /**
             * loop over all of the selected items and create a new array of tags for all selected items
             */
            scope.updateKeywordsArray = function ()
            {
                scope.selectedTags = [];
                for (var idx in scope.selectedItems)
                {
                    var _node = scope.selectedItems[idx];

                    if (_node.metadata !== undefined)
                    {
                        var _keywords = _node.metadata.keywords.split(",");
                        for (var indx = 0; indx < _keywords.length; indx++)
                        {
                            var word = _keywords[indx];
                            if (word !== undefined)
                            {
                                word = word.toLowerCase();
                            }
                            var existingPos = scope.selectedTags.indexOf(word);
                            if (existingPos == -1)
                            {
                                scope.selectedTags.push(word);
                            }
                        }
                    }
                }

                //$scope.$broadcast("photos:grid:keywords:selectedTags", $scope.selectedTags);
            };

        }

    };
};


keywordsDirective.$inject = ['metadataService'];
module.exports = keywordsDirective;