/*
 * Script de build raíz (Kotlin DSL).
 *
 * Decisiones de diseño:
 *   - Los plugins de Boot + Spring Dependency Management se declaran con
 *     `apply false`. Esto significa que los plugins están *disponibles*
 *     para cada sub-proyecto pero sólo se aplican realmente dentro del
 *     `build.gradle.kts` de cada servicio. Así el proyecto raíz queda
 *     como un puro agregador (no produce artefacto) mientras que las
 *     versiones siguen centralizadas aquí.
 *   - Bajamos intencionalmente de Boot 4.0.5 (el que emite Spring
 *     Initializr por defecto) a 3.3.5 LTS. Razón: jjwt 0.12.x,
 *     jakarta-persistence, spring-data-jpa y el resto del ecosistema
 *     están validados contra 3.3. 4.x es demasiado nuevo para una
 *     prueba técnica de 1 día en la que no podemos permitirnos cazar
 *     bugs de compatibilidad del ecosistema.
 *   - El toolchain de Java 21 se aplica en los subproyectos, alineado
 *     con el objetivo del repo.
 */
plugins {
    java
    id("org.springframework.boot") version "3.3.5" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
}

allprojects {
    group = "develope"
    version = "0.0.1-SNAPSHOT"
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    // `-parameters` conserva los nombres de parámetros de constructores/métodos
    // en el bytecode, lo que permite a Spring y Jackson enlazar los componentes
    // de records por nombre sin anotaciones extra. Esencial porque nuestros
    // DTOs son records.
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-parameters"))
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            events("passed", "failed", "skipped")
        }
    }
}
