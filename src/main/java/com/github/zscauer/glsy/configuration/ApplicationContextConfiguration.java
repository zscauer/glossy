package com.github.zscauer.glsy.configuration;

import com.github.zscauer.glsy.common.ActiveRecord;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import javax.sql.DataSource;
import java.util.Objects;

@Dependent
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
final class ApplicationContextConfiguration {

    DataSource dataSource;

    public ApplicationContextConfiguration(@Nonnull final DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource,
                "DataSource component was not registered during application context initialization.");
    }

    void onStart(@Observes final StartupEvent startupEvent) {
        ActiveRecord.setDatasource(dataSource);
    }

}