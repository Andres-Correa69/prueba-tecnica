/*
 * Script de build de auth-service.
 *
 * Responsabilidad: exponer `/auth/register` y `/auth/login` y emitir JWTs HS256.
 *
 * Por qué estas dependencias:
 *   - web          : controladores REST.
 *   - data-jpa     : persistir usuarios en SQL Server (esquema `auth`).
 *   - security     : BCrypt + cadena de filtros (deshabilitamos la mayor
 *                    parte, sólo usamos el encoder + cadena stateless —
 *                    documentado en SecurityConfig).
 *   - validation   : @Valid en los DTOs de request (Bean Validation / jakarta).
 *   - mssql-jdbc   : driver de SQL Server.
 *   - jjwt         : emisión de tokens HS256 (la validación vive en cars-service).
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

    // jjwt 0.12.x: API rediseñada — Jwts.builder().claims().subject(...).and()…
    // Mantener la versión fijada; la API no es binariamente compatible entre 0.12 y 0.11.
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
