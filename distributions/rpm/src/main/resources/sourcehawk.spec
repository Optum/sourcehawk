%define __jar_repack 0
Name: ${rpm.package}
Version: ${rpm.package.version}
Release: ${rpm.package.release}%{?dist}
Summary: ${global.project.description}

Vendor: ${global.organization.name}
Group: Development/Tools
Packager: ${global.organization.name}

License: ${global.project.license.abbrev.rpm}
URL: ${global.project.url}
Source0: ${project.scm.url}/archive/v${rpm.package.version}.tar.gz

BuildRoot: /rpmbuild/

%description
${global.project.description}

%prep
echo "BUILDROOT = $RPM_BUILD_ROOT"
mkdir -p $RPM_BUILD_ROOT
cp /tmp/rpmbuild-LICENSE ../BUILD/LICENSE
cp -r /tmp/rpmbuild/* $RPM_BUILD_ROOT/
exit

%files
%license LICENSE
%defattr(-,${rpm.package},${rpm.package},-)
%attr(0755, ${rpm.package}, ${rpm.package}) /usr/bin/*
%attr(0644, ${rpm.package}, ${rpm.package}) /usr/share/man/man1/*
%attr(0755, ${rpm.package}, ${rpm.package}) /usr/share/bash-completion/completions/*

%post
ln -s /usr/bin/${rpm.package} /usr/bin/shawk

%postun
rm /usr/bin/shawk
