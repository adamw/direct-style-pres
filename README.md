# Direct Style Scala Stack - an experiment

Talk from Scalar 2024 (see the [video](https://scalar-conf.com/)). To try out:

* install [sbt](https://www.scala-sbt.org/)
* start the docker containers defined in `docker-compose.yml` (`docker-compose up`)
* create a `src/main/resources/application.conf` file with `openai-api-key = "your-key"`
* run `sbt run`
* open the [HTTP API docs](http://localhost:8080/docs) and [jaeger UI](http://localhost:16686) 

To browse the code, [start here](https://github.com/adamw/direct-style-pres/blob/master/src/main/scala/scalar/fastAi.scala).
