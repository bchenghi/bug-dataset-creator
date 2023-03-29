# bug-dataset-creator

## Set up
Ensure maven is using Java 17. (`mvn --version`)

Run 
- `mvn dependency:purge-local-repository -DactTransitively=false`
- `mvn -DskipTests=true install`