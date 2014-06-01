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

module.exports = function (grunt) {

	var os = require("os");

    // load all grunt tasks
    require('matchdep').filterDev('grunt-*').forEach(grunt.loadNpmTasks);

    // some other cool plugins to look at...
	// https://npmjs.org/package/grunt-phonegap-build
	// https://npmjs.org/package/grunt-s3

	/* grunt: build a develop dist
	 * grunt: release: build a release dist
	 * grunt server: start server and build a develop dist any time a file changes
	 */

	// default task
	grunt.registerTask('default', ['clean', 'build']);

	// build tasks
	grunt.registerTask('build', ['copy', 'build-atom-shell-app']);


	grunt.initConfig({

		// ========================================
		// Common
		// ========================================

		// config
		assetsVersion: 1, // increment each release
		distdir: 'dist',
		tempdir: '.temp',
		pkg: grunt.file.readJSON('package.json'),
        'bower-install': {
            target: {
                html: '<%= src %>/index.html',
                ignorePath: '<%= src %>/'
            }
        },
        banner:
			'/*! <%= pkg.title || pkg.name %> - v<%= pkg.version %> - <%= grunt.template.today("yyyy-mm-dd") %>\n' +
				'<%= pkg.homepage ? " * " + pkg.homepage + "\\n" : "" %>' +
				' * Copyright (c) <%= grunt.template.today("yyyy") %> <%= pkg.author %>;\n' +
				' * Licensed <%= _.pluck(pkg.licenses, "type").join(", ") %>\n */\n',
		src: {
			js: ['src/**/*.js'],
			html: ['src/**/*.tpl.html']
		},


		// clean
		clean: ['./dist/*', '<%= tempdir %>/*'],

        // copy
        copy: {
            assets: {
                files: [
                    {
                        cwd: 'src/assets/',
                        src: '**',
                        dest: './dist/assets/',
                        expand: true
                    }
                ]
            },
            html: {
                files: [
                    {
                        cwd: 'src/',
                        src: '*.html',
                        dest: './dist/',
                        expand: true
                    }
                ]
            },
            js: {
                files: [
                    {
                        cwd: 'src/',
                        src: '*.js',
                        dest: './dist/',
                        expand: true
                    }
                ]
            },
            json: {
                files: [
                    {
                        cwd: 'src/',
                        src: '*.json',
                        dest: './dist/',
                        expand: true
                    }
                ]
            },
            resources: {
                files: [
                    {
                        cwd: 'src/resources/',
                        src: '*',
                        dest: './dist/resources',
                        expand: true
                    },
                    {
                        cwd: '../familydam-server/target',
                        src: 'familydam-*-standalone.jar',
                        dest: './dist/resources',
                        expand: true
                    }
                ]
            }
        },

        'download-atom-shell': {
            version: '0.12.3',
            outputDir: 'binaries'
        },

        'build-atom-shell-app': {
            options: {
                platforms: ["darwin", "win32", "linux"],
                app_dir:"dist",
                cache_dir:"binaries",
                build_dir:"binary-dist"
            }
        }

	});

};