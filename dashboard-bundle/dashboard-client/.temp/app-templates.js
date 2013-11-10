angular.module('dashboard.templates', ['modules/main/main.tpl.html', 'modules/photos/photos-info.aside.tpl.html', 'modules/photos/photos.grid.tpl.html', 'modules/photos/photos.list.tpl.html', 'modules/photos/photos.tpl.html', 'modules/user/preferences/preferences.tpl.html', 'modules/user/usermanager/usermanager.tpl.html']);

angular.module("modules/main/main.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("modules/main/main.tpl.html",
    "\n" +
    "<div class=\"navbar\">\n" +
    "    <div class=\"navbar-inner\">\n" +
    "        <div class=\"container\">\n" +
    "            <a class=\"navbar-brand\" href=\"index.html\"><span>FamilyCloud</span></a>\n" +
    "            <!-- start: Header Menu -->\n" +
    "            <div class=\"nav-no-collapse header-nav\">\n" +
    "                <ul class=\"nav pull-right\">\n" +
    "                    <!-- start: User Dropdown -->\n" +
    "                    <li class=\"dropdown\">\n" +
    "                        <a class=\"btn dropdown-toggle\" data-toggle=\"dropdown\" href=\"ui-sliders-progress.html#\">\n" +
    "                            <i class=\"halflings-icon white user\"></i> MIKE NIMER\n" +
    "                            <span class=\"caret\"></span>\n" +
    "                        </a>\n" +
    "                        <ul class=\"dropdown-menu\">\n" +
    "                            <li><a href=\"#/user/preferences\"><i class=\"halflings-icon white user\"></i> Preferences</a></li>\n" +
    "                            <li><a href=\"#/user/manager\"><i class=\"halflings-icon white user\"></i> User Manger</a></li>\n" +
    "                            <li><a href=\"login.html\"><i class=\"halflings-icon white off\"></i> Logout</a></li>\n" +
    "                        </ul>\n" +
    "                    </li>\n" +
    "                    <!-- end: User Dropdown -->\n" +
    "                </ul>\n" +
    "            </div>\n" +
    "\n" +
    "            <!-- end: Header Menu -->\n" +
    "        </div>\n" +
    "    </div>\n" +
    "</div>\n" +
    "<!-- start: Header -->\n" +
    "\n" +
    "<div class=\"container\">\n" +
    "    <div class=\"row\">\n" +
    "\n" +
    "        <!-- start: Main Menu -->\n" +
    "        <nav id=\"sidebar-left\" class=\"col-sm-1 hidden-xs\" ng-hide=\"ModeFullScreen\" >\n" +
    "            <div class=\"nav-collapse sidebar-nav collapse navbar-collapse bs-navbar-collapse\">\n" +
    "                <ul class=\"nav nav-tabs nav-stacked main-menu\">\n" +
    "                    <li ng-class=\"{ active: $state.includes('photos') }\">\n" +
    "                        <a ui-sref=\"photos.grid\"><i class=\"icon-bar-chart\"></i><span class=\"hidden-sm\">Photos {{$state.stateName}}</span></a>\n" +
    "                    </li>\n" +
    "                    <li ng-class=\"{ active: activePath=='/music' }\">\n" +
    "                        <a class=\"dropmenu\" href=\"#/music\"><i class=\"icon-eye-open\"></i><span class=\"hidden-sm\"> Music</span></a>\n" +
    "                    </li>\n" +
    "                    <li ng-class=\"{ active: activePath=='/movies' }\">\n" +
    "                        <a href=\"#/movies\"><i class=\"icon-dashboard\"></i><span class=\"hidden-sm\"> Movies</span></a>\n" +
    "                    </li>\n" +
    "                    <li>\n" +
    "                        <a class=\"dropmenu\" href=\"\"><i class=\"icon-folder-close-alt\"></i><span class=\"hidden-sm\"> Documents</span></a>\n" +
    "                    </li>\n" +
    "                    <li>\n" +
    "                        <a class=\"dropmenu\" href=\"\"><i class=\"icon-edit\"></i><span class=\"hidden-sm\"> Email</span></a>\n" +
    "                    </li>\n" +
    "                    <li>\n" +
    "                        <a class=\"dropmenu\" href=\"\"><i class=\"icon-list-alt\"></i><span class=\"hidden-sm\"> Social Media</span></a>\n" +
    "                    </li>\n" +
    "                    <li></li>\n" +
    "                    <li></li>\n" +
    "                    <li></li>\n" +
    "                    <li></li>\n" +
    "                    <li></li>\n" +
    "                </ul>\n" +
    "            </div>\n" +
    "        </nav>\n" +
    "        <!-- end: Main Menu -->\n" +
    "\n" +
    "        <!-- start: Content -->\n" +
    "        <div class=\"primaryView col-xs-11\"  >\n" +
    "            <div ui-view></div>\n" +
    "        </div>\n" +
    "        <!-- end: Content -->\n" +
    "\n" +
    "\n" +
    "        <a id=\"widgets-area-button\" class=\"hidden-sm hidden-xs open\"><i class=\"icon-reorder\"></i></a>\n" +
    "    </div><!--/row-->\n" +
    "\n" +
    "    </div>\n" +
    "</div>\n" +
    "\n" +
    "\n" +
    "\n" +
    "<div class=\"clearfix\"></div>\n" +
    "    <footer>\n" +
    "    <div class=\"container\">\n" +
    "        <span id=\"systemStatus\">...</span>\n" +
    "        <span id=\"toolbar\" class=\"pull-right\">\n" +
    "            <button type=\"button\" class=\"glyphicon glyphicon-fullscreen btn btn-default btn-sm\" ng-click=\"toggleFullScreenMode()\"></button>\n" +
    "            <span class=\"hidden-phone\" style=\"text-align:right;float:right\">Powered by: FamilyCloud Project</span>\n" +
    "        </span>\n" +
    "    </div>\n" +
    "</footer>\n" +
    "");
}]);

angular.module("modules/photos/photos-info.aside.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("modules/photos/photos-info.aside.tpl.html",
    "\n" +
    "<ul class=\"nav tab-menu nav-tabs\" id=\"myTab\">\n" +
    "    <li class=\"active\"><a href=\"#charts\"><i class=\"icon-bar-chart\"></i></a></li>\n" +
    "    <li><a href=\"#users\"><i class=\"icon-group\"></i></a></li>\n" +
    "    <li><a href=\"#messages\"><i class=\"icon-envelope\"></i></a></li>\n" +
    "</ul>\n" +
    "\n" +
    "<div id=\"myTabContent\" class=\"tab-content\">\n" +
    "    <div class=\"tab-pane active\" id=\"charts\">\n" +
    "        <div class=\"bar-stat\">\n" +
    "            <span class=\"title\">Account balance</span>\n" +
    "            <span class=\"value\">$19 999,99</span>\n" +
    "            <span class=\"chart green\">7,3,2,6,6,3,9,0,1,4</span>\n" +
    "        </div>\n" +
    "\n" +
    "        <hr>\n" +
    "\n" +
    "        <div id=\"cpu-usage\">\n" +
    "            DATA COLUMN\n" +
    "        </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"tab-pane\" id=\"users\">\n" +
    "        <ul class=\"users-list\">\n" +
    "            <li>\n" +
    "                <a href=\"ui-sliders-progress.html#\">View all users</a>\n" +
    "            </li>\n" +
    "        </ul>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"tab-pane\" id=\"messages\">\n" +
    "\n" +
    "        <ul class=\"messages-list\">\n" +
    "\n" +
    "            <li>\n" +
    "                <a href=\"ui-sliders-progress.html#\">View all messages</a>\n" +
    "            </li>\n" +
    "        </ul>\n" +
    "\n" +
    "    </div>\n" +
    "</div>\n" +
    "\n" +
    "\n" +
    "");
}]);

angular.module("modules/photos/photos.grid.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("modules/photos/photos.grid.tpl.html",
    "<div class=\"content\">\n" +
    "    Photo GRID\n" +
    "</div>\n" +
    "");
}]);

angular.module("modules/photos/photos.list.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("modules/photos/photos.list.tpl.html",
    "<div class=\"content\">\n" +
    "    Photo LIST\n" +
    "</div>\n" +
    "");
}]);

angular.module("modules/photos/photos.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("modules/photos/photos.tpl.html",
    "<div class=\"photos section\" ng-class=\"{'col-xs-12':!ModeFullScreen, 'col-sm-8':!ModeFullScreen, 'col-xs-12': ModeFullScreen}\">\n" +
    "    <div id=\"toolbar\">\n" +
    "        <span>/Photos</span>\n" +
    "\n" +
    "        <span class=\"pull-right\">\n" +
    "            <a ui-sref=\"photos.list\"\n" +
    "               class=\"glyphicon glyphicon-list btn btn-default btn-sm\"\n" +
    "                ng-class=\"{active: layout == 'list'}\" ng-click=\"layout = 'list'\"></a>\n" +
    "            &nbsp;\n" +
    "            <a ui-sref=\"photos.grid\"\n" +
    "               class=\"glyphicon glyphicon-th btn btn-default btn-sm\"\n" +
    "               ng-class=\"{active: layout == 'grid'}\" ng-click=\"layout = 'grid'\"></a>\n" +
    "        </span>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"content\">\n" +
    "        <div ui-view>Photos Go HERE</div>\n" +
    "    </div>\n" +
    "\n" +
    "</div>\n" +
    "\n" +
    "\n" +
    "<!-- start: Widgets Area -->\n" +
    "<aside id=\"sidebar-right\" class=\"col-sm-3 hidden-xs\" ng-hide=\"ModeFullScreen\"  >\n" +
    "    <div class=\"sidebar-nav\">\n" +
    "        Photo Info\n" +
    "    </div>\n" +
    "</aside>\n" +
    "<!-- end: Widgets Area -->\n" +
    "\n" +
    "\n" +
    "");
}]);

angular.module("modules/user/preferences/preferences.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("modules/user/preferences/preferences.tpl.html",
    "<div class=\"userPreferences section col-sm-12\">\n" +
    "    <div id=\"toolbar\">\n" +
    "        <span>/user/preferences</span>\n" +
    "\n" +
    "        <span class=\"pull-right\">\n" +
    "            <!-- icons -->\n" +
    "        </span>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"content\">\n" +
    "        User Preferences\n" +
    "    </div>\n" +
    "</div>");
}]);

angular.module("modules/user/usermanager/usermanager.tpl.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("modules/user/usermanager/usermanager.tpl.html",
    "<div class=\"userManager section col-sm-12\">\n" +
    "    <div id=\"toolbar\">\n" +
    "        <span>/user/manager</span>\n" +
    "\n" +
    "        <span class=\"pull-right\">\n" +
    "            <!-- icons -->\n" +
    "        </span>\n" +
    "    </div>\n" +
    "\n" +
    "    <div class=\"content\">\n" +
    "        User Manager\n" +
    "    </div>\n" +
    "</div>");
}]);
