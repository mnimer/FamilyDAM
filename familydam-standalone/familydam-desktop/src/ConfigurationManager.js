
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
 *--
 */

(function() {
    var app = require('app');
    var os = require('os');
    var fs = require('fs');
    var ipc = require('ipc');
    var http = require('http');
    var BrowserWindow = require('browser-window');

    var settings = {};
    var settingsFile = "resources/systemprops.json";

    var appRoot;
    var configWindow;

    var link = function (_app, _configWindow)
    {
        this.appRoot = _app;
        configWindow = _configWindow;
    };



    /**
     * Once the embedded java server is running load the application from the server.
     */
    var loadConfigApplication = function()
    {
        console.log("Load internal configuration window (" +new Date() +")");

        var configWindow = new BrowserWindow({width:900, height:600, center:true, frame:true, show:false, title:'FamilyDAM Configuration Wizard'});

        configWindow.loadUrl('file://' + __dirname + '/config/index.html');
        configWindow.webContents.on('did-finish-load', function()
        {
            configWindow.webContents.send('settingConfig', settings);
        });
        configWindow.show();
        configWindow.focus();
        //app.dock.bounce("informational");


        // Call back handler which invoked from the webpage when all of the fields have been filled out.
        ipc.on('saveConfig', function(event, _settings)
        {
            console.log("save settings : " +_settings );

            settings = JSON.parse(_settings);
            settings.state = "READY";

            var encodedSettings = JSON.stringify(settings);

            fs.writeFile( __dirname +'/resources/systemprops.json',  encodedSettings, {'encoding':'utf8'}, function (err, data)
            {
                storageLocationInitialize();
                configWindow.hide();
                this.appRoot.loadMainApplication(settings);
            });
        });
    };


    var validateSettingsFile = function()
    {
        if( !fs.existsSync( __dirname +"/"  +settingsFile) )
        {
            //todo: create default file
            console.warn("settings file does not exists");
        }


        fs.readFile( __dirname +'/resources/systemprops.json',  {'encoding':'utf8'}, function (err, data)
        {
            if (err) {
                console.log(err);
                throw err;
            }

            console.log(data);
            settings = JSON.parse(data);

            if( settings.state == "READY" && storageLocationInitialize() )
            {
                this.appRoot.loadMainApplication(settings);
            }else{
                loadConfigApplication();
            }
        });
    };


    function storageLocationIsValid() {

        var target = settings.storageLocation +"/familydam-" +settings.serverVersion +"-SNAPSHOT-standalone.jar";

        if( fs.existsSync(  target  ) )
        {
            return true;
        }

        return false;
    }


    function storageLocationInitialize() {

        var target = settings.storageLocation +"/familydam-" +settings.serverVersion +"-SNAPSHOT-standalone.jar";

        try{
            fs.mkdirSync(settings.storageLocation);
        }catch(err){
            //swallow
        }


        if( fs.existsSync(settings.storageLocation) )
        {
            if( !fs.existsSync(  target  ) )
            {
                //copy jar to new dir
                var source = __dirname +"/resources/familydam-" +settings.serverVersion +"-SNAPSHOT-standalone.jar";
                //copy
                this.appRoot.sendClientMessage('info', "Copying FamilyDAMServer to "+settings.storageLocation, false);
                console.log( "{ConfigurationManager} copy: dest=" +source);
                console.log( "{ConfigurationManager} copy: target=" +target);
                fs.writeFileSync(target, fs.readFileSync(source));
            }

            return true;
        }

        return false;
    }


    module.exports = {

        initializeServer : function(_app, _configWindow)
        {
            link(_app, _configWindow);

            validateSettingsFile();
        }

    };


}).call(this);