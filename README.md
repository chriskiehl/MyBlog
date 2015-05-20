#About

Here be the source code for my Blog. 

##Web Stack: 

* Nginx
* Gunicorn 
* Postgres
* Django
* Rest Framework
* Python27

Some notes: 

It's heavily gears towards Nginx's proxy caching, thus no application level cache machinery is involved. All the super long decorator cache timeouts are there only to pump the relevant headers over to Nginx so it can do its thing. 

This blog is viewed by literally dozens of people per month -- DOZENS! And each of them deserve a snappy site!
