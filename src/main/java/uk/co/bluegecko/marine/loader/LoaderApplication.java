package uk.co.bluegecko.marine.loader;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import uk.co.bluegecko.marine.shared.application.AbstractApplication;

@SpringBootApplication(exclude = {
		DataSourceAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class
})
@ConfigurationPropertiesScan
public class LoaderApplication extends AbstractApplication {

	public static void main(String[] args) {
		exit(run(LoaderApplication.class, WebApplicationType.NONE, args));
	}
}