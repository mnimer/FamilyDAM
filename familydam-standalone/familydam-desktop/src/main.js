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
var serverManager = require('./ServerManager');
var configurationManager = require('./ConfigurationManager');
var BrowserWindow = require('browser-window');  // Module to create native browser window.
var fileManager = require('./FileManager');  // Module to create native browser window.

// Report crashes to our server.
require('crash-reporter').start();

// Keep a global reference of the window object, if you don't, the window will
// be closed automatically when the javascript object is GCed.
var splashWindow = null;
var configWindow = null;
var mainWindow = null;



/******************************
 * Event Handlers
 */
process.on('exit', function () {
    serverManager.kill();
});


// Quit when all windows are closed.
app.on('will-quit', function() {
    serverManager.kill();
});


// Quit when all windows are closed.
app.on('window-all-closed', function() {
    serverManager.kill();

    if (process.platform != 'darwin')
        app.quit();
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
    splashWindow = new BrowserWindow({width:600, height:400, center:true, frame:false, show:true});
    configWindow = new BrowserWindow({width:600, height:400, center:true, frame:false, show:false});
    mainWindow = new BrowserWindow({
        width:1024,
        height:800,
        center:true,
        frame:true,
        show:false,
        title:'FamilyDAM - The Digital Asset Manager for Families'});



    // and load the index.html of the app.
    console.log('open:' +'file://' + __dirname + '/splash.html');
    splashWindow.loadUrl('file://' + __dirname + '/splash.html');
    splashWindow.focus();
    //splashWindow.loadUrl('http://localhost:8080');

    // Emitted when the window is closed.
    splashWindow.on('closed', function() {
        // Dereference the window object, usually you would store windows
        // in an array if your app supports multi windows, this is the time
        // when you should delete the corresponding element.
        splashWindow = null;
    });


    configWindow.on('closed', function() {
        configWindow = null;
    });


    // Emitted when the window is closed.
    mainWindow.on('closed', function() {
        // Dereference the window object, usually you would store windows
        // in an array if your app supports multi windows, this is the time
        // when you should delete the corresponding element.
        splashWindow = null;
        configWindow = null;
        mainWindow = null;
        serverManager.kill();
        if (process.platform != 'darwin')
        {
            app.quit();
        }
    });


    // Check the settings configuration before opening up the main app.
    var timer = setInterval(function(){
        clearTimeout(timer);
        configurationManager.initializeServer(app, configWindow);
    }, 1000);


    // Start the embedded Sling Server
    //serverManager.startServer(splashWindow, mainWindow);
});



/******************************
 * App level functions
 */

/**
 * Launch the main application
 * @param _settings
 */
app.loadMainApplication = function(_settings) {
    //start jar
    //console.log("{loadMainApplication}" +_settings);
    serverManager.startServer(_settings, app, splashWindow, mainWindow);

    splashWindow.hide();
    mainWindow.show();
    mainWindow.maximize();

    //mainWindow.loadUrl('file://' + __dirname  +'/dashboard-prototype/index.html');
};


app.sendClientMessage = function(_type, _message, _logToConsole)
{
    if( _logToConsole )
    {
        console.log(_type +":" +_message);
    }
    if (splashWindow !== undefined && splashWindow.webContents != null) splashWindow.webContents.send(_type, _message);
    if (mainWindow !== undefined && mainWindow.webContents != null) mainWindow.webContents.send(_type, _message);
};




ipc.on('asynchronous-reply', function(arg) {
    console.log("Asyn-Reply:" +arg); // prints "pong"
});



