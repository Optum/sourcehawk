# glibc is required for proper DNS resolution within app
FROM busybox:1.32.0-glibc

# Dynamically pass in name
ARG NAME="sourcehawk"

# Setup user and group
ENV GROUP=${NAME} USER=${NAME}
RUN addgroup ${GROUP} && adduser -h /home/${USER} -G ${NAME} -D ${USER}

# Copy the native image executable into the image
ARG NATIVE_IMAGE_PATH
COPY --chown=${GROUP}:${USER} ${NATIVE_IMAGE_PATH} /usr/bin/sourcehawk

# Give the native image executable permissions
RUN chmod +x /usr/bin/${NAME}

# Set the user and working directory
USER ${USER}
WORKDIR /home/${USER}

# Set the native image as the entrypoint
ENTRYPOINT ["/usr/bin/sourcehawk"]