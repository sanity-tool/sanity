# sanity
[![Build Status](https://travis-ci.org/sanity-tool/sanity.svg?branch=master)](https://travis-ci.org/sanity-tool/sanity)
[![codecov](https://codecov.io/gh/sanity-tool/sanity/branch/master/graph/badge.svg)](https://codecov.io/gh/sanity-tool/sanity)

## Testing with native parser
```
mvn clean verify -Pparser-native
```
## Testing with bitreader service parser
```
docker run -d -p 8080:8080 sanitytool/bitreader-service
BITREADER_URL=http://localhost:8080 mvn clean verify -Pparser-remote
```
