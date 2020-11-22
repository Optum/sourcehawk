%define __jar_repack 0
Name: ${rpm.package}
Version: ${rpm.package.version}
Release: ${rpm.package.release}
Summary: ${global.project.description}
License: ${global.project.license}
Vendor: ${global.organization.name}
URL: ${global.project.url}
Group: Development/Tools
Packager: ${global.organization.name}
autoprov: yes
autoreq: yes
BuildRoot: /rpmbuild/

%description
${global.project.description}

%prep
echo "BUILDROOT = $RPM_BUILD_ROOT"
mkdir -p $RPM_BUILD_ROOT
cp -r /tmp/rpmbuild/* $RPM_BUILD_ROOT/
exit

%files
%defattr(-,${rpm.package},${rpm.package},-)
%attr(0755, ${rpm.package}, ${rpm.package}) /usr/local/bin/*
%attr(0644, ${rpm.package}, ${rpm.package}) /usr/local/share/man/man1/*
%attr(0755, ${rpm.package}, ${rpm.package}) /usr/share/bash-completion/completions/*

%post
ln -s /usr/local/bin/${rpm.package} /usr/local/bin/shawk

%postun
rm /usr/local/bin/shawk
