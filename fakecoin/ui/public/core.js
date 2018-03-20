var wallet = angular.module('wallet', []);

function mainController($scope, $http) {
    $scope.formData = {};

    // when landing on the page, get all wallets and show them
    $http.get('/api/wallets')
        .success(function(data) {
            $scope.wallets = data.wallets;
            console.log(data);
        })
        .error(function(data) {
            console.log('Error: ' + data);
        });

    // when submitting the add form, send the text to the node API
    $scope.createTransaction = function() {
        $http.post('/api/wallets/sign', $scope.formData)
            .success(function(data) {
                $scope.formData = {}; // clear the form so our user is ready to enter another
                // $scope.wallets = data;
                console.log(data);
            })
            .error(function(data) {
                console.log('Error: ' + data);
            });
    };

}