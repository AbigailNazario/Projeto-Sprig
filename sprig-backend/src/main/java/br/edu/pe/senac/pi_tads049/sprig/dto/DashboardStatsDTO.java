package br.edu.pe.senac.pi_tads049.sprig.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private String tempoMedioEntrega;
    private Long entregasConcluidas;
    private Long entregasEmAndamento;
    private String custoPorKm;
}