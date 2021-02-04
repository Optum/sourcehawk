ARG GRAALVM_VERSION=21.0.0-java8
FROM optum-docker-builders.bintray.io/nativeimage:graalvm-ce-${GRAALVM_VERSION}

# Build Arguments
ARG NAME
ARG JAR
ARG WORKDIR

# Copy shaded native jar into build
COPY ${JAR} ${WORKDIR}/native-image.jar

# Define the working directory
WORKDIR ${WORKDIR}

# Build the native image
RUN native-image -cp native-image.jar \
    -H:+ReportExceptionStackTraces \
    --report-unsupported-elements-at-runtime \
    --no-fallback \
    --static