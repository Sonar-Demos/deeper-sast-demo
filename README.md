# deeper-sast-demo

## Storyboard

The goal of this demo is to show the deeper SAST capabilities of the Java engine. We want to highlight that the usage of external libraries can introduce severe security vulnerabilities, which can be detected by Deep SAST.

The demo is a fictive Spring application implementing different functionalities which are vulnerable to security issues detected by our engine. All of these issues contain at least one step where the data flow:

1. originates from a user-controllable source within a library,
2. passes through a library, or
3. ends in a dangerous sink within a library.

Thus these issues are only detected because of the deeper SAST feature of the engine.

There are four issues: two of these are already committed to the main branch of the application. Additionally, there are two pending pull requests (PR), which each introduce another vulnerability. For these issues, the chosen examples aim to demonstrate that the proposed source code in the PR does not look dangerous or security-sensitive and would likely be merged.

### Issue 1 - Session Cookie Handling (main branch)

* Vulnerability Type: Deserialization ([S5135](https://rules.sonarsource.com/java/RSPEC-5135/))
* Deeper SAST Dataflow:
  * Passthrough: `org.apache.commons.codec.binary.Base64.decodeBase64`

This vulnerability resides within the session cookie handling of the application. A vulnerability is introduced by deserializing user-controllable data from a header (`Session-Auth`), which can be exploited to execute arbitrary code. The data provided in the header is passed through the `decodeBase64` library function before being deserialized.


### Issue 2 - User Images (main branch)

* Vulnerability Type: Path Injection ([S2083](https://rules.sonarsource.com/java/RSPEC-2083/))
* Deeper SAST Dataflow:
  * Source:  `org.springframework.web.context.request.getRemoteUser`
  * Passthrough: `org.apache.tomcat.util.buf.UDecoder.URLDecode`
  * Sink: `cn.hutool.cache.file.LRUFileCache.getFileBytes`

This vulnerability resides within the code responsible for retrieving user images. The library function `getRemoteUser` is used to retrieve the user-controllable username, which is passed through the `URLDecode` library function. The result is concatenated to a file path, which is passed to the `getFileBytes` library function introducing a path injection vulnerability.


### Issue 3 - User Migration (PR 1 - Introduce user migration feature)

* Vulnerability Type: SQL Injection ([S3649](https://rules.sonarsource.com/java/RSPEC-3649/))
* Deeper SAST Dataflow:
  * Sink: `com.mysql.cj.jdbc.ConnectionImpl.setSavepoint`

This PR adds a feature to migrate users from the existing H2 database to MySQL. Although the proposed change does not seem to contain any vulnerabilities, the `setSavepoint` library function is vulnerable to SQL injection if the passed argument is user-controllable. Thus this PR introduces a critical vulnerability due to the usage of the unsafe library function.

### Issue 4 - XML User Import (PR 2 - Allow the import of users)

* Vulnerability Type: Deserialization ([S5135](https://rules.sonarsource.com/java/RSPEC-5135/))
* Deeper SAST Dataflow:
  * Sink: `ca.odell.glazedlists.impl.io.BeanXMLByteCoder.decode`

This PR adds a new feature to import users from an XML file. Although the code itself does not seem to contain any vulnerabilities, the `decode` library function is vulnerable to deserialization if the passed argument is user-controllable. Thus this PR introduces a critical vulnerability due to the usage of the unsafe library function.


## Setup instructions

This repository is supposed to be added as a SonarCloud project for analysis via GitHub actions.

* Fork this project *with all branches* (untick the default checkbox, "Copy the `main` branch only").
* Go to the `Actions` tab of your forked repository and enable workflows by selecting `I understand my workflows, go ahead and enable them`.
* Go to [sonarcloud.io](https://sonarcloud.io/sessions/new) and sign up with your GitHub account.
* Create a new organization under your name if there is none.
* Give SonarCloud permission to see the forked repository.
* Add your repository as a new Project.
* Go to `Administration` -> `Analysis Method` and uncheck `Automatic Analysis`.
* Select `Set up analysis via other methods` -> `With GitHub Actions`.
  * Add the displayed GitHub Secret to your repository.
  * Update the `sonar.organization` value in the `pom.xml` file.
  * Copy & paste the displayed content to `.github/workflows/build.yml` and set the `java-version` to `17`.
* On your GitHub repository: Go to the `Pull requests` tab and create a new PR from the `introduce-user-migration-feature` and `allow-imports` branches to the `main` branch of your fork. Be careful that, by default, the PR targets the upstream repository.

The first two issues will be displayed on the `main` branch and the other two issues on distinct Pull Requests.

## Real-World Examples

This table contains examples of deeper SAST findings in real-world projects:

| Link | Lang | Project | Issue type | Comment |
| --- | --- | --- | --- | --- |
| [SonarCloud](https://sonarcloud.io/project/issues?id=org.monarchinitiative.exomiser:exomiser&open=AYaYnkzfchh4gFFcKnDe) | Java | Small open-source | Log Injection | Dataflow with passthrough |
| [SonarCloud](https://sonarcloud.io/project/issues?id=dmatej_glassfish&open=AYX94MJvhKb2vw3fmeKu) | Java | Eclipse Glassfish | Path Injection | Innocent looking sink |
| [SonarCloud](https://sonarcloud.io/project/issues?id=siguser_benchmarkjava-gh&open=AYjEl06qrBlwzkIdugy7) | Java | OWASP Benchmark | SQL Injection | Rare sink, popular benchmark |
| [SonarCloud](https://sonarcloud.io/project/issues?id=lightswitch05_zwave-js-ui&open=AYaUAEYH58jG26HSBsPN) | TS | Medium open-source | File Delete | Simple, critical issue in TS |
| [SonarCloud](https://sonarcloud.io/project/issues?id=Visclo96_spring-boot&open=AYbbZ3D1FcQgtBJC0HXd) | Java | Spring Boot | Deserialize | Simple issue, popular project |
