# My Super Awesome Blog

<p align='center'>
 <img src='https://cloud.githubusercontent.com/assets/1408720/7717628/8907ae1e-fe72-11e4-8c40-d21102dc6cdc.jpg'>
</p>

Build on top of Clojure/ring with a light sprinkling of Clojurescript + Re-Frame for the admin interactions 
 
Rendered server side and served up hot and fresh from an nginx proxy cache.  


**Core Features:** 
 
 * is blog 

 
  

# Running this mamajama (development):

**profiles.clj**

Add a `profiles.clj` in the same directory as `project.clj`.

`profiles.clj` holds all the config data not kept in source control. 

```
{:profiles/prod {:env {:aws-access-key "{key}"
                       :aws-secret-key "{key}"
                       :username "{key}"
                       :password "{key}"}}
 :profiles/dev {:env {:aws-access-key "asdf"
                      :aws-secret-key "asdf"
                      :username "admin"
                      :password "password"}}}
```


Server 

```lein ring server-headless 3001```


Client + REPL 

```lein figwheel```

Port forwarding to use browser on host:
 
```
ssh -L 3000:localhost:3000 devbox@192.168.0.107
```



# Deploying from scratch 
 
Currently targets a bare Digital Ocean droplet running `Ubuntu 18.04 x64`.  

 


Initial bookkeeping

```
useradd ckiehl -m 
passwd ckiehl 

# add the sudo permissions 
usermod -aG sudo ckiehl

# setup ssh access 
su - ckiehl 
mkdir ~/.ssh 
chmod 700 ~/.ssh

# Add the public key
~/.ssh/authorized_keys

chmod 600 ~/.ssh/authorized_keys
```

## Baseline Server Setup


**Dependencies**
 
```
apt-get update
apt-get install -y openjdk-11-jdk
apt-get install -y imagemagick
apt-get install -y libpq-dev nginx
apt-get install -y memcached
apt-get install -y leiningen
```
 
**Directory Setup**

```
cd ~
git clone XXX
```

**Create the required DynamoDB tables**

```
lein with-profile prod exec -p script/bootstrap.clj actually-run-this
```



**Get the uberjar path**

```
# Compiles the frontend to produce a main.js
lein cljsbuild once prod

# I was super lazy and built the creds at the toplevel of the file 
# so the env vars need to have strings present to avoid null warning 
# during compile  
AWS_ACCESS_KEY=XXX AWS_SECRET_KEY=XXX AWS_REGION=us-west-1 AWS_ENDPOINT=us-west-1 lein uberjar
```

It'll produce something like 

```
Created /path/to/thing/target/thing-0.1.0-SNAPSHOT-standalone.jar
```

Take note of this path. 
 

### Application Uberjar systemd
 
conf lives at

```
/etc/systemd/system/webapp.service
```


```
[Unit]
Description=Webapp daemon
After=network.target

[Service]

Environment=USERNAME=ckiehl
Environment=PASSWORD={key}
Environment=AWS_ACCESS_KEY={key}
Environment=AWS_SECRET_KEY={key}
Environment=AWS_ENDPOINT=us-west-1
Environment=AWS_REGION=us-east-1  # s3 region

User=ckiehl
Group=ckiehl
WorkingDirectory=/home/ckiehl/tmp-my-blog/
ExecReload=/bin/kill -s HUP $MAINPID
ExecStart=/usr/bin/java -jar /home/ckiehl/tmp-my-blog/target/myblog-0.1.0-SNAPSHOT-standalone.jar
ExecStop=/bin/kill -s TERM $MAINPID
PrivateTmp=true
Restart=on-failure

[Install]
WantedBy=multi-user.target

```

**Enable the Systemd**

```
sudo systemctl enable webapp.service
sudo systemctl daemon-reload 
sudo systemctl start webapp.service
sudo systemctl status webapp.service
```

### Nginx Config


conf lives at:

```/etc/nginx/sites-available/blogomatono```

conf 

```
proxy_cache_path /data/nginx/cache levels=1:2 keys_zone=STATIC:10m  inactive=24h  max_size=1g;

server {
    listen 80;
    server_name chriskiehl.com;

    access_log /etc/nginx/logs/nginx-access.log;

    location = /favicon.ico { access_log off; log_not_found off; }
    location /css/ {
        add_header Cache-Control "max-age=31557600";
        alias /home/ckiehl/tmp-my-blog/resources/public/css/;
    }

    location /js/external/ {
        add_header Cache-Control "max-age=31557600";
        alias /home/ckiehl/tmp-my-blog/resources/public/js/external/;
    }

    location /js/ {
        add_header Cache-Control "max-age=3100000";
        alias /home/ckiehl/tmp-my-blog/resources/public/js/;
    }


    location /api/ {
        gzip off;
        include proxy_params;
        proxy_pass http://localhost:8080/api/;
     }

    location /admin/ {
        gzip off;
        include proxy_params;
        proxy_pass http://localhost:8080/admin/;
     }

    location / {
        add_header Cache-Control "max-age=86400";
        include proxy_params;
        add_header X-Proxy-Cache $upstream_cache_status;
        proxy_pass             http://localhost:8080/;
        proxy_set_header       Host $host;
        proxy_buffering        on;
        proxy_cache            STATIC;
        proxy_cache_valid      200  20s;
        proxy_cache_use_stale  error timeout invalid_header updating http_500 http_502 http_503 http_504;
    }
}
```


Create symlink

```
sudo ln -s /etc/nginx/sites-available/blogomatono /etc/nginx/sites-enabled/blogomatono
```


Relevant dirs for logging and forward caching 

```
sudo mkdir /etc/nginx/logs/
sudo mkdir -p /data/nginx/cache
```


Sanity check the config with 

```
sudo nginx -t
```

Nginx controls:

```
sudo systemctl start nginx.service
sudo systemctl stop nginx.service
sudo systemctl restart nginx.service
```





## building the jar 

```
lein uberjar
```

## Compiling the frontend 

```
lein cljsbuild once prod
```


