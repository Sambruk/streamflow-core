'use strict';

angular.module('sf')
.factory('checkPermissionService', function(){

	var checkCommands = function(scope, commands, commandsToCheck, paraPermission){
		// Commands and paraPermission must be sent in the same order
		if(commandsToCheck.length){
			commandsToCheck.forEach(function(command, index){
				checkCommand(scope, commands, commandsToCheck[index], paraPermission[index]);
			});
		}
		return scope;
	}

	var checkQueries = function(){

	}

	var checkCommand = function(scope, commands, command, paraPermission){
		if(_.find(commands, function(obj){return obj.id == command})){
			scope[paraPermission] = true;
		}
		return scope;
	}

	return {
		checkCommands: checkCommands,
		checkCommand: checkCommand,
		checkQueries: checkQueries
	}	
});