package com.starfang.realm;

import java.util.Locale;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

public class DynamicMigrations implements RealmMigration {

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        final RealmSchema schema = realm.getSchema();
        if (oldVersion == 0) {
            // migrate from v0 to v1
            oldVersion++;
        }

        if (oldVersion == 1) {
            // migrate from v1 to v2
            final RealmObjectSchema unitsSchema = schema.get("Units");
            if (unitsSchema != null) {
                unitsSchema.addField("lastModified", long.class);
            }

            oldVersion++;
        }

        if (oldVersion < newVersion) {
            throw new IllegalStateException(
                    String.format(
                            Locale.KOREA, "Migration missing from v%d to v%d"
                            , oldVersion, newVersion));
        }
    }
}
