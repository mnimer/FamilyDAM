
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



    var mainWindow;

    // local properties
    var checkServerInterval;
    var serverLoaded = false;
    var serverStarted = false;
    var userServletStarted = false;
    var prc;
    var tail;
    var tailErr;
    var settings;

    var link = function(_settings, _appRoot, _splashWindow, _mainWindow)
    {
        console.log("{serverManager} link");
        console.dir(_settings);
        this.settings = _settings;
        this.appRoot = _appRoot;
        this.splashWindow = _splashWindow;
        this.mainWindow = _mainWindow;
    };



    var checkLoadingStatus = function()
    {
        console.log("Check Loading Status");
        if( !serverLoaded )
        {
            http.get("http://localhost:" + serverPort + "/dashboard", function (res)
            {
                console.log("response: " + res.statusCode);
                if (res.statusCode == 200 || res.statusCode == 302)
                {
                    serverLoaded = true;
                }
            }).on('error', function (e)
            {
                console.log("error: " + e.message);
                serverLoaded = false;
            });
        }
    };

    /**
     * Once the embedded java server is running load the application from the server.
     */
    var loadApplication = function(_settings)
    {
        console.log("Load embedded application (" +new Date() +")");
        splashWindow.hide();

        console.log("splash:" +this.splashWindow );
        console.log("main:" +this.mainWindow );


        this.mainWindow.loadUrl('http://localhost:' +this.settings.port +'/dashboard');
        this.mainWindow.show();
        //mainWindow.maximize();
        this.mainWindow.focus();
        //var shell = require('shell');
        //shell.openExternal('http://localhost:' +port +'/dashboard');
        //app.dock.bounce("informational");
    };

    var getMaxMemArg = function ()
    {
        var totalMB = os.totalmem() / 1024 / 1024 / 1024;
        var partialMB = totalMB / 4;

        if( partialMB > 2 )
        {
            return "-Xmx" +(partialMB*1024) +"M";
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
                this.appRoot.sendClientMessage('error', "port=" + serverPort + " -- " + _data, true);
            }
            if (_data.indexOf("Startup completed") != -1)
            {
                this.appRoot.sendClientMessage('info', "Server started on: http://localhost:" + serverPort + " at " + new Date(), true);

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

        startServer : function(_settings, _appRoot, _splashWindow, _mainWindow)
        {
            link(_settings, _appRoot, _splashWindow, _mainWindow);

            // setup logs
            var outLogFile = _settings['storageLocation'] +'/familydam-out.log';
            var outLogErrFile = _settings['storageLocation'] +'/familydam-err.log';


            //_appRoot.sendClientMessage('info', "max:" +getMaxMemArg(), true);
            //_appRoot.sendClientMessage('info', "Starting FamilyDam Server: " + process.resourcesPath, true);
            _appRoot.sendClientMessage('info', "System - TotalMem:" +os.totalmem() +" - FreeMem:" +os.freemem(), true);

            var spawn = require('child_process').spawn;


            var jarPath = _settings['storageLocation'] +"/familydam-" +_settings['serverVersion'] +"-SNAPSHOT-standalone.jar";
            var jarPort = _settings['port'];
            _appRoot.sendClientMessage('info', "Starting FamilyDAMServer", false);
            _appRoot.sendClientMessage('debug', "START:" +jarPath +":" +jarPort, true);

            var cmd = "java";
            var args = ['-jar',  jarPath, '-p', jarPort];

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
                _appRoot.sendClientMessage('debug', _data, true);
                fs.appendFile(outLogFile, _data);
                processStdOut(_data);
            });


        },

        kill : function()
        {
            if( prc !== undefined ) prc.kill();
            if( tail !== undefined ) tail.kill();
            if( tailErr !== undefined ) tailErr.kill();
            if( checkServerInterval !== undefined ) clearInterval(checkServerInterval);
        }
    };


}).call(this);