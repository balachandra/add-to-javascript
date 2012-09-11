add-to-javascript
=================

Converts html templates into javascript variables during build. this would be helpful while using any javascript based template frameworks.

Usage:

```xml
<plugin>
  <groupId>com.satmetrix</groupId>
	<artifactId>add-to-javascript</artifactId>
	<version>0.2</version>
	<executions>
		<execution>
			<phase>install</phase>
			<goals>
				<goal>install</goal>
			</goals>
		</execution>
	</executions>
	<configuration>
	  <fileType>html</fileType>
		<sourceFolder>
			<param>${basedir}/target/template/folder</param>
		</sourceFolder>
		<outputDir>${basedir}/target/js/folder</outputDir>
	</configuration>
</plugin>
```

Dependencies:

  This plugin uses yuicompressor for minifying compiled output