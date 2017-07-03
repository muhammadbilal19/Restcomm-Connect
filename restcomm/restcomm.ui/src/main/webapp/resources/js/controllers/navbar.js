'use strict';

var rcMod = angular.module('rcApp');

rcMod.controller('UserMenuCtrl', function($scope, $http, $resource, $rootScope, $location, $uibModal, AuthService, Notifications, RCommAccounts, $state) {

  /* watch location change and update root scope variable for rc-*-pills */
  $rootScope.$on('$locationChangeStart', function(/*event, next, current*/) {
    $rootScope.location = $location.path();
  });

  //$scope.auth = AuthService;
  //$scope.sid = SessionService.get('sid');
  $scope.friendlyName = AuthService.getFrientlyName();

  $scope.testNotifications = function() {
    Notifications.info('This is an info message');
    Notifications.warn('This is an warning message');
    Notifications.error('This is an error message');
    Notifications.success('This is an success message');
  };

  $scope.logout = function() {
    AuthService.logout();
    $state.go('public.login');
  };

  //if(AuthService.isLoggedIn()) {
  var accountsList;
  function getAccountList () {
   accountsList = RCommAccounts.query(function() {
        $scope.accountsList = accountsList;
        for (var x in accountsList){
          if(accountsList[x].sid == $scope.sid) {
            $scope.currentAccount = accountsList[x];
          }
        }
      });
  };
  getAccountList();

  // when new sub-account is created make sure the list is updated
  $scope.$on("account-created", function () {
    getAccountList();
  });
  //}

  // add account -------------------------------------------------------------

  $scope.showRegisterAccountModal = function () {
    var registerAccountModal = $uibModal.open({
      controller: RegisterAccountModalCtrl,
      scope: $scope,
      templateUrl: 'modules/modals/modal-register-account.html'
    });

    registerAccountModal.result.then(
      function () {
        // what to do on modal completion...
      },
      function () {
        // what to do on modal dismiss...
    	  $scope.subAccountsList = accountsList;
      }
    );
  };

  $scope.showAboutModal = function () {
    $uibModal.open({
      controller: AboutModalCtrl,
      scope: $scope,
      windowClass: 'temp-modal-lg',
      templateUrl: 'modules/modals/modal-about.html'
    });
  };

});

rcMod.controller('SubAccountsCtrl', function($scope, $resource, $stateParams, RCommAccounts,Notifications) {
	$scope.predicate = 'name';  
    $scope.reverse = false;
    $scope.search = {};
    $scope.currentPage = 1;  
     $scope.maxSize = 5; //pagination max size
    $scope.entryLimit = 10; //max rows for data table
     $scope.order = function (predicate) {  
    $scope.reverse = ($scope.predicate === predicate) ? !$scope.reverse : false;  
     $scope.predicate = predicate; 
    };
    $scope.statusFilter = 'Any';
  
    var subAccountsList = RCommAccounts.query(function(list) {
		// remove logged (parent) account from the list
		var i = 0;
		while (i < list.length) {
			if (list[i].sid == $scope.sid )
			  list.splice(i,1)
			else
			  i ++;
		}
      $scope.subAccountsList = list;
      $scope.totalItems = list.length;
    });;
    
    $scope.setEntryLimit = function(limit) {
        $scope.entryLimit = limit;
        $scope.numPerPage = Math.ceil($scope.subAccountsList.length / $scope.entryLimit);
      }; 
  
    $scope.paginate = function (value) {  
      var begin, end, index;  
      begin = ($scope.currentPage - 1) * $scope.entryLimit;  
      end = begin + $scope.entryLimit;  
      index = $scope.subAccountsList.indexOf(value);  
      return (begin <= index && index < end);  
    };

    $scope.$watch('statusFilter', function (value) {
        if (value == 'Any')
            $scope.search.status = '';
        else
            $scope.search.status = value.toLowerCase();
    });
  }); 



rcMod.controller('ProfileCtrl', function($scope, $resource, $stateParams, SessionService,AuthService, RCommAccounts, md5,Notifications, $location, $dialog) {
    var loggedUserAccount = AuthService.getAccount();
    // retrieve the account in the URL
    $scope.urlAccountSid = $stateParams.accountSid;
    // make a copy of the urlAccount to help detect changes in the form
    $scope.urlAccount = RCommAccounts.view({accountSid:$scope.urlAccountSid},function (data) {
        $scope.urlAccountBackup = angular.copy($scope.urlAccount);
    });
    // retrieve the sub-account of the logged account
    $scope.loggedSubAccounts = RCommAccounts.query();
    $scope.accountChanged = false;
    $scope.formIsValid = false;
    $scope.passwordsDiffer = false;
    $scope.strongPassword = false;
    // watch for changes in the password fields
    $scope.$watchCollection('[newPassword, newPassword2]', function() {
        if ($scope.newPassword) {
            if($scope.newPassword != $scope.newPassword2) {
                $scope.passwordsDiffer = true;
                $scope.profileForm.newPassword.$valid = false;
                return;
            }
        }
        $scope.passwordsDiffer = false;
        $scope.profileForm.newPassword.$valid = true;
    });
    $scope.setAccountStatus = function (status) {
        $scope.urlAccount.status = status;
        $scope.profileForm.$setDirty(); // set it manually since there is no model to bind to
    }
    $scope.resetChanges = function () {
        $scope.profileForm.$setPristine();
        $scope.urlAccount = angular.copy($scope.urlAccountBackup);
        $scope.newPassword = '';
        $scope.newPassword2 = '';
    }

    $scope.updateProfile = function() {
    var params = {FriendlyName: $scope.urlAccount.friendly_name, Type: $scope.urlAccount.type, Status: $scope.urlAccount.status,Role: $scope.urlAccount.role};
        if ($scope.newPassword) {
            params['Password'] = $scope.newPassword;
        }
        RCommAccounts.update({accountSid:$scope.urlAccount.sid}, $.param(params), function() {
            // update our backup model and keep editing
            $scope.urlAccountBackup = angular.copy($scope.urlAccount);
            $scope.newPassword = '';
            $scope.newPassword2 = '';
            $scope.profileForm.$setPristine();
            Notifications.success('Profile updated successfully.');
        }, function() {
            // error
            Notifications.error('Failure updating profile. Please check data and try again.');
        });
    };
    $scope.$on("account-created", function () {
        console.log("Received account-created notification");
        $scope.loggedSubAccounts = RCommAccounts.query();
    });

    $scope.closeAccount = function (account) {
        var title = 'Close account';
        var msg = 'Are you sure you want to close account ' + account.sid + ' (' + account.friendly_name +  ') ? This action cannot be undone.';
        var btns = [{result:'cancel', label: 'Cancel', cssClass: 'btn-default'}, {result:'confirm', label: 'Close!', cssClass: 'btn-danger'}];
        // show configurmation
        $dialog.messageBox(title, msg, btns).open().then(function (result) {
            if (result == "confirm") {
                RCommAccounts.update({accountSid:account.sid}, $.param({Status:"closed"}), function() {
                    $scope.urlAccount = RCommAccounts.view({accountSid:$scope.urlAccountSid},function (data) {
                        $scope.urlAccountBackup = angular.copy($scope.urlAccount);
                    });
                    //$scope.getAccounts();
                }, function() { // error
                    Notifications.error("Can't close Account '" + account.friendly_name + "'");
                });
            }
        });
    }
});

// Register Account Modal

var RegisterAccountModalCtrl = function ($scope, $uibModalInstance, RCommAccounts, Notifications, AuthService, $rootScope) {
    var loggedUserAccount = AuthService.getAccount();
    $scope.statuses = ['ACTIVE','UNINITIALIZED','SUSPENDED','INACTIVE','CLOSED'];
    $scope.newAccount = {role: loggedUserAccount.role};
    $scope.createAccount = function(account) {
    if(account.email && account.password) {
      // Numbers.register({PhoneNumber:number.number});ild
      account.friendlyName = account.friendlyName || account.email;
      RCommAccounts.register($.param(
        {
          EmailAddress : account.email,
          Password: account.password,
          Role: account.role,
          Status: account.status,
          FriendlyName: account.friendlyName ? account.friendlyName : account.email
        }),
        function() { // success
          Notifications.success('Account  "' + account.friendlyName + '" created successfully!');
          $rootScope.$broadcast("account-created"); // handler should refresh sub-account list
          $uibModalInstance.close();
        },
        function(response, status) { // error
            Notifications.error("Can't create account." + ((response.data && response.data.message) ? response.data.message : ""))
        }
      );
    }
    else {
      Notifications.error('Required fields are missing.');
    }
  };
  
  $scope.cancel = function () {
    $uibModalInstance.dismiss('cancel');
  };
};

var AboutModalCtrl = function($scope, $uibModalInstance, RCommJMX, RCVersion) {

	$scope.Math = window.Math;

	$scope.getData = function() {
		$scope.version = RCVersion.get({
			accountSid : $scope.sid
		}, function(data) {
			if (data) {
				var version = $scope.version;
				var pattern = /(\d{4})(\d{2})(\d{2})(\d{2})(\d{2})/;
				$scope.releaseDate = new Date(data.Date.replace(pattern,
						'$1-$2-$3 $4:$5'));
			}

		}, function() {
		});
		$scope.info = RCommJMX.get({
			path : 'java.lang:type=*'
		}, function(data) {
			$scope.OS = data.value['java.lang:type=OperatingSystem'];
			$scope.JVM = data.value['java.lang:type=Runtime'];
			$scope.Memory = data.value['java.lang:type=Memory'];
			$scope.Threads = data.value['java.lang:type=Threading'];
		}, function() {
		});
	};

	$scope.cancel = function() {
		$uibModalInstance.dismiss('cancel');
	};

	$scope.getData();
};
