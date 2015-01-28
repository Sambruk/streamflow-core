'use strict';

angular.module('sf')
.factory('fileService', function($upload, $routeParams){

	var uploadFiles = function($files, url){
		if($files.length === 1){
			uploadFile($files[0], url);
		}else{
	    $files.forEach(function(file){
				uploadFile(file, url);
		  });
	  }
	};

	var uploadFile = function(file, url){
    return $upload.upload({
      url: url,
      headers: {'Content-Type': 'multipart/formdata'},
      file: file
    });
	};

	return {
		uploadFiles: uploadFiles,
		uploadFile: uploadFile
	};
});
