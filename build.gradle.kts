plugins {
	java
	id("org.springframework.boot") version "3.1.5"
	id("io.spring.dependency-management") version "1.1.3"
	id("org.asciidoctor.jvm.convert") version "3.3.2"
	id("jacoco")
	id("org.sonarqube") version "4.4.1.3373"
	id("com.google.cloud.tools.jib") version "3.4.0"
}

group = "com"
version = "1.0.0"
description = "E-commerce wishlist"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

ext {
	set("springCloudVersion", "2022.0.4")
	set("testcontainersVersion", "1.19.1")
	set("cucumberVersion", "7.14.0")
	set("junitVersion", "5.10.0")
}

dependencies {
	// spring boot starters
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	// documentation & api
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:2.2.0")
	implementation("com.fasterxml.jackson.core:jackson-databind")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	implementation("com.fasterxml.jackson.module:jackson-module-parameter-names")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:mongodb")
	testImplementation("io.cucumber:cucumber-java:${property("cucumberVersion")}")
	testImplementation("io.cucumber:cucumber-junit:${property("cucumberVersion")}")
	testImplementation("io.cucumber:cucumber-spring:${property("cucumberVersion")}")
	testImplementation("org.mockito:mockito-junit-jupiter")
	testImplementation("org.assertj:assertj-core")
	testImplementation("com.github.tomakehurst:wiremock-jre8:2.35.0")
	testImplementation("org.awaitility:awaitility:4.2.0")
	testImplementation("io.rest-assured:rest-assured:5.3.2")
	testImplementation("io.rest-assured:json-path:5.3.2")
	testImplementation("io.rest-assured:xml-path:5.3.2")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
		mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
