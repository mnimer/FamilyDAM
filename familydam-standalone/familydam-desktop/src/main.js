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

var app = require('app');  // Module to control application life.
var ipc = require('ipc');
var slingServerManager = require('./slingServerManager');
var BrowserWindow = require('browser-window');  // Module to create native browser window.

// Report crashes to our server.
require('crash-reporter').start();

// Keep a global reference of the window object, if you don't, the window will
// be closed automatically when the javascript object is GCed.
var mainWindow = null;

// Quit when all windows are closed.
app.on('will-quit', function() {
    slingServerManager.kill();
});

// Quit when all windows are closed.
app.on('window-all-closed', function() {
    slingServerManager.kill();

    if (process.platform != 'darwin')
        app.quit();
});

process.on('exit', function () {
    slingServerManager.kill();
});


// Called when user tries to open a new url to leave the application
app.on('open-url', function(event, path) {
    console.log("Open-URL: " +path);

    // Create the browser window.
    var childWindow = new BrowserWindow({width:1024, height:800, frame:true});

    // and load the index.html of the app.
    childWindow.loadUrl(path);

    // Emitted when the window is closed.
    childWindow.on('closed', function() {
        // Dereference the window object, usually you would store windows
        // in an array if your app supports multi windows, this is the time
        // when you should delete the corresponding element.
        childWindow = null;
    });
});

// Called when user tries to open a new url to leave the applicate
app.on('open-file', function(event, url) {
    console.log("Open-FILE: " +url);

});

// This method will be called when atom-shell has done everything
// initialization and ready for creating browser windows.
app.on('ready', function() {
    // Create the browser window.
    splashWindow = new BrowserWindow({width:600, height:400, center:true, frame:false});
    mainWindow = new BrowserWindow({width:1024, height:800, center:true, frame:true, show:false, title:'FamilyDAM - The Digital Asset Manager for Families'});

    // and load the index.html of the app.
    splashWindow.loadUrl('file://' + __dirname + '/index.html');
    splashWindow.focus();
    //splashWindow.loadUrl('http://localhost:8080');

    // Emitted when the window is closed.
    splashWindow.on('closed', function() {
        // Dereference the window object, usually you would store windows
        // in an array if your app supports multi windows, this is the time
        // when you should delete the corresponding element.
        splashWindow = null;
    });

    // Emitted when the window is closed.
    mainWindow.on('closed', function() {
        // Dereference the window object, usually you would store windows
        // in an array if your app supports multi windows, this is the time
        // when you should delete the corresponding element.
        mainWindow = null;
        slingServerManager.kill();
        if (process.platform != 'darwin')
        {
            app.quit();
        }
    });

    // Start the embedded Sling Server
    slingServerManager.startServer(splashWindow, mainWindow);
});


ipc.on('asynchronous-reply', function(arg) {
    console.log("Asyn-Reply:" +arg); // prints "pong"
});


