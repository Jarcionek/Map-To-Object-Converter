#!/usr/bin/env bash

echo 'ENTER RELEASE VERSION:'
read version
echo 'ENTER GPG PASSPHRASE:'
read passphrase

sed "s;^    <version>.*</version>;    <version>${version}</version>;" pom.xml > pom.xml2
rm pom.xml
mv pom.xml2 pom.xml

git add pom.xml
git commit -m "prepare release map-to-object-converter-${version}"
git tag "${version}"

rm -rf target/

mvn package
mvn source:jar
mvn javadoc:jar

mkdir target/release

cp pom.xml target/release/map-to-object-converter-${version}.pom
cp target/*.jar target/release

cd target/release
echo "${passphrase}" | gpg --passphrase-fd 0 -ab map-to-object-converter-${version}.pom
echo "${passphrase}" | gpg --passphrase-fd 0 -ab map-to-object-converter-${version}.jar
echo "${passphrase}" | gpg --passphrase-fd 0 -ab map-to-object-converter-${version}-javadoc.jar
echo "${passphrase}" | gpg --passphrase-fd 0 -ab map-to-object-converter-${version}-sources.jar
