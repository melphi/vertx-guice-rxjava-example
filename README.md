# Vert.x, Guice, RxJava real world example

This code shows a real World project with:
- Java 11
- Vert.x (properly mixed with blocking code)
- Guice
- RxJava
- Apache Avro
- ElasticSearch
- Google Cloud Storage

As the code was migrated to Go Lang I decided to leave it for anyone who is interested in these technologies.

## How it works

- [Application.java](https://github.com/melphi/vertx-guice-rxjava-example/blob/master/src/main/java/net/dainco/Application.java) starts the Guice dependency injector to initialize all components. 
- [StockNewsPipeline.java](https://github.com/melphi/vertx-guice-rxjava-example/blob/master/src/main/java/net/dainco/module/news/pipeline/StockNewsPipeline.java) is a pipeline which reads news from a google cloud storage (via [RawResourceReader.java](https://github.com/melphi/vertx-guice-rxjava-example/blob/de6a3a1d6d13d4f61d48a786e80150ccb8e3e7e7/src/main/java/net/dainco/module/news/pipeline/reader/RawResourceReader.java#L52)), extracts the news content and updates an ElasticSearch index (via [StockNewsWriter.java](https://github.com/melphi/vertx-guice-rxjava-example/blob/master/src/main/java/net/dainco/module/news/pipeline/writer/StockNewsWriter.java)). 
