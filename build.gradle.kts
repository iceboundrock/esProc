plugins {
    java
    `maven-publish`
    signing
}

group = "com.scudata.esproc"
version = "20250313"
description = "SPL(Structured Process Language) A programming language specially for structured data computing."

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<JavaCompile> {
    options.encoding = "GBK"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.xmlgraphics:batik-all:1.16")
    implementation("commons-codec:commons-codec:1.15")
    implementation("org.apache.commons:commons-collections4:4.1")
    implementation("commons-logging:commons-logging:1.2")
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("com.google.zxing:core:3.3.0")
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    implementation("org.apache.httpcomponents:httpcore:4.4.16")
    implementation("org.apache.httpcomponents:httpmime:4.5.14")
    implementation("com.ibm.icu:icu4j:60.3")
    implementation("javax.servlet:javax.servlet-api:3.1.0")
    implementation("org.json:json:20240303")
    implementation("org.jsoup:jsoup:1.15.4")
    implementation("com.github.albfernandez:juniversalchardet:2.3.2")
    implementation("com.github.insubstantial:laf-plugin:7.3")
    implementation("com.github.insubstantial:laf-widget:7.3")
    implementation("net.jpountz.lz4:lz4:1.3.0")
    implementation("com.fifesoft:rsyntaxtextarea:3.0.3")
    implementation("com.zaxxer:SparseBitSet:1.2")
    implementation("com.github.insubstantial:substance:7.3")
    implementation("com.github.insubstantial:trident:7.3")
    implementation("org.hsqldb:hsqldb:2.7.3:jdk8")
    implementation("org.springframework:spring-beans:5.3.39")
    implementation("org.springframework:spring-context:5.3.39")
    implementation("org.springframework:spring-core:5.3.39")
    implementation("org.springframework:spring-jdbc:5.3.39")
    implementation("org.springframework:spring-tx:5.3.39")
    implementation("org.apache.poi:poi:5.2.5")
    implementation("org.apache.poi:poi-ooxml:5.2.5")
    implementation("org.apache.poi:poi-ooxml-lite:5.2.5")
    implementation("commons-io:commons-io:2.17.0")
    implementation("org.apache.xmlbeans:xmlbeans:5.2.0")
    implementation("org.apache.commons:commons-compress:1.27.0")
    implementation("org.apache.logging.log4j:log4j-api:2.21.1")
    implementation("com.mixpanel:mixpanel-java:1.5.3")
}

sourceSets {
    main {
        resources {
            srcDirs("src/main/java")
        }
    }
}

// Include project files in META-INF
val copyMetaInfFiles by tasks.registering(Copy::class) {
    from(projectDir)
    into("$buildDir/resources/main/META-INF")
    include("LICENSE")
    include("NOTICE")
    include("README")
    include("README.md")
}

tasks.processResources {
    dependsOn(copyMetaInfFiles)
}

tasks.javadoc {
    options {
        this as StandardJavadocDocletOptions
        addStringOption("Xdoclint:none", "-quiet")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("${project.group}:${project.name}")
                description.set(project.description)
                url.set("http://www.scudata.com/")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        name.set("SCUDATA")
                        email.set("yangchunli@scudata.com")
                        organization.set("SCUDATA Ltd.")
                        organizationUrl.set("http://www.scudata.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/SPLWare/esProc.git")
                    developerConnection.set("scm:git:ssh://github.com/SPLWare/esProc.git")
                    url.set("http://github.com/SPLWare/esProc")
                }
            }
        }
    }

    repositories {
        maven {
            name = "OSSRH"
            val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)

            credentials {
                username = project.findProperty("ossrhUsername") as String? ?: System.getenv("OSSRH_USERNAME")
                password = project.findProperty("ossrhPassword") as String? ?: System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
    setRequired({
        !version.toString().endsWith("SNAPSHOT") && gradle.taskGraph.hasTask("publish")
    })
}