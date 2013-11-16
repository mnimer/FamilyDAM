var LoginService = function($http)
{

    this.login = function(username, password, successCallback, errorCallback)
    {
        var _config = {};
        _config.headers = {};
        _config.headers['Content-Type'] = "application/x-www-form-urlencoded";
        _config.headers['X-Requested-With'] = "XMLHttpRequest";

        var _args = $.param({ j_username:username, j_password:password, resource:"/", selectedAuthType:"form", _charset_:"UTF-8", j_use_cookie_auth:"true", j_uri:"/foo" });

        var method =  $http.post('/j_security_check', _args, _config);

        if( successCallback !== undefined ){
            method.success(function(data, status, headers, config){
                successCallback(data, status, headers, config);
            });
        }
        if( errorCallback !== undefined ){
            method.error(function(data, status, headers, config){
                errorCallback(data, status, headers, config);
            });
        }

        return method;
    };


    this.getUser = function(username, successCallback, errorCallback)
    {

        var method =  $http.get('/system/userManager/user/' +username +'.2.json');

        if( successCallback !== undefined ){
            method.success(function(data, status, headers, config){
                successCallback(data, status, headers, config);
            });
        }
        if( errorCallback !== undefined ){
            method.error(function(data, status, headers, config){
                errorCallback(data, status, headers, config);
            });
        }

        return method;
    };

};

LoginService.$inject = ['$http'];
module.exports = LoginService;