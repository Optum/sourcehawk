# Minimal Debian Base Image
ARG FROM=bitnami/minideb:buster
FROM ${FROM}

# Build Arguments
ARG DEBIAN_DIRECTORY
ARG DEB_FILE

# Copy the debian package files
COPY ${DEBIAN_DIRECTORY} /debian/

# Create the debian package
RUN chmod +x /debian/usr/bin/sourcehawk && dpkg-deb --build /debian ${DEB_FILE}