'use strict';

angular.module('sf')
.factory('checkPermissionService', function(){

	var checkPermissions = function(scope, permissions, permissionsToCheck, paraPermission){
		// Commands and paraPermission must be sent in the same order
		// console.log(paraPermission + ": " + permissions);
		if(permissionsToCheck.length){
			permissionsToCheck.forEach(function(permission, index){
				checkPermission(scope, permissions, permissionsToCheck[index], paraPermission[index]);
			});
		}
		return scope;
	}

	var checkPermission = function(scope, permissions, permission, paraPermission){
		// console.log(paraPermission + ": ")
		// console.log(permissions)
		if(_.find(permissions, function(obj){return obj.id == permission})){
			scope[paraPermission] = true;
		}
		return scope;
	}

	return {
		checkPermissions: checkPermissions,
		checkPermission: checkPermission
	}	
});