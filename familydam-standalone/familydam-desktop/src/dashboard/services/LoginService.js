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

var LoginService = function($http)
{

    this.login = function(username, password)
    {
        var _config = {};
        _config.headers = {};
        _config.headers['Content-Type'] = "application/x-www-form-urlencoded";
        _config.headers['X-Requested-With'] = "XMLHttpRequest";

        var _args = $.param({ j_username:username, j_password:password, resource:"/", selectedAuthType:"form", _charset_:"UTF-8", j_use_cookie_auth:"true", j_uri:"/foo" });

        var method =  $http.post('http://localhost:9000/j_security_check', _args, _config);

        return method;
    };


    this.getUser = function(username)
    {
        var method =  $http.get('http://localhost:9000/system/userManager/user/' +username +'.2.json');
        return method;
    };

};

LoginService.$inject = ['$http'];
module.exports = LoginService;