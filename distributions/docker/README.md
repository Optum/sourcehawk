# Sourcehawk Docker Image

### Scanning from local directory

```shell script
docker run --rm -v $PWD:/work optumopensource/sourcehawk
```

Or with a custom working directory:

```shell script
docker run --rm -v $PWD:/tmp -w /tmp optumopensource/sourcehawk
```

The volume mounting is necessary in order to give the container access to the files to scan.