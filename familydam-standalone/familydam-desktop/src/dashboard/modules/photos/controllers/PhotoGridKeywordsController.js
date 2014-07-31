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


var PhotosGridKeywordsController = function ($scope, metadataService)
{
    var _tags = [];
    var path = "/content/dam/photos";

    // properties related to the list of selected images
    $scope.selectedItems = [];
    $scope.selectedTags = [];
    $scope.select2Options = {
        'multiple': true,
        'simple_tags': true,
        'query': function(query)
        {
            var words = {results: []};

            for( var indx in _tags)
            {
                var item = _tags[indx];
                if( item.word !== undefined && item.word.substring(0, query.term.length) == query.term)
                {
                    words.results.push({'id': item.word, 'text': item.word});
                }
            }
            query.callback(words);
        }
    };


    $scope.$on("photos:grid:keywords:selectedTags", function(event, data){
        $scope.selectedTags = data;
    });


    $scope.$on("photos:grid:keywords:selectedItems", function(event, data){
        $scope.selectedItems = data;
    });


    $scope.$watch('selectedTags', function (newVal, oldVal, arg1, arg2)
    {
        if (oldVal == newVal) return;

        if( typeof(newVal) == "string" )
        {
            newVal = newVal.split(",");
        }

        metadataService.updateTags($scope.selectedItems, newVal);

    }, true);


    var itemAddedHandler = function(word)
    {
        metadataService.addTag($scope.selectedItems, word);
    };


    var itemRemovedHandler = function(word)
    {
        metadataService.removeTag($scope.selectedItems, word);
    };


    var init = function ()
    {
        metadataService.keywordsByPath(path).then(function (data)
        {
            /**
            var words = [];
            for( var item in data )
            {
                var w = data[item];
                if(w.word != undefined && w.word.length > 0 )
                {
                    words.push( w.word );
                }
            }
             **/

            _tags = data;
        });
    };
    init();
};

PhotosGridKeywordsController.$inject = ['$scope', 'metadataService'];
module.exports = PhotosGridKeywordsController;