package com.starfang.realm;

import android.util.Log;

import com.starfang.realm.primitive.RealmInteger;
import com.starfang.realm.source.rok.Vertex;

import java.util.Locale;

import io.realm.DynamicRealm;
import io.realm.RealmList;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

public class DynamicMigrations implements RealmMigration {

    private static final String TAG = "FANG_MIGRATION";

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        final RealmSchema schema = realm.getSchema();


        if (oldVersion == 0) {
            /*migrate from v0 to v1
            - Class 'BarbarianCamp' has been added.
            - Class 'BarbarianKeep' has been added.
            - Class 'Vertex' has been added.
            - Property 'Building.bookCost' has been added.
            - Property 'Building.arrowCost' has been added.
            */
            final RealmObjectSchema buildingSchema = schema.get("Building");
            if (buildingSchema != null) {
                buildingSchema.addField("bookCost", int.class);
                buildingSchema.addField("arrowCost", int.class);
            }

            schema.create("Vertex")
                    .addField("id", int.class)
                    .addPrimaryKey("id")
                    .addField("x", int.class)
                    .addField("y", int.class)
                    .addField("vc", int.class);

            schema.create("BarbarianCamp")
                    .addField("summonTime", long.class)
                    .addRealmListField("allowedAllyIds", schema.get("RealmInteger"))
                    .addRealmObjectField("vertex", schema.get("Vertex"));

            schema.create("BarbarianKeep")
                    .addRealmObjectField("vertex", schema.get("Vertex"));
            Log.d(TAG, "realm migration v" + oldVersion + "to v" + (oldVersion+1));
            oldVersion++;
        }

        if (oldVersion == 1) {
            // migrate from v1 to v2
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
