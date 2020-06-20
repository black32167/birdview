# Birdview
## Abstract
Birdview is a command-line tool enabling you to represent your work context in form of 
list of recently done (or planned) activities.

The tool also can try to group related activities together using 
[TF-IDF](https://en.wikipedia.org/wiki/Tf–idf) - based approach to determine degree of relatedness between activities.

The following types of activity items are supported:
 - jira tickets
 - trello cards
 - Github pull requests
 
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
    }
]
```
To find out how to generate token please refer the following documentaion:
- [How To Generate Access Token For Github](https://help.github.com/en/github/authenticating-to-github/creating-a-personal-access-token-for-the-command-line) 
- [How To Generate Access Token For trello](https://developer.atlassian.com/cloud/trello/guides/rest-api/api-introduction/) 
- [How To Generate Access Token For jira](https://confluence.atlassian.com/cloud/api-tokens-938839638.html) 

### Running
Let’s assume that your bv-sources.json config is located in the `${HOME}/.birdview` folder.
Then you could just run in your terminal (assuming the [docker](https://www.docker.com/products/docker-desktop) is installed):
```shell script
$ docker run --rm -v "${HOME}/.birdview":/config black32167/birdview -s progress

2020-05-05 - Document my recent work : https://trello.com/c/xxxxxx/82-document-recent-work
2020-05-05 - Fixed production issue : https://github.com/MyOrg/repo/pull/14556
[Infrastructure Improvements]
    2020-05-05 - Infrastructure Improvements: Provision standby RDS instance: https://your-jira-domain.atlassian.net/browse/XXXX-123
    2020-05-05 - Infrastructure Improvements: Create alerts for EC2 low disk space : https://your-jira-domain.atlassian.net/browse/XXXX-321
```
To adjust period of interest, you could use ‘--daysBack’ parameter:
```
$ docker run --rm -v "${HOME}/.birdview":/config black32167/birdview -s progress --daysBack 14
```

To retrieve all available items were updated recently use ‘any’ command:
```
$ docker run --rm -v "${HOME}/.birdview":/config black32167/birdview -s any --daysBack 14
```
