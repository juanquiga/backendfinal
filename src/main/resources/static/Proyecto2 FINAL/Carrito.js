// =========================
// CONFIG
// =========================
const API_BASE = "https://TU-BACKEND.onrender.com/api"; // ← CAMBIA ESTO
let token = localStorage.getItem("token");

// =========================
// CARRITO LOCAL
// =========================
let carrito = JSON.parse(localStorage.getItem("carrito")) || [];

function guardarCarrito() {
  localStorage.setItem("carrito", JSON.stringify(carrito));
}

// =========================
// MOSTRAR CARRITO
// =========================
function mostrarCarrito() {
  const contenedor = document.getElementById("lista-carrito");
  contenedor.innerHTML = "";

  let total = 0;

  carrito.forEach((item, index) => {
    total += item.precio * item.cantidad;

    const div = document.createElement("div");
    div.classList.add("carrito-item");

    div.innerHTML = `
      <span>${item.producto}</span>

      <div class="cantidad-control">
        <button class="btn-restar" data-index="${index}">-</button>
        <input type="number" min="1" value="${item.cantidad}" data-index="${index}">
        <button class="btn-sumar" data-index="${index}">+</button>
      </div>

      <span>$${item.precio * item.cantidad}</span>
    `;

    contenedor.appendChild(div);
  });

  const totalDiv = document.createElement("h3");
  totalDiv.textContent = `Total: $${total}`;
  contenedor.appendChild(totalDiv);

  // Eventos dinámicos
  document.querySelectorAll(".btn-sumar").forEach(btn =>
    btn.addEventListener("click", (e) => {
      const idx = e.target.dataset.index;
      carrito[idx].cantidad++;
      guardarCarrito();
      mostrarCarrito();
    })
  );

  document.querySelectorAll(".btn-restar").forEach(btn =>
    btn.addEventListener("click", (e) => {
      const idx = e.target.dataset.index;
      if (carrito[idx].cantidad > 1) {
        carrito[idx].cantidad--;
      } else {
        carrito.splice(idx, 1);
      }
      guardarCarrito();
      mostrarCarrito();
    })
  );

  document.querySelectorAll(".cantidad-control input").forEach(input =>
    input.addEventListener("change", (e) => {
      const idx = e.target.dataset.index;
      let cant = parseInt(e.target.value);
      if (isNaN(cant) || cant < 1) cant = 1;
      carrito[idx].cantidad = cant;
      guardarCarrito();
      mostrarCarrito();
    })
  );
}

mostrarCarrito();

// =========================
// ENVIAR PEDIDO AL BACKEND
// =========================
document.getElementById("pedido-form").addEventListener("submit", async (e) => {
  e.preventDefault();

  if (!token) {
    alert("⚠️ Debes iniciar sesión para hacer un pedido");
    return;
  }

  const data = {
    nombreCliente: document.getElementById("nombre").value,
    telefono: document.getElementById("telefono").value,
    direccion: document.getElementById("direccion").value,
    itemsJson: JSON.stringify(carrito),
    total: carrito.reduce((acc, item) => acc + item.precio * item.cantidad, 0)
  };

  try {
    const res = await fetch(`${API_BASE}/pedidos`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify(data)
    });

    if (!res.ok) {
      throw new Error("Error creando pedido");
    }

    alert("✅ Pedido enviado correctamente");

    localStorage.removeItem("carrito");
    carrito = [];
    mostrarCarrito();

  } catch (err) {
    console.error(err);
    alert("❌ Error enviando el pedido");
  }
});