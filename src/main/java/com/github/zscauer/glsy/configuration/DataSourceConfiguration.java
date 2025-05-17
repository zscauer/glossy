package com.github.zscauer.glsy.configuration;

import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.supplier.AgroalConnectionPoolConfigurationSupplier;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;
import io.quarkus.arc.DefaultBean;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Slf4j
@Dependent
@RequiredArgsConstructor
@RegisterForReflection(targets = {io.agroal.pool.ConnectionHandler[].class})
@SuppressWarnings("unused")
final class DataSourceConfiguration {

    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private enum DataSourceKind {

        h2(org.h2.Driver.class, ";") {
            @Override
            String generateUrl(final Optional<String> location, final Optional<String> connectionParameters) {
                return "%s%s%s%s%s".formatted(
                        location.orElse("jdbc:h2:file:./glsy-data/h2base"),
                        connectionParameterDelimeter(),
                        connectionParameters.orElse("MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DEFAULT_NULL_ORDERING=HIGH;DEFRAG_ALWAYS=TRUE;RECOMPILE_ALWAYS=TRUE"),
                        connectionParameterDelimeter(),
                        "INIT=CREATE SCHEMA IF NOT EXISTS glsy\\;SET SCHEMA glsy"
                );
            }
        },
        postgresql(org.postgresql.Driver.class, "?") {
            @Override
            String generateUrl(final Optional<String> location, final Optional<String> connectionParameters) {
                final StringBuilder builder = new StringBuilder();
                builder.append(location.orElseThrow(() ->
                        new IllegalArgumentException("Location of datasource for '{}' not provided. Please provide correct location or choose another data source kind.")));
                builder.append(connectionParameterDelimeter());
                builder.append("currentSchema=glsy");
                connectionParameters.ifPresent(value -> {
                    builder.append("&");
                    builder.append(value);
                });

                return builder.toString();
            }
        };

        Class<? extends java.sql.Driver> driver;
        String connectionParameterDelimeter;

        private static final DataSourceKind DEFAULT = h2;

        @SuppressWarnings("all")
        private static DataSourceKind fromInput(final Optional<String> selectedDriver) {
            return selectedDriver.map(driverName -> {
                try {
                    return DataSourceKind.valueOf(driverName.toLowerCase());
                } catch (final Exception e) {
                    log.warn("Unable to define data source driver from value '{}' or provided data source type not supported, use default ({}) instead.", driverName, DEFAULT);
                    return DEFAULT;
                }
            }).orElseGet(() -> {
                log.warn("Value of db-kind not provided, use default ({}) instead.", DEFAULT);
                return DEFAULT;
            });
        }

        @SuppressWarnings("all")
        abstract String generateUrl(Optional<String> location, Optional<String> connectionParameters);

    }

    @Produces
    @Startup
    @Singleton
    @DefaultBean
    @SuppressWarnings("all")
    DataSource dataSourceProducer(@ConfigProperty(name = "configuration.datasource.db-kind") final Optional<String> dbKind,
                                  @ConfigProperty(name = "configuration.datasource.location") final Optional<String> location,
                                  @ConfigProperty(name = "configuration.datasource.connection-parameters") final Optional<String> connectionParameters,
                                  @ConfigProperty(name = "configuration.datasource.username") final Optional<String> username,
                                  @ConfigProperty(name = "configuration.datasource.password") final Optional<String> password) throws SQLException {
        final DataSourceKind dataSourceKind = DataSourceKind.fromInput(dbKind);

        final AgroalDataSourceConfigurationSupplier dataSourceConfiguration = new AgroalDataSourceConfigurationSupplier();
        final AgroalConnectionPoolConfigurationSupplier poolConfiguration = dataSourceConfiguration.connectionPoolConfiguration();

        // configure pool
        dataSourceConfiguration.connectionPoolConfiguration()
                .initialSize(10)
                .maxSize(10)
                .minSize(10)
                .maxLifetime(Duration.of(5, ChronoUnit.MINUTES))
                .acquisitionTimeout(Duration.of(30, ChronoUnit.SECONDS));

        // configure supplier
        poolConfiguration.connectionFactoryConfiguration()
                .connectionProviderClass(dataSourceKind.driver())
                .jdbcUrl(dataSourceKind.generateUrl(location, connectionParameters))
                .credential(new NamePrincipal(username.orElse("glsy")))
                .credential(new SimplePassword(password.orElse("glsy")));

        return AgroalDataSource.from(dataSourceConfiguration.get());
    }

}
