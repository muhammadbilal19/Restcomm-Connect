var App = angular.module('Rvd', [
	'angularFileUpload',
	'ngRoute',
	'ngDragDrop',
	'ui.bootstrap',
	'ui.bootstrap.collapse',
	'ui.bootstrap.popover',
	'ui.sortable',
	'basicDragdrop',
	'pascalprecht.translate',
	'ngSanitize',
	'ngResource',
	'ngCookies',
	'ngIdle',
	'ui.router'
]);

var rvdMod = App;

App.config(['$stateProvider','$urlRouterProvider', '$translateProvider', function ($stateProvider,$urlRouterProvider,$translateProvider) {
    $stateProvider.state('root',{
        resolve:{
            init: function (initializer) {
                console.log('Initializing RVD');
                return initializer.init();
            }
        }
    });
    $stateProvider.state('root.public',{});
    $stateProvider.state('root.public.login',{
        url:"/login",
        views: {
            'container@': {
                templateUrl: 'templates/login.html',
                controller: 'loginCtrl'
            }
        }
    });
    $stateProvider.state('root.rvd',{
        views: {
            'authmenu@': {
                templateUrl: 'templates/index-authmenu.html',
                controller: 'authMenuCtrl'
            }
        },
        resolve: {
            authorize: function (init, authentication) { // block on init ;-)
                authentication.checkRvdAccess(); // pass required role here
            }
        }
    });
    $stateProvider.state('root.rvd.home',{
        url:"/home",
        views: {
            'container@': {
                templateUrl: 'templates/home.html'
            }
        }
    });
    /*
    $stateProvider.state('root.rvd.projectManager',{
        views: {
            'container@': {
                templateUrl: 'templates/projectManager.html',
                controller: 'projectManagerCtrl'
            }
        }
    });
    */
    //$stateProvider.state('root.rvd.designer',{});

    $translateProvider.useStaticFilesLoader({
        prefix: '/restcomm-rvd/languages/',
        suffix: '.json'
    });
    $translateProvider.useCookieStorage();
    $translateProvider.preferredLanguage('en-US');
}]);

/*
App.config([ '$routeProvider', '$translateProvider', function($routeProvider, $translateProvider) {

	$routeProvider.when('/project-manager/:projectKind', {
		templateUrl : 'templates/projectManager.html',
		controller : 'projectManagerCtrl',
		resolve: {
			authInfo: function (authentication) {return authentication.authResolver();}
		}
	})
	.when('/home', {
		templateUrl : 'templates/home.html',
		controller : 'homeCtrl',
		resolve: {
			authInfo: function (authentication) {return authentication.authResolver();}
		}
	})
	.when('/designer/:applicationSid=:projectName', {
		templateUrl : 'templates/designer.html',
		controller : 'designerCtrl',
		resolve: {
			authInfo: function (authentication) {return authentication.authResolver();},
			//projectSettings: function (projectSettingsService, $route) {return projectSettingsService.retrieve($route.current.params.projectName);},
			project: function(designerService, $route) { return designerService.openProject($route.current.params.applicationSid); },
			bundledWavs: function(designerService) { return designerService.getBundledWavs()}
		}
	})
	.when('/packaging/:applicationSid=:projectName', {
		templateUrl : 'templates/packaging/form.html',
		controller : 'packagingCtrl',
		resolve: {
			rappWrap: function(RappService) {return RappService.getRapp();},
			authInfo: function (authentication) {return authentication.authResolver();},
			rvdSettingsResolver: function (rvdSettings) {return rvdSettings.refresh();} // not meant to return anything back. Just trigger the fetching of the settings
		}
	})
	.when('/packaging/:applicationSid=:projectName/download', {
		templateUrl : 'templates/packaging/download.html',
		controller : 'packagingDownloadCtrl',
		resolve: {
			binaryInfo: packagingDownloadCtrl.getBinaryInfo,
			authInfo: function (authentication) {return authentication.authResolver();}
		}
	})
	.when('/upgrade/:projectName', {
		templateUrl : 'templates/upgrade.html',
		controller : 'upgradeCtrl',
		resolve: {
			authInfo: function (authentication) {return authentication.authResolver();}
		}
	})
	.when('/login', {
		templateUrl : 'templates/login.html',
		controller : 'loginCtrl'
	})
	.when('/designer/:applicationSid=:projectName/log', {
		templateUrl : 'templates/projectLog.html',
		controller : 'projectLogCtrl'
	})
	.otherwise({
		redirectTo : '/home'
	});

}]);
*/

App.config(function(IdleProvider, KeepaliveProvider, TitleProvider) {
    // configure Idle settings
    IdleProvider.idle(3600); // one hour
    IdleProvider.timeout(15); // in seconds
    KeepaliveProvider.interval(300); // 300 sec - every five minutes
    TitleProvider.enabled(false); // it is enabled by default
})
.run(function(Idle){
    // start watching when the app runs. also starts the Keepalive service by default.
    Idle.watch();
});

App.factory( 'dragService', [function () {
	var dragInfo;
	var dragId = 0;
	var pDragActive = false;
	var serviceInstance = {
		newDrag: function (model) {
			dragId ++;
			pDragActive = true;
			if ( typeof(model) === 'object' )
				dragInfo = { id : dragId, model : model };
			else
				dragInfo = { id : dragId, class : model };

			return dragId;
		},
		popDrag:  function () {
			if ( pDragActive ) {
				var dragInfoCopy = angular.copy(dragInfo);
				pDragActive = false;
				return dragInfoCopy;
			}
		},
		dragActive: function () {
			return pDragActive;
		}

	};
	return serviceInstance;
}]);

/*
App.factory('protos', function () {
	var protoInstance = {
		nodes: {
				voice: {kind:'voice', name:'module', label:'Untitled module', steps:[], iface:{edited:false,editLabel:false}},
				ussd: {kind:'ussd', name:'module', label:'Untitled module', steps:[], iface:{edited:false,editLabel:false}},
				sms: {kind:'sms', name:'module', label:'Untitled module', steps:[], iface:{edited:false,editLabel:false}},
		},
	};
	return protoInstance;
});
*/


App.filter('excludeNode', function() {
    return function(items, exclude_named) {
        var result = [];
        items.forEach(function (item) {
            if (item.name !== exclude_named) {
                result.push(item);
            }
        });
        return result;
    }
});

