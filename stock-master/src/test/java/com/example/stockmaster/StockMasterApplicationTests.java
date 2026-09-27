package com.example.stockmaster;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.stockmaster.dto.GAPRequest;
import com.example.stockmaster.dto.InventarioRequest;
import com.example.stockmaster.dto.OrdenDeRestockRequest;
import com.example.stockmaster.dto.PPTORequest;
import com.example.stockmaster.dto.ProductosRequest;
import com.example.stockmaster.dto.RepuestoRequest;
import com.example.stockmaster.dto.SucursalRequest;
import com.example.stockmaster.dto.TraspasoRequest;
import com.example.stockmaster.model.enums.EstadoRestock;
import com.example.stockmaster.model.enums.EstadoTraspaso;
import com.example.stockmaster.service.GAPService;
import com.example.stockmaster.service.InventarioService;
import com.example.stockmaster.service.OrdenDeRestockService;
import com.example.stockmaster.service.PPTOService;
import com.example.stockmaster.service.ProductosService;
import com.example.stockmaster.service.SucursalService;
import com.example.stockmaster.service.TraspasoService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class StockMasterApplicationTests {

    @Autowired SucursalService sucursalService;
    @Autowired ProductosService productosService;
    @Autowired InventarioService inventarioService;
    @Autowired PPTOService pptoService;
    @Autowired OrdenDeRestockService ordenService;
    @Autowired GAPService gapService;
    @Autowired TraspasoService traspasoService;

	@Test
	void contextLoads() {
	}

    @Test
    @Transactional
    void flujoRestockGapYTraspaso() {
        var origen = sucursalService.crear(new SucursalRequest("CENTRO", "Centro", null, true));
        var destino = sucursalService.crear(new SucursalRequest("NORTE", "Norte", null, true));
        productosService.crear(new ProductosRequest("P-001", "Filtro", "Filtro de aceite",
                new BigDecimal("10.00"), new BigDecimal("15.00")));
        inventarioService.crear(new InventarioRequest(origen.id(), "P-001", 0));
        inventarioService.crear(new InventarioRequest(destino.id(), "P-001", 0));
        var ppto = pptoService.crear(new PPTORequest(new BigDecimal("150.00"), LocalDate.now()));

        var orden = ordenService.crear(new OrdenDeRestockRequest(origen.id(), ppto.id(),
                List.of(new RepuestoRequest("P-001", 10, new BigDecimal("12.00"))),
                null, EstadoRestock.RECIBIDA));
        var gap = gapService.crear(new GAPRequest(orden.id()));

        assertThat(orden.costoTotal()).isEqualByComparingTo("120.00");
        assertThat(gap.diferencia()).isEqualByComparingTo("30.00");

        traspasoService.crear(new TraspasoRequest(origen.id(), destino.id(), "P-001", 4,
                null, EstadoTraspaso.COMPLETADO));
        assertThat(inventarioService.listar()).extracting("cantidad").containsExactlyInAnyOrder(6, 4);
    }

}
