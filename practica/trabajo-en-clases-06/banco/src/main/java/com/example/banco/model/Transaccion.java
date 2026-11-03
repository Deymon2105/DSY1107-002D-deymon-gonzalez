package com.example.banco.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaccion {

    private String usuario;
    private Double monto;
    private String origen; // "cajero", "web", "app"
    private Long timestamp;
}
