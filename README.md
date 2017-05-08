# Library Integration project
[![Build Status](https://travis-ci.org/c-tash/library-integration.svg?branch=master)](https://travis-ci.org/c-tash/library-integration)

This project's goal is to store and merge all of the library data in one place.
The only requirement is the data format - [MODS](http://www.loc.gov/standards/mods/)

---
## It features:

* Near-duplicate detection algorithm based on SimHash.

---
## How to run:
### Version 1.1
Version 1.1 is a console application, this was changed due to overhead of a web service.
Requirements:
1. Redis for Windows - in-memory database. (For Linux you can get redis through `apt-get install redis-server` or `yum install redis` depending on your package manager.)
2. Java 8+.

To run:
1. build a jar by `./gradlew jar`, the jar will be in `build/libs`
2. run jar by `java -jar library-integration-[version].jar [command]` 

#### Commands:

1. `parseInit [path]`- parses directory for mods XML files and extract data needed for the program. A file `docs.blob` is going to be created and filled with this data.
2. `find`- parses the docs blob-file and creates `duplicates.blob` file with first duplicate filtered results.
3. `collect` - collects the `duplicates.blob` result to create final results in file. For this you need to setup redis. The application is configured for default redis settings.
4. `getstat` - calculates different statistics for found duplicates.

#### IDE import
There are tasks in gradle for creating Intellij IDEA and Eclipse projects. `gradlew idea` and `gradle eclipse`.
