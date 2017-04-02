var app = angular.module('Config', []);

var servicePath = "http://localhost:50003";

app.constant('personUrl',servicePath+"/Person");
app.constant('orgUrl',servicePath+"/Org");
app.constant('categoryUrl',servicePath+"/Category");

