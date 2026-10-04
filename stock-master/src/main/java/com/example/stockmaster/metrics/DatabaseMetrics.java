package com.example.stockmaster.metrics;

import com.example.stockmaster.repository.GAPRepository;
import com.example.stockmaster.repository.InventarioRepository;
import com.example.stockmaster.repository.OrdenDeRestockRepository;
import com.example.stockmaster.repository.PPTORepository;
import com.example.stockmaster.repository.ProductosRepository;
import com.example.stockmaster.repository.RepuestoRepository;
import com.example.stockmaster.repository.SucursalRepository;
import com.example.stockmaster.repository.TraspasoRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Expone en Prometheus (via /actuator/prometheus) la cantidad de registros
 * que hay en cada tabla principal de la base de datos.
 */
@Component
public class DatabaseMetrics {

    public DatabaseMetrics(MeterRegistry registry,
                           ProductosRepository productos,
                           RepuestoRepository repuestos,
                           SucursalRepository sucursales,
                           TraspasoRepository traspasos,
                           OrdenDeRestockRepository ordenesDeRestock,
                           InventarioRepository inventarios,
                           GAPRepository gaps,
                           PPTORepository pptos) {
        gauge(registry, "productos", productos, ProductosRepository::count);
        gauge(registry, "repuestos", repuestos, RepuestoRepository::count);
        gauge(registry, "sucursales", sucursales, SucursalRepository::count);
        gauge(registry, "traspasos", traspasos, TraspasoRepository::count);
        gauge(registry, "ordenes_restock", ordenesDeRestock, OrdenDeRestockRepository::count);
        gauge(registry, "inventarios", inventarios, InventarioRepository::count);
        gauge(registry, "gaps", gaps, GAPRepository::count);
        gauge(registry, "pptos", pptos, PPTORepository::count);
    }

    private <T> void gauge(MeterRegistry registry, String entidad, T origen,
                           java.util.function.ToLongFunction<T> counter) {
        Gauge.builder("stockmaster_registros_total", origen, o -> (double) counter.applyAsLong(o))
                .tag("entidad", entidad)
                .description("Cantidad de registros en la tabla " + entidad)
                .register(registry);
    }
}
