
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

(function() {
    var app = require('app');
    var os = require('os');
    var fs = require('fs');
    var ipc = require('ipc');
    var http = require('http');



    var splashWindow, mainWindow;



    var link = function (_splashWindow, _mainWindow)
    {
        splashWindow = _splashWindow;
        mainWindow = _mainWindow;
    };



    /**
     * Once the embedded java server is running load the application from the server.
     */
    var loadApplication = function(port)
    {
        console.log("Load internal configuration application (" +new Date() +")");

        splashWindow.close();

        mainWindow.loadUrl('file://' + __dirname + '/config/index.html');
        mainWindow.show();
        mainWindow.focus();
        //app.dock.bounce("informational");
    };



    module.exports = {

        initializeServer : function(_splashWindow, _mainWindow)
        {
            link(_splashWindow, _mainWindow);

            loadApplication();
        }

    };


}).call(this);