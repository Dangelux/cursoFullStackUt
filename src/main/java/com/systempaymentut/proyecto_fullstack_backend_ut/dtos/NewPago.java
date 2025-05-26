package com.systempaymentut.proyecto_fullstack_backend_ut.dtos;

import java.time.LocalDate;

import com.systempaymentut.proyecto_fullstack_backend_ut.enums.TypePago;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewPago {
    
    private double cantidad;
    private TypePago typePago;
    private LocalDate date;

    private String codigoEstudiante;
}
