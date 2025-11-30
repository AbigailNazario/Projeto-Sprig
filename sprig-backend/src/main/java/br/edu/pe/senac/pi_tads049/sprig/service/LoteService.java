package br.edu.pe.senac.pi_tads049.sprig.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.pe.senac.pi_tads049.sprig.dto.LoteDTO;
import br.edu.pe.senac.pi_tads049.sprig.entidades.Armazem;
import br.edu.pe.senac.pi_tads049.sprig.entidades.Fornecedor;
import br.edu.pe.senac.pi_tads049.sprig.entidades.Lote;
import br.edu.pe.senac.pi_tads049.sprig.entidades.Status2;
import br.edu.pe.senac.pi_tads049.sprig.repositorios.ArmazemRepository;
import br.edu.pe.senac.pi_tads049.sprig.repositorios.FornecedorRepository;
import br.edu.pe.senac.pi_tads049.sprig.repositorios.LoteRepository;

@Service
public class LoteService {

    @Autowired
    private LoteRepository loteRepository;
    
    @Autowired
    private ArmazemRepository armazemRepository;
    
    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Transactional
    public Lote cadastrarLote(Lote lote) {
        lote.setStatus(Status2.Em_Estoque);
        // Generate QR code if not provided
        if (lote.getQrCode() == null || lote.getQrCode().isEmpty()) {
            lote.setQrCode(generateQRCode());
        }
        return loteRepository.save(lote);
    }
    
    @Transactional
    public LoteDTO criar(LoteDTO dto) {
        Lote lote = new Lote();
        lote.setNumeroLote(dto.getNumeroLote());
        lote.setEspecie(dto.getEspecie());
        lote.setQuantidade(dto.getQuantidade());
        
        // FIXED: Convert LocalDate to Instant properly
        if (dto.getValidade() != null) {
            // Use atStartOfDay() with ZoneId to convert LocalDate -> ZonedDateTime -> Instant
            lote.setValidade(dto.getValidade().atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        
        lote.setDataRecebimento(LocalDate.now());
        
        // FIXED: Handle status properly - set default if null
        if (dto.getStatus() != null && !dto.getStatus().isEmpty()) {
            lote.setStatus(Status2.valueOf(dto.getStatus()));
        } else {
            lote.setStatus(Status2.Em_Estoque); // Default status
        }
        
        // Generate QR code
        lote.setQrCode(generateQRCode());
        
        // Set armazem (Search by Name OR ID)
        if (dto.getArmazemNome() != null && !dto.getArmazemNome().isEmpty()) {
             Armazem armazem = armazemRepository.findByNome(dto.getArmazemNome())
                .orElseThrow(() -> new RuntimeException("Armazém com nome '" + dto.getArmazemNome() + "' não encontrado"));
             lote.setArmazem(armazem);
        } else if (dto.getArmazemId() != null) {
            Armazem armazem = armazemRepository.findById(dto.getArmazemId())
                .orElseThrow(() -> new RuntimeException("Armazém não encontrado"));
            lote.setArmazem(armazem);
        } else {
            throw new RuntimeException("Nome do Armazém é obrigatório");
        }
        
        if (dto.getFornecedorCnpj() != null) {
            Fornecedor fornecedor = fornecedorRepository.findByCnpj(dto.getFornecedorCnpj())
                .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado"));
            lote.setFornecedor(fornecedor);
        } else {
            throw new RuntimeException("Fornecedor é obrigatório");
        }
        
        Lote saved = loteRepository.save(lote);
        return converterParaDTO(saved);
    }
    
    @Transactional
    public LoteDTO atualizar(Integer id, LoteDTO dto) {
        Lote lote = loteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Lote não encontrado"));
            
        // Update simple fields
        if (dto.getNumeroLote() != null) lote.setNumeroLote(dto.getNumeroLote());
        if (dto.getEspecie() != null) lote.setEspecie(dto.getEspecie());
        if (dto.getQuantidade() != null) lote.setQuantidade(dto.getQuantidade());
        
        // Update date
        if (dto.getValidade() != null) {
            lote.setValidade(dto.getValidade().atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        
        // Update status
        if (dto.getStatus() != null && !dto.getStatus().isEmpty()) {
            lote.setStatus(Status2.valueOf(dto.getStatus()));
        }
        
        // Update Warehouse (Name or ID)
        if (dto.getArmazemNome() != null && !dto.getArmazemNome().isEmpty()) {
             Armazem armazem = armazemRepository.findByNome(dto.getArmazemNome())
                .orElseThrow(() -> new RuntimeException("Armazém com nome '" + dto.getArmazemNome() + "' não encontrado"));
             lote.setArmazem(armazem);
        } else if (dto.getArmazemId() != null) {
            Armazem armazem = armazemRepository.findById(dto.getArmazemId())
                .orElseThrow(() -> new RuntimeException("Armazém não encontrado"));
            lote.setArmazem(armazem);
        }
        
        // Update Provider
        if (dto.getFornecedorCnpj() != null) {
            Fornecedor fornecedor = fornecedorRepository.findByCnpj(dto.getFornecedorCnpj())
                .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado"));
            lote.setFornecedor(fornecedor);
        }
        
        Lote saved = loteRepository.save(lote);
        return converterParaDTO(saved);
    }

    public List<Lote> listarTodos() {
        return loteRepository.findAll();
    }
    
    public List<LoteDTO> listarTodosDTO() {
        return loteRepository.findAll().stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public Optional<Lote> buscarPorId(Integer id) {
        return loteRepository.findById(id);
    }
    
    public List<LoteDTO> buscarPorArmazem(Integer armazemId) {
        return loteRepository.findAll().stream()
                .filter(lote -> lote.getArmazem() != null && lote.getArmazem().getIdArmazem() == armazemId)
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    public List<Lote> listarLotesDisponiveis() {
        return loteRepository.findByStatusOrderByValidadeAsc(Status2.Em_Estoque);
    }

    public List<Lote> listarLotesProximosVencimento(int diasAntecedencia) {
        LocalDate dataLimite = LocalDate.now().plusDays(diasAntecedencia);
        return loteRepository.findLotesProximosVencimento(dataLimite, Status2.Em_Estoque);
    }

    @Transactional
    public Lote atualizarStatus(Integer id, Status2 novoStatus) {
        Lote lote = loteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Lote não encontrado"));
        lote.setStatus(novoStatus);
        return loteRepository.save(lote);
    }

    @Transactional
    public Lote atualizarQuantidade(Integer id, int novaQuantidade) {
        Lote lote = loteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Lote não encontrado"));
        
        if (novaQuantidade < 0) {
            throw new RuntimeException("Quantidade não pode ser negativa");
        }
        
        lote.setQuantidade(novaQuantidade);
        return loteRepository.save(lote);
    }
    
    @Transactional
    public void atualizarQuantidade(Integer id, Integer novaQuantidade) {
        Lote lote = loteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Lote não encontrado"));
        
        if (novaQuantidade < 0) {
            throw new RuntimeException("Quantidade não pode ser negativa");
        }
        
        lote.setQuantidade(novaQuantidade);
        loteRepository.save(lote);
    }

    @Transactional
    public void deletarLote(Integer id) {
        if (!loteRepository.existsById(id)) {
            throw new RuntimeException("Lote não encontrado");
        }
        loteRepository.deleteById(id);
    }
    
    private String generateQRCode() {
        // Generate a simple QR code identifier
        // In a real implementation, you would use a QR code library
        return "QR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private LoteDTO converterParaDTO(Lote lote) {
        LoteDTO dto = new LoteDTO();
        dto.setId(lote.getIdLote());
        dto.setNumeroLote(lote.getNumeroLote());
        dto.setEspecie(lote.getEspecie());
        dto.setQuantidade(lote.getQuantidade());
        
        // Convert Instant to LocalDate for frontend
        if (lote.getValidade() != null) {
            dto.setValidade(LocalDate.ofInstant(lote.getValidade(), ZoneId.systemDefault()));
        }
        
        dto.setDataRecebimento(lote.getDataRecebimento());
        dto.setStatus(lote.getStatus().toString());
        dto.setQrCode(lote.getQrCode());
        
        if (lote.getArmazem() != null) {
            dto.setArmazemId(lote.getArmazem().getIdArmazem());
            dto.setArmazemNome(lote.getArmazem().getNome());
            dto.setArmazemCapacidade(lote.getArmazem().getCapacidadeTotal());
        }
        
        if (lote.getFornecedor() != null) {
            dto.setFornecedorCnpj(lote.getFornecedor().getCnpj());
            dto.setFornecedorNome(lote.getFornecedor().getNome());
        }
        
        return dto;
    }
}