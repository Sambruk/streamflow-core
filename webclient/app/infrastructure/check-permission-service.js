'use strict';

angular.module('sf')
.factory('checkPermissionService', function(){

	var checkPermissions = function(scope, permissions, permissionsToCheck, paraPermission){
		// permissions - the returned permissions the user have.
		// permissionsToCheck - the permissions to check against for letting the user do certain actions.
		// paraPermissions - set to true if the user have permission to certain commands/queries.
		// Commands and paraPermission must be sent in the same order

		if(permissionsToCheck.length){
			permissionsToCheck.forEach(function(permission, index){
				checkPermission(scope, permissions, permissionsToCheck[index], paraPermission[index]);
			});
		}
		return scope;
	}

	var checkPermission = function(scope, permissions, permission, paraPermission){
		if(_.find(permissions, function(obj){return obj.id == permission})){
			scope[paraPermission] = true;
		}
		return scope;
	}

	return {
		checkPermissions: checkPermissions
	}	
});