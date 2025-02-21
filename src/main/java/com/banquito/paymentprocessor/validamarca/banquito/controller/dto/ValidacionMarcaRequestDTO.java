package com.banquito.paymentprocessor.validamarca.banquito.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ValidacionMarcaRequestDTO {
    
    @NotBlank(message = "El número de tarjeta es requerido")
    @Size(min = 16, max = 16, message = "El número de tarjeta debe tener 16 dígitos")
    @Pattern(regexp = "^[0-9]{16}$", message = "El número de tarjeta debe contener solo números")
    private String numeroTarjeta;

    @NotBlank(message = "La marca es requerida")
    @Size(max = 3, message = "La marca no puede exceder los 3 caracteres")
    private String marca;

    @NotBlank(message = "El CVV es requerido")
    @Size(min = 3, max = 4, message = "El CVV debe tener entre 3 y 4 dígitos")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "El CVV debe contener solo números")
    private String cvv;

    @NotBlank(message = "La fecha de caducidad es requerida")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "La fecha de caducidad debe tener el formato MM/YY")
    private String fechaCaducidad;
} 