var app = angular.module('Config', []);

var servicePath = "http://localhost:2222";

app.constant('personUrl',servicePath+"/Person");
app.constant('orgUrl',servicePath+"/Org");


