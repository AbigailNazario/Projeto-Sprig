package br.edu.pe.senac.pi_tads049.sprig.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.edu.pe.senac.pi_tads049.sprig.dto.DashboardDTO;
import br.edu.pe.senac.pi_tads049.sprig.dto.DashboardStatsDTO;
import br.edu.pe.senac.pi_tads049.sprig.dto.DeliveryRouteDTO;
import br.edu.pe.senac.pi_tads049.sprig.entidades.Agricultor;
import br.edu.pe.senac.pi_tads049.sprig.entidades.Entregas;
import br.edu.pe.senac.pi_tads049.sprig.repositorios.AgricultorRepository;
import br.edu.pe.senac.pi_tads049.sprig.repositorios.EntregasRepository;
import br.edu.pe.senac.pi_tads049.sprig.repositorios.EstoqueRepository;
import br.edu.pe.senac.pi_tads049.sprig.repositorios.LoteRepository;
import br.edu.pe.senac.pi_tads049.sprig.repositorios.MotoristaRepository;
import br.edu.pe.senac.pi_tads049.sprig.repositorios.RotaRepository;
import br.edu.pe.senac.pi_tads049.sprig.repositorios.VeiculoRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service para cálculo de métricas do Dashboard
 * Agrega dados de múltiplos repositórios para fornecer visão geral do sistema
 */
@Service
public class DashboardService {
    
    @Autowired 
    private EntregasRepository entregaRepo;
    
    @Autowired 
    private LoteRepository loteRepo;
    
    @Autowired 
    private RotaRepository rotaRepo;
    
    @Autowired
    private EstoqueRepository estoqueRepo;
    
    @Autowired
    private VeiculoRepository veiculoRepo;
    
    @Autowired
    private MotoristaRepository motoristaRepo;
    
    @Autowired
    private AgricultorRepository agricultorRepo;

    /**
     * Calcula todas as métricas do dashboard
     * @return DashboardDTO com todas as métricas calculadas
     */
    public DashboardDTO getDashboardMetrics() {
        DashboardDTO dto = new DashboardDTO();
        
        // Métricas de Lotes
        dto.setTotalLotes(loteRepo.countLotes());
        
        // Métricas de Entregas
        long entregasConcluidas = entregaRepo.countEntregasConcluidas();
        long entregasEmRota = entregaRepo.countEntregasEmRota();
        long entregasPendentes = entregaRepo.countEntregasPendentes();
        
        dto.setEntregasConcluidas(entregasConcluidas);
        dto.setEntregasEmRota(entregasEmRota);
        dto.setEntregasPendentes(entregasPendentes);
        
        // Calcula percentual de entregas concluídas
        long totalEntregas = entregasConcluidas + entregasEmRota + entregasPendentes;
        double pctEntregue = totalEntregas > 0 
            ? (double) entregasConcluidas / totalEntregas * 100.0
            : 0.0;
        dto.setPercentualEntregue(Math.round(pctEntregue * 100.0) / 100.0); // 2 casas decimais
        
        // Métricas de Tempo
        Double tempoMedio = entregaRepo.avgTempoEntregaHoras();
        dto.setTempoMedioEntregaHoras(tempoMedio != null ? tempoMedio : 0.0);
        
        // Métricas de Volume
        Long volume = entregaRepo.sumQuantidadeEntregue();
        dto.setVolumeTotalEntregue(volume != null ? volume : 0L);
        
        // Métricas de Custo e Distância
        Double custoTotal = rotaRepo.sumCustoEstimado();
        Double distanciaTotal = rotaRepo.sumDistanciaTotal();
        
        dto.setCustoTotalEstimado(custoTotal != null ? custoTotal : 0.0);
        dto.setDistanciaTotalPercorrida(distanciaTotal != null ? distanciaTotal : 0.0);
        
        // Calcula custo por km
        Double custoPorKm = (custoTotal != null && distanciaTotal != null && distanciaTotal > 0)
            ? custoTotal / distanciaTotal
            : 0.0;
        dto.setCustoPorKm(Math.round(custoPorKm * 100.0) / 100.0); // 2 casas decimais
        
        // Métricas de Estoque
        dto.setEstoquesAbaixoDoMinimo((long) estoqueRepo.findEstoquesAbaixoDoMinimo().size());
        dto.setEstoquesAcimaDoMaximo((long) estoqueRepo.findEstoquesAcimaDoMaximo().size());
        
        // Métricas de Veículos e Motoristas
        dto.setTotalVeiculos((long) veiculoRepo.findAll().size());
        dto.setTotalMotoristas((long) motoristaRepo.findAll().size());
        
        // Veículos em rota = entregas em rota (assumindo 1 veículo por entrega)
        dto.setVeiculosEmRota(entregasEmRota);
        
        return dto;
    }
    
    /**
     * Retorna estatísticas formatadas para o frontend
     * @return DashboardStatsDTO com dados formatados
     */
    public DashboardStatsDTO getDashboardStats() {
        DashboardDTO metrics = getDashboardMetrics();
        
        DashboardStatsDTO stats = new DashboardStatsDTO();
        
        // Formata tempo médio de entrega (converte horas para formato "Xh Ymin")
        Double tempoHoras = metrics.getTempoMedioEntregaHoras();
        int horas = tempoHoras.intValue();
        int minutos = (int) ((tempoHoras - horas) * 60);
        stats.setTempoMedioEntrega(String.format("%dh %dmin", horas, minutos));
        
        // Entregas concluídas
        stats.setEntregasConcluidas(metrics.getEntregasConcluidas());
        
        // Entregas em andamento (em rota + pendentes)
        stats.setEntregasEmAndamento(metrics.getEntregasEmRota() + metrics.getEntregasPendentes());
        
        // Formata custo por km
        stats.setCustoPorKm(String.format("R$ %.2f", metrics.getCustoPorKm()));
        
        return stats;
    }
    
    /**
     * Retorna rotas de entrega para exibição no mapa
     * @return Lista de DeliveryRouteDTO
     */
    public List<DeliveryRouteDTO> getDeliveryRoutes() {
        List<DeliveryRouteDTO> routes = new ArrayList<>();
        
        // Busca todas as entregas
        List<Entregas> entregas = entregaRepo.findAll();
        
        for (Entregas entrega : entregas) {
            if (entrega.getDestino() != null) {
                DeliveryRouteDTO route = new DeliveryRouteDTO();
                
                // ID da entrega
                route.setId(Integer.toString(entrega.getIdEntregas()));
                
                // Status da entrega (converte para formato do frontend)
                // Verifica se o status não é nulo antes de converter
                String statusStr = entrega.getStatus() != null ? entrega.getStatus().toString() : "Pendente";
                String status = convertStatus(statusStr);
                route.setStatus(status);
                
                // Localização (nome do destino ou endereço)
                // Garante que location não seja nula
                String location = "Destino " + entrega.getIdEntregas();
                if (entrega.getDestino() != null && entrega.getDestino().getNomeDestino() != null) {
                    location = entrega.getDestino().getNomeDestino();
                }
                route.setLocation(location);
                
                // Coordenadas (usa coordenadas do destino se disponíveis, senão usa coordenadas padrão de Recife)
                DeliveryRouteDTO.CoordinatesDTO coords = new DeliveryRouteDTO.CoordinatesDTO();

                if (entrega.getDestino().getLatitude() != null && entrega.getDestino().getLongitude() != null) {
                    coords.setLat(entrega.getDestino().getLatitude().doubleValue());
                    coords.setLng(entrega.getDestino().getLongitude().doubleValue());
                } else {
                    // Coordenadas padrão
                    coords.setLat(-8.0476 + (Math.random() * 0.1 - 0.05));
                    coords.setLng(-34.877  + (Math.random() * 0.1 - 0.05));
                }

                route.setCoordinates(coords);
            }
        }
        
        // Se não houver entregas, retorna dados de exemplo
        if (routes.isEmpty()) {
            routes.add(createExampleRoute("1", "entregue", "Armazém Central", -8.0476, -34.877));
            routes.add(createExampleRoute("2", "em_rota", "Zona Norte", -8.0276, -34.897));
            routes.add(createExampleRoute("3", "em_rota", "Zona Sul", -8.0676, -34.857));
            routes.add(createExampleRoute("4", "atraso", "Zona Oeste", -8.0576, -34.907));
        }
        
        return routes;
    }
    
    /**
     * Converte status do backend para formato do frontend
     */
    private String convertStatus(String backendStatus) {
        if (backendStatus == null) return "armazem";
        
        switch (backendStatus.toLowerCase()) {
            case "entregue":
                return "entregue";
            case "em_rota":
                return "em_rota";
            case "pendente":
                return "armazem";
            case "atrasado":
                return "atraso";
            default:
                return "armazem";
        }
    }
    
    /**
     * Cria uma rota de exemplo
     */
    private DeliveryRouteDTO createExampleRoute(String id, String status, String location, double lat, double lng) {
        DeliveryRouteDTO route = new DeliveryRouteDTO();
        route.setId(id);
        route.setStatus(status);
        route.setLocation(location);
        
        DeliveryRouteDTO.CoordinatesDTO coords = new DeliveryRouteDTO.CoordinatesDTO();
        coords.setLat(lat);
        coords.setLng(lng);
        route.setCoordinates(coords);
        
        return route;
    }
    
    /**
     * Retorna métricas filtradas para um agricultor específico
     * Mostra apenas informações relevantes para o agricultor (suas entregas)
     * @param emailAgricultor Email do agricultor para filtrar
     * @return DashboardDTO com métricas filtradas
     */
    public DashboardDTO getDashboardMetricsAgricultor(String emailAgricultor) {
        DashboardDTO dto = new DashboardDTO();
        
        // Busca o agricultor pelo email
        Agricultor agricultor = agricultorRepo.findByEmail(emailAgricultor)
            .orElseThrow(() -> new RuntimeException("Agricultor não encontrado com email: " + emailAgricultor));
        
        // Busca todas as entregas
        List<Entregas> todasEntregas = entregaRepo.findAll();
        
        // Filtra entregas do agricultor (destinos que pertencem ao agricultor)
        List<Entregas> entregasAgricultor = todasEntregas.stream()
            .filter(e -> e.getDestino() != null && 
                        e.getDestino().getAgricultor() != null &&
                        e.getDestino().getAgricultor().getCpf() == agricultor.getCpf())
            .collect(Collectors.toList());
        
        // Calcula métricas específicas do agricultor
        long entregasConcluidas = entregasAgricultor.stream()
            .filter(e -> "Entregue".equals(e.getStatus().toString()))
            .count();
            
        long entregasEmRota = entregasAgricultor.stream()
            .filter(e -> "Em_rota".equals(e.getStatus().toString()))
            .count();
            
        long entregasPendentes = entregasAgricultor.stream()
            .filter(e -> "Pendente".equals(e.getStatus().toString()))
            .count();
        
        dto.setEntregasConcluidas(entregasConcluidas);
        dto.setEntregasEmRota(entregasEmRota);
        dto.setEntregasPendentes(entregasPendentes);
        
        // Calcula percentual de entregas concluídas
        long totalEntregas = entregasConcluidas + entregasEmRota + entregasPendentes;
        double pctEntregue = totalEntregas > 0 
            ? (double) entregasConcluidas / totalEntregas * 100.0
            : 0.0;
        dto.setPercentualEntregue(Math.round(pctEntregue * 100.0) / 100.0);
        
        // Volume total recebido pelo agricultor
        long volumeTotal = entregasAgricultor.stream()
            .mapToLong(Entregas::getQuantidadeEntregue)
            .sum();
        dto.setVolumeTotalEntregue(volumeTotal);
        
        // Tempo médio de entrega para o agricultor
        double tempoMedio = entregasAgricultor.stream()
            .filter(e -> e.getDataEntrega() != null && e.getDataPrevista() != null)
            .mapToLong(e -> java.time.temporal.ChronoUnit.HOURS.between(
                e.getDataPrevista().atStartOfDay(), 
                e.getDataEntrega().atStartOfDay()))
            .average()
            .orElse(0.0);
        dto.setTempoMedioEntregaHoras(Math.round(tempoMedio * 100.0) / 100.0);
        
        // Total de lotes recebidos
        long totalLotes = entregasAgricultor.stream()
            .filter(e -> e.getLote() != null)
            .map(e -> e.getLote().getIdLote())
            .distinct()
            .count();
        dto.setTotalLotes(totalLotes);
        
        // Zera métricas não relevantes para agricultor
        dto.setCustoTotalEstimado(0.0);
        dto.setDistanciaTotalPercorrida(0.0);
        dto.setCustoPorKm(0.0);
        dto.setEstoquesAbaixoDoMinimo(0L);
        dto.setEstoquesAcimaDoMaximo(0L);
        dto.setTotalVeiculos(0L);
        dto.setTotalMotoristas(0L);
        dto.setVeiculosEmRota(0L);
        
        return dto;
    }
}