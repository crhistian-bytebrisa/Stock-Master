package com.example.stockmaster.metrics;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Evita que las peticiones internas de infraestructura aparezcan como tráfico
 * de la API en las métricas http_server_requests.
 *
 * Prometheus se scrapea a sí mismo cada 15s contra /actuator/prometheus, y
 * Swagger golpea /swagger-ui y /v3/api-docs. Sin este filtro esas rutas
 * inflan los contadores por método, por código de estado y por endpoint, y
 * ensucian el pastel de la API con rutas que no son del negocio.
 *
 * Se filtran en el origen (no en PromQL) para que no se registren en ninguna
 * serie ni consuman cardinalidad. Quien prefiera solo esconderlo en los
 * gráficos puede agregar {uri!~"/actuator.*|/swagger-ui.*|/v3/api-docs.*"} a
 * las consultas del dashboard.
 */
@Configuration
public class HttpMetricsFilter {

    private static final List<String> EXCLUIDOS = List.of(
            "/actuator",
            "/swagger-ui",
            "/v3/api-docs",
            "/webjars");

    @Bean
    public MeterFilter infraHttpRequestsFilter() {
        return new MeterFilter() {
            @Override
            public MeterFilterReply accept(Meter.Id id) {
                if (!id.getName().startsWith("http.server.requests")) {
                    return MeterFilterReply.NEUTRAL;
                }
                String uri = id.getTag("uri");
                if (uri == null) {
                    return MeterFilterReply.NEUTRAL;
                }
                for (String prefijo : EXCLUIDOS) {
                    if (uri.equals(prefijo) || uri.startsWith(prefijo + "/") || uri.startsWith(prefijo + "?")) {
                        return MeterFilterReply.DENY;
                    }
                }
                return MeterFilterReply.NEUTRAL;
            }
        };
    }
}