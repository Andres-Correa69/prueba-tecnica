/*
 * Script de build de cars-service.
 *
 * Responsabilidad: CRUD de autos acotado al usuario autenticado. Cada
 * request se valida contra un JWT firmado por auth-service (HS256,
 * secreto compartido).
 *
 * Por qué se incluye jjwt aunque este servicio NO emite tokens:
 * aún lo necesitamos para parsear, validar la firma y extraer los
 * claims en el JwtAuthenticationFilter.
 */
plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    runtimeOnly("com.microsoft.sqlserver:mssql-jdbc")

    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
