
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
    var fs = require('fs');
    var ipc = require('ipc');
    var http = require('http');
    var dialog = require('dialog');

    /******************************
     * RECEIVED Messages
     */
    ipc.on('openFileAndFolderPickerRequest', function(event) {
        console.log("openFileAndFolderPicker:" +event);
        console.dir(event);
        dialog.showOpenDialog({ properties: [ 'openFile', 'openDirectory', 'multiSelections' ]}, function(files){
            console.log("selected files=" +files);
            event.sender.send('openFileAndFolderPickerReply', files);
        });
    });



    // put public properties & methods here.
    module.exports = {};


}).call(this);