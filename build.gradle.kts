import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.5"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.spring") version "2.0.0"
    kotlin("plugin.jpa") version "2.0.0"
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.Embeddable")
    annotation("jakarta.persistence.MappedSuperclass")
}

group = "kr.weit"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.awspring.cloud:spring-cloud-aws-starter-s3:3.1.1")

    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    // DB
    runtimeOnly("com.oracle.database.jdbc:ojdbc10:19.21.0.0")
    implementation("org.flywaydb:flyway-core:9.16.3")
    implementation("org.redisson:redisson-spring-boot-starter:3.30.0")
    implementation("com.linecorp.kotlin-jdsl:jpql-dsl:3.4.1")
    implementation("com.linecorp.kotlin-jdsl:jpql-render:3.4.1")
    implementation("com.linecorp.kotlin-jdsl:spring-data-jpa-support:3.4.1")
    implementation("com.linecorp.kotlin-jdsl:hibernate-kotlin-jdsl-jakarta:2.2.1.RELEASE")
    implementation("org.hibernate.orm:hibernate-spatial:6.5.2.Final")

    // Secret & Config
    implementation(platform("io.awspring.cloud:spring-cloud-aws-dependencies:3.1.1"))
    implementation("io.awspring.cloud:spring-cloud-aws-starter-parameter-store:3.1.1")
    implementation("org.springframework.cloud:spring-cloud-starter-bootstrap:4.1.2")

    // Utils
    implementation("org.apache.tika:tika-core:2.9.1")
    implementation("com.sksamuel.scrimage:scrimage-webp:4.1.3")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Test
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.mockk:mockk:1.13.11")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testImplementation("io.kotest:kotest-runner-junit5:5.9.0")
    testImplementation("io.kotest:kotest-assertions-core:5.9.0")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.3")
    testImplementation("io.kotest.extensions:kotest-extensions-testcontainers:2.0.2")
    testImplementation("org.testcontainers:testcontainers:1.19.8")
    testImplementation("org.testcontainers:junit-jupiter:1.19.8")
    testImplementation("org.testcontainers:oracle-xe:1.19.8")
    testImplementation("com.redis:testcontainers-redis:2.2.2")
    testImplementation("org.testcontainers:localstack:1.19.8")

    // Monitoring
    implementation("io.sentry:sentry-spring-boot-starter-jakarta:7.9.0")
    implementation("io.micrometer:micrometer-registry-prometheus:1.13.0")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.getByName("bootJar") {
    enabled = true
}

tasks.getByName<Jar>("jar") {
    enabled = false
}
