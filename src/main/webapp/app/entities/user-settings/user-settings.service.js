(function() {
    'use strict';
    angular
        .module('21PointsApp')
        .factory('UserSettings', UserSettings);

    UserSettings.$inject = ['$resource'];

    function UserSettings ($resource) {
        var resourceUrl =  'api/user-settings/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
