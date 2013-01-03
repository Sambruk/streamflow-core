window.mockBackend = window.mockBackend || (function () {
  'use strict';

  var mockBackend = {};


  mockBackend.root = {"commands":[], "index":null, "queries":[], "resources":[
    {
      "classes":"resource",
      "href":"workspace/",
      "id":"workspace",
      "rel":"workspace",
      "text":"Workspace"
    },
    {
      "classes":"resource",
      "href":"crystal/",
      "id":"crystal",
      "rel":"crystal",
      "text":"Crystal"
    },
    {
      "classes":"resource",
      "href":"overview/",
      "id":"overview",
      "rel":"overview",
      "text":"Overview"
    },
    {
      "classes":"resource",
      "href":"administration/",
      "id":"administration",
      "rel":"administration",
      "text":"Administration"
    },
    {
      "classes":"resource",
      "href":"account/",
      "id":"account",
      "rel":"account",
      "text":"Account"
    }
  ]};

  mockBackend.workspace = {"commands":[], "index":{
    "_type":"se.streamsource.dci.value.link.LinksValue",
    "links":[
      {
        "classes":"drafts",
        "href":"drafts/",
        "id":"drafts",
        "rel":"drafts",
        "text":"Drafts"
      },
      {
        "classes":"search",
        "href":"search/",
        "id":"search",
        "rel":"search",
        "text":"Search"
      },
      {
        "classes":"inbox",
        "href":"projects/b35873ba-4007-40ac-9936-975eab38395a-3f/inbox/",
        "id":"b35873ba-4007-40ac-9936-975eab38395a-3f",
        "rel":"inbox",
        "text":"Streamflow"
      },
      {
        "classes":"assignments",
        "href":"projects/b35873ba-4007-40ac-9936-975eab38395a-3f/assignments/",
        "id":"b35873ba-4007-40ac-9936-975eab38395a-3f",
        "rel":"assignments",
        "text":"Streamflow"
      }
    ]
  }, "queries":[
    {
      "classes":"query",
      "href":"casecounts",
      "id":"casecounts",
      "rel":"casecounts",
      "text":"Casecounts"
    },
    {
      "classes":"query",
      "href":"index",
      "id":"index",
      "rel":"index",
      "text":"Index"
    }
  ], "resources":[
    {
      "classes":"resource",
      "href":"search/",
      "id":"search",
      "rel":"search",
      "text":"Search"
    },
    {
      "classes":"resource",
      "href":"cases/",
      "id":"cases",
      "rel":"cases",
      "text":"Cases"
    },
    {
      "classes":"resource",
      "href":"projects/",
      "id":"projects",
      "rel":"projects",
      "text":"Projects"
    },
    {
      "classes":"resource",
      "href":"drafts/",
      "id":"drafts",
      "rel":"drafts",
      "text":"Drafts"
    },
    {
      "classes":"resource",
      "href":"perspectives/",
      "id":"perspectives",
      "rel":"perspectives",
      "text":"Perspectives"
    }
  ]};

  mockBackend.projects = {"commands":[], "index":{"_type":"se.streamsource.dci.value.link.LinksValue", "links":[
    {"classes":null, "href":"b35873ba-4007-40ac-9936-975eab38395a-3f/", "id":"b35873ba-4007-40ac-9936-975eab38395a-3f", "rel":"project", "text":"Streamflow"}
  ]}, "queries":[
    {"classes":"query", "href":"index", "id":"index", "rel":"index", "text":"Index"}
  ], "resources":[]}

  mockBackend.project1 = {"commands":[], "index":null, "queries":[], "resources":[
    {
      "classes":"resource",
      "href":"assignments/",
      "id":"assignments",
      "rel":"assignments",
      "text":"Assignments"
    },
    {
      "classes":"resource",
      "href":"inbox/",
      "id":"inbox",
      "rel":"inbox",
      "text":"Inbox"
    }
  ]};

  mockBackend.project1Inbox = {"commands":[], "index":null, "queries":[
    {
      "classes":"query",
      "href":"cases",
      "id":"cases",
      "rel":"cases",
      "text":"Cases"
    }
  ], "resources":[

  ]};

  mockBackend.project1InboxCases = {"links":[
    {
      "_type":"se.streamsource.streamflow.surface.api.CaseListItemDTO",
      "classes":null,
      "href":"/api/workspace/cases/f9d9a7f7-b8ef-4c56-99a8-3b9b5f2e7159-0/",
      "id":"f9d9a7f7-b8ef-4c56-99a8-3b9b5f2e7159-0",
      "rel":"mycases/opencase",
      "text":"Test Test Test 2",
      "caseId":"20121113-1",
      "caseType":"formulärstest",
      "creationDate":"2012-11-13T09:04:27.064Z",
      "project":"Streamflow"
    },
    {
      "_type":"se.streamsource.streamflow.surface.api.CaseListItemDTO",
      "classes":null,
      "href":"/api/workspace/cases/b35873ba-4007-40ac-9936-975eab38395a-30/",
      "id":"b35873ba-4007-40ac-9936-975eab38395a-30",
      "rel":"mycases/opencase",
      "text":"Kontakt Center Thingy",
      "caseId":"20121112-1",
      "caseType":"formulärstest",
      "creationDate":"2012-10-26T12:47:07.345Z",
      "project":"Streamflow"
    }
  ]};

  mockBackend.customer = {"commands":[], "index":null, "queries":[], "resources":[
    {
      "classes":"resource",
      "href":"profile/",
      "id":"profile",
      "rel":"profile",
      "text":"Profile"
    },
    {
      "classes":"resource",
      "href":"open/",
      "id":"open",
      "rel":"open",
      "text":"Open"
    },
    {
      "classes":"resource",
      "href":"closed/",
      "id":"closed",
      "rel":"closed",
      "text":"Closed"
    }
  ]};

  mockBackend.profile = {"commands":[
    {
      "classes":"command",
      "href":"changemessagedeliverytype",
      "id":"changemessagedeliverytype",
      "rel":"changemessagedeliverytype",
      "text":"Changemessagedeliverytype"
    },
    {
      "classes":"command",
      "href":"update",
      "id":"update",
      "rel":"update",
      "text":"Update"
    }
  ], "index":{
    "_type":"se.streamsource.streamflow.api.workspace.cases.contact.ContactDTO",
    "addresses":[
      {
        "address":"Tussilagovägen 1",
        "city":"Holm",
        "contactType":"HOME",
        "country":"Sweden",
        "region":"Halland",
        "zipCode":"302 79"
      }
    ],
    "company":"",
    "contactId":"",
    "contactPreference":null,
    "emailAddresses":[
      {
        "contactType":"HOME",
        "emailAddress":"henrik.reinhold@gmail.com"
      }
    ],
    "isCompany":false,
    "name":"Henrik",
    "note":"",
    "phoneNumbers":[
      {
        "contactType":"HOME",
        "phoneNumber":"46701476168"
      }
    ],
    "picture":""
  }, "queries":[
    {
      "classes":"query",
      "href":"messagedeliverytype",
      "id":"messagedeliverytype",
      "rel":"messagedeliverytype",
      "text":"Messagedeliverytype"
    },
    {
      "classes":"query",
      "href":"index",
      "id":"index",
      "rel":"index",
      "text":"Index"
    }
  ], "resources":[]}

  mockBackend.open = {"commands":[], "index":null, "queries":[
    {
      "classes":"query",
      "href":"cases",
      "id":"cases",
      "rel":"cases",
      "text":"Cases"
    }
  ], "resources":[]};

  mockBackend.cases = {"links":[
    {
      "_type":"se.streamsource.streamflow.surface.api.CaseListItemDTO",
      "classes":null,
      "href":"f9d9a7f7-b8ef-4c56-99a8-3b9b5f2e7159-0/",
      "id":"f9d9a7f7-b8ef-4c56-99a8-3b9b5f2e7159-0",
      "rel":"mycases/opencase",
      "text":"Testärende 2",
      "caseId":"20121113-1",
      "caseType":"formulärstest",
      "creationDate":"2012-11-13T09:04:27.064Z",
      "project":"Streamflow"
    },
    {
      "_type":"se.streamsource.streamflow.surface.api.CaseListItemDTO",
      "classes":null,
      "href":"b35873ba-4007-40ac-9936-975eab38395a-30/",
      "id":"b35873ba-4007-40ac-9936-975eab38395a-30",
      "rel":"mycases/opencase",
      "text":"En rubrik här skulle inte vara så dumt",
      "caseId":"20121112-1",
      "caseType":"formulärstest",
      "creationDate":"2012-10-26T12:47:07.345Z",
      "project":"Streamflow"
    }
  ]};





  // http://localhost:8090/streamflow/surface/customers/197606030001/open/f9d9a7f7-b8ef-4c56-99a8-3b9b5f2e7159-0/conversations/



  mockBackend.conversation1Messages = {"commands":[
    {
      "classes":"command",
      "href":"createmessage",
      "id":"createmessage",
      "rel":"createmessage",
      "text":"Createmessage"
    }
  ], "index":{
    "_type":"se.streamsource.dci.value.link.LinksValue",
    "links":[
      {
        "_type":"se.streamsource.streamflow.api.workspace.cases.conversation.MessageDTO",
        "classes":null,
        "href":"f9d9a7f7-b8ef-4c56-99a8-3b9b5f2e7159-17/",
        "id":"f9d9a7f7-b8ef-4c56-99a8-3b9b5f2e7159-17",
        "rel":null,
        "text":"Ett första meddelande...",
        "createdOn":"2012-11-13T09:06:29.333Z",
        "hasAttachments":false,
        "sender":"MyName"
      },
      {
        "_type":"se.streamsource.streamflow.api.workspace.cases.conversation.MessageDTO",
        "classes":null,
        "href":"975febee-9bac-405e-bdf2-105d84b3d22f-1/",
        "id":"975febee-9bac-405e-bdf2-105d84b3d22f-1",
        "rel":null,
        "text":"Lorizzle ipsizzle dolizzle sit amizzle, adipiscing . Nullam ghetto velizzle, bizzle volutpizzle, i'm in the shizzle mammasay mammasa mamma oo sa, get down get down vizzle, fo shizzle. Pellentesque egizzle tortor. Sed erizzle. Doggy shiz dolor brizzle turpis tempizzle hizzle. pellentesque nibh izzle turpizzle. Yippiyo izzle tortor. Pellentesque eleifend rhoncizzle nisi. In gizzle dang dawg dictumst. Hizzle yippiyo. Curabitur tellizzle urna, pretizzle eu, pimpin' ac, shiz vitae, nunc. Bizzle suscipit. Bling bling semper shit sed fo shizzle.",
        "createdOn":"2012-11-14T08:29:43.682Z",
        "hasAttachments":false,
        "sender":"MyName"
      },
      {
        "_type":"se.streamsource.streamflow.api.workspace.cases.conversation.MessageDTO",
        "classes":null,
        "href":"975febee-9bac-405e-bdf2-105d84b3d22f-5/",
        "id":"975febee-9bac-405e-bdf2-105d84b3d22f-5",
        "rel":null,
        "text":"Vad händer om jag skriver något här?",
        "createdOn":"2012-11-14T08:33:57.263Z",
        "hasAttachments":false,
        "sender":"MyName"
      }
    ]
  }, "queries":[
    {
      "classes":"query",
      "href":"index",
      "id":"index",
      "rel":"index",
      "text":"Index"
    }
  ], "resources":[]};


  mockBackend.case1 = {"commands":[
    {
      "classes":"command",
      "href":"close",
      "id":"close",
      "rel":"close",
      "text":"Close"
    },
    {
      "classes":"command",
      "href":"delete",
      "id":"delete",
      "rel":"delete",
      "text":"Delete"
    },
    {
      "classes":"command",
      "href":"assign",
      "id":"assign",
      "rel":"assign",
      "text":"Assign"
    },
    {
      "classes":"command",
      "href":"sendto",
      "id":"sendto",
      "rel":"sendto",
      "text":"Sendto"
    },
    {
      "classes":"command",
      "href":"restrict",
      "id":"restrict",
      "rel":"restrict",
      "text":"Restrict"
    }
  ], "index":{
    "_type":"se.streamsource.streamflow.api.workspace.cases.CaseDTO",
    "classes":null,
    "href":"/streamflow/workspace/cases/0f0008c2-4d6e-453a-b255-0d6ec86145f9-2/",
    "id":"0f0008c2-4d6e-453a-b255-0d6ec86145f9-2",
    "rel":"case",
    "text":"formulärstest",
    "assignedTo":null,
    "caseId":"20121219-1",
    "caseType":{
      "classes":null,
      "href":"",
      "id":"b35873ba-4007-40ac-9936-975eab38395a-44",
      "rel":null,
      "text":"formulärstest"
    },
    "createdBy":"WebForms",
    "creationDate":"2012-12-19T07:45:10.684Z",
    "hasAttachments":false,
    "hasContacts":false,
    "hasConversations":true,
    "hasSubmittedForms":true,
    "labels":{
      "links":[
        {
          "classes":null,
          "href":"delete?entity=b35873ba-4007-40ac-9936-975eab38395a-62",
          "id":"b35873ba-4007-40ac-9936-975eab38395a-62",
          "rel":"delete",
          "text":"information"
        }
      ]
    },
    "owner":"Streamflow",
    "resolution":null,
    "restricted":false,
    "status":"OPEN"
  }, "queries":[
    {
      "classes":"query",
      "href":"permissions",
      "id":"permissions",
      "rel":"permissions",
      "text":"Permissions"
    },
    {
      "classes":"query",
      "href":"index",
      "id":"index",
      "rel":"index",
      "text":"Index"
    },
    {
      "classes":"query",
      "href":"exportpdf",
      "id":"exportpdf",
      "rel":"exportpdf",
      "text":"Exportpdf"
    },
    {
      "classes":"query",
      "href":"possibleresolutions",
      "id":"possibleresolutions",
      "rel":"possibleresolutions",
      "text":"Possibleresolutions"
    },
    {
      "classes":"query",
      "href":"possiblesendto",
      "id":"possiblesendto",
      "rel":"possiblesendto",
      "text":"Possiblesendto"
    }
  ], "resources":[
    {
      "classes":"resource",
      "href":"general/",
      "id":"general",
      "rel":"general",
      "text":"General"
    },
    {
      "classes":"resource",
      "href":"submitformonclose/",
      "id":"submitformonclose",
      "rel":"submitformonclose",
      "text":"Submitformonclose"
    },
    {
      "classes":"resource",
      "href":"submitformondelete/",
      "id":"submitformondelete",
      "rel":"submitformondelete",
      "text":"Submitformondelete"
    },
    {
      "classes":"resource",
      "href":"note/",
      "id":"note",
      "rel":"note",
      "text":"Note"
    },
    {
      "classes":"resource",
      "href":"submittedforms/",
      "id":"submittedforms",
      "rel":"submittedforms",
      "text":"Submittedforms"
    },
    {
      "classes":"resource",
      "href":"formdrafts/",
      "id":"formdrafts",
      "rel":"formdrafts",
      "text":"Formdrafts"
    },
    {
      "classes":"resource",
      "href":"possibleforms/",
      "id":"possibleforms",
      "rel":"possibleforms",
      "text":"Possibleforms"
    },
    {
      "classes":"resource",
      "href":"conversations/",
      "id":"conversations",
      "rel":"conversations",
      "text":"Conversations"
    },
    {
      "classes":"resource",
      "href":"contacts/",
      "id":"contacts",
      "rel":"contacts",
      "text":"Contacts"
    },
    {
      "classes":"resource",
      "href":"caselog/",
      "id":"caselog",
      "rel":"caselog",
      "text":"Caselog"
    },
    {
      "classes":"resource",
      "href":"attachments/",
      "id":"attachments",
      "rel":"attachments",
      "text":"Attachments"
    }
  ]};

  return mockBackend;
})();