var PhotoService = function($http) {

    this.list = function(path, successCallback, errorCallback) {
        var get =  $http.get(path +'.1.json',{ cache: false });


        if( successCallback !== undefined ){
            get.success(function(data, status, headers, config){
                successCallback(data, status, headers, config);
            });
        }
        if( errorCallback !== undefined ){
            get.error(function(data, status, headers, config){
                errorCallback(data, status, headers, config);
            });
        }

        return get;
    };
};


PhotoService.$inject = ['$http'];
module.exports = PhotoService;
