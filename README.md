# My Super Awesome Blog

<p align='center'>
 <img src='https://cloud.githubusercontent.com/assets/1408720/7717628/8907ae1e-fe72-11e4-8c40-d21102dc6cdc.jpg'>
</p>


A clunky static site generator which builds my blog from an [Obsidian](https://obsidian.md/) vault.  


**Core Features:** 
 
 * is blog 


### Usage

**Adding a new page:** 

```
lein exec -p script/manage.clj add-page /path/to/file.md
```

**Updating the root pages (home / RSS.xml):**

```
lein exec -p script/manage.clj update-roots
```
 
 

## Running this mamajama (development):

**profiles.clj**

Add a `profiles.clj` in the same directory as `project.clj`.

`profiles.clj` holds all the config data not kept in source control. 

```
{:profiles/prod {:env {:aws-access-key "{key}"
                       :aws-secret-key "{key}"
                       :username "{key}"
                       :password "{key}"
                       :vault-url "path/to/vault/root"
                       :s3-host "path/to/public/bucket"}}
 :profiles/dev {:env {:aws-access-key "asdf"
                      :aws-secret-key "asdf"
                      :username "admin"
                      :password "password"
                      :vault-url "path/to/vault/root"
                      :s3-host "path/to/public/bucket"}}}
```


Server 

```lein ring server-headless 3001```


OR via Docker + EB Test 

```eb local run --port 5001```

## Deploying

~~Deployed on a $5 Digital Ocean App Instance~~
Now deployed on Beanstalk because I wanted to get rid of Google Analytics but still do some basic log processing 'for free' via Cloud Watch.

### Prerequisites

* Install the [AWS CLI](https://aws.amazon.com/cli/)
 * Install the [Elastic Beanstalk CLI](https://github.com/aws/aws-elastic-beanstalk-cli-setup)

### One time setup

Initialize the Beanstalk application. 

```
>eb init

Select a default region
1) us-east-1 : US East (N. Virginia)
2) us-west-1 : US West (N. California)
3) us-west-2 : US West (Oregon)
4) eu-west-1 : EU (Ireland)
5) eu-central-1 : EU (Frankfurt)
6) ap-south-1 : Asia Pacific (Mumbai)
7) ap-southeast-1 : Asia Pacific (Singapore)
8) ap-southeast-2 : Asia Pacific (Sydney)
9) ap-northeast-1 : Asia Pacific (Tokyo)
10) ap-northeast-2 : Asia Pacific (Seoul)
11) sa-east-1 : South America (Sao Paulo)
12) cn-north-1 : China (Beijing)
13) cn-northwest-1 : China (Ningxia)
14) us-east-2 : US East (Ohio)
15) ca-central-1 : Canada (Central)
16) eu-west-2 : EU (London)
17) eu-west-3 : EU (Paris)
18) eu-north-1 : EU (Stockholm)
19) eu-south-1 : EU (Milano)
20) ap-east-1 : Asia Pacific (Hong Kong)
21) me-south-1 : Middle East (Bahrain)
22) af-south-1 : Africa (Cape Town)
(default is 3): 3


Enter Application Name
(default is "MyBlog"): Blog
Application Blog has been created.

It appears you are using Docker. Is this correct?
(Y/n): y
Select a platform branch.
1) Docker running on 64bit Amazon Linux 2
2) ECS running on 64bit Amazon Linux 2
(default is 1): 1

Do you wish to continue with CodeCommit? (Y/n): n
Do you want to set up SSH for your instances?
(Y/n): n
```

Create a new environment

```
>eb create

Enter Environment Name
(default is Blog-dev): Blog-Production
Enter DNS CNAME prefix
(default is Blog-Production):

Select a load balancer type
1) classic
2) application
3) network
(default is 2): 2

Would you like to enable Spot Fleet requests for this environment? (y/N): n
```

### Deploy 

```
eb deploy
```





## A Few CW Insight Queries 

What's most popular?  

```
fields @message 
| parse '* - * [*] "* * *" * *' as host, identity, dateTimeString, httpVerb, url, protocol, statusCode, bytes
| display host, url
| filter @message not like /(?i).*HealthChecker.*/
| filter url like /about|\/article\/|^\/$/
| stats count() as total_hits by url
| sort total_hits desc
```

How many requests per ${bin}? 

```
fields @message 
| parse '* - * [*] "* * *" * *' as host, identity, dateTimeString, httpVerb, url, protocol, statusCode, bytes
| filter @message not like /(?i).*HealthChecker.*/
| filter url like /about|\/article\/|^\/$/
| stats count() as total_hits by bin(5min) as ts 
| sort ts asc
```


