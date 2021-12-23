package holiday.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DBConfig {
	@Bean(name = "dsCustom")
	public DataSource dataSource() {
		return DataSourceBuilder.create() //
				.username("akiwlykkaycrmb") //
				.password("f641283c297fd24e1f213e8e5ccb0d70885fbac212769c0e0f0f18bb705c87ec") //
				.url("jdbc:postgresql://ec2-52-209-134-160.eu-west-1.compute.amazonaws.com:5432/d6oqe6pendciak?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory&sslmode=allow") //
				.driverClassName("org.postgresql.Driver")//
				.build();
	}

	@Bean(name = "jdbcCustom")
	@Autowired
	public JdbcTemplate jdbcTemplate(@Qualifier("dsCustom") DataSource dsCustom) {
		return new JdbcTemplate(dsCustom);
	}
}