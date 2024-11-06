package com.github.zscauer.glsy.configuration;

import com.github.zscauer.glsy.tools.Constants;
import io.quarkus.arc.All;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.Dependent;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.ClassProvider;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.output.MigrateResult;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.resource.classpath.ClassPathResource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Dependent
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
final class FlywayConfiguration {

    FluentConfiguration configuration;

    FlywayConfiguration(final DataSource dataSource, @All final List<JavaMigration> javaMigrations) {
        this.configuration = Flyway.configure()
                .dataSource(dataSource)
                .locations("db/migration")
                .defaultSchema("glsy")
                .createSchemas(true)
                .baselineOnMigrate(true) // Default false, must be set to true for initial migration on existing databases
                .executeInTransaction(true)
                .javaMigrations(javaMigrations.toArray(new JavaMigration[0]));
    }

    @Startup
    void performMigration() {
        if (Constants.nativeExecution()) {
            configuration.resourceProvider(new GraalVMResourceProvider(configuration.getLocations()));
            configuration.javaMigrationClassProvider(new GraalVMClassProvider());
        }

        final Flyway flyway = configuration.load();
        final MigrateResult migrate = flyway.migrate();
    }

    private record GraalVMResourceProvider(
            Location[] locations
    ) implements ResourceProvider {

        private GraalVMResourceProvider(final Location[] locations) {
            this.locations = Arrays.copyOf(locations, locations.length);
        }

        @Override
        public LoadableResource getResource(final String name) {
            if (getClassLoader().getResource(name) == null) {
                return null;
            }
            return new ClassPathResource(null, name, getClassLoader(), StandardCharsets.UTF_8);
        }

        @Override
        public Collection<LoadableResource> getResources(final String prefix, final String[] suffixes) {
            try (final FileSystem fileSystem = FileSystems.newFileSystem(URI.create("resource:/"), Collections.emptyMap())) {
                final List<LoadableResource> result = new ArrayList<>();
                for (final Location location : locations) {
                    final Path path = fileSystem.getPath(location.getPath());
                    try (final Stream<Path> files = Files.walk(path)) {
                        files.filter(Files::isRegularFile)
                                .filter(file -> file.getFileName().toString().startsWith(prefix))
                                .filter(file -> hasSuffix(file.getFileName().toString(), suffixes))
                                .map(file -> new ClassPathResource(null, file.toString(), getClassLoader(), StandardCharsets.UTF_8))
                                .forEach(result::add);
                    }
                }

                return result.stream().sorted().toList(); // Sort DB migration files

            } catch (final IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        private boolean hasSuffix(final String input, final String[] suffixes) {
            for (final String suffix : suffixes) {
                if (input.endsWith(suffix) || input.toLowerCase().endsWith(suffix.toLowerCase())) {
                    return true;
                }
            }

            return false;
        }

        private static ClassLoader getClassLoader() {
            return GraalVMResourceProvider.class.getClassLoader();
        }
    }

    private record GraalVMClassProvider() implements ClassProvider<JavaMigration> {
        @Override
        public Collection<Class<? extends JavaMigration>> getClasses() {
            return Collections.emptySet();
        }
    }

}
