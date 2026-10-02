const productList = document.getElementById("product-list");
const quoteProduct = document.getElementById("quote-product");
const quoteForm = document.getElementById("quote-form");
const productForm = document.getElementById("product-form");
const quoteStatus = document.getElementById("quote-status");
const productStatus = document.getElementById("product-status");

const money = new Intl.NumberFormat("es-MX", {
  style: "currency",
  currency: "MXN",
});

async function fetchProducts() {
  const response = await fetch("/api/products");
  if (!response.ok) {
    throw new Error("No se pudo cargar el catálogo");
  }
  return response.json();
}

function renderProducts(products) {
  if (!products.length) {
    productList.innerHTML = '<p class="muted">Aún no hay productos en el catálogo.</p>';
    quoteProduct.innerHTML = '<option value="">Sin productos disponibles</option>';
    return;
  }

  productList.innerHTML = products
    .map(
      (product, index) => `
      <article class="product-row" style="animation-delay: ${index * 0.05}s">
        <div>
          <h3>${escapeHtml(product.name)}</h3>
          <div class="meta">
            <span class="tag">${escapeHtml(product.category)}</span>
            <span>ID ${product.id}</span>
          </div>
        </div>
        <p>${escapeHtml(product.description)}</p>
        <div class="price">${money.format(product.basePrice)}</div>
      </article>
    `
    )
    .join("");

  quoteProduct.innerHTML =
    '<option value="">Selecciona un producto</option>' +
    products
      .map(
        (product) =>
          `<option value="${product.id}">${escapeHtml(product.name)} — ${money.format(product.basePrice)}</option>`
      )
      .join("");
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function setStatus(element, message, type) {
  element.textContent = message;
  element.className = `form-status ${type || ""}`.trim();
}

async function loadCatalog() {
  try {
    const products = await fetchProducts();
    renderProducts(products);
  } catch (error) {
    productList.innerHTML = `<p class="form-status error">${escapeHtml(error.message)}</p>`;
  }
}

quoteForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  setStatus(quoteStatus, "Enviando…");

  const form = new FormData(quoteForm);
  const payload = {
    clientName: form.get("clientName").toString().trim(),
    clientEmail: form.get("clientEmail").toString().trim(),
    clientPhone: form.get("clientPhone").toString().trim() || null,
    productId: Number(form.get("productId")),
    quantity: Number(form.get("quantity")),
    message: form.get("message").toString().trim() || null,
  };

  try {
    const response = await fetch("/api/quotations", {
      method: "POST",
      headers: { "Content-Type": "application/json; charset=utf-8" },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || "No se pudo registrar la cotización");
    }

    quoteForm.reset();
    setStatus(quoteStatus, "Solicitud enviada correctamente.", "ok");
  } catch (error) {
    setStatus(quoteStatus, error.message, "error");
  }
});

productForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  setStatus(productStatus, "Guardando…");

  const form = new FormData(productForm);
  const payload = {
    name: form.get("name").toString().trim(),
    category: form.get("category").toString().trim(),
    description: form.get("description").toString().trim(),
    basePrice: Number(form.get("basePrice")),
  };

  try {
    const response = await fetch("/api/products", {
      method: "POST",
      headers: { "Content-Type": "application/json; charset=utf-8" },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || "No se pudo registrar el producto");
    }

    productForm.reset();
    setStatus(productStatus, "Producto agregado al catálogo.", "ok");
    await loadCatalog();
  } catch (error) {
    setStatus(productStatus, error.message, "error");
  }
});

loadCatalog();
