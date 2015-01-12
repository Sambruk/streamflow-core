'use strict';

angular.module('sf')
.factory('checkPermissionService', function(){

	var checkCommands = function(scope, commands, commandsToCheck, paraPermission){
		// TODO: Check how this works if there's many commands. Ex. check contact, update/delete
		// TODO: Don't forget to fix the unit-tests
		if(commandsToCheck.length){
			commandsToCheck.forEach(function(command, index){
				checkCommand(scope, commandsToCheck[index], commands, paraPermission);
			});
		}
		return scope;
	}

	var checkQueries = function(){

	}

	var checkCommand = function(scope, command, commands, paraPermission){;
		if(_.find(commands, function(obj){return obj.id == command})){
			scope[paraPermission] = true;
		}
		return scope;
	}

	return {
		checkCommands: checkCommands,
		checkQueries: checkQueries
	}	
});