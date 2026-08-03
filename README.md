# Crawler test

## How to execute
To launch the server just execute the following and you will end up with a SpringBoot server and a local Postgresql database running
```
docker compose up -d
```

## How to test it
Once the server is up, go to the url "http://localhost:8080" and you will see a simple static page with two different inputs:
- Url
- Max depth

The first field is required, is where we put the initial url and the second one is optional, in case we want a limit regarding the depth.

Once you hit the button "Crawl" it will create immediately a job and the page will start a poll process to retrieve the final results.
Finally, you will obtain a JSON shown in the page and also a button to download the JSON as well.

## How to stop it
Just execute
```
docker compose down
```
And the server will go down, in case you want to remove the volume as well then execute
```
docker compose down -v
```