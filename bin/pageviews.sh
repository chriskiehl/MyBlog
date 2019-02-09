#!/usr/bin/env bash
cd /home/ckiehl/MyBlog
lein with-profile prod exec -p script/popularity.clj actually-run-this