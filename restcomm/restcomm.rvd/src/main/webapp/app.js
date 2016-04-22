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
            },
            'container@': {
                template: '<ui-view/>',
                controller: 'containerCtrl'
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
        templateUrl: 'templates/home.html'
    });
    $stateProvider.state('root.rvd.projectManager',{
        url: '/project-manager/:projectKind',
        templateUrl: 'templates/projectManager.html',
        controller: 'projectManagerCtrl'
    });
    $stateProvider.state('root.rvd.designer', {
        url: '/designer/:applicationSid=:projectName',
        templateUrl : 'templates/designer.html',
        controller : 'designerCtrl',
        resolve: {
            project: function(designerService, $stateParams, $state) {
                return designerService.openProject($stateParams.applicationSid);
            },
            bundledWavs: function(designerService) { return designerService.getBundledWavs()}
        }

    });
    $stateProvider.state('root.rvd.projectLog', {
        url: '/designer/:applicationSid=:projectName/log',
    	templateUrl : 'templates/projectLog.html',
    	controller : 'projectLogCtrl'
    });
    $stateProvider.state('root.rvd.packaging',{template:'<ui-view/>'}); // does nothing for now
    $stateProvider.state('root.rvd.packaging.details',{
        url: '/packaging/:applicationSid=:projectName',
        templateUrl : 'templates/packaging/form.html',
        controller : 'packagingCtrl',
        resolve: {
            rappWrap: function(RappService) {return RappService.getRapp();},
            rvdSettingsResolver: function (rvdSettings) {return rvdSettings.refresh();} // not meant to return anything back. Just trigger the fetching of the settings
        }
    });
    $stateProvider.state('root.rvd.packaging.download', {
        url:'/packaging/:applicationSid=:projectName/download',
   		templateUrl : 'templates/packaging/download.html',
   		controller : 'packagingDownloadCtrl',
   		resolve: {
   			binaryInfo: packagingDownloadCtrl.getBinaryInfo
   		}
    });
    // not sure what this state does. It should probably be removed
    $stateProvider.state('root.rvd.upgrade', {
        url: '/upgrade/:projectName',
        templateUrl : 'templates/upgrade.html',
        controller : 'upgradeCtrl'
    });

    //$stateProvider.state('root.rvd.designer',{});
    $urlRouterProvider.otherwise('/home');

    $translateProvider.useStaticFilesLoader({
        prefix: '/restcomm-rvd/languages/',
        suffix: '.json'
    });
    $translateProvider.useCookieStorage();
    $translateProvider.preferredLanguage('en-US');
}]);

/*
App.config([ '$routeProvider', '$translateProvider', function($routeProvider, $translateProvider) {

	.when('/upgrade/:projectName', {
		templateUrl : 'templates/upgrade.html',
		controller : 'upgradeCtrl',
		resolve: {
			authInfo: function (authentication) {return authentication.authResolver();}
		}
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

