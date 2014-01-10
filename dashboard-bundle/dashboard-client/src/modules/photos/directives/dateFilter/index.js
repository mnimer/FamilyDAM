module.exports = function() {
        return {
            scope: {
                'event':"@",
                'label':"@",
                'dt':"@"
            },
            replace: true,
            templateUrl: "modules/photos/directives/dateFilter/dateFilter.tpl.html",
            link: function(scope, elem,attrs)
            {
                var _event = "filter:date";

                scope.$watch('label', function(value, oldValue, scope)
                {
                    scope.label = value;
                });
                scope.$watch('event', function(value, oldValue, scope)
                {
                    _event = value;
                });
                // model
                scope.$watch('dt', function(value) {
                    scope.$emit(_event, value);
                });


                scope.open = function($event) {
                    $event.preventDefault();
                    $event.stopPropagation();

                    scope.opened = true;
                };

            }
        };
    };
