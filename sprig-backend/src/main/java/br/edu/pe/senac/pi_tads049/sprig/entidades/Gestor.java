package br.edu.pe.senac.pi_tads049.sprig.entidades;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "gestor")
public class Gestor extends Usuario {
	private String setorResponsavel;
    
    public Gestor() {
        super();
    }

    public Gestor(String nome, Long cpf, String regiaoAtuacao, String email, String senha) {
        super(nome, cpf, regiaoAtuacao, email, senha, Perfil.GESTOR);
    }
}