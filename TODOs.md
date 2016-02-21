1. Automate release
  - change version
  - git tag
  - generate files
    - `mvn package`
    - `mvn source:jar`
    - `mvn javadoc:jar`
    - pom file
  - sign files:
    - `gpg -ab filename`
    - `gpg --verify filename`
  - upload files:
    - `mvn deploy:deploy-file -DgroupId=uk.co.jpawlak -DartifactId=map-to-object-converter -Dversion=1.0 -DgeneratePom=false -Dpackaging=jar -DrepositoryId=sonatype -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2 -DpomFile=pom.xml -Dfile=target/map-to-object-converter-1.0.jar`
    - repositoryId is the id of the server with user credentials saved in `.m2/settings.xml`
    - to do this manually, go to `oss.sonatype.org / Staging Upload` and select the files
  - update quick start in readme
  - change version
  - push changes to remote (together with tags):
    - `git push --tags`

2. bounded wildcards for Optional fields

3. Multi value to single field register-able converters
