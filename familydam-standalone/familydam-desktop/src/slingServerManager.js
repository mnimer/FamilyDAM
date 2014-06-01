
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
    //var os = require('os');
    var fs = require('fs');
    var ipc = require('ipc');
    var http = require('http');



    var splashWindow, mainWindow;

    // local properties
    var checkServerInterval;
    var serverLoaded = false;
    var serverStarted = false;
    var userServletStarted = false;
    var prc = undefined;
    var tail = undefined;
    var tailErr = undefined;
    var serverPort = 8080; //default

    var link = function (_splashWindow, _mainWindow)
    {
        splashWindow = _splashWindow;
        mainWindow = _mainWindow;
    };

    var sendClientMessage = function(_type, _message, _logToConsole)
    {
        if( _logToConsole )
        {
            console.log(_type +":" +_message);
        }
        if (mainWindow != undefined && mainWindow.webContents != null) mainWindow.webContents.send(_type, _message);
    };

    var checkLoadingStatus = function()
    {
        if( !serverLoaded )
        {
            http.get("http://localhost:" + serverPort + "/dashboard", function (res)
            {
                //console.log("Got response: " + res.statusCode);
                if (res.statusCode == 200 || res.statusCode == 302)
                {
                    serverLoaded = true;
                }
            }).on('error', function (e)
            {
                //console.log("Got error: " + e.message);
                serverLoaded = false;
            });
        }
    }

    /**
     * Once the embedded java server is running load the application from the server.
     */
    var loadApplication = function(port)
    {
        console.log("Load embedded application (" +new Date() +")");

        splashWindow.close();

        mainWindow.loadUrl('http://localhost:' +port +'/dashboard');
        mainWindow.show();
        //mainWindow.maximize();
        mainWindow.focus();
        //var shell = require('shell');
        //shell.openExternal('http://localhost:' +port +'/dashboard');
        //app.dock.bounce("informational");
    };

    var getMaxMemArg = function ()
    {
        var totalMB = os.totalmem() / 1024 / 1024 / 1024;
        var halfMB = totalMB / 4;

        if( halfMB > 2 )
        {
            return "-Xmx" +(halfMB*1024) +"M";
        }else{
            return "-Xmx2048M";
        }
    };


    function processStdOut(_data)
    {
        try //todo: extract
        {
            if (_data.indexOf("HTTP server port:") != -1)
            {
                serverPort = _data.substr(_data.indexOf("port:") + 6).trim();
                sendClientMessage('error', "port=" + serverPort + " -- " + _data, true);
            }
            if (_data.indexOf("Startup completed") != -1)
            {
                sendClientMessage('info', "Server started on: http://localhost:" + serverPort + " at " + new Date(), true);

                // Call the server every 1 sec to see if the osgi bundles are loaded and running.
                // Once it's loaded, we'll load the application from the bundle.
                checkServerInterval = setInterval(function ()
                {
                    console.log("checking server status: " + serverLoaded);
                    if (serverLoaded)
                    {
                        clearInterval(checkServerInterval);
                        serverStarted = true;
                        loadApplication(serverPort);
                    }
                    else
                    {
                        checkLoadingStatus();
                    }
                }, 1000);
            }
        }
        catch (err)
        {
            console.log(err);
        }
    }

    module.exports = {

        startServer : function(_splashWindow, _mainWindow)
        {
            var outLogFile = process.resourcesPath +'/familydam-out.log';
            var outLogErrFile = process.resourcesPath +'/familydam-err.log';
            link(_splashWindow, _mainWindow);

            //sendClientMessage('info', "System - TotalMem:" +os.totalmem() +" - FreeMem:" +os.freemem(), true);
            //sendClientMessage('info', "max:" +getMaxMemArg(), true);
            sendClientMessage('info', "starting FamilyDam Server: " + process.resourcesPath, true);

            var spawn = require('child_process').spawn;


            var cmd = "java";
            var args = ['-Xmx4096M',  '-jar',  'app/resources/familydam-1.0.0-SNAPSHOT-standalone.jar', '-p', '9000'];

            /**** debug command
             var debugPrc = spawn('pwd',  []);
             debugPrc.stdout.on('data', function (data) {
                console.log('debug: ' + data);
                if( mainWindow != undefined ) mainWindow.webContents.send('info', data);
            });
             ***/

            if (process.platform == 'darwin' || process.platform == 'linux')
            {
                try
                {
                    var killExistingJavaProc = spawn('pkill', ['-f', 'java.*familydam']);
                }catch (err){ /* swallow */ }
            }

            prc = spawn(cmd,  args, {
                cwd: process.resourcesPath
            });
            //prc.unref();
            prc.stdout.setEncoding("utf8");
            prc.stdout.on('data', function (data)
            {
                var _data = data.toString();
                sendClientMessage('info', _data, true);
                fs.appendFile(outLogFile, _data);
                processStdOut(_data);
            });


            /***
            // Watch the output log
            tail = spawn("tail", ['-f', process.resourcesPath +'/familydam-out.log']);
            //tail.unref();
            //process.stdout.setEncoding("utf-8");
            tail.stdout.on('data', function (data) {
                var _data = data.toString().replace(/(\r\n|\n|\r)/gm,"");

                sendClientMessage('info', _data, false);

                if( _data.indexOf("HTTP server port:") != -1 )
                {
                    serverPort = _data.substr(_data.indexOf("port:")+6).trim();
                    sendClientMessage('error', "port=" +serverPort +" -- " +_data, true);
                }
                if( _data.indexOf("Startup completed") != -1 )
                {
                    sendClientMessage('info', "Server started on: http://localhost:" +serverPort +" at " +new Date(), true);

                    // Call the server every 1 sec to see if the osgi bundles are loaded and running.
                    // Once it's loaded, we'll load the application from the bundle.
                    checkServerInterval = setInterval(function(){
                        console.log("checking server status: " +serverLoaded);
                        if( serverLoaded )
                        {
                            clearInterval(checkServerInterval);
                            serverStarted = true;
                            loadApplication(serverPort);
                        }else{
                            checkLoadingStatus();
                        }
                    }, 1000);

                }
            });



            // Watch the error log
            tailErr = spawn("tail", ['-f', process.resourcesPath +'/familydam-err.log']);
            //tailErr.unref();
            tailErr.stdout.on('data', function (data){
                console.log("err: " +data);
                sendClientMessage('error', data);
            });
             **/

        },

        kill : function()
        {
            if( prc != undefined ) prc.kill();
            if( tail != undefined ) tail.kill();
            if( tailErr != undefined ) tailErr.kill();
            if( checkServerInterval != undefined ) clearInterval(checkServerInterval);
        }
    };


}).call(this);