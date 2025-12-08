# Marketplace API - Sistema de Ecommerce
### Contexto del negocio

Una compañía de ecommerce dedicada a la venta de gadgets tecnológicos y accesorios inteligentes está renovando su plataforma interna.
Su objetivo es reemplazar sistemas previos por una API moderna, modular, segura, bien documentada y altamente escalable.

La nueva plataforma debe gestionar:
- Clientes
- Categorías
- Productos
- Inventario
- Órdenes (ventas)
- Integración con productos de terceros

### Problema a resolver:

La empresa te contrata como desarrollador backend senior.
Debes diseñar y construir el núcleo de la API del marketplace, cumpliendo los requerimientos funcionales y técnicos descritos a continuación.

No se indicará cómo debe implementarse cada parte; el diseño arquitectónico y técnico queda completamente a tu criterio.

-------------------------------------------------------
API moderna para gestión de marketplace con arquitectura hexagonal, desarrollada en Spring Boot 3.4.0 y Java 21.


## 📋 Características Principales

### Módulos Implementados

1. **Gestión de Clientes**
   - Registro y actualización de clientes
   - Historial de actividad
   - Cálculo de antigüedad usando `java.time` y `java.util.Date` (legacy)
   - Cálculo de ciclos de renovación usando `Calendar` (legacy)

2. **Gestión de Categorías**
   - Categorías anidadas multinivel
   - Validación recursiva de jerarquía (sin ciclos)
   - Obtención de árbol completo desde cualquier nodo
   - Validación de coherencia y consistencia

3. **Gestión de Productos**
   - CRUD completo de productos
   - Información comercial (precio, promociones)
   - Metadatos flexibles
   - Cálculos con matrices para similitud y recomendación
   - Búsqueda y filtrado

4. **Integración con Productos de Terceros**
   - Consumo de API externa (FakeAPI Platzi)
   - Procesamiento concurrente con hilos virtuales
   - Sincronización inteligente (almacenar/actualizar/mostrar)
   - Manejo de grandes volúmenes
   - Prevención de duplicidades

5. **Gestión de Inventario**
   - Control de stock por producto
   - Movimientos de inventario (entradas, salidas, ajustes, reservas)
   - Reserva de stock para órdenes
   - Sincronización thread-safe
   - Reportes de stock bajo

6. **Órdenes y Ventas**
   - Creación de órdenes con validación de stock
   - Cálculo automático de montos, impuestos y descuentos
   - Gestión de estados (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
   - Reserva y descuento de stock
   - Historial de órdenes

7. **Manejo de Archivos**
   - Importación de productos y clientes desde CSV
   - Exportación a CSV
   - Almacenamiento de logs históricos
   - Lectura de configuraciones desde archivos

8. **Generación de Reportes**
   - Reporte PDF comercial
   - Reportes Excel (inventario, ventas, productos externos)

## 🏗️ Arquitectura

El proyecto sigue **Arquitectura Hexagonal (Ports & Adapters)**:

```
src/main/java/com/marketplace/
├── domain/              # Capa de dominio (núcleo del negocio)
│   ├── model/          # Entidades de dominio
│   ├── port/           # Interfaces (puertos)
│   └── service/       # Servicios de dominio
├── application/        # Capa de aplicación
│   ├── service/        # Servicios de aplicación
│   ├── usecases/       # Casos de uso (interfaces)
│   └── dto/            # DTOs de aplicación
└── infrastructure/      # Capa de infraestructura
    ├── adapter/        # Adaptadores (persistencia, externos)
    ├── rest/           # Controladores REST
    └── config/         # Configuraciones
```

### Modelo de dominio clave

- **Clientes**
  - Datos personales básicos y fechas importantes (`birthDate`, `registrationDate`, etc.).
  - Cálculo de antigüedad y ciclos de renovación.

- **Categorías y Subcategorías**
  - Árbol jerárquico multinivel usando `Category` con referencia a `parent` y lista de `subCategories`.
  - Una **categoría raíz** tiene `parent = null`.
  - Una **subcategoría** tiene `parent` apuntando a otra categoría.
  - El sistema valida que **no haya ciclos** en la jerarquía.

- **Productos**
  - Cada `Product` pertenece a **una categoría** (normalmente una subcategoría hoja).
  - La relación se hace por **ID de categoría** (`categoryId`) en el DTO de entrada.
  - Soporta precios normales, promocionales, metadatos y vector de `features` para recomendaciones.

- **Órdenes, Inventario y Productos Externos**
  - Órdenes consumen stock desde `Inventory`.
  - Inventario registra movimientos y reservas.
  - Productos externos se sincronizan desde una API externa y se integran al catálogo.

## 🛠️ Tecnologías Utilizadas

- **Java 21** (Switch expressions, Pattern matching, Virtual threads)
- **Spring Boot 3.4.0**
- **Spring Data JPA**
- **H2 Database** (en memoria)
- **Lombok**
- **OpenAPI/Swagger** (Documentación API)
- **WebClient** (Cliente HTTP reactivo)
- **OpenCSV** (Procesamiento CSV)
- **OpenPDF** (Generación PDF)
- **Apache POI** (Generación Excel)
- **MapStruct** (Mappers)

## 🔐 Seguridad y Autenticación (JWT)

La API incorpora autenticación basada en **JWT (JSON Web Token)** y autorización por **roles**:

- **Endpoints de autenticación**
  - `POST /auth/register` → Registra un nuevo usuario y devuelve un JWT.
  - `POST /auth/login` → Autentica un usuario existente y devuelve un JWT.

- **Usuarios de ejemplo precargados** (`src/main/resources/data.sql`)
  - **Administrador**
    - Email: `admin@technicaldtm.com`
    - Password: `password`
    - Rol: `ADMIN`
  - **Usuario normal**
    - Email: `user@technicaldtm.com`
    - Password: `password`
    - Rol: `USER`

- **Uso en Swagger**
  1. Llama a `POST /auth/login` con uno de los usuarios de ejemplo.
  2. Copia el valor del campo `token` de la respuesta.
  3. En Swagger UI (`/swagger-ui.html`), pulsa en **Authorize** (esquema `bearerAuth`) y pega `Bearer <token>`.
  4. A partir de ese momento, las llamadas a endpoints protegidos se realizarán con el JWT.

- **Reglas de autorización principales**
  - `ROLE_USER`:
    - Puede ver la lista de productos (`GET /api/v1/products`), detalle (`GET /api/v1/products/{id}`) y búsqueda (`GET /api/v1/products/search`).
    - Puede ver todas las categorías (`GET /api/v1/categories`) y detalle (`GET /api/v1/categories/{id}`).
    - Puede ver **solo sus propias órdenes** (`GET /api/v1/orders/customer/{customerId}`) si el `Customer` asociado tiene su mismo email.
    - Puede registrar/ver su propia actividad de cliente (`POST /api/v1/customers/{id}/activity`) si el `Customer` asociado tiene su mismo email.
  - `ROLE_ADMIN`:
    - Tiene acceso completo a todos los endpoints de la API (productos, categorías, órdenes, clientes, inventario, ficheros, reportes, productos externos, etc.).

### Clases nuevas relacionadas con JWT y seguridad

- **Dominio / Persistencia**
  - `com.marketplace.domain.model.User` → Modelo de dominio para usuarios del sistema.
  - `com.marketplace.domain.model.Role` → Rol de usuario (`ADMIN`, `USER`).
  - `com.marketplace.domain.port.UserPersistencePort` → Puerto de persistencia para usuarios.
  - `com.marketplace.infrastructure.adapter.entity.UserEntity` → Entidad JPA para la tabla `users`.
  - `com.marketplace.infrastructure.adapter.entity.RoleEntity` → Entidad JPA para la tabla `roles`.
  - `com.marketplace.infrastructure.adapter.repository.UserJpaRepository` → Repositorio Spring Data JPA de usuarios.
  - `com.marketplace.infrastructure.adapter.repository.RoleJpaRepository` → Repositorio Spring Data JPA de roles.
  - `com.marketplace.infrastructure.adapter.mapper.UserDboMapper` → Mapeo entre `User` y `UserEntity` (incluyendo roles).
  - `com.marketplace.infrastructure.adapter.UserSpringJpaAdapter` → Adapter que implementa `UserPersistencePort` usando JPA.

- **Servicios de aplicación y DTOs**
  - `com.marketplace.application.service.AuthService` → Lógica de registro de usuarios (asigna roles y encripta contraseñas).
  - `com.marketplace.infrastructure.rest.dto.request.LoginRequestDto` → DTO para login (`email`, `password`).
  - `com.marketplace.infrastructure.rest.dto.request.RegisterRequestDto` → DTO para registro de usuario.
  - `com.marketplace.infrastructure.rest.dto.response.AuthResponseDto` → Respuesta con el JWT (`token`, `tokenType`).

- **Seguridad / JWT**
  - `com.marketplace.infrastructure.config.JwtProperties` → Carga `security.jwt.secret` y `security.jwt.expiration-ms` desde `application.yaml`.
  - `com.marketplace.infrastructure.config.JwtTokenProvider` → Genera, valida y parsea tokens JWT.
  - `com.marketplace.infrastructure.config.CustomUserDetailsService` → Implementación de `UserDetailsService` basada en `UserEntity`.
  - `com.marketplace.infrastructure.config.JwtAuthenticationFilter` → Filtro que lee el header `Authorization`, valida el JWT y autentica al usuario en el `SecurityContext`.
  - `com.marketplace.infrastructure.config.SecurityConfig` → Configuración de Spring Security (stateless, rutas públicas `/auth/**`, esquema de filtros, `PasswordEncoder`, `AuthenticationManager`).

- **Controlador de autenticación**
  - `com.marketplace.infrastructure.rest.controller.AuthController`  
    - Expone `/auth/register` y `/auth/login`.  
    - Autentica con `AuthenticationManager` y genera el JWT con `JwtTokenProvider`.  

### Clases modificadas para integrar roles y seguridad

- **Controladores REST**
  - `ProductController`, `CategoryController`, `OrderController`, `CustomerApiController`, `InventoryController`, `ReportController`, `FileController`, `ExternalProductController`  
    - Se añadieron anotaciones `@PreAuthorize` para restringir el acceso según el rol (`USER`/`ADMIN`).  
    - Se añadieron anotaciones `@SecurityRequirement(name = "bearerAuth")` para que Swagger muestre que los endpoints requieren JWT.  
  - `OrderController`  
    - En `getOrdersByCustomer` se añadió lógica para que un usuario normal solo pueda ver sus propias órdenes (comparando `authentication.getName()` con el email del `Customer`).  
  - `CustomerApiController`  
    - En `recordCustomerActivity` se añadió lógica para que un usuario normal solo pueda registrar/ver su propia actividad de cliente.

- **Configuración de Swagger/OpenAPI**
  - `SwaggerConfig`  
    - Se actualizó `GroupedOpenApi` para incluir también las rutas `/auth/**` en la documentación.
  - `SpringDocConfig`  
    - Se definió un esquema de seguridad global `bearerAuth` (JWT) para OpenAPI.  
    - Se añaden requisitos de seguridad a todos los endpoints (excepto `/auth/**`) y se mantiene la limpieza de esquemas problemáticos ya existente.

- **Datos de ejemplo**
  - `src/main/resources/data.sql`  
    - Se agregaron inserciones en las tablas `roles`, `users` y `user_roles` para crear:  
      - Un usuario administrador (`admin@technicaldtm.com`, rol `ADMIN`).  
      - Un usuario normal (`user@technicaldtm.com`, rol `USER`).  
    - Ambos con la contraseña encriptada (`password`).

## 🚀 Requisitos

- Java 21 o superior
- Maven 3.6+
- IDE (IntelliJ IDEA, Eclipse, VS Code)

## 📦 Instalación y Ejecución

### 1. Clonar el repositorio

```bash
git clone <repository-url>
cd marketplace
```

### 2. Compilar el proyecto

```bash
mvn clean install
```

### 3. Ejecutar la aplicación

```bash
mvn spring-boot:run
```

O ejecutar directamente la clase `MarketplaceApplication`.

### 4. Acceder a la aplicación

- **API Base URL**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:marketplace_db`
  - Username: `sa`
  - Password: `password`

## 📚 Endpoints Principales

### Clientes
- `POST /api/v1/customers` - Registrar cliente
- `GET /api/v1/customers/{id}` - Obtener cliente
- `GET /api/v1/customers` - Listar todos los clientes

### Categorías
- `POST /api/v1/categories` - Crear categoría
- `GET /api/v1/categories/{id}` - Obtener categoría
- `GET /api/v1/categories` - Obtener árbol de categorías
- `GET /api/v1/categories/{id}/hierarchy` - Obtener jerarquía completa

### Productos
- `POST /api/v1/products` - Crear producto
- `GET /api/v1/products/{id}` - Obtener producto
- `GET /api/v1/products` - Listar productos
- `GET /api/v1/products/search?q={term}` - Buscar productos
- `GET /api/v1/products/{id}/recommendations` - Obtener recomendaciones
- `POST /api/v1/products/{id}/similarity` - Calcular similitud (matrices)

### Órdenes
- `POST /api/v1/orders` - Crear orden
- `GET /api/v1/orders/{id}` - Obtener orden
- `GET /api/v1/orders` - Listar órdenes
- `PUT /api/v1/orders/{id}/status?status={status}` - Actualizar estado
- `POST /api/v1/orders/{id}/cancel` - Cancelar orden

### Inventario
- `GET /api/v1/inventory/product/{productId}` - Obtener inventario
- `GET /api/v1/inventory/low-stock` - Productos con stock bajo
- `POST /api/v1/inventory/product/{productId}/add?quantity={qty}` - Agregar stock
- `POST /api/v1/inventory/product/{productId}/remove?quantity={qty}` - Remover stock
- `GET /api/v1/inventory/product/{productId}/movements` - Historial de movimientos

### Productos Externos
- `POST /api/v1/external-products/sync` - Sincronizar productos externos
- `GET /api/v1/external-products/{externalId}` - Obtener producto externo

### Archivos
- `POST /api/v1/files/import/customers` - Importar clientes desde CSV
- `POST /api/v1/files/import/products` - Importar productos desde CSV
- `GET /api/v1/files/export/customers` - Exportar clientes a CSV
- `GET /api/v1/files/export/products` - Exportar productos a CSV

### Reportes
- `GET /api/v1/reports/commercial/pdf` - Reporte comercial PDF
- `GET /api/v1/reports/inventory/excel` - Reporte inventario Excel
- `GET /api/v1/reports/sales/excel` - Reporte ventas Excel
- `GET /api/v1/reports/external-products/excel` - Reporte productos externos Excel

## 💡 Características Técnicas Avanzadas

### Java Avanzado

- ✅ **Programación Funcional**: Streams, lambdas, method references
- ✅ **Recursividad**: Validación de jerarquía de categorías
- ✅ **Matrices**: Cálculos de similitud y recomendación de productos
- ✅ **Manejo de Archivos**: CSV, logs, configuraciones
- ✅ **Generación PDF**: Reportes comerciales
- ✅ **Generación Excel**: Reportes de inventario y ventas
- ✅ **java.time y Date/Calendar**: Manejo de fechas modernas y legacy
- ✅ **Switch Expressions y Pattern Matching**: Java 21
- ✅ **Concurrencia**: CompletableFuture, Virtual Threads, sincronización
- ✅ **POO Avanzada**: Encapsulación, herencia, polimorfismo
- ✅ **Patrones de Diseño**: Adapter, Strategy, Factory, etc.
- ✅ **Excepciones Personalizadas**: Manejo de errores específicos

### Spring y Arquitectura

- ✅ **JPA**: Persistencia con Spring Data JPA
- ✅ **Mappers/DTOs**: Separación de capas
- ✅ **API Documentada**: OpenAPI/Swagger
- ✅ **Arquitectura Hexagonal**: Separación clara de responsabilidades
- ✅ **Perfiles de Entorno**: Configuración por ambiente
- ✅ **DB en Memoria**: H2 para desarrollo

### Concurrencia en el proyecto (ExecutorService, CompletableFuture, hilos virtuales)

La concurrencia se utiliza principalmente en la integración con productos externos:

- **Configuración de hilos virtuales**  
  - Archivo: `VirtualThreadConfig` (`infrastructure/config/VirtualThreadConfig.java`)  
  - Define un `ExecutorService` global basado en **hilos virtuales** (`Executors.newVirtualThreadPerTaskExecutor()`), expuesto como bean `virtualThreadExecutor`.  
  - Este executor se usa para ejecutar tareas I/O‑bound (llamadas a la API externa) en hilos muy ligeros, sin bloquear el pool clásico de hilos de Spring.

- **Procesamiento asíncrono con `CompletableFuture`**  
  - Archivo: `ExternalProductManagementService` (`application/service/ExternalProductManagementService.java`)  
  - Método `fetchAndSynchronizeProducts()`:
    - Anotado con `@Async`, devuelve `CompletableFuture<List<Product>>`.
    - Encadena operaciones con `thenApply` y `exceptionally` para **sincronizar productos externos en segundo plano** sin bloquear el hilo HTTP.
  - Método `fetchExternalProduct(String externalId)`:
    - Devuelve `CompletableFuture<Product>` al consultar un producto externo por ID.

- **Procesamiento concurrente de lotes con hilos virtuales**  
  - En `ExternalProductManagementService.processExternalProductsBatch(...)`:
    - Usa `CompletableFuture.supplyAsync(..., virtualThreadExecutor)` sobre un `stream` de productos externos.
    - Cada producto se valida y procesa en paralelo sobre **hilos virtuales**, lo que permite manejar grandes volúmenes sin saturar el sistema.
    - Después, hace `join()` de todos los futuros y filtra los productos válidos.

- **Sincronización y consistencia de datos**  
  - La lógica de negocio (validaciones, marcado de productos externos, activación por defecto, etc.) se ejecuta dentro de estas tareas asíncronas, pero **toda la escritura en la base de datos** sigue pasando por los servicios de aplicación y los puertos de dominio, manteniendo la **arquitectura hexagonal** y la consistencia transaccional.

## 🧪 Pruebas

Para ejecutar las pruebas:

```bash
mvn test
```

### Tipos de Pruebas

- **Unitarias**: Servicios de dominio y aplicación
- **Integración**: Adaptadores de persistencia
- **Funcionales**: Endpoints REST

## 📝 Ejemplos de Uso

### Crear un Cliente

```bash
curl -X POST http://localhost:8080/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Juan",
    "lastName": "Pérez",
    "email": "juan@example.com",
    "birthDate": "1990-01-15",
    "phone": "+1234567890"
  }'
```

### Crear una Categoría (raíz y subcategoría)

- **Categoría raíz** (sin padre):

```bash
curl -X POST http://localhost:8080/api/v1/categories \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Zapatos",
    "description": "Calzado en general",
    "code": "ZAPATOS",
    "active": true
  }'
```

- **Subcategoría** (hija de una categoría existente)  
  Supongamos que la categoría raíz `Zapatos` tiene `id = 1`:

```bash
curl -X POST http://localhost:8080/api/v1/categories \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Deportivos",
    "description": "Zapatos deportivos",
    "code": "ZAPATOS_DEP",
    "parentId": 1,
    "active": true
  }'
```

- Resultado de ejemplo de una categoría con subcategorías (`GET /api/v1/categories`):

```json
{
  "id": 1,
  "name": "Zapatos",
  "description": "Calzado en general",
  "code": "ZAPATOS",
  "active": true,
  "parentId": null,
  "parentName": null,
  "subCategories": [
    {
      "id": 2,
      "name": "Deportivos",
      "description": "Zapatos deportivos",
      "code": "ZAPATOS_DEP",
      "active": true,
      "parentId": 1,
      "parentName": "Zapatos",
      "subCategories": []
    }
  ],
  "fullPath": "Zapatos"
}
```

> **Nota**: para crear una categoría raíz no envíes `parentId` o envíalo como `null`. Si envías un `parentId` que no existe, el servicio responde con error `400 Parent category not found`.

### Crear un Producto asociado a una subcategoría

El endpoint `POST /api/v1/products` espera un **DTO de entrada** con `categoryId` en lugar de un objeto `Category` completo.  
Normalmente se asocia el producto a una **subcategoría hoja**, por ejemplo `Deportivos` con `id = 2`:

```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "NIKE-LEBRON-DEP",
    "name": "Nike Lebron Deportivos",
    "description": "Zapatillas Nike Lebron deportivas",
    "price": 330,
    "promotionalPrice": 300,
    "categoryId": 2,
    "features": [0.1, 0.8, 0.4],
    "active": true
  }'
```

La respuesta (`ProductResponseDto`) incluirá la categoría completa como `category` (con `id`, `name`, `parentId`, `fullPath`, etc.) para que el cliente pueda mostrar el contexto del producto dentro del árbol de categorías.

### Sincronizar Productos Externos

```bash
curl -X POST http://localhost:8080/api/v1/external-products/sync
```

### Crear Producto Externo (Fake Store API)

Crea un producto directamente en la API externa (sin persistencia local, salvo la respuesta mapeada).
El `category.id` debe existir en la API de Platzi (por defecto IDs 1-5 suelen existir).

```bash
curl -X POST http://localhost:8080/api/v1/external-products/create \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Producto prueba",
    "price": 100,
    "description": "Descripcion prueba",
    "category": {
      "id": 1
    },
    "metadata": {
      "images": "https://placehold.co/600x400"
    }
  }'
```

### Crear una Orden

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "items": [
      {
        "productId": 1,
        "quantity": 2,
        "unitPrice": 99.99
      }
    ]
  }'
```

### Importar Clientes desde CSV (formato)

El endpoint `POST /api/v1/files/import/customers` espera un archivo CSV con el siguiente formato
(la primera fila es cabecera):

```csv
firstName,lastName,email,phone,address,birthDate,status
Juan,Pérez,juan@example.com,+34111111111,"Calle 1, Madrid","1990-01-15",ACTIVE
Ana,García,ana@example.com,+34222222222,"Avenida 2, Barcelona","1985-06-20",INACTIVE
```

- `birthDate` debe ir en formato `yyyy-MM-dd`.
- `status` es opcional; si se omite, el servicio lo tratará según la lógica de negocio.

### Importar Productos desde CSV (formato)

El endpoint `POST /api/v1/files/import/products` acepta el mismo formato que genera
`GET /api/v1/files/export/products`. Ejemplo de archivo CSV (incluyendo cabecera):

```csv
ID,SKU,Name,Description,Price,Promotional Price,Category ID,Active,Is External,External Provider ID
,SKU-001,"Zapatillas Nike","Zapatillas deportivas",89.99,79.99,2,true,false,
,SKU-EXT-123,"Gorra externa","Gorra desde proveedor externo",15.50,,5,true,true,123
```

- La columna `ID` se ignora al importar (la base de datos genera un nuevo ID).
- `Category ID` debe ser un ID de categoría válido ya existente.
- `Active` e `Is External` aceptan `true` o `false`.
- `Promotional Price` y `External Provider ID` pueden quedar vacíos.

## 🔧 Configuración

### application.yaml

El archivo de configuración principal está en `src/main/resources/application.yaml`.

### Variables de Entorno

Puedes configurar:
- `external.api.base-url`: URL base de la API externa
- `external.api.products-path`: Ruta para productos externos

## 📖 Documentación Adicional

- La documentación completa de la API está disponible en Swagger UI
- Cada endpoint incluye descripciones y ejemplos
- Los modelos de dominio están documentados con JavaDoc

## 🤝 Contribución

Este proyecto fue desarrollado como demostración de habilidades técnicas avanzadas en Java y Spring Boot, siguiendo las mejores prácticas de arquitectura de software.

## 📄 Licencia

Este proyecto es de uso educativo y demostrativo.

## ✨ Notas Importantes

1. **Base de Datos**: H2 en memoria (se reinicia al reiniciar la aplicación)
2. **API Externa**: Por defecto usa FakeAPI Platzi (https://api.escuelajs.co/api/v1/products)
3. **Concurrencia**: Los productos externos se procesan usando hilos virtuales de Java 21
4. **Validaciones**: Las validaciones de jerarquía de categorías previenen ciclos recursivos
5. **Inventario**: El sistema maneja reservas de stock y previene condiciones de carrera

---

**Desarrollado por Marcos Gonzalez - usando Arquitectura Hexagonal y Spring Boot**

