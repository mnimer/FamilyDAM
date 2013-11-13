var PhotoService = function($http) {
    var basePath = "/content/dam";

    this.list = function(path, successCallback, errorCallback) {

        // make sure the path starts with /
        if( path.substring(0,1) != "/"){
            path = "/" +path;
        }

        var get =  $http.get(basePath +path +'.2.json',{ cache: false });

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


    this.createFolder = function(path, title, successCallback, errorCallback) {
        // make sure the path starts with /
        if( path.substring(0,1) != "/"){
            path = "/" +path;
        }
        var _url = basePath +path +"/*";
        var _data = ":nameHint=" +title +"&jcr:primaryType=nt:folder";
            _data[":nameHint"] = title;
            _data["jcr:primaryType"] = "nt:folder";
        var _config = {};
            _config.headers = {};
            _config.headers['Content-Type'] = "application/x-www-form-urlencoded";

        var post =  $http.post(_url, _data, _config);

        if( successCallback !== undefined ){
            post.success(function(data, status, headers, config){
                successCallback(data, status, headers, config);
            });
        }
        if( errorCallback !== undefined ){
            post.error(function(data, status, headers, config){
                errorCallback(data, status, headers, config);
            });
        }

        return post;
    };
};


PhotoService.$inject = ['$http'];
module.exports = PhotoService;
