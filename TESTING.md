# Guía de Testing

Este documento describe la estrategia de testing implementada en el proyecto Marketplace.

## Tipos de Tests

### 1. Tests Unitarios

Los tests unitarios prueban componentes individuales de forma aislada, sin dependencias externas.

#### Ubicación
- `src/test/java/com/marketplace/domain/service/` - Servicios de dominio
- `src/test/java/com/marketplace/application/service/` - Servicios de aplicación

#### Ejemplos
- `CategoryDomainServiceTest` - Valida lógica de negocio de categorías
- `OrderDomainServiceTest` - Prueba cálculos de órdenes
- `ProductDomainServiceTest` - Verifica cálculos con matrices
- `CustomerManagementServiceTest` - Prueba gestión de clientes
- `ProductManagementServiceTest` - Verifica gestión de productos

### 2. Tests de Integración

Los tests de integración prueban la interacción entre componentes, especialmente con la base de datos.

#### Ubicación
- `src/test/java/com/marketplace/infrastructure/adapter/` - Adaptadores de persistencia

#### Ejemplos
- `CustomerSpringJpaAdapterIntegrationTest` - Prueba persistencia de clientes
- `CategorySpringJpaAdapterIntegrationTest` - Verifica persistencia de categorías

#### Características
- Usan `@DataJpaTest` para pruebas con base de datos en memoria
- Configuran `spring.jpa.hibernate.ddl-auto=create-drop`
- Importan los adapters y mappers necesarios

### 3. Tests Funcionales

Los tests funcionales prueban los endpoints REST de forma completa, simulando peticiones HTTP.

#### Ubicación
- `src/test/java/com/marketplace/infrastructure/rest/controller/` - Controladores REST

#### Ejemplos
- `CustomerControllerFunctionalTest` - Prueba endpoints de clientes
- `CategoryControllerFunctionalTest` - Verifica endpoints de categorías

#### Características
- Usan `@WebMvcTest` para pruebas de capa web
- Utilizan `MockMvc` para simular peticiones HTTP
- Mockean los servicios de aplicación

## Ejecutar Tests

### Todos los tests
```bash
mvn test
```

### Tests unitarios específicos
```bash
mvn test -Dtest=CategoryDomainServiceTest
```

### Tests de integración
```bash
mvn test -Dtest=*IntegrationTest
```

### Tests funcionales
```bash
mvn test -Dtest=*FunctionalTest
```

### Con cobertura
```bash
mvn test jacoco:report
```

## Cobertura de Código

El proyecto incluye tests para:

- ✅ Servicios de dominio (validaciones, cálculos)
- ✅ Servicios de aplicación (lógica de negocio)
- ✅ Adaptadores de persistencia (JPA)
- ✅ Controladores REST (endpoints)

## Mejores Prácticas

1. **Nombres descriptivos**: Usar `@DisplayName` para describir qué prueba cada test
2. **Arrange-Act-Assert**: Estructurar tests en tres fases claras
3. **Aislamiento**: Cada test debe ser independiente
4. **Mocks**: Usar mocks para dependencias externas
5. **Assertions claras**: Usar assertions específicas y mensajes descriptivos

## Estructura de un Test

```java
@DisplayName("Description of what is being tested")
class MyServiceTest {

    @BeforeEach
    void setUp() {
        // Arrange: Preparar datos de prueba
    }

    @Test
    @DisplayName("Should do something when condition")
    void shouldDoSomethingWhenCondition() {
        // Given: Datos de entrada
        // When: Ejecutar acción
        // Then: Verificar resultado
    }
}
```

## Dependencias de Testing

- **JUnit 5**: Framework de testing
- **Mockito**: Para mocks y stubs
- **Spring Boot Test**: Utilidades de testing de Spring
- **H2 Database**: Base de datos en memoria para tests

