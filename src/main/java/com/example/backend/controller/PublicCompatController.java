package com.example.backend.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.backend.dto.PedidoDTO;
import com.example.backend.dto.ProductoDTO;
import com.example.backend.service.PedidoService;
import com.example.backend.service.ProductoService;

@RestController
@RequestMapping("/api/public")
public class PublicCompatController {

    private final ProductoService productoService;
    private final RestTemplate restTemplate;
    private final PedidoService pedidoService;

    @Value("${app.google.script.url:https://script.googleusercontent.com/macros/echo?user_content_key=AehSKLhlW_UJZ2i2DGgSkW64K0KiaSOqUWEAtQDFTXhBtgATs0Kbye1S3u3RtbyUDJAVRw-FgN_DCBlmeXNE6fDI3pzZ5zDhg037ku__H_Hu0JRDEeunLgUYbI79m5uzpwYEpb3Sl3QRudRC4NOouRGh1jFyGGxT9Cd4yFjx0-Ps8i-MjhF-a0kuRCsadoe2quai4ho405GMT5NG7bwQTH71ycGthF--6-My7lvlv5ayPC1Fo2IMVXuM-MrbAnpF5t-CzGKqbOSKar2svztWyloEaseGWTgYMJQKDxJnGSdr&lib=M1SZ6R3DgFK_8gLlGeqwInnfCTEkP8scq}")
    private String googleScriptUrl;

    @Value("${app.google.pedidos.url:https://script.googleusercontent.com/macros/echo?user_content_key=AehSKLgHslYiPVFOAx0Csb_Ouk-h7jtl0W0xDYLrSvykLxWYNjp2naDfSd6rQ0R4QZQdoHJZzljNiarBLrNyOk7xoImCcczIlpD6aZ11IKrAY5E0LKttmGZFe-m4QhutuXHOI70bVa5IAvePoDrysiTvKHYYAociVfYu26ZJCla0AqlPDFX_1gCE3r6ZSBYrmNV6Vc5GDIm_i7XTF9cNz0KbHkxAKUBbvNTNfaFo2bXGygp4FCAcxf3e8Jp_heVICnbrDG8YLDHg09HF7rKR3Yp4wys3VCBR1qqDUa8gWA58&lib=M1SZ6R3DgFK_8gLlGeqwInnfCTEkP8scq}")
    private String googlePedidosUrl;

    public PublicCompatController(ProductoService productoService, RestTemplate restTemplate, PedidoService pedidoService) {
        this.productoService = productoService;
        this.restTemplate = restTemplate;
        this.pedidoService = pedidoService;
    }

    @GetMapping("/menu")
    public ResponseEntity<Map<String, Object>> getMenuLikeGoogleScript() {
        // 1) Try to fetch and proxy the Google Script response directly
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> scriptBody = restTemplate.getForObject(googleScriptUrl, Map.class);
            if (scriptBody != null && scriptBody.containsKey("data")) {
                return ResponseEntity.ok(scriptBody);
            }
        } catch (Exception ignored) {
            // fall back to local DB if remote script is not reachable
        }

        // 2) Fallback: build a compatible { data: [...] } from local DB
        List<ProductoDTO> productos = productoService.obtenerTodos();
        List<Map<String, Object>> data = new ArrayList<>();
        for (ProductoDTO p : productos) {
            Map<String, Object> item = new HashMap<>();
            item.put("Nombre ", p.getNombre());
            item.put("Precio ", p.getPrecio());
            item.put("Descripcion", p.getDescripcion() == null ? "" : p.getDescripcion());
            item.put("imagen", p.getImagenUrl() == null || p.getImagenUrl().isBlank() ? "placeholder.jpg" : p.getImagenUrl());
            data.add(item);
        }
        Map<String, Object> body = new HashMap<>();
        body.put("data", data);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/echo")
    public ResponseEntity<Map<String, Object>> echo(@RequestBody(required = false) Map<String, Object> payload) {
        Map<String, Object> body = new HashMap<>();
        body.put("ok", true);
        body.put("data", payload == null ? new HashMap<>() : payload);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> publicLogin(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Map<String, Object> response = new HashMap<>();
        
        if (username == null || password == null) {
            response.put("ok", false);
            response.put("error", "Username and password required");
            return ResponseEntity.status(400).body(response);
        }

        try {
            var userOpt = productoService.getUsuarioRepository().findByUsername(username);
            if (userOpt.isEmpty()) {
                response.put("ok", false);
                response.put("error", "Invalid credentials");
                return ResponseEntity.status(401).body(response);
            }

            var user = userOpt.get();
            if (!productoService.getPasswordEncoder().matches(password, user.getPassword())) {
                response.put("ok", false);
                response.put("error", "Invalid credentials");
                return ResponseEntity.status(401).body(response);
            }

            String token = productoService.getJwtUtils().generateToken(user.getUsername());
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("username", user.getUsername());
            userData.put("role", user.getRole());
            userData.put("token", token);

            response.put("ok", true);
            response.put("data", userData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("ok", false);
            response.put("error", "Server error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> publicRegister(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Map<String, Object> response = new HashMap<>();
        
        if (username == null || password == null) {
            response.put("ok", false);
            response.put("error", "Username and password required");
            return ResponseEntity.status(400).body(response);
        }

        try {
            if (productoService.getUsuarioRepository().existsByUsername(username)) {
                response.put("ok", false);
                response.put("error", "Username already exists");
                return ResponseEntity.status(400).body(response);
            }

            var newUser = new com.example.backend.model.Usuario(
                username, 
                productoService.getPasswordEncoder().encode(password), 
                "ROLE_USER"
            );
            productoService.getUsuarioRepository().save(newUser);

            String token = productoService.getJwtUtils().generateToken(newUser.getUsername());
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("username", newUser.getUsername());
            userData.put("role", newUser.getRole());
            userData.put("token", token);

            response.put("ok", true);
            response.put("message", "User registered successfully");
            response.put("data", userData);
            return ResponseEntity.status(201).body(response);
        } catch (Exception e) {
            response.put("ok", false);
            response.put("error", "Server error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // --- Pedidos: acepta POST desde formulario (x-www-form-urlencoded) ---
    @PostMapping(value = "/pedidos", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, Object>> submitPedidoForm(
            @RequestParam(name = "nombre") String nombre,
            @RequestParam(name = "telefono") String telefono,
            @RequestParam(name = "direccion") String direccion,
            @RequestParam(name = "items") String itemsJson,
            @RequestParam(name = "total") Integer total
    ) {
        return crearPedidoYResponder(nombre, telefono, direccion, itemsJson, total);
    }

    // --- Pedidos: acepta POST JSON ---
    @PostMapping(value = "/pedidos", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> submitPedidoJson(@RequestBody Map<String, Object> payload) {
        String nombre = asString(payload.get("nombre"));
        String telefono = asString(payload.get("telefono"));
        String direccion = asString(payload.get("direccion"));
        String itemsJson = asString(payload.get("items"));
        Integer total = asInteger(payload.get("total"));
        return crearPedidoYResponder(nombre, telefono, direccion, itemsJson, total);
    }

    // --- Pedidos: GET proxy a Google Script (retorna { pedidos: [...] }) ---
    @GetMapping("/pedidos")
    public ResponseEntity<Map<String, Object>> getPedidosPublic() {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> scriptBody = restTemplate.getForObject(googlePedidosUrl, Map.class);
            if (scriptBody != null) {
                return ResponseEntity.ok(scriptBody);
            }
        } catch (Exception e) {
            // fallback vacío si el script no está disponible
        }
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("pedidos", List.of());
        return ResponseEntity.ok(fallback);
    }

    private ResponseEntity<Map<String, Object>> crearPedidoYResponder(String nombre, String telefono, String direccion, String itemsJson, Integer total) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (nombre == null || telefono == null || direccion == null || itemsJson == null || total == null) {
                response.put("ok", false);
                response.put("error", "Faltan campos requeridos");
                return ResponseEntity.badRequest().body(response);
            }

            // 1. Guardar en BD local
            PedidoDTO dto = new PedidoDTO();
            dto.setNombreCliente(nombre);
            dto.setTelefono(telefono);
            dto.setDireccion(direccion);
            dto.setItemsJson(itemsJson);
            dto.setTotal(total);

            PedidoDTO creado = pedidoService.crear(dto);

            // 2. Intentar enviar también al Google Script (dual-write)
            try {
                enviarPedidoAGoogleScript(nombre, telefono, direccion, itemsJson, total);
            } catch (Exception e) {
                // Log error pero no fallar - el pedido ya está guardado localmente
                System.err.println("Warning: No se pudo enviar pedido a Google Script: " + e.getMessage());
            }

            Map<String, Object> data = new HashMap<>();
            data.put("id", creado.getId());
            data.put("estado", creado.getEstado());
            data.put("total", creado.getTotal());

            response.put("ok", true);
            response.put("message", "Pedido creado exitosamente");
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("ok", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private void enviarPedidoAGoogleScript(String nombre, String telefono, String direccion, String items, Integer total) {
        // Construir URL con parámetros para Google Script POST
        // Google Apps Script espera parámetros URL-encoded
        String url = googlePedidosUrl.split("\\?")[0]; // Base URL sin query params
        
        // Crear mapa de datos para enviar como form-urlencoded
        org.springframework.util.MultiValueMap<String, String> formData = 
            new org.springframework.util.LinkedMultiValueMap<>();
        formData.add("nombre", nombre);
        formData.add("telefono", telefono);
        formData.add("direccion", direccion);
        formData.add("items", items);
        formData.add("total", String.valueOf(total));

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

        org.springframework.http.HttpEntity<org.springframework.util.MultiValueMap<String, String>> request = 
            new org.springframework.http.HttpEntity<>(formData, headers);

        // POST al Google Script
        restTemplate.postForEntity(url, request, String.class);
    }

    private String asString(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private Integer asInteger(Object o) {
        try {
            if (o == null) return null;
            if (o instanceof Number n) return n.intValue();
            return Integer.parseInt(String.valueOf(o));
        } catch (Exception ex) {
            return null;
        }
    }
}
