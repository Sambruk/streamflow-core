angular.module('sf')
.factory('webformRulesService', function(){
// TODO: Make other pages work accordingly, see _

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

  //TODO: It seems like we dont check rules for the other page here.
  // Investigate and fix.

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
      var ruleFulfilled = _ruleValuePresent(rootObj, obj.rule.values[0]);

      if(ruleFulfilled === true){
        _displayField(obj.field);
      } else {
        _hideField(obj.field);
      }
    } else {
    	//Maybe do something else here ...
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