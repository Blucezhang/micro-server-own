(function() {
"use strict";

var app = angular.module("angular-smarty-config", []);

app.service("smartyConfig", ["$timeout", "$q", function($timeout, $q) {
    var possibleSuggestions = ["bakasana", "adho mukha svanasana", "adho mukha vrkshasana",
                               "badafasdf","百度百科","哈哈21312312","a","aa","aaa","王呵呵","李XX"];

    function getSmartySuggestions(prefix) {
        var deferred = $q.defer();
        $timeout(function() {
            var suggestions = findSuggestions(prefix);
            if (suggestions.length > 0) {
                deferred.resolve(suggestions);
            } else {
                deferred.reject([]);
            }
        }, 0);
        return deferred.promise;
    };

    function findSuggestions(prefix) {
        var prefix = prefix.toLowerCase();
        var suggestions = [];
        for (var i = 0; i < possibleSuggestions.length; i++) {
            if (possibleSuggestions[i].length < prefix.length) {
                continue;
            } else if (possibleSuggestions[i].slice(0, prefix.length) == prefix) {
                suggestions.push(possibleSuggestions[i]);
            }
        }
        return suggestions;
    };

    return {
        getSmartySuggestions: getSmartySuggestions
    }
}]);

app.service("smartySuggestor", ["smartyConfig", function(smartyConfig) {
    var getSmartySuggestions = smartyConfig.getSmartySuggestions;

    return {
        getSmartySuggestions: getSmartySuggestions
    };
}]);
})();
