ARG GRAALVM_VERSION=22.0.0-java11
FROM ghcr.io/optum/ci/nativeimage:graalvm-ce-${GRAALVM_VERSION}

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
    --gc=epsilon \
    --no-fallback \
    --libc=musl \
    --static