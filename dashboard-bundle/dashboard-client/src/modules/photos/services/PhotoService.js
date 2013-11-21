var PhotoService = function($http) {
    var basePath = "/content/dam";


    /**
     * This method is used to invoke hateoas links returned from the other services
     * @param path
     * @param successCallback
     * @param errorCallback
     * @returns {*|Array|Object|Mixed|promise|HTMLElement}
     */
    this.invokeLink = function(path, successCallback, errorCallback) {

        // make sure the path starts with /
        if( path.substring(0,1) != "/"){
            path = "/" +path;
        }

        var get =  $http.get(basePath +path +'.1.json',{ cache: false });

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


    /**
     * Load one layer at a time, used by the list view to show the contents of a folder.
     * @param path
     * @param successCallback
     * @param errorCallback
     * @returns {*|Array|Object|Mixed|promise|HTMLElement}
     */
    this.list = function(path, successCallback, errorCallback) {

        // make sure the path starts with /
        if( path.substring(0,1) != "/"){
            path = "/" +path;
        }

        var get =  $http.get(basePath +path +'.1.json',{ cache: false });

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


    /**
     * Search all photos with limit/offset paging support, used by the grid view.
     * @param path
     * @param successCallback
     * @param errorCallback
     * @returns {*|Array|Object|Mixed|promise|HTMLElement}
     */
    this.search = function( limit, offset, successCallback, errorCallback )
    {
        var searchPath = "/dashboard-api/photos/search?limit=" +limit +"&offset=" +offset;

        var get =  $http.get(searchPath);

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


    /**
     * Create new folder (name=title) under the path
     * @param path
     * @param title
     * @param successCallback
     * @param errorCallback
     * @returns {*|HttpPromise}
     */
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
