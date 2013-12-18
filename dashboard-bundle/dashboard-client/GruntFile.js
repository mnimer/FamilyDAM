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
	grunt.registerTask('default', ['clean','copy', 'build','build-shared-libs', 'deploy']);

	// build tasks
	grunt.registerTask('build', ['build-css', 'build-js']);
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
					},
                    {
                        cwd: './bower_components/bootstrap/dist/css/',
                        src: '*.css',
                        dest: './dist/assets/css/',
                        expand: true
                    },
                    {
                        cwd: './bower_components/bootstrap/dist/fonts/',
                        src: '**',
                        dest: './dist/assets/fonts/',
                        expand: true
                    },
                    {
                        cwd: './bower_components/ng-tags-input',
                        src: '*.css',
                        dest: './dist/assets/css/',
                        expand: true
                    }
				]
            },
			statichtml: {
				files: [
					{
						cwd: 'src/',
						src: 'index.html',
						dest: './dist/',
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
			files: ['gruntFile.js', '<%= src.js %>', '!src/components/**/*.js', '!src/assets/js/**/*.js'],
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
				src: ['src/**/*.tpl.html'],
				dest: '.temp/app-templates.js',
				module: 'dashboard.templates'
			}
		},

		// browserify
		browserify2: {
			'shared-libs': {
				entry: './src/shared-libs.js',
				compile: './dist/assets/js/shared-libs.js',
				debug: true,
				options: {
					expose: {
                        angular: './bower_components/angular/angular.js',
                        'angular-ui-router': './bower_components/angular-ui-router/release/angular-ui-router.js',
                        'angular-dragdrop': './bower_components/angular-dragdrop/src/angular-dragdrop.js',
                        'angular-cookies': './bower_components/angular-cookies/angular-cookies.js',
                        'angular-resource': './bower_components/angular-resource/angular-resource.js',
                        'jQuery': './bower_components/jquery/jquery.js',
                        'jquery-ui': './bower_components/jquery-ui/ui/jquery-ui.js',
                        'ui.bootstrap': './bower_components/angular-bootstrap/ui-bootstrap.js',
                        'ui.bootstrap.tpls': './bower_components/angular-bootstrap/ui-bootstrap-tpls.js',
                        'angular-file-upload': './bower_components/angular-file-upload/angular-file-upload.js',
                        'ng-tags-input': './bower_components/ng-tags-input/ng-tags-input.js',
                        'infinite-scroller': './bower_components/ng-tags-input/ng-tags-input.js'
                    }

				}
			},
			'dashboard': {
				entry: './src/app.js',
				compile: './dist/assets/js/app.js',
				debug: true,
				options: {
                    expose: {
                        'dashboard-templates': './.temp/app-templates.js'
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
					cssDir: 'dist/assets/css'
				}
			},
			release: {
				options: {
					sassDir: 'src',
					cssDir: 'dist/assets/css',
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
			assets: {
				files: 'src/assets/**',
				tasks: ['copy:assets', 'deploy']
			},
			css: {
				files: ['src/**/*.scss'],
				tasks: ['compass:develop', 'deploy']
			},
			js: {
				files: 'src/**/*.js',
				tasks: ['build-js', 'deploy']
			},
			html: {
				files: ['src/**/*.tpl.html', 'src/**/index.html'],
				tasks: ['copy:statichtml', 'build-js', 'deploy']
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
                port:8080,
                user:"admin",
                pass:"admin",
                exclude: ["*.git"]
            },
            dist: {
                src: "dist",
                dest: "/content/dashboard"
            }

        }



	});

};