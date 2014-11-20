'use strict';

angular.module('sf')
.factory('fileService', function($upload, $routeParams){

	var uploadFiles = function($files, url){
    $files.forEach(function(file){
	    $upload.upload({
	    	// TODO: extract url to be more dynamic
	      url: url,
	      headers: {'Content-Type': 'multipart/formdata'},
	      file: file // or list of files ($files) for html5 only
	    })
	    // .progress(function(evt) {
	    //   scope.uploadProgress = parseInt(100.0 * evt.loaded / evt.total);
	    //   console.log(scope.uploadProgress);
	    //   // console.log('percent: ' + parseInt(100.0 * evt.loaded / evt.total));
	    // })
	    .success(function(data, status, headers, config) {
	      // file is uploaded successfully
	      console.log(data);
	    });
	  });
	}

	return {
		uploadFiles: uploadFiles
	}	

});