# Epic — explicación detallada de funciones

Este documento describe, en texto y con detalle, qué hace el proyecto Epic, cómo está organizado y qué hace cada función (método, endpoint, script del navegador y pieza de configuración).

Epic es una aplicación web de catálogo de productos y solicitudes de cotización. El backend es Spring Boot 4 (Java 21). El frontend es una página estática (HTML, CSS y JavaScript) servida por el mismo servidor. Los datos se guardan en una base H2 en memoria.

---

## 1. Qué puede hacer un usuario

Hay tres flujos principales:

1. **Ver el catálogo.** Al abrir la página, el navegador pide todos los productos al API y los muestra (nombre, categoría, id, descripción y precio en pesos mexicanos).
2. **Solicitar una cotización.** El cliente elige un producto, indica cantidad, nombre, correo (y opcionalmente teléfono y mensaje). El servidor valida los datos, comprueba que el producto exista y guarda la solicitud.
3. **Registrar un producto (panel de administrador en la misma página).** Se envían nombre, categoría, precio base y descripción. Si todo es válido, el producto se guarda y el catálogo de la página se recarga.

También existen endpoints de consulta de cotizaciones pensados para el backend o herramientas como Postman, no para la interfaz pública.

---

## 2. Cómo arrancar y cómo se configura

Punto de entrada: `EpicApplication.main`. Arranca Spring Boot y carga todo el paquete `com.epic`.

Configuración en `application.properties`:

- Puerto HTTP: **8080**.
- Base de datos: H2 en memoria (`jdbc:h2:mem:epicdb`), usuario `sa`, sin contraseña.
- Hibernate crea o actualiza tablas (`ddl-auto=update`).
- `data.sql` se ejecuta al arrancar (`defer-datasource-initialization=true` y `sql.init.mode=always`), así hay tres productos de ejemplo.
- Consola H2: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:epicdb`).
- SQL visible en la consola (`show-sql=true`).

Al apagar el servidor, **se pierde la base**, porque es en memoria.

Página web: `http://localhost:8080/` (archivo `static/index.html`).

---

## 3. Arquitectura por capas

El código Java se separa así:

| Capa | Qué hace |
|------|----------|
| Controlador (`*Controller`) | Recibe HTTP, valida el cuerpo con `@Valid` y llama al servicio. |
| Servicio (`*Service`) | Reglas de negocio: buscar producto, armar la cotización, persistir. |
| Repositorio (`*Repository`) | Acceso a base de datos (Spring Data JPA). |
| Entidad / formulario | Forma de los datos: tabla JPA o DTO de entrada. |
| `ApiExceptionHandler` | Convierte errores de validación y “producto no encontrado” en JSON con código HTTP. |

El frontend no usa un framework: `app.js` habla con `/api/products` y `/api/quotations` mediante `fetch`.

---

## 4. Modelo de datos

### Producto (`Product`, tabla `products`)

| Campo | Reglas |
|-------|--------|
| `id` | Long, autogenerado. |
| `name` | Obligatorio, máximo 120 caracteres. |
| `description` | Obligatoria, máximo 1000. |
| `category` | Obligatoria, máximo 80. |
| `basePrice` | Obligatorio, `BigDecimal`, no negativo, precisión 12 y 2 decimales. |

Constructores: vacío (JPA) y uno con nombre, descripción, categoría y precio.

### Solicitud de cotización (`QuotationRequest`, tabla `quotation_requests`)

| Campo | Reglas |
|-------|--------|
| `id` | Long, autogenerado. |
| `clientName` | Obligatorio, máximo 120. |
| `clientEmail` | Obligatorio, formato email, máximo 160. |
| `clientPhone` | Opcional, máximo 30. |
| `product` | Relación muchos-a-uno con `Product` (`product_id`, no nulo). Carga EAGER. |
| `quantity` | Obligatorio, mínimo 1. |
| `message` | Opcional, máximo 1000. |
| `requestedAt` | Fecha y hora de creación. |

`onCreate` (`@PrePersist`): si `requestedAt` viene vacío, se pone `LocalDateTime.now()` justo antes de insertar.

### Formulario de cotización (`QuotationRequestForm`)

No es una tabla. Es el JSON que llega al POST de cotizaciones: mismos datos de cliente, `productId` (Long obligatorio), `quantity` y `message`. El servicio busca el producto por ese id y arma la entidad `QuotationRequest`.

---

## 5. API HTTP

Base: `http://localhost:8080`.

### Productos — `ProductController` (`/api/products`)

**`POST /api/products`** — método Java: `register`

- Cuerpo: JSON de producto (sin `id`; el servidor lo genera).
- Validación Bean Validation.
- Respuesta: **201 Created** y el producto guardado (con `id`).
- Uso: formulario “Registrar producto” en la web.

**`GET /api/products`** — `listAll`

- Respuesta: **200** y lista de todos los productos.
- Uso: catálogo y opciones del `<select>` de cotización.

**`GET /api/products/{id}`** — `getById`

- Si existe: **200** y el producto.
- Si no: **404** `{"message":"Producto no encontrado con id: …"}`.
- Uso interno: el servicio de cotizaciones llama a `ProductService.findById` (no necesariamente este endpoint desde el HTML).

### Cotizaciones — `QuotationRequestController` (`/api/quotations`)

**`POST /api/quotations`** — `register`

- Cuerpo: JSON de `QuotationRequestForm`.
- Respuesta: **201** y la solicitud persistida, con el producto embebido y `requestedAt`.
- Si el `productId` no existe: **404**.
- Si fallan validaciones: **400** con mapa de campos.

**`GET /api/quotations`** — `listAll`

- Lista todas las solicitudes. No hay pantalla en el HTML actual.

**`GET /api/quotations/product/{productId}`** — `listByProduct`

- Primero comprueba que el producto exista (si no, 404).
- Luego devuelve las cotizaciones de ese producto (puede ser lista vacía).

---

## 6. Funciones Java, una por una

### `EpicApplication`

- **`main(String[] args)`**  
  Arranca el contexto Spring: web, JPA, validación, H2 y recursos estáticos.

### `ProductService`

- **Constructor `ProductService(ProductRepository)`**  
  Inyección del repositorio.

- **`register(Product product)`**  
  Guarda el producto. No asigna `id` a mano: lo hace la base.

- **`findAll()`**  
  Devuelve todos los productos.

- **`findById(Long id)`**  
  Busca por id. Si no hay resultado, lanza `ProductNotFoundException`.

### `ProductRepository`

Interfaz `JpaRepository<Product, Long>`. Hereda `save`, `findAll`, `findById`, etc. No hay consultas extra.

### `ProductNotFoundException`

Excepción de runtime. El mensaje es `"Producto no encontrado con id: " + id`.

### `QuotationRequestService`

- **Constructor**  
  Recibe repositorio de cotizaciones y `ProductService` (para no duplicar la búsqueda de producto).

- **`register(QuotationRequestForm form)`**  
  1. Carga el producto con `productService.findById` (falla si no existe).  
  2. Crea un `QuotationRequest` y copia nombre, correo, teléfono, producto, cantidad y mensaje.  
  3. Guarda. `requestedAt` lo rellena `onCreate` si hace falta.

- **`findAll()`**  
  Todas las cotizaciones.

- **`findByProductId(Long productId)`**  
  Llama a `findById` del producto (404 si no existe) y luego `quotationRequestRepository.findByProductId`.

### `QuotationRequestRepository`

Además de JPA estándar:

- **`findByProductId(Long productId)`**  
  Spring Data genera `WHERE product_id = ?`.

### `QuotationRequest.onCreate`

Si `requestedAt` es null, lo asigna al instante actual.

### `ApiExceptionHandler`

- **`handleValidation(MethodArgumentNotValidException)`**  
  **400**. Cuerpo: `message` = `"Error de validación"` y `errors` = mapa campo → mensaje (los de las anotaciones `@NotBlank`, `@Email`, etc.).

- **`handleProductNotFound(ProductNotFoundException)`**  
  **404**. Cuerpo: `{"message": "…"}`.

### Getters y setters

En `Product`, `QuotationRequest` y `QuotationRequestForm` solo leen o escriben campos. Jackson los usa para JSON; JPA para la base.

### Prueba `EpicApplicationTests.contextLoads`

Comprueba que el contexto Spring arranca. No prueba endpoints ni reglas de negocio.

---

## 7. Funciones del JavaScript (`static/js/app.js`)

Al cargar la página se toman referencias al listado, al select de producto, a los dos formularios y a los textos de estado. El formato de dinero es `es-MX` / `MXN`.

- **`fetchProducts()`**  
  `GET /api/products`. Si la respuesta no es OK, lanza error `"No se pudo cargar el catálogo"`. Si es OK, parsea JSON.

- **`renderProducts(products)`**  
  Si la lista está vacía: mensaje de catálogo vacío y select “Sin productos disponibles”.  
  Si hay datos: una fila por producto (nombre, categoría, id, descripción, precio) y opciones del select `id — precio`. El HTML de textos se escapa.

- **`escapeHtml(value)`**  
  Sustituye `& < > " '` para no inyectar HTML en nombres o descripciones.

- **`setStatus(element, message, type)`**  
  Escribe el mensaje de estado y aplica clase `ok` o `error` (o ninguna).

- **`loadCatalog()`**  
  Llama a `fetchProducts` + `renderProducts`. Si falla, muestra el error en el listado. Se ejecuta al final del script y otra vez después de registrar un producto.

- **Envío de `#quote-form`**  
  Evita recargar la página. Arma JSON (`clientPhone` y `message` vacíos pasan a `null`). `POST /api/quotations`. Éxito: limpia el formulario y “Solicitud enviada correctamente.” Error: muestra `message` del servidor o un texto genérico.

- **Envío de `#product-form`**  
  Igual patrón con `POST /api/products`. Éxito: limpia, avisa “Producto agregado al catálogo.” y recarga el catálogo.

---

## 8. Interfaz HTML (qué hay en pantalla)

`index.html`, idioma español:

- Cabecera fija: marca Epic y anclas a Catálogo, Cotizar y Registrar.
- Hero: título y botones hacia catálogo y cotización.
- `#catalogo`: contenedor `#product-list` (se rellena por JS).
- `#cotizar`: formulario de cotización.
- `#admin`: formulario de alta de producto (sin login; cualquier visitante puede usarlo).
- Pie con el nombre Epic.

Estilos en `css/styles.css`: tema oscuro, tipografías Cormorant Garamond y Manrope, listado en filas, formularios en rejilla, animaciones y diseño adaptable (una columna bajo 800px / 560px). No define “funciones” de negocio; solo presentación.

---

## 9. Datos de ejemplo (`data.sql`)

Al arrancar se insertan:

1. Laptop Pro 14 — Electrónica — 18999.00  
2. Silla ergonómica — Mobiliario — 3499.50  
3. Licencia Suite Oficina — Software — 2499.00  

---

## 10. Dependencias relevantes (`pom.xml`)

- Spring Boot 4.1.1, Java 21.
- Web MVC, Data JPA, Validation, H2, consola H2.
- Lombok está en el pom; las entidades actuales usan getters/setters a mano.
- Starters de test para web, JPA y validación.

---

## 11. Recorrido de un caso típico

1. El usuario abre `/`.
2. `loadCatalog()` pide `GET /api/products`.
3. `ProductController.listAll` → `ProductService.findAll` → `ProductRepository.findAll`.
4. El JS pinta filas y llena el select.
5. El usuario envía una cotización.
6. `POST /api/quotations` con el formulario.
7. Validación; si falla, `handleValidation` (400).
8. `register` del servicio busca el producto; si no está, 404.
9. Se guarda la fila en `quotation_requests` con fecha automática.
10. El navegador muestra éxito.

Alta de producto: `POST /api/products` → `save` → 201 → `loadCatalog()` otra vez.

---

## 12. Límites actuales (para no confundir lo que hay con lo que no hay)

- No hay autenticación ni roles; el “admin” es solo una sección de la misma página.
- No hay edición ni borrado de productos ni de cotizaciones.
- No hay cálculo de total (precio × cantidad) ni envío de correo.
- La lista de cotizaciones no se muestra en la web.
- La base H2 en memoria no sobrevive un reinicio.
- Un solo test: que el contexto cargue.

Este archivo es la descripción textual del comportamiento real del código en la rama `functions`, no una lista de tareas pendientes.
