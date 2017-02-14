(function() {
    'use strict';

    angular
        .module('21PointsApp')
        .controller('UserSettingsDetailController', UserSettingsDetailController);

    UserSettingsDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'UserSettings', 'User'];

    function UserSettingsDetailController($scope, $rootScope, $stateParams, previousState, entity, UserSettings, User) {
        var vm = this;

        vm.userSettings = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('21PointsApp:userSettingsUpdate', function(event, result) {
            vm.userSettings = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
