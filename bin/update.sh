git pull
rm resources/public/js/main.js
rm -rf resources/public/js/out/
lein cljsbuild once prod
AWS_ACCESS_KEY=XXX AWS_SECRET_KEY=XXX AWS_REGION=us-west-1 AWS_ENDPOINT=us-west-1 lein uberjar



