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


## Deploying

Deployed on a $5 Digital Ocean App Instance. 





