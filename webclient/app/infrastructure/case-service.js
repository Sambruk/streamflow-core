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
.factory('caseService', function ($rootScope, httpService, backendService, navigationService, SfCase, $http, debounce, formMapperService) {

    var workspaceId = 'workspacev2';

    var caseBase = function(caseId){
     return [
        {resources: workspaceId},
        {resources: 'cases', unsafe: true},
        {resources: caseId, unsafe: true}
      ];
    };

   var getConversationMessage = function(caseId, conversationId, messageId) {
      return backendService.get({
        specs:caseBase(caseId).concat([
          {resources: 'conversations'},
          {'index.links': conversationId},
          {resources: 'messages'},
          {'index.links': messageId}
          ]),
        onSuccess:function (resource, result) {
          result.push(resource.response.index.text);
          caseBase.broadcastMessage(result.status);
        },
        onFailure:function(err){
          caseBase.broadcastMessage(err);
        }
      });
    };

    var getConversationMessageAttachments = function(caseId, conversationId, messageId) {
      return backendService.get({
        specs:caseBase(caseId).concat([
          {resources: 'conversations'},
          {'index.links': conversationId},
          {resources: 'messages'},
          {'index.links': messageId},
          {resources: 'attachments'}
          ]),
        onSuccess:function(resource, result){
          resource.response.index.links.forEach(function(item){result.push(item)});
          caseBase.broadcastMessage(result.status);
        },
        onFailure:function(err){
          caseBase.broadcastMessage(err);
        }
      });
    };

    var getAttachmentPermissions = function(caseId, attachmentId, attachment) {
      return backendService.get({
        specs:caseBase(caseId).concat([
          {resources: 'attachments'},
          {'index.links': attachmentId}
        ]),
        onSuccess:function(resource){
          attachment.commands = resource.response.commands;
          attachment.queries = resource.response.queries;
        },
        onFailure:function(err){
          caseBase.broadcastMessage(err);
        }
      });
    };

    //caseBase.bcMessage = null;
    //TODO: Refactor (use a var instead of property)
    var bcMessage = null;

    caseBase.broadcastMessage = function(msg){
      //caseBase.bcMessage = msg;
      bcMessage = msg;
      caseBase.initBroadcastMessage();
    };

    caseBase.initBroadcastMessage = function(message){
      $rootScope.$broadcast('httpRequestInitiated');
    };

    return {
      getWorkspace: function(){
        return workspaceId;
      },
      getMessage: function(){
        //return caseBase.bcMessage;
        return bcMessage;
      },
      getSelected: function(caseId) {
        return backendService.get({
          specs: caseBase(caseId),
          onSuccess:function (resource, result) {
            result.queries = resource.response.queries;
            result.push(new SfCase(resource.response.index));
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);
          }
        });
      },
      getSelectedCommands: function(caseId) {
        return backendService.get({
          specs: caseBase(caseId),
          onSuccess:function (resource, result) {
            if(resource.response.commands.length === 0){
              result.push(resource.response);
            } else {
              resource.response.commands.forEach(function(item){result.push(item)});
            }
          }
        });
      },

      getPossibleResolutions: function(caseId) {
        return backendService.get({
          specs:caseBase(caseId).concat([
            {queries: 'possibleresolutions'}
          ]),
          onSuccess:function (resource, result) {
            resource.response.links.forEach(function(item){result.push(item)});
          }
        });
      },

      resolveCase: function(caseId, resolutionId, callback) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {commands: 'resolve'}
            ]),
          {entity: resolutionId}).then(_.debounce(callback)());
      },

      restrictCase: function(caseId) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {commands: 'restrict'}
            ]),
          {});
      },

      unrestrictCase: function(caseId) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {commands: 'unrestrict'}
            ]),
          {});
      },

     getPermissions: function(caseId){
        return backendService.get({
            specs:caseBase(caseId).concat([
                {queries: 'permissions'}
            ]),
            onSuccess:function(resource, result) {
                result.push(resource.response);
                caseBase.broadcastMessage(result.status);
            }
        });
      },
      getPossibleSendTo: function(caseId) {
        return backendService.get({
          specs:caseBase(caseId).concat([
            {queries: 'possiblesendto'}
          ]),
          onSuccess:function (resource, result) {

            //NOTE: pushing resource.response.links to result if no links.
            // this might be wrong approach
            if(resource.response.links.length ===0){
              result.push(resource.response.links);
            } else {
              resource.response.links.forEach(function(item){result.push(item)});
            }
          }
        });
      },

      sendCaseTo: function(caseId, sendToId, callback) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {commands: 'sendto'}
          ]),
          {entity: sendToId}).then(_.debounce(callback)()).then(function(result){
            caseBase.broadcastMessage(result.status);
          }),
          function(error){
            caseBase.broadcastMessage(error);
          };
      },

      getCasePdf: function(caseId, submittedForms, attachments, conversations, contacts, caseLog) {
        var downloadURL = function downloadURL(url) {
        var hiddenIFrameID = 'hiddenDownloader',
            iframe = document.getElementById(hiddenIFrameID);
        if (iframe === null) {
            iframe = document.createElement('iframe');
            iframe.id = hiddenIFrameID;
            iframe.style.display = 'none';
            document.body.appendChild(iframe);
        }
        iframe.src = url;
        };

        var url = httpService.apiUrl+'workspacev2/cases/'+caseId+'/exportpdf?submittedForms='+submittedForms+'&attachments='+attachments+'&conversations='+conversations+'&contacts='+contacts+'&caselog='+caseLog;
        downloadURL(url);
      },

      closeCase: function(caseId, callback) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {commands: 'close'}
          ]),
          {}).then(function(result){
            caseBase.broadcastMessage(result.status);
          }, function(error){
            caseBase.broadcastMessage(error);
          }).then(callback);
      },

      reopenCase: function(caseId, callback) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {commands: 'reopen'}
          ]),
          {}).then(function(result){
            caseBase.broadcastMessage(result.status);
          }, function(error){
            caseBase.broadcastMessage(error);
          }).then(callback);
      },

      deleteCase: function(caseId, callback) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {commands: 'delete'}
          ]),
          {}).then(function(result){
            caseBase.broadcastMessage(result.status);
          }, function(error){
            caseBase.broadcastMessage(error);
          }).then(callback);
      },

      assignCase: function(caseId, callback) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {commands: 'assign'}
          ]),
          {}).then(function(result){
            caseBase.broadcastMessage(result.status);
          }, function(error){
            caseBase.broadcastMessage(error);
          }).then(callback);
      },

      unassignCase: function(caseId, callback) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {commands: 'unassign'}
          ]),
          {}).then(function(result){
            caseBase.broadcastMessage(result.status);
          }, function(error){
            caseBase.broadcastMessage(error);
          }).then(callback);
      },

      markUnread: function(caseId, callback) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {commands: 'markunread'}
          ]),
          {}).then(function(result){
            caseBase.broadcastMessage(result.status);
          }, function(error){
            caseBase.broadcastMessage(error);
          }).then(callback);
      },

      markRead: function(caseId, callback) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {commands: 'markread'}
          ]),
          {}).then(function(result){
            caseBase.broadcastMessage(result.status);
          }, function(error){
            caseBase.broadcastMessage(error);
          }).then(callback);
      },

      Read: function(caseId) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {commands: 'read'}
          ]),
          {}).then(function(result){
            caseBase.broadcastMessage(result.status);
          }, function(error){
            caseBase.broadcastMessage(error);
          }).then(callback);
      },

      getSelectedNote: function(caseId) {
        return backendService.get({
          specs:caseBase(caseId).concat([{resources: 'note'}]),
          onSuccess:function (resource, result) {
            result.push(resource.response.index);
            result.commands = resource.response.commands;
          },
        });
      },

      addNote: function(caseId, value) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {resources: 'note'},
            {commands: 'addnote'}
          ]),
          value);
      },

      getAllNotes: function(caseId) {
        return backendService.get({
          specs:caseBase(caseId).concat([
            {resources: 'note'},
            {queries: 'allnotes'}
            ]),
          onSuccess:function (resource, result) {
            resource.response.links.forEach(function(item){result.push(item)});
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);
          }
        });
      },

      getSelectedGeneral: function(caseId) {
        return backendService.get({
          specs:caseBase(caseId).concat([{resources: 'general'}]),
          onSuccess:function (resource, result) {
            var index = resource.response.index;

            if (index.dueOn)
              index.dueOnShort = index.dueOn.split("T")[0]

            result.push(index);
            result.commands = resource.response.commands;
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);
          }
        });
      },

      getSelectedConversations: function(caseId) {
        return backendService.get({
          specs:caseBase(caseId).concat([{resources: 'conversations'}]),
          onSuccess:function (resource, result) {
            result.commands = resource.response.commands;
            resource.response.index.links.forEach(function(link){
              result.push(link);
              caseBase.broadcastMessage(result.status);
            });
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);
          }
        });
      },

      getSelectedAttachments: function(caseId) {
        return backendService.get({
          specs:caseBase(caseId).concat([{resources: 'attachments'}]),
          onSuccess:function (resource, result) {
            resource.response.index.links.forEach(function(link, index){
              result.push(link);
              getAttachmentPermissions(caseId, link.id, result[index]);
            });
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);
          }
        });
      },

      deleteAttachment: function(caseId, attachmentId, callback) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {resources: 'attachments'},
            {'index.links': attachmentId},
            {commands: 'delete'}
            ]),
          {}).then(callback);
      },

      getSelectedContact: function(caseId, contactIndex) {
        return backendService.get({
          specs:caseBase(caseId).concat([
            {resources: 'contacts', unsafe: true},
            {resources: contactIndex, unsafe: true}
          ]),
          onSuccess:function (resource, result) {
            result.push(resource.response.index);
            result.commands = resource.response.commands;
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);
          }
        });
      },

      getSelectedContacts: function(caseId) {
        return backendService.get({
          specs:caseBase(caseId).concat([
            {resources: 'contacts'}
          ]),
          onSuccess:function (resource, result) {
            resource.response.index.contacts.forEach(function(item){result.push(item)});
            result.commands = resource.response.commands;
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);
          }
        });
      },

      addContact: function(caseId, value) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {resources: 'contacts', unsafe: true},
            {commands: 'add'}
          ]),
          value).then(function(result){
            caseBase.broadcastMessage(result.status);
          },
          function(error){
            caseBase.broadcastMessage(error);
          });
      },

      updateContact: function(caseId, contactIndex, value) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {resources: 'contacts', unsafe: true},
            {resources: contactIndex, unsafe: true},
            {commands: 'update'}
          ]),
          value).then(function(result){
            caseBase.broadcastMessage(result.status);
          },
          function(error){
            caseBase.broadcastMessage(error);
          });
      },
      getCaseLogCommands: function(caseId) {
        return backendService.get({
          specs:caseBase(caseId).concat([
              {resources: 'caselog'}
            ]),
          onSuccess:function (resource, result) {
            result.commands = resource.response.commands;
            result.push(resource.response);
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);
          }
        });
      },
      getCaseLogDefaultParams: function(caseId) {
        return backendService.get({
          specs:caseBase(caseId).concat([
              {resources: 'caselog'},
              {resources: 'defaultfilters', unsafe: true}
            ]),
          onSuccess:function (resource, result) {
            result.push(resource.response);
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);
          }
        });
      },
      getSelectedCaseLog: function(caseId) {

          //TODO: Look at why this is getting called twice on the caselog list page and if no way around it, maybe make sure the results are cached
          return backendService.get({
              specs:caseBase(caseId).concat([
                  {resources: 'caselog'},
                  {queries: 'list?system=true&systemTrace=true&form=true&conversation=true&attachment=true&contact=true&custom=true'}
              ]),
              onSuccess:function (resource, result) {
                  resource.response.links.forEach(function(link){
                      result.push(link);
                  });
                  caseBase.broadcastMessage(result.status);
              },
              onFailure:function(err){
                caseBase.broadcastMessage(err);
              }
          });
      },
      getSelectedFilteredCaseLog: function(caseId, queryfilter) {
        //console.log(queryfilter);
        //TODO: Look at why this is getting called twice on the caslog list page and if no way around it, maybe make sure the results are cached
        return backendService.get({
          specs:caseBase(caseId).concat([
              {resources: 'caselog'},
              {queries: 'list?system='+ queryfilter.system +
              '&systemTrace='+ queryfilter.systemTrace +
              '&form='+ queryfilter.form +
              '&conversation='+ queryfilter.conversation +
              '&attachment='+ queryfilter.attachment +
              '&contact='+ queryfilter.contact +
              '&custom='+ queryfilter.custom +''}
            ]),
          onSuccess:function (resource, result) {
            resource.response.links.reverse().forEach(function(link){
              result.push(link);
            });
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);
          }
        });
      },
      createCaseLogEntry: function(caseId, value) {
        //debugger;
        var specs = caseBase(caseId).concat([{resources: 'caselog'}, {commands: 'addmessage'}]),
            data = {string: value},
            responseSelector;
        return backendService.postNested(specs, data).then(function(response){
          caseBase.broadcastMessage(response.status);
          return response;
        }, function(err){
          caseBase.broadcastMessage(err);
          return err;
        });
        //backendService.postNested()
        /*return backendService.postNested(
          caseBase(caseId).concat([
            {resources: 'caselog'},
            {commands: 'addmessage'}
          ]),
          {string: value}).then(function(result){
            caseBase.broadcastMessage(result.status);
          }),
          function(error){
            caseBase.broadcastMessage(error);
          };*/
      },
      getPossibleCaseTypes: function(caseId) {
        return backendService.get({
          specs:caseBase(caseId).concat([
            {resources: 'general'},
            {queries: 'possiblecasetypes'}
          ]),
          onSuccess:function (resource, result) {
            var caseTypeOptions = resource.response.links;
            caseTypeOptions.forEach(function(item){result.push(item)});
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);
          }
        });
      },
      changeCaseType: function(caseId, caseTypeId){
        return backendService.postNested(
          caseBase(caseId).concat([
              {resources: 'general'},
              {commands: 'casetype'}
          ]),
          {entity: caseTypeId}).then(function(result){
              caseBase.broadcastMessage(result.status);
          },
          function(error){
              caseBase.broadcastMessage(error);
          });
        },

        changeCaseDescription: function(caseId, caseDescriptionId) {
          return backendService.postNested(
            caseBase(caseId).concat([
                {resources: 'general'},
                {commands: 'changedescription'}
            ]),
            {description: caseDescriptionId}).then(function(result){
                caseBase.broadcastMessage(result.status);
            },
            function(error){
                caseBase.broadcastMessage(error);
            });
        },

      getCaseLabel: function(caseId) {
        return backendService.get({
          specs:caseBase(caseId).concat([
              {resources: 'general'},
              {resources: 'labels'}
          ]),
          onSuccess:function (resource, result) {
              resource.response.index.links.forEach(function(item){
                  result.push(item);
                  caseBase.broadcastMessage(result.status);
              });
              result.commands = resource.response.commands;
           },
          onFailure:function(err){
              caseBase.broadcastMessage(err);
          }
        });
      },

      getPossibleCaseLabels: function(caseId) {
        return backendService.get({
            specs:caseBase(caseId).concat([
                {resources: 'general'},
                {resources: 'labels'},
                {queries: 'possiblelabels'}
            ]),
            onSuccess:function (resource, result) {
                var labelOptions = resource.response.links;

                labelOptions.forEach(function(item){result.push(item)});
                caseBase.broadcastMessage(result.status);
            },
            onFailure:function(err){
                caseBase.broadcastMessage(err);
            }
        });
      },

      addCaseLabel: function(caseId, labelId){
        return backendService.postNested(
            caseBase(caseId).concat([
                {resources: 'general'},
                {resources: 'labels'},
                {commands: 'addlabel'}
            ]),
          {entity: labelId }).then(function(result){
                  caseBase.broadcastMessage(result.status);
          },
            function(error){
              caseBase.broadcastMessage(error);
            });
      },
      deleteCaseLabel: function(caseId, labelId) {
        return backendService.postNested(
            caseBase(caseId).concat([
                {resources: 'general'},
                {resources: 'labels'},
                {'index.links': labelId},
                {commands: 'delete'}
            ]),
            {}).then(function(result){
                caseBase.broadcastMessage(result.status);
            },
            function(error){
                caseBase.broadcastMessage(error);
            });
      },
      getPossiblePriorities: function(caseId){
          return backendService.get({
              specs:caseBase(caseId).concat([
                  {resources: 'general'},
                  {queries: 'priorities'}
              ]),
              onSuccess:function (resource, result) {
                  var priorityOptions = resource.response.links;

                  priorityOptions.forEach(function(item){result.push(item)});
                  caseBase.broadcastMessage(result.status);
              },
              onFailure:function(err){
                  caseBase.broadcastMessage(err);
              }
          });

      },
      changePriorityLevel: function(caseId, priorityId){
            return backendService.postNested(
                caseBase(caseId).concat([
                    {resources: 'general'},
                    {commands: 'changepriority'}
                ]),
                {id: priorityId }).then(function(result){
                    caseBase.broadcastMessage(result.status);
                },
                function(error){
                    caseBase.broadcastMessage(error);
                });
        },
      changeDueOn: function(caseId, dueOn){
        return backendService.postNested(
          caseBase(caseId).concat([
            {resources: 'general'},
            {commands: 'changedueon'}
          ]),
          {date: dueOn}).then(function(result){
            caseBase.broadcastMessage(result.status);
          },
          function(error){
            caseBase.broadcastMessage(error);
          });
        },

      updateSimpleValue: debounce(function(caseId, resource, command, property, value, callback) {

        var toSend = {};
        toSend[property] = value;

        return backendService.postNested(
          caseBase(caseId).concat([
            {resources: resource},
            {commands: command}
          ]),
          toSend).then(_.debounce(callback)()).then(function(result){
            caseBase.broadcastMessage(result.status);
          }),
          function(error){
            caseBase.broadcastMessage(error);
          };
      }, 1000),

      getSelectedPossibleForms: function(caseId) {
        return backendService.get({
          specs:caseBase(caseId).concat([{resources: 'possibleforms'}]),
          onSuccess:function (resource, result) {
            resource.response.index.links.forEach(function(item){result.push(item)});
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);
          }
        });
      },

      getPossibleForm: function(caseId, formId) {
        return backendService.get({
          specs:caseBase(caseId).concat([
            {resources: 'possibleforms'},
            {'index.links': formId.replace("/", "")}
          ]),
          onSuccess:function (resource, result) {
            result.push(resource.response);
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);
          }
        });
      },

      createSelectedForm: function(caseId, formId) {
        return backendService.postNested(
            caseBase(caseId).concat([
                {resources: 'possibleforms'},
                {'index.links': formId.replace("/", "")},
                {commands: 'create'}
            ]),
            {}).then(function(result){
                caseBase.broadcastMessage(result.status);
                return result;
            },
            function(error){
                caseBase.broadcastMessage(error);
            });
      },

      addViewModelProperties: function(pages){
        _.forEach(pages, function(page){
          _.forEach(page.fields, function(field){
            formMapperService.addProperties(field)
          });
        });
      },

      getFormDraft: function(caseId, draftId) {
        var that = this;
        return backendService.get({
          specs:caseBase(caseId).concat([
            {resources: 'formdrafts/' + draftId, unsafe: true}
            ]),
          onSuccess:function (resource, result) {
            var index = resource.response.index;

            index.draftId = draftId;

            index.enhancedPages = angular.copy(index.pages);
            that.addViewModelProperties(index.enhancedPages);

            result.push(index);
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);
          }
        });
      },

      getFormDraftId: function(caseId, formId){
        return backendService.get({
          specs:caseBase(caseId).concat([
            {resources: 'possibleforms'},
            {'index.links': formId.replace("/", "")},
            {queries: 'formdraft'}
            ]),
          onSuccess:function(resource, result){
            result.push(resource.response);
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);
          }
        });
      },

      getFormDraftFromForm: function(caseId, formDraftId){
        var that = this;
        return backendService.get({
          specs:caseBase(caseId).concat([
            {resources: 'formdrafts'},
            {resources: formDraftId, unsafe: true}
            ]),
          onSuccess:function(resource, result){
            var index = resource.response.index;

            // BUT WHY?!
            if(!index.enhancedPages){
              index.enhancedPages = angular.copy(index.pages);
            }
            that.addViewModelProperties(index.enhancedPages);
            index.draftId = formDraftId;

            result.push(index);
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);
          }
        });
      },

      getFormDraftAttachment: function(caseId, formDraftId){
        return backendService.get({
          specs:caseBase(caseId).concat([
            {resources: 'formdrafts/' + formDraftId, unsafe: true},
            {resources: 'formattachments'}
            ]),
          onSuccess:function(resource, result){
            resource.response.index.links.forEach(function(attachment){
              result.push(attachment);
            });
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);
          }
        });
      },

      updateFormDraftAttachmentField: function(caseId, formDraftId, fileName, attachmentId, fieldId){
        return backendService.postNested(
          caseBase(caseId).concat([
            {resources: 'formdrafts/' + formDraftId, unsafe: true},
            {commands: 'updateattachmentfield'}
            ]),
          {'name': fileName, 'attachment': attachmentId, 'field': fieldId}).then(function(result){
            caseBase.broadcastMessage(result.status);
          },
          function(error){
            caseBase.broadcastMessage(error);
          });
      },

      deleteFormDraftAttachment: function(caseId, formDraftId, attachmentId){
        return backendService.postNested(
          caseBase(caseId).concat([
             {resources: 'formdrafts/' + formDraftId, unsafe: true},
             {resources: 'formattachments'},
             {'index.links': attachmentId},
             {commands: 'delete'}
            ]),
          {}).then(function(result){
            caseBase.broadcastMessage(result.status);
          },
          function(error){
            caseBase.broadcastMessage(error);
          });
      },

      updateField: debounce(function(caseId, formId, fieldId, value) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {resources: 'formdrafts/' + formId, unsafe: true},
            {commands: 'updatefield'}
          ]),
          {field: fieldId, value: value}).then(function(result){
            caseBase.broadcastMessage(result.status);
          },
          function(error){
            caseBase.broadcastMessage(error);
          });
          }, 1000),

      submitForm: function(caseId, formId) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {resources: 'formdrafts/' + formId, unsafe: true},
            {commands: 'submit'}
          ]),
          {}).then(function(result){
            caseBase.broadcastMessage(result.status);
          },
          function(error){
            caseBase.broadcastMessage(error);
          });
      },

      getSubmittedForms: function(caseId, formId) {
        return backendService.get({
          specs:caseBase(caseId).concat([{
            resources: 'submittedforms'
          }]),
          onSuccess:function (resource, result) {

            // NOTE: Need to index all forms and THEN filter them
            // since query takes `index` as parameter
            // where `index` is the index in the entire form list
            resource.response.index.forms.forEach(function(item, index){
              item.index = index;
            });

            var forms = _.filter(resource.response.index.forms, function(form){
              return form.id === formId;
            });
            forms.reverse().forEach(function(item, index){
              item.submissionDate = item.submissionDate.split("T")[0];
              result.push(item)
            });
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);
          }
        });
      },

      getSubmittedFormList: function(caseId) {
        return backendService.get({
          specs:caseBase(caseId).concat([{
            resources: 'submittedforms'
          }]),
          onSuccess:function (resource, result){
            resource.response.index.forms.forEach(function(item, index){
                item.index = index;
                result.push(item);
            });
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
              caseBase.broadcastMessage(err);
          }
        });
      },

      getSubmittedForm: function(caseId, index) {
        return backendService.get({
          specs:caseBase(caseId).concat([
            {resources: 'submittedforms'},
            {queries: 'submittedform?index=' + index}
          ]),
          onSuccess:function (resource, result) {
            result.push(resource.response);
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);
          }
        });
      },

      // Conversations
      createConversation: function(caseId, value) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {resources: 'conversations'},
            {commands: 'create'}
          ]),
          {topic: value}).then(function(result){
            caseBase.broadcastMessage(result.status);
            return result;
          },
          function(error){
            caseBase.broadcastMessage(error);
          });
      },
      getConversationMessages: function(caseId, conversationId) {
        return backendService.get({
          specs:caseBase(caseId).concat([
            {resources: 'conversations'},
            {'index.links': conversationId},
            {resources: 'messages'}
            ]),
          onSuccess:function (resource, result) {
            resource.response.index.links.forEach(function(item){
              item.text = getConversationMessage(caseId, conversationId, item.id);
              if(item.hasAttachments === true){
                item.attachments = getConversationMessageAttachments(caseId, conversationId, item.id);
              }
              result.push(item);
            });
            result.commands = resource.response.commands;
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);
          }
        });
      },
      getMessageDraft: function(caseId, conversationId) {
        return backendService.get({
          specs:caseBase(caseId).concat([
            {resources: 'conversations'},
            {'index.links': conversationId},
            {resources: 'messages'},
            {resources: 'messagedraft', unsafe: true}
            ]),
          onSuccess:function (resource, result) {
            result.push(resource.response.index.string);
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);
          }
        });
      },

      getMessageDraftAttachments: function(caseId, conversationId){
        return backendService.get({
          specs:caseBase(caseId).concat([
            {resources: 'conversations'},
            {'index.links': conversationId},
            {resources: 'messages'},
            {resources: 'messagedraft', unsafe: true},
            {resources: 'attachments'}
            ]),
          onSuccess:function(resource, result){
            resource.response.index.links.forEach(function(attachment){
              result.push(attachment);
            });
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);
          }
        });
      },

      deleteDraftAttachment: function(caseId, conversationId, attachmentId){
        return backendService.postNested(
          caseBase(caseId).concat([
            {resources: 'conversations'},
            {'index.links': conversationId},
            {resources: 'messages'},
            {resources: 'messagedraft', unsafe: true},
            {resources: 'attachments'},
            {'index.links': attachmentId},
            {commands: 'delete'}
            ]),
          {}).then(function(result){
            caseBase.broadcastMessage(result.status);
          },
          function(error){
            caseBase.broadcastMessage(error);
          });
      },

      updateMessageDraft: function(caseId, conversationId, value) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {resources: 'conversations'},
            {'index.links': conversationId},
            {resources: 'messages'},
            {resources: 'messagedraft', unsafe: true},
            {commands: 'changemessage'}
            ]),
          {message: value}).then(function(result){
            caseBase.broadcastMessage(result.status);
          },
          function(error){
            caseBase.broadcastMessage(error);
          });
      },
      createMessage: function(caseId, conversationId, value) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {resources: 'conversations'},
            {'index.links': conversationId},
            {resources: 'messages'},
            {commands: 'createmessagefromdraft'}
            ]),
          {}).then(function(result){
            caseBase.broadcastMessage(result.status);
          },
          function(error){
            caseBase.broadcastMessage(error);
          });
      },
     getConversationParticipants: function(caseId, conversationId) {
        return backendService.get({
          specs:caseBase(caseId).concat([
            {resources: 'conversations'},
            {'index.links': conversationId},
            {resources: 'participants'}
            ]),
          onSuccess:function (resource, result) {
            resource.response.index.links.forEach(function(item){result.push(item)});
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);
          }
        });
      },
      getPossibleConversationParticipants: function(caseId, conversationId) {
        return backendService.get({
          specs:caseBase(caseId).concat([
            {resources: 'conversations'},
            {'index.links': conversationId},
            {resources: 'participants'},
            {queries: 'possibleparticipants'}
            ]),
          onSuccess:function (resource, result) {
            resource.response.links.forEach(function(item){result.push(item)});
            caseBase.broadcastMessage(result.status);
          },
          onFailure:function(err){
            caseBase.broadcastMessage(err);
          }
        });
      },
      addParticipantToConversation: function(caseId, conversationId, participant) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {resources: 'conversations'},
            {'index.links': conversationId},
            {resources: 'participants'},
            {commands: 'addparticipant'}
            ]),
          {entity: participant}).then(function(result){
            caseBase.broadcastMessage(result.status);
          },
          function(error){
            caseBase.broadcastMessage(error);
          });
      },
      addExternalParticipantToConversation: function(caseId, conversationId, participantEmail) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {resources: 'conversations'},
            {'index.links': conversationId},
            {resources: 'participants'},
            {commands: 'addexternalparticipant'}
            ]),
          {emailAddress: participantEmail}).then(function(result){
            caseBase.broadcastMessage(result.status);
          },
          function(error){
            caseBase.broadcastMessage(error);
          });
      },
      deleteParticipantFromConversation: function(caseId, conversationId, participant) {
        return backendService.postNested(
          caseBase(caseId).concat([
            {resources: 'conversations'},
            {'index.links': conversationId},
            {resources: 'participants'},
            {'index.links': participant},
            {commands: 'delete'}
            ]),
          {}).then(function(result){
            caseBase.broadcastMessage(result.status);
          },
          function(error){
            caseBase.broadcastMessage(error);
          });
      }

    }
  });
