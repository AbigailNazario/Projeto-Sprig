package br.edu.pe.senac.pi_tads049.sprig.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

import br.edu.pe.senac.pi_tads049.sprig.entidades.Perfil;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoteDTO {
    private Integer id;
    private Integer numeroLote;
    private String especie;
    private Integer quantidade;
    private LocalDate validade;
    private LocalDate dataRecebimento;
    private String status;
    private String qrCode;
    
    // Informações do Armazém
    private Integer armazemId;
    private String armazemNome;
    private Integer armazemCapacidade;
    
    // Informações do Fornecedor
    private Integer idFornecedor;
    private String fornecedorCnpj;
    private String fornecedorNome;
}