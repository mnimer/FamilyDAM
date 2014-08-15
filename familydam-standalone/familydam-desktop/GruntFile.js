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
	//grunt.registerTask('build', ['copy']);



	// default task
	grunt.registerTask('default', ['clean','copy','build-shared-libs','build']);
	grunt.registerTask('default-deploy', ['default', 'deploy']);

	// build tasks
	grunt.registerTask('build', ['build-css', 'build-js', 'build-atom-shell-app']);
	grunt.registerTask('build-css', ['compass:develop']);
	grunt.registerTask('build-js', ['jshint','html2js','browserify2:dashboard']);
	grunt.registerTask('build-shared-libs', ['browserify2:shared-libs']);
	grunt.registerTask('deploy', ['slingPost']);

	// server task
	grunt.registerTask('server', ['clean','copy','build', 'build-shared-libs', 'server-start', 'open', 'watch']);




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
		clean: ['./dist/*', '<%= tempdir %>/*', './binary-dist/*',],

        // copy
        copy: {
            'dashboard-assets': {
                files: [
                    {
                        cwd: 'src/dashboard/assets/',
                        src: '**',
                        dest: './dist/dashboard/assets/',
                        expand: true
                    },
                     {
                         cwd: './bower_components/bootstrap/dist/css/',
                         src: '*.css',
                         dest: './dist/dashboard/assets/css/',
                         expand: true
                     },
                     {
                         cwd: './bower_components/bootstrap/dist/fonts/',
                         src: '**',
                         dest: './dist/dashboard/assets/fonts/',
                         expand: true
                     },
                     {
                         cwd: './bower_components/ng-tags-input',
                         src: '*.css',
                         dest: './dist/dashboard/assets/css/',
                         expand: true
                     }
                ]
            },
            'dashboard-proto-assets': {
                files: [
                    {
                          cwd: './src/dashboard-prototype',
                          src: '*',
                          dest: './dist/dashboard-prototype',
                          expand: true
                      },
                      {
                          cwd: './src/dashboard-prototype',
                          src: '*',
                          dest: './binary-dist/darwin/atom-shell/Atom.app/Contents/Resources/app/dashboard-prototype',
                          expand: true
                      },
                      {
                          cwd: 'src/dashboard-prototype/assets/',
                          src: '**',
                          dest: './dist/dashboard-prototype/assets/',
                          expand: true
                      },
                      {
                          cwd: './src/dashboard-prototype',
                          src: '*.js',
                          dest: './dist/dashboard-prototype',
                          expand: true
                      },
                      {
                          cwd: './src/dashboard-prototype',
                          src: '*.js',
                          dest: './binary-dist/darwin/atom-shell/Atom.app/Contents/Resources/app/dashboard-prototype',
                          expand: true
                      },
                      {
                         cwd: './bower_components/',
                         src: '**',
                         dest: './dist/dashboard-prototype/components/',
                         expand: true
                     },
                     {
                         cwd: './bower_components/platform',
                         src: '*',
                         dest: './dist/dashboard-prototype/components/platform/',
                         expand: true
                     },
                     {
                         cwd: './bower_components/polymer',
                         src: '*',
                         dest: './dist/dashboard-prototype/components/polymer/',
                         expand: true
                     }
                ]
            },
            'configAssets': {
                files: [
                     {
                          cwd: './src/config',
                          src: '*',
                          dest: './dist/config',
                          expand: true
                      },
                      {
                          cwd: './src/config',
                          src: '*',
                          dest: './binary-dist/darwin/atom-shell/Atom.app/Contents/Resources/app/config',
                          expand: true
                      },
                      {
                          cwd: './src/',
                          src: '*.js',
                          dest: './dist/config',
                          expand: true
                      },
                      {
                          cwd: './src/',
                          src: '*.js',
                          dest: './binary-dist/darwin/atom-shell/Atom.app/Contents/Resources/app',
                          expand: true
                      },
                      {
                         cwd: './bower_components/',
                         src: 'core-*/**',
                         dest: './dist/config/components/',
                         expand: true
                     },
                     {
                         cwd: './bower_components/',
                         src: 'paper-*/**',
                         dest: './dist/config/components/',
                         expand: true
                     },
                     {
                         cwd: './bower_components/platform',
                         src: '*',
                         dest: './dist/config/components/platform/',
                         expand: true
                     },
                     {
                         cwd: './bower_components/polymer',
                         src: '*',
                         dest: './dist/config/components/polymer/',
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
                    },
                    {
                        cwd: 'src/',
                        src: '*.html',
                        dest: './binary-dist/darwin/atom-shell/Atom.app/Contents/Resources/app/',
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
                    },
                    {
                        cwd: 'src/',
                        src: '*.js',
                        dest: './binary-dist/darwin/atom-shell/Atom.app/Contents/Resources/app/',
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
            },
            statichtml: {
                files: [
                    {
                        cwd: 'src/dashboard/',
                        src: 'index.html',
                        dest: './dist/dashboard/',
                        expand: true
                    },
                    {
                        cwd: 'src/dashboard-prototype/',
                        src: '*.html',
                        dest: './dist/dashboard-prototype/',
                        expand: true
                    },
                    {
                        cwd: 'src/config/',
                        src: 'index.html',
                        dest: './dist/config/',
                        expand: true
                    },
                    {
                        cwd: 'src/dashboard/',
                        src: 'index.html',
                        dest: './binary-dist/darwin/atom-shell/Atom.app/Contents/Resources/app/dashboard/',
                        expand: true
                    },
                    {
                        cwd: 'src/dashboard-prototype/',
                        src: 'index.html',
                        dest: './binary-dist/darwin/atom-shell/Atom.app/Contents/Resources/app/dashboard-prototype/',
                        expand: true
                    },
                    {
                        cwd: 'src/config/',
                        src: 'index.html',
                        dest: './binary-dist/darwin/atom-shell/Atom.app/Contents/Resources/app/config/',
                        expand: true
                    }
                ]
            }
        },


		// ========================================
		// JavaScript
		// ========================================

		// js hint
		jshint: {
			files: ['gruntFile.js', '<%= src.js %>', '!src/components/**/*.js', '!src/dashboard/assets/js/**/*.js', '!src/dashboard-prototype/assets/**/*.js', '!src/config/components/**/*.js'],
			options: {
				curly:false,
				eqeqeq:false,
				immed:false,
				latedef:true,
				newcap:true,
				noarg:true,
				sub:true,
				boss:true,
				eqnull:true,
                evil:true,
				globals: {}
			}
		},

		// html2js
		html2js: {
			'dashboard': {
				src: ['src/dashboard/**/*.tpl.html'],
				dest: '.temp/dashboard/app-templates.js',
				module: 'dashboard.templates'
			}
		},

		// browserify
		browserify2: {
			'shared-libs': {
				entry: './src/dashboard/shared-libs.js',
				compile: './dist/dashboard/assets/js/shared-libs.js',
				debug: true,
				options: {
					expose: {
                        angular: './bower_components/angular/angular.js',
                        'angular-ui-router': './bower_components/angular-ui-router/release/angular-ui-router.js',
                        'angular-dragdrop': './bower_components/angular-dragdrop/src/angular-dragdrop.js',
                        'angular-cookies': './bower_components/angular-cookies/angular-cookies.js',
                        'angular-resource': './bower_components/angular-resource/angular-resource.js',
                        'angular-file-upload': './bower_components/angular-file-upload/angular-file-upload.js',
                        'angular-ui-select2':'./bower_components/angular-ui-select2/src/select2.js',
                        'angular-sanitize':'./bower_components/angular-sanitize/angular-sanitize.js',
                        'jquery': './bower_components/jquery/jquery.js',
                        'jquery-ui': './bower_components/jquery-ui/jquery-ui.js',
                        'ui.bootstrap': './bower_components/angular-bootstrap/ui-bootstrap.js',
                        'ui.bootstrap.tpls': './bower_components/angular-bootstrap/ui-bootstrap-tpls.js',
                        'infinite-scroll': './bower_components/ngInfiniteScroll/build/ng-infinite-scroll.js',
                        'treeControl':'./src/dashboard/assets/js/angular-tree-control-modified.js',
                        'vr.directives.wordCloud':'./bower_components/angular-word-cloud/build/angular-word-cloud.js',
                        'select2':'./bower_components/select2/select2.js',
                        'momentjs':'./bower_components/momentjs/min/moment-with-langs.js',
                        'com.2fdevs.videogular':'./bower_components/videogular/videogular.js',
                        'com.2fdevs.videogular.plugins.controls':'./bower_components/videogular-controls/controls.js',
                        'com.2fdevs.videogular.plugins.buffering':'./bower_components/videogular-buffering/buffering.js',
                        'com.2fdevs.videogular.plugins.overlayplay':'./bower_components/videogular-overlay-play/overlay-play.js'
                    }
				}
			},
			'dashboard': {
				entry: './src/dashboard/app.js',
				compile: './dist/dashboard/assets/js/app.js',
				debug: true,
				options: {
                    expose: {
                        'dashboard-templates': './.temp/dashboard/app-templates.js'
                    }
                }
			}
		},


		// ========================================
		// COMPASS
		// ========================================

		compass: {
			develop: {
				options: {
					sassDir: 'src',
					cssDir: 'dist/dashboard/assets/css'
				}
			},
			release: {
				options: {
					sassDir: 'src',
					cssDir: 'dist/dashboard/assets/css',
					environment: 'production'
				}
			}
		},

		// ========================================
		// Server
		// ========================================

        open : {
            chrome : {
                path: 'http://localhost:8080/index.html',
                app: 'Google Chrome'
            }
        },

		watch: {
			options: {
				livereload: true
			},
			'dashboardAssets': {
				files: 'src/dashboard/assets/*.*',
				tasks: ['copy:dashboard-assets'] /*, 'deploy'*/
			},
			'dashboardPrototype': {
				files: 'src/dashboard-prototype/assets/**',
				tasks: ['copy:dashboard-proto-assets'] /*, 'deploy'*/
			},
			'dashboardPrototypeHtml': {
				files: 'src/dashboard-prototype/*.html',
				tasks: ['copy:dashboard-proto-assets'] /*, 'deploy'*/
			},
			'configAssets': {
				files: 'src/config/*.*',
				tasks: ['copy:configAssets']
			},
			'configAssets2': {
				files: 'src/*.js',
				tasks: ['copy:configAssets']
			},
			css: {
				files: ['src/**/*.scss'],
				tasks: ['compass:develop'] /*, 'deploy'*/
			},
			js: {
				files: 'src/**/*.js',
				tasks: ['build-js', 'copy:configAssets'] /*, 'deploy'*/
			},
			html: {
				files: ['src/**/*.tpl.html', 'src/**/*.html'],
				tasks: ['copy:statichtml', 'build-js']  /*, 'deploy'*/
			},
			grunt: {
				files: ['GruntFile.js'],
				tasks: ['default']
			}
		},

		// ========================================
		// Release
		// ========================================

		// uglify
		uglify: {
			dist: {
				options: {
					banner: '<%= banner %>'
				},
				files: {

				}
			}
		},

		// karma (unit tests)
		karma: {
			unit: {
				configFile: 'test/config/unit.js'
			},
			watch: {
				configFile: 'test/config/unit.js',
				autoWatch: true
			}
		},

        slingPost: {
            options: {
                host:"localhost",
                port:8888,
                user:"admin",
                pass:"admin",
                exclude: ["*.git"]
            },
            dist: {
                src: "dist",
                dest: "/content/dashboard"
            }
        },

        'download-atom-shell': {
            version: '0.15.6',
            outputDir: 'binaries'
        },

        'build-atom-shell-app': {
            options: {
                platforms: ["darwin", "win32", "linux"],
                app_dir:"dist",
                cache_dir:"binaries",
                build_dir:"binary-dist",
                atom_shell_version: 'v0.15.6'
            }
        }

	});

};