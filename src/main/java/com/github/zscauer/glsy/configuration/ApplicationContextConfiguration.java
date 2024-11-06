package com.github.zscauer.glsy.configuration;

import com.github.zscauer.glsy.common.ActiveRecord;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.sql.DataSource;
import java.util.Objects;

@Dependent
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
final class ApplicationContextConfiguration {

    DataSource dataSource;

    void onStart(@Observes final StartupEvent startupEvent) {
        Objects.requireNonNull(dataSource);
        ActiveRecord.setDatasource(dataSource);
    }

}
