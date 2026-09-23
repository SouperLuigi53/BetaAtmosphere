# Direct Beta bytecode comparisons

These optional checks run the original fluid and dimension classes from a locally supplied, official Beta 1.7.3 client JAR. The Beta JAR is not included. The `aw` and `uu` stubs supply only texture storage and block texture indices so the fluid classes can run without OpenGL; the original update methods run unchanged. No reference classes enter the release JAR.

From the project root, with JDK 25 on PATH, first compile the port:

```powershell
.\gradlew.bat classes --console=plain
$betaJar = 'C:\path\to\b1.7.3.jar'
New-Item -ItemType Directory -Path work/reference-check -Force | Out-Null
$referenceSources = @(Get-ChildItem tools/reference -Filter '*.java' | ForEach-Object FullName)
javac -classpath build/classes/java/main -d work/reference-check @referenceSources
$referenceClasspath = "work/reference-check;build/classes/java/main;$betaJar"
java --add-opens java.base/java.lang=ALL-UNNAMED -classpath $referenceClasspath FluidReferenceCheck
java -classpath $referenceClasspath SkyReferenceCheck
```

Expected: 3,072,000 complete fluid RGBA pixel matches (four sprites, three seeds, 1,000 ticks each) and 192,000 exact-float celestial-angle/sunset comparisons (two days at four sub-tick positions). The Java module opening is used only to seed Beta's `Math.random()` for repeatable comparisons; it is not needed by the mod.

Reference client SHA-1: `43db9b498cb67058d2e12d394e6507722e71bb45`.
