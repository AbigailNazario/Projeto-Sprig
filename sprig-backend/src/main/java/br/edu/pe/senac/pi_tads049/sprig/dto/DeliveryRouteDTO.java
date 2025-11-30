package br.edu.pe.senac.pi_tads049.sprig.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para rotas de entrega compatível com o frontend
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryRouteDTO {
    private String id;
    private String status; // "entregue", "em_rota", "atraso", "armazem"
    private String location;
    private CoordinatesDTO coordinates;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoordinatesDTO {
        private Double lat;
        private Double lng;
    }
}