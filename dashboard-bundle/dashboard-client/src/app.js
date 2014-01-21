/*
 * This file is part of FamilyCloud Project.
 *
 *     The FamilyCloud Project is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     The FamilyCloud Project is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with the FamilyCloud Project.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Initial definition of the Angular Application. This class imports (requires) all of the modules for the application.
 */
require('dashboard-templates');

// Define the required modules
var App = angular.module('dashboard', [
        'ngCookies',
        'ngResource',
        'ngDragDrop',
        'ui.router',
        'ui.bootstrap',
        'ui.bootstrap.tpls',
        'infinite-scroll',
        'treeControl',
        'vr.directives.wordCloud',
        'dashboard.templates',
        'ui.select2',
        require('./modules/main').name,
        require('./modules/login').name,
        require('./modules/home').name,
        require('./modules/files').name,
        require('./modules/photos').name,
        require('./directives/fileUpload').name,
        require('./modules/user/preferences').name,
        require('./modules/user/usermanager').name])


    .config(['$stateProvider', '$urlRouterProvider', function ($stateProvider, $urlRouterProvider)
    {
        // For any unmatched url, redirect to /state1
        $urlRouterProvider.when('', '/login');
        $urlRouterProvider.otherwise("/login");
    }]);

App.$inject = ['ui.router'];


// Array Remove - By John Resig (MIT Licensed)
Array.prototype.remove = function(from, to) {
    var rest = this.slice((to || from) + 1 || this.length);
    this.length = from < 0 ? this.length + from : from;
    return this.push.apply(this, rest);
};


