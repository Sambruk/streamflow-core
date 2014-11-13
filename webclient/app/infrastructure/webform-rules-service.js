/*
 *
 * Copyright 2009-2014 Jayway Products AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
angular.module('sf')
.factory('webformRulesService', function(){
// TODO: Test all possible form configurations.
// If something does not work, look in the viewcontroller and the view template
// to verify that everything there looks appropriate

  //Recursive function that traverses down the object and looks for matches
  // between the objects values and the expected value.
  // Returns true or false depending on whether a match was found.
  var _findDeep = function(items, attrs) {
    function match(value) {
      for (var key in attrs) {
        if(!_.isUndefined(value)) {
          if(value == null){
            return false;
          }
          if (attrs[key] !== value[key]) {
            return false;
          }
        }
      }
      return true;
    }

    function traverse(value) {
      var result;
      _.forEach(value, function (val) {
        if (result == true) {
          return;
        }
        if (match(val) && val.field) {
          result = true;
          return;
        }
        if (_.isObject(val) || _.isArray(val)) {
          result = traverse(val);
        }
      });
      return result;
    }

    var a = traverse(items);
    if(a == true){
      return true;
    } else {
      //return traverse(items);
      return false;
    }
  };

  //Init function
  var _applyRules = function(obj){
  	var rootObj = obj;
  	_applyRulesToElements(obj, rootObj);
  }

  //Recursive function that traverses down the object tree and executes
  // displayFieldIfRuleValuePresent for each object/array in the tree
  var _applyRulesToElements = function(obj, rootObj){
    _.each(obj, function(objItem){
    	if(objItem == null){
    		return;
    	}
      if(typeof objItem == 'object' && objItem !== null){
        _displayFieldIfRuleValuePresent(objItem, rootObj);
        _applyRulesToElements(objItem, rootObj);
      } else {
        // Maybe do something else here...
      }
    });
  };

  //Checks if the object has any parents with value that matches the objects rule value
  // if true it displays the object field, else it hides it.
  var _displayFieldIfRuleValuePresent = function(obj, rootObj){
    if(!obj.rule){
      return;
    }
    if(obj.rule.field.length > 1 && obj.rule.visibleWhen === true ){//&& obj.rule.values.length > 0){
      //console.log('has rule');
      //console.log(obj);
      var ruleFulfilled = _ruleValuePresent(rootObj, obj.rule.values[0]);
      //console.log('rule fulfilled?');
      //console.log(ruleFulfilled);
      var fieldId;
      if(obj.field){
        //console.log('Object has field attribute');
        fieldId = obj.field;
      } else if(obj.page) {
        //console.log('Object has page attribute');
        fieldId = obj.page;
      }

      if(ruleFulfilled === true){
        _displayField(fieldId);
      } else {
        _hideField(fieldId);
      }
    } else {
    	if(obj.field){
        _displayField(obj.field);
      }
      if(obj.page){
        _displayField(obj.page);
      }
    }
  };

  //Recursive function that traverses down the object tree until it finds an object with
  // value that matches the ruleVal
  var _ruleValuePresent = function(obj, ruleVal){
    return _findDeep(obj, {'value': ruleVal});
  };

  //Display field (remove ng-hide class if present)
  var _displayField = function(fieldId){
    var element = $('#' + fieldId);
    if(element.hasClass('ng-hide')){
      element.removeClass('ng-hide');
    }
    element.addClass('ng-show');
  };

  //Hide field (remove ng-show class if present)
  var _hideField = function(fieldId){
    debugger;
    var fld = $('#'+fieldId);
   //console.log(fld);
    //fld.css('display', 'hidden');
   //  debugger;
    
    //console.log('hiding field: ' + fieldId);
    var element = $('#' + fieldId);
    if(element.hasClass('ng-show')){
      element.removeClass('ng-show');
    }
    element.addClass('ng-hide');
  };

  return {
    findDeep: _findDeep,
  	applyRules: _applyRules
  };
});