# Birdview
## Abstract
Birdview is a  tool enabling you to assembly your work context in form of list 
of recently done or planned activities and helps to generate detailed stand-up reports.

The tool also tries to group related activities using various hints.

The following types of activity items are supported:
 - Jira tickets
 - Trello cards
 - Github pull requests
 - Google doc documents
 
## Usage
### Configuration
To use the tool you need to configure access to sources of your features in form of JSON file
('bv-sources.json')

```json
[
    {
        "sourceType":"jira",
        "baseUrl" : "https://your-jira-domain.atlassian.net",
        "user" : "your-email@your-company.com",
        "token" : "your-personal-jira-token"
    },
    {
        "sourceType":"trello",
        "baseUrl" : "https://api.trello.com",
        "key" : "your-personal-trello-key",
        "token" : "your-personal-trello-token"
    },
    {
        "sourceType":"github",
        "baseUrl" : "https://api.github.com",
        "user" : "your-email@your-company.com",
        "token" : "your-personal-trello-token"
    },
    {
        "sourceType": "gdrive",
        "clientId": "your-gdrive-client-id",
        "clientSecret": "your-gdrive-secret"
    }
]
```
To find out how to generate token please refer the following documentation:
- [How To Generate Access Token For Github](https://help.github.com/en/github/authenticating-to-github/creating-a-personal-access-token-for-the-command-line) 
- [How To Generate Access Token For trello](https://developer.atlassian.com/cloud/trello/guides/rest-api/api-introduction/) 
- [How To Generate Access Token For jira](https://confluence.atlassian.com/cloud/api-tokens-938839638.html)
- To get access to the Google Drive you need to register Birdview in the [Google API Console](https://console.developers.google.com/) 

### Running
#### Web mode
```
$ docker run --rm -i -p 8888:8888 -v "${HOME}/.birdview":/config black32167/birdview
```
Then open 'http://localhost:8888/' in your browser. Wait. Reload.

### Update
To pull the fresh version use 'docker pull':
```shell script
$ docker pull black32167/birdview
```