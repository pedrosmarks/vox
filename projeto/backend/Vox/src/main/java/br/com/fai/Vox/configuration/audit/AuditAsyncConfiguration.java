package br.com.fai.Vox.configuration.audit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Habilita execução assíncrona (gravação de auditoria fora da thread da request)
 * e agendamento (job de retenção). O executor é pequeno e com fila limitada:
 * a auditoria é best-effort, então em sobrecarga é preferível descartar o
 * registro a acumular memória.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AuditAsyncConfiguration {

    @Bean(name = "auditTaskExecutor")
    public Executor auditTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("audit-");
        // Se a fila encher, descarta silenciosamente a tarefa mais antiga em vez
        // de bloquear a thread da request.
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy());
        executor.initialize();
        return executor;
    }
}
