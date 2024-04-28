package uk.co.bluegecko.marine.loader.configuration;

import org.springframework.boot.task.ThreadPoolTaskExecutorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskConfiguration {

	@Bean
	public ThreadPoolTaskExecutorCustomizer executorCustomizer() {
		return taskExecutor -> taskExecutor.setDaemon(true);
	}

}