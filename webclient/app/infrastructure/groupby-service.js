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
'use strict';

angular.module('sf')
.factory('groupByService', function(){
  
  var groupingOptions = [{name:'Ärendetyp', value:'caseTypeText'},
      {name:'Förfallodatum*', value:'dueOn'},
      {name:'Förvärvare', value:'assignedTo'},
      {name:'Projekt', value:'owner'},
      {name:'*Prioritet*', value:'priority'}];
  
  var getGroupingOptions = function(){
  	return groupingOptions;
  };

  var groupBy = function(currentCases, originalCurrentCases, selectedGroupItem){
    var groupCurrentCases = [];

    _.each(currentCases, function(item){
      switch(selectedGroupItem.value) {
        case 'caseTypeText':
          if(!item.caseType){
            item.caseTypeText = 'Ingen ärendetyp';
          } else {
            item.caseTypeText = item.caseType.text.toLowerCase();
          }
          break;
        case 'assignedTo':
          if(!item.assignedTo){
            item.assignedTo = 'Ingen förvärvare';
          }
          break;
        case 'owner':
          if(!item.owner){
            item.owner = 'Inget projekt';
          }
        default:
          currentCases = originalCurrentCases;
      }

      if(selectedGroupItem){
        groupCurrentCases.push(item);
      }
    });

    if(groupCurrentCases.length){
      currentCases = groupCurrentCases;
    }
    if(selectedGroupItem){
      currentCases['grouped'] = true;
    }

    return currentCases;
  };

   return {
   	groupBy: groupBy,
   	getGroupingOptions: getGroupingOptions
   }

});
