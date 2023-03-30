# bug-dataset-creator

## Set up
Ensure maven is using Java 17. (`mvn --version`)

Run 
- `mvn dependency:purge-local-repository -DactTransitively=false`
- `mvn -DskipTests=true install`

## To update `instrumentator.jar`
- Place `instrumentator.jar` to the root of the project
- Run:
  - `mvn install:install-file -Dfile=instrumentator.jar -DgroupId=microbat -DartifactId=instrumentator -Dversion=0.0.1 -Dpackaging=jar -DgeneratePom=true -DlocalRepositoryPath=lib -DcreateChecksum=true -U`
  - `mvn dependency:purge-local-repository -DactTransitively=false`
  - `mvn -DskipTests=true install`
- Update the `instrumentator.jar` in the `%USERPROFILE%\lib\resources\java-mutation-framework\lib` directory

## To update `java-mutation-framework` jar file
The steps are similar to updating `instrumentator.jar`
- Place `java-mutation-framework-0.0.1-SNAPSHOT-jar-with-dependencies.jar` to the root of the project
- Run:
  - `mvn install:install-file -Dfile=java-mutation-framework-0.0.1-SNAPSHOT-jar-with-dependencies.jar -DgroupId=java-mutation-framework -DartifactId=java-mutation-framework -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar -DlocalRepositoryPath=lib -DcreateChecksum=true -U`
  - `mvn dependency:purge-local-repository -DactTransitively=false`
  - `mvn -DskipTests=true install`
