# 09 — Glosario de conceptos

> Diccionario de "términos raros" que aparecen en el código o que pueden
> mencionar en el examen. Cada entrada tiene: **definición corta**,
> **para qué sirve** y **ejemplo concreto en este proyecto**.

Ordenado alfabéticamente.

---

## Aggregate (raíz de agregado)

**Qué es.** Un "paquete" de objetos del dominio que se tratan como una
unidad. Solo la raíz tiene un identificador público; los objetos internos
solo existen a través de ella.

**Para qué.** Garantizar invariantes: si la raíz está bien, todo el
agregado está bien. Evita que código ajeno manipule piezas sueltas sin
respetar las reglas.

**Ejemplo aquí.** La clase `Car` es una raíz de agregado. Los VOs
(`Placa`, `Marca`, `Anio`…) son parte del agregado pero solo se construyen
vía la raíz. No puedes tener un `Placa` inválida porque no puede
construirse suelta — el método factory de `Car` llama a `Placa.of(...)`
que valida.

---

## BCrypt

**Qué es.** Algoritmo de hasheo de contraseñas diseñado para ser **lento**.

**Para qué.** Un atacante con tu base de datos robada no puede probar
mil millones de contraseñas por segundo — solo unos pocos cientos,
porque cada intento cuesta ~70 ms.

**Ejemplo aquí.** `BCryptPasswordHasherAdapter.java`. Usa cost=10
(~70 ms por hash). El formato del string almacenado es
`$2a$10$XXXXXXXXXXXXXXXXXXXXXXXXXX` — el algoritmo, el costo, el salt
y el hash, todo en una línea.

---

## Bean Validation (JSR-380, `@NotBlank`, `@Size`…)

**Qué es.** Estándar de Java para validar objetos anotando sus campos.

**Para qué.** Rechazar inputs malformados antes de que toquen la lógica
de negocio.

**Ejemplo aquí.** `RegisterRequest.java`:
```java
public record RegisterRequest(
    @NotBlank @Size(min = 3, max = 30) String username,
    @NotBlank @Size(min = 8, max = 100) String password
) {}
```
Si el cliente manda `{"username":""}` Spring devuelve 400 automáticamente,
sin que la lógica nuestra se ejecute.

---

## Bearer token

**Qué es.** Un token que dice "quien lo presenta tiene el permiso".
Se envía en el header `Authorization: Bearer <token>`.

**Para qué.** Mecanismo estándar HTTP para pasar credenciales. El
servidor no mantiene sesión — cada petición trae todo lo necesario.

**Ejemplo aquí.** Después del login, el frontend guarda el JWT y
todas las peticiones a cars-service incluyen:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## Bounded Context

**Qué es.** (Domain-Driven Design) Una frontera dentro del sistema
donde un concepto tiene UN significado específico.

**Ejemplo aquí.** `auth` y `cars` son dos bounded contexts:
- En `auth`, un "User" tiene username, password, creación.
- En `cars`, NO hay objeto User. Solo existe un `OwnerId` — un UUID que
  apunta al `auth.Users.id` pero sin entidad asociada.

Esto permite que los dos microservicios evolucionen sin pisarse.

---

## Command (patrón)

**Qué es.** Un objeto inmutable que encapsula la intención de hacer algo.

**Para qué.** Alimentar casos de uso con un único argumento tipado,
en vez de 5 parámetros sueltos.

**Ejemplo aquí.** `CreateCarCommand`:
```java
public record CreateCarCommand(
    UUID ownerId, String marca, String modelo, int anio,
    String placa, String color, String fotoUrl
) {}
```
El controlador lo construye y se lo pasa a
`CreateCarUseCase.create(command)`. Un día podemos añadir 10 campos más
sin cambiar la firma del use case.

---

## CORS (Cross-Origin Resource Sharing)

**Qué es.** Mecanismo del navegador que controla qué orígenes pueden
hacer peticiones a un backend.

**Para qué.** Proteger al usuario: si visita `hack.com` y esa página
intenta hacer `fetch('http://banco.com/transferir')`, el banco debe
haber permitido explícitamente ese origen, o el navegador lo bloquea.

**Ejemplo aquí.** El frontend vive en `http://localhost:5173` y ambos
backends en `:8081` y `:8082`. Como son orígenes distintos (puerto diferente),
se necesita CORS. Mira `CorsConfig.java`: permite solo
`http://localhost:5173`, métodos GET/POST/PUT/DELETE/OPTIONS y el header
`Authorization`.

---

## CSRF (Cross-Site Request Forgery)

**Qué es.** Ataque donde una página maliciosa hace que tu navegador
envíe peticiones a otra página donde ya estás logueado usando tus
cookies.

**Por qué NO aplica aquí.** CSRF ataca sesiones basadas en cookies.
Nosotros usamos Bearer tokens en header `Authorization`, que el
navegador NO envía automáticamente. Por eso tenemos
`http.csrf(csrf -> csrf.disable())` en `SecurityConfig`.

---

## DataGrid (MUI X)

**Qué es.** Componente de tabla de Material UI con paginación, sorting,
filtros y acciones por fila incluidas.

**Ejemplo aquí.** `CarTable.tsx`. Le decimos `paginationMode="server"` para
que no pagine en el cliente: cada cambio de página manda una nueva
petición con `?page=&size=` al backend.

---

## DDL / DML

- **DDL (Data Definition Language)** — sentencias que definen la estructura:
  `CREATE TABLE`, `ALTER TABLE`, `DROP`.
- **DML (Data Manipulation Language)** — sentencias que mueven datos:
  `INSERT`, `UPDATE`, `DELETE`, `SELECT`.

**Ejemplo aquí.** `02-create-tables.sql` es DDL. Cuando Hibernate
genera un `INSERT INTO cars.Cars (...)` eso es DML.

---

## DI (Dependency Injection) / IoC (Inversion of Control)

- **IoC** — "no soy yo quien crea mis colaboradores, alguien me los pasa".
- **DI** — mecanismo para hacer IoC: pasar los colaboradores por
  constructor, setter o field.

**Ejemplo aquí.** El `CarController` no hace `new CreateCarService(...)`.
Lo recibe por constructor, y Spring (el contenedor IoC) decide qué
implementación concreta pasar, basado en la config de `BeanConfig.java`.

---

## DTO (Data Transfer Object)

**Qué es.** Un objeto plano que SOLO transporta datos entre capas
(típicamente HTTP ↔ lógica). No tiene comportamiento.

**Para qué.** Desacoplar el formato que ven los clientes del formato
interno. Si renombramos un campo del dominio, no rompemos la API.

**Ejemplo aquí.** `CarResponse.java`. El dominio `Car` tiene métodos como
`ensureOwnedBy(...)`; el DTO solo tiene campos que van como JSON.

---

## IDOR (Insecure Direct Object Reference)

**Qué es.** Vulnerabilidad en la que un usuario puede ver/editar/borrar
objetos de OTRO usuario simplemente cambiando un id en la URL.

**Ejemplo clásico**: `GET /cars/xxx-yyy-zzz` me devuelve un auto sin
verificar que ese auto sea mío.

**Cómo lo prevenimos.** Todo método del repositorio recibe un `OwnerId`:
`findByIdAndOwner(carId, ownerId)`. Si el carro no te pertenece,
devolvemos 404 (no 403, para no filtrar su existencia).

---

## Inmutabilidad

**Qué es.** Un objeto cuyo estado no se puede cambiar después de creado.

**Para qué.** Si tienes un `Car`, sabes que nadie puede modificarlo
"desde otro hilo" o "a escondidas". Esto hace el código predecible.

**Ejemplo aquí.** Todos los VOs y aggregates son inmutables. Para
"actualizar" un carro NO modificamos el objeto — devolvemos una nueva
instancia vía `car.withUpdates(...)`.

---

## Instant

**Qué es.** Tipo de Java 8+ que representa un momento exacto en el tiempo
(UTC, preciso al nanosegundo).

**Para qué.** Evitar los líos de zonas horarias de `Date` / `Timestamp`.
Un `Instant` siempre significa lo mismo sin importar dónde corre el código.

**Ejemplo aquí.** `Car.createdAt()` devuelve `Instant`. En la BD se
guarda como `DATETIME2(3)` (UTC). En el JSON se serializa como
`2026-04-16T05:55:40.29Z`.

---

## JavaDoc

**Qué es.** Bloques de comentarios `/** ... */` encima de clases/métodos
que se convierten en documentación navegable.

**Ejemplo aquí.** Cada clase pública tiene uno. Además, siguiendo
convención, explicamos el **por qué** de decisiones no obvias (ej.
"¿por qué no `@Service`?" en `RegisterUserService.java`).

---

## JPA (Jakarta Persistence API)

**Qué es.** La *especificación* de Java para mapear objetos a tablas.
Hibernate es la implementación que usa Spring Boot por defecto.

**Ejemplo aquí.** Las anotaciones `@Entity`, `@Table`, `@Column`,
`@Id` en `CarJpaEntity.java` son JPA. Hibernate las lee y genera el SQL.

---

## JWT (JSON Web Token)

**Qué es.** Un string codificado en Base64 con tres partes (`header.payload.signature`)
que contiene claims firmados.

**Partes de un JWT**:
1. **Header** → algoritmo (ej. `HS256`) y tipo.
2. **Payload** → los claims (`sub`, `iss`, `exp`, tus propios).
3. **Signature** → garantía de que nadie modificó el payload.

**Ejemplo real** (el que emite auth-service):
```
eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ1ZmluZXQtYXV0b3MiLCJzdWIiOiI1ZjZhLi4uIiwidXNlcm5hbWUiOiJhbmRyZXMiLCJpYXQiOjE3MTMyMDAwMDAsImV4cCI6MTcxMzIwMzYwMH0.FIRMA
```

Decodificado:
- header: `{"alg":"HS256"}`
- payload: `{"iss":"ufinet-autos","sub":"5f6a...","username":"andres","iat":...,"exp":...}`
- firma: HMAC-SHA256 del header + payload usando `JWT_SECRET`.

**Claim = "afirmación"**. `sub` = subject (id del dueño),
`iss` = issuer (emisor), `exp` = expiration (cuándo vence).

---

## ORM (Object-Relational Mapping)

**Qué es.** "Traducir" entre objetos (Java) y filas de tablas (SQL).

**Ejemplo aquí.** Hibernate — traducimos `Car` ↔ `cars.Cars`. El mapping
está en `CarJpaEntity.java` (anotaciones) y `CarPersistenceMapper.java`
(conversión dominio ↔ JPA).

---

## Puerto / Adaptador (Hexagonal)

- **Puerto** — una **interface** del dominio/aplicación que describe qué
  necesita, sin decir cómo se implementa. Ej. `UserRepositoryPort`.
- **Adaptador** — la **implementación** concreta, vive en
  `infrastructure/`. Ej. `UserPersistenceAdapter` (usando JPA/SQL Server).

**Ejemplo de swap fácil.** Si quieres reemplazar BCrypt por Argon2,
escribes un `Argon2PasswordHasherAdapter` que implementa
`PasswordHasherPort`. Los use cases no se enteran.

---

## Record (Java)

**Qué es.** Una clase Java declarada con `record` — inmutable,
con constructor canónico, `equals`/`hashCode`/`toString` generados.

**Ejemplo aquí.** Todos los DTOs, commands, y value objects simples
son records: `CarRequest`, `CreateCarCommand`, `AuthenticatedUser`.

```java
public record AuthenticatedUser(UUID userId, String username) {}
```

Esa línea equivale a ~30 líneas de una clase tradicional.

---

## Rehidratar

**Qué es.** Reconstruir un objeto del dominio a partir de datos persistidos
(por ejemplo, una fila de la BD), sin pasar por el flujo de creación.

**Por qué existe como método separado.** El factory `Car.create(...)`
genera un id nuevo y pone `createdAt=now`. Pero al leer de la BD
**ya tenemos** ese id y esa fecha, no queremos generar otros. Para
eso tenemos `Car.rehydrate(id, ownerId, ..., createdAt, updatedAt)`
— "aquí están los datos existentes, reconstruye el objeto tal cual".

**Ejemplo aquí.** `CarPersistenceMapper.toDomain(entity)` → llama a
`Car.rehydrate(...)`.

---

## Spring Security Filter Chain

**Qué es.** Una cadena de filtros HTTP que procesa cada petición antes
de que llegue al controlador.

**En cars-service**:
```
[CorsFilter] → [JwtAuthenticationFilter] → [demás filtros] → [CarController]
```

Nuestro `JwtAuthenticationFilter`:
1. Lee `Authorization: Bearer ...`.
2. Decodifica + valida con `JwtDecoder`.
3. Pone el `AuthenticatedUser` en `SecurityContextHolder`.
4. Si algo falla → 401 y aborta la cadena.

---

## Stateless (sin estado)

**Qué es.** El servidor NO mantiene información del cliente entre
peticiones. Cada request debe traer todo lo necesario.

**Para qué.** Escalado horizontal trivial: cualquier instancia puede
atender cualquier petición. Rolling restarts sin dropear sesiones.

**Ejemplo aquí.** Ambos servicios están configurados con
`SessionCreationPolicy.STATELESS`. No hay cookies, no hay sesiones
en memoria. Solo el JWT cada vez.

---

## Timestamp (en general)

**Qué es.** Un número que representa un **momento en el tiempo**.

**Formatos comunes**:
- **Unix epoch** (segundos desde 1970-01-01 UTC): `1713200000`.
- **ISO-8601** (string legible): `2026-04-16T05:55:40Z`.

**En este proyecto**:
- El JWT tiene `iat` y `exp` como epoch segundos.
- Los `createdAt` / `updatedAt` se almacenan en BD como `DATETIME2(3)` y
  se serializan en JSON como ISO-8601 (`"2026-04-16T05:55:40.290Z"`).
- En Java los representamos con `Instant`.

---

## Use Case (Caso de uso)

**Qué es.** Una operación de negocio expresada como una clase/método.
No conoce HTTP ni BD — solo la intención de negocio.

**Ejemplos aquí**:
- `RegisterUserUseCase` → "registrar un usuario".
- `CreateCarUseCase` → "crear un auto".
- `ListCarsByOwnerUseCase` → "listar los autos del dueño".

**¿Por qué son interfaces?** Para que los controladores dependan del
contrato, no de la implementación — y poder swapearlos / decorarlos.

---

## UUID (Universally Unique Identifier)

**Qué es.** Un identificador de 128 bits con formato
`5f6a1234-abcd-4ef5-89ab-cdef12345678`. Dos generados al azar tienen
probabilidad astronómicamente baja de colisión.

**Para qué lo usamos**:
- IDs de usuarios y autos.
- No revelan el orden de creación (vs. IDs numéricos que sí).
- El cliente puede pre-generarlos si quisiera (no lo hacemos aquí).

**Cómo se guarda en SQL Server**: tipo `UNIQUEIDENTIFIER`.

---

## Value Object (VO)

**Qué es.** Un objeto pequeño, inmutable, definido por su valor (no por
su identidad). Dos VOs con el mismo contenido son iguales.

**Para qué.** Empaquetar invariantes. Un `Placa` no puede existir con
formato inválido — la construcción lo valida.

**Ejemplos en el proyecto**:
- `Username`, `PasswordHash` (auth).
- `Placa`, `Marca`, `Modelo`, `Anio`, `Color`, `FotoUrl` (cars).

**La firma típica**:
```java
public final class Anio {
    private final int value;
    private Anio(int value) { this.value = value; }
    public static Anio of(int value) {
        // validación
        return new Anio(value);
    }
    public int value() { return value; }
    // equals / hashCode basados en value
}
```

---

## Zod

**Qué es.** Librería TypeScript para describir y validar la forma de los
datos. Parecido a Bean Validation pero en el cliente.

**Ejemplo aquí.** `carSchemas.ts`:
```ts
export const carFormSchema = z.object({
  marca: z.string().min(1).max(50),
  anio: z.coerce.number().int().min(1900).max(currentYear + 1),
  placa: z.string().regex(/^[A-Z0-9]{3}-?[A-Z0-9]{3}$/),
  ...
});
```

Se conecta a `react-hook-form` vía `@hookform/resolvers/zod` y aporta:
- Validación en tiempo real mientras escribes.
- Tipado de TypeScript automático (`CarFormValues = z.infer<...>`).

---

## Preguntas frecuentes del sustentador

> **¿Por qué hay dos microservicios?**
> Separación de responsabilidades, despliegue independiente y superficie
> de ataque reducida en auth. Ver `docs/02-microservicios.md`.

> **¿Por qué tantos archivos? ¿No se puede hacer más corto?**
> Se puede, pero sacrificas la separación hexagonal que nos protege de
> acoplarnos al framework. Para un CRUD trivial es sobreingeniería;
> para un proyecto profesional de medio/largo plazo se paga solo.

> **¿Por qué HS256 y no RS256?**
> HS256 (simétrico) es lo más simple para 2 servicios del mismo equipo
> con un secreto compartido. RS256 (asimétrico) es más apropiado entre
> equipos distintos. Ver `docs/03-seguridad-jwt.md`.

> **¿Qué pasa si alguien cambia el `ownerId` en el body de POST /cars?**
> Nada: el controlador NO lee `ownerId` del body. Lo saca del claim
> `sub` del JWT vía `@CurrentUser`. Ver `CarController.java`.
