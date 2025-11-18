package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {}