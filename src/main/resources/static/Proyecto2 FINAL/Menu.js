// =========================
// CONFIG
// =========================
const API_BASE = "https://TU-BACKEND.onrender.com/api"; // ← CAMBIA ESTO

let carrito = JSON.parse(localStorage.getItem("carrito")) || [];

// =========================
// CARGAR PRODUCTOS DESDE BACKEND
// =========================
async function cargarProductos() {
  try {
    const res = await fetch(`${API_BASE}/productos`);
    const productos = await res.json();

    const contenedor = document.getElementById("productos");
    contenedor.innerHTML = "";

    productos.forEach(prod => {
      const card = document.createElement("div");
      card.classList.add("card");

      card.innerHTML = `
        <h3>${prod.nombre}</h3>
        <img src="${prod.imagenUrl}" alt="${prod.nombre}">
        <p>${prod.descripcion}</p>
        <p><strong>$${prod.precio} COP</strong></p>

        <button class="add-to-cart"
          data-product="${prod.nombre}"
          data-price="${prod.precio}">
          Agregar al Carrito
        </button>
      `;

      contenedor.appendChild(card);
    });

    asignarEventosCarrito();

  } catch (e) {
    console.error("Error cargando productos:", e);
  }
}

// =========================
// AÑADIR AL CARRITO
// =========================
function asignarEventosCarrito() {
  document.querySelectorAll(".add-to-cart").forEach(btn => {
    btn.addEventListener("click", () => {
      const producto = btn.dataset.product;
      const precio = parseInt(btn.dataset.price);

      const item = carrito.find(i => i.producto === producto);

      if (item) {
        item.cantidad++;
      } else {
        carrito.push({ producto, precio, cantidad: 1 });
      }

      localStorage.setItem("carrito", JSON.stringify(carrito));
      alert(`${producto} añadido al carrito ✔️`);
    });
  });
}

// =========================
// INICIO
// =========================
cargarProductos();