package br.edu.pe.senac.pi_tads049.sprig.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.pe.senac.pi_tads049.sprig.dto.DashboardDTO;
import br.edu.pe.senac.pi_tads049.sprig.dto.DashboardStatsDTO;
import br.edu.pe.senac.pi_tads049.sprig.dto.DeliveryRouteDTO;
import br.edu.pe.senac.pi_tads049.sprig.service.DashboardService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DashboardController {
    @Autowired private DashboardService dashboardService;

    // Endpoints originais com autenticação
    @GetMapping("/dashboardG")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<DashboardDTO> getDashboardG() {
        return ResponseEntity.ok(dashboardService.getDashboardMetrics());
    }
    
    @GetMapping("/dashboardT")
    @PreAuthorize("hasRole('TECNICO')")
    public ResponseEntity<DashboardDTO> getDashboardT() {
        return ResponseEntity.ok(dashboardService.getDashboardMetrics());
    }
    
    @GetMapping("/dashboardA")
    @PreAuthorize("hasRole('AGRICULTOR')")
    public ResponseEntity<DashboardDTO> getDashboardA() {
        return ResponseEntity.ok(dashboardService.getDashboardMetrics());
    }
    
    // Novos endpoints para o frontend (sem autenticação obrigatória para desenvolvimento)
    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }
    
    @GetMapping("/dashboard/routes")
    public ResponseEntity<List<DeliveryRouteDTO>> getDeliveryRoutes() {
        return ResponseEntity.ok(dashboardService.getDeliveryRoutes());
    }
}