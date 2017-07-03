'use strict';

var rcMod = angular.module('rcApp');

// Numbers : RestComm Clients : List ------------------------------------------------

rcMod.controller('ClientsCtrl', function($scope, $resource, $uibModal, $dialog, SessionService, RCommClients, RCommApps, Notifications) {

  $scope.sid = SessionService.get("sid");

  // edit incoming client friendly name --------------------------------------
  $scope.editingFriendlyName = "";

  $scope.setFriendlyName = function(client) {
    var params = {Login: client.login, FriendlyName: client.friendly_name};

    RCommClients.update({accountSid:$scope.sid, clientSid:client.sid}, $.param(params),
      function() { // success
        $scope.editingFriendlyName = "";
      },
      function() { // error
      // TODO: Show alert
      }
    );
  }

  // add incoming client -----------------------------------------------------

  $scope.showRegisterSIPClientModal = function () {
    var registerSIPClientModal = $uibModal.open({
      controller: 'ClientDetailsCtrl',
      scope: $scope,
      templateUrl: 'modules/modals/modal-register-sip-client.html',
      resolve: { localApps: function (rappService) { return rappService.refreshLocalApps();} }
    });

    registerSIPClientModal.result.then(
      function () {
        // what to do on modal completion...
        $scope.clientsList = RCommClients.query({accountSid:$scope.sid});
      },
      function () {
        // what to do on modal dismiss...
      }
    );
  };

  // delete RestComm client -------------------------------------------------------

  $scope.confirmClientDelete = function(client) {
    confirmClientDelete(client, $dialog, $scope, Notifications, RCommClients);
  }

  $scope.clientsList = RCommClients.query({accountSid:$scope.sid});
});

// Numbers : RestComm Clients : Details (also used for Modal) -----------------------

rcMod.controller('ClientDetailsCtrl', function ($scope, $stateParams, $location, $dialog, $uibModalInstance, SessionService, RCommClients, RCommApps, Notifications, localApps, Applications) {

	$scope.localApps = Applications.filterByKind(localApps,'voice');
  // are we editing details...
  if($scope.clientSid = $stateParams.clientSid) {
    $scope.sid = SessionService.get("sid");

    $scope.clientDetails = RCommClients.get({accountSid:$scope.sid, clientSid: $scope.clientSid});
  } // or registering a new one ?
  else {
    // start optional items collapsed
    $scope.clientDetails = {};
    $scope.isCollapsed = true;

    $scope.closeRegisterSIPClient = function () {
      $uibModalInstance.dismiss('cancel');
    };
  }

  // query for available apps
  //$scope.availableApps = RCommApps.query();

  var createSIPClientParams = function(client) {
    var params = {};

    // Mandatory fields
    var effectivePassword = ($scope.editingPass && client.newPassword) ? client.newPassword : client.password;
    if(client.login && effectivePassword) {
      params["Login"] = client.login;
      params["Password"] = effectivePassword;
    }
    else {
      alert("You must provide Login and Password.");
    }

    // Optional fields
    if (client.friendly_name) {
      params["FriendlyName"] = client.friendly_name;
    }
    // Always send, value consistency controlled by actions performed by user
    params["VoiceApplicationSid"] = client.voice_application_sid;
    params["VoiceUrl"] = client.voice_url;
    params["VoiceMethod"] = client.voice_method;
    
    if (client.voice_fallback_url || client.voice_fallback_url === "") {
      params["VoiceFallbackUrl"] = client.voice_fallback_url;
      params["VoiceFallbackMethod"] = client.voice_fallback_method;
    }
    if (client.status_callback_url || client.status_callback_url === "" ) {
      params["StatusCallback"] = client.status_callback_url;
      params["StatusCallbackMethod"] = client.status_callback_method;
    }
    // disabled
    if (client.sms_url) {
      params["SmsUrl"] = client.sms_url;
      params["SmsMethod"] = client.sms_method;
    }
    // disabled
    if (client.sms_fallback_url) {
      params["SmsFallbackUrl"] = client.sms_fallback_url;
      params["SmsFallbackMethod"] = client.sms_fallback_method;
    }

    return params;
  }

  $scope.registerSIPClient = function(client) {
    var params = createSIPClientParams(client);
    RCommClients.register({accountSid: $scope.sid}, $.param(params),
      function() { // success
        Notifications.success('RestComm Client "' + client.login + '" created successfully!');
        $uibModalInstance.close();
      },
      function() { // error
        // TODO: Show alert
      }
    );
  };

  $scope.updateSIPClient = function(client) {
    var params = createSIPClientParams(client);
    RCommClients.update({accountSid: $scope.sid, clientSid: $scope.clientSid}, $.param(params),
      function() { // success
        Notifications.success('RestComm Client "' + client.login + '" updated successfully!');
        $location.path( "/numbers/clients" );
      },
      function() { // error
        Notifications.error('Failed to update client "' + client.login + '".');
      }
    );
  };

  $scope.confirmClientDelete = function(client) {
    confirmClientDelete(client, $dialog, $scope, Notifications, RCommClients, $location);
  }

  $scope.togglePasswordEdit = function () {
    $scope.editingPass = !$scope.editingPass;
    if (!$scope.editingPass)
      $scope.clientDetails.newPassword = "";
  }
});

var confirmClientDelete = function(client, $dialog, $scope, Notifications, RCommClients, $location) {
  var title = 'Delete RestComm Client \'' + client.login + '\'';
  var msg = 'Are you sure you want to delete RestComm Client ' + client.login + ' (' + client.friendly_name +  ') ? This action cannot be undone.';
  var btns = [{result:'cancel', label: 'Cancel', cssClass: 'btn-default'}, {result:'confirm', label: 'Delete!', cssClass: 'btn-danger'}];

  $dialog.messageBox(title, msg, btns)
    .open()
    .then(function(result) {
      if (result == "confirm") {
        RCommClients.delete({accountSid:$scope.sid, clientSid:client.sid}, {},
          function() {
        	Notifications.success('RestComm Client "' + client.login + '" has been deleted.');
            if($location) {
              $location.path( "/numbers/clients" );
            }
            else {
              $scope.clientsList = RCommClients.query({accountSid:$scope.sid});
            }
          },
          function() {
        	  Notifications.error('Failed to delete client "' + client.login + '".');
          }
        );
      }
    });
};
