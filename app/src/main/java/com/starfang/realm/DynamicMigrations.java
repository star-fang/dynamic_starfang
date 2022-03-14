package com.starfang.realm;

import android.util.Log;

import com.starfang.realm.primitive.RealmInteger;
import com.starfang.realm.primitive.RealmIntegerPair;
import com.starfang.realm.primitive.RealmString;
import com.starfang.realm.source.cat.Passives;
import com.starfang.realm.source.cat.UnitTypes;
import com.starfang.realm.source.cat.Units;
import com.starfang.realm.source.rok.Building;
import com.starfang.realm.source.rok.Land;
import com.starfang.realm.source.rok.RokName;

import java.util.Locale;

import io.realm.DynamicRealm;
import io.realm.RealmList;
import io.realm.RealmMigration;
import io.realm.RealmObject;
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

            final RealmObjectSchema vertexSchema = schema.create("Vertex")
                    .addField("id", int.class)
                    .addPrimaryKey("id")
                    .addField("x", int.class)
                    .addField("y", int.class)
                    .addField("vc", int.class);

            final RealmObjectSchema realmIntegerSchema = schema.get("RealmInteger");
            if (realmIntegerSchema != null && vertexSchema != null) {
                schema.create("BarbarianCamp")
                        .addField("summonTime", long.class)
                        .addRealmListField("allowedAllyIds", realmIntegerSchema)
                        .addRealmObjectField("vertex", vertexSchema);

                schema.create("BarbarianKeep")
                        .addRealmObjectField("vertex", vertexSchema);
            }
            Log.d(TAG, "realm migration v" + oldVersion + "to v" + (oldVersion + 1));
            oldVersion++;
        }

        if (oldVersion == 1) {
            /*migrate from v1 to v2
             - Class 'Artifacts' has been added.
             - Class 'ArtifactsCate' has been added.
             - Class 'Reinforcement' has been added.
             - Property 'Units.namesakeCount' has been added
             - Property 'Units.typeAndName' has been added
            */

            final RealmObjectSchema realmIntegerSchema = schema.get("RealmInteger");
            final RealmObjectSchema realmStringSchema = schema.get("RealmString");
            final RealmObjectSchema passivesSchema = schema.get("Passives");
            final RealmObjectSchema unitTypesSchema = schema.get("UnitTypes");
            final RealmObjectSchema unitsSchema = schema.get("Units");

            if (realmIntegerSchema != null
                    && unitTypesSchema != null
                    && unitsSchema != null
                    && passivesSchema != null
                    && realmStringSchema != null) {
                unitsSchema
                        .addField("namesakeCount", int.class)
                        .addField("typeAndName", String.class)
                        .addIndex("typeAndName");

                final RealmObjectSchema artifactsCateSchema = schema.create("ArtifactsCate")
                        .addField("id", int.class)
                        .addPrimaryKey("id")
                        .addField("subCate", String.class)
                        .addField("mainCate", String.class)
                        .addField("atkType", String.class)
                        .addField("wisType", String.class)
                        .addField("defType", String.class)
                        .addField("agiType", String.class)
                        .addField("mrlType", String.class)
                        .addRealmListField("unitTypeIds", realmIntegerSchema)
                        .addRealmListField("unitTypes", unitTypesSchema);

                if (artifactsCateSchema != null) {
                    schema.create("Artifacts")
                            .addField("id", int.class)
                            .addPrimaryKey("id")
                            .addField("name", String.class)
                            .addIndex("name")
                            .addField("grade", String.class)
                            .addField("categoryId", int.class)
                            .addField("atk", int.class)
                            .addField("wis", int.class)
                            .addField("def", int.class)
                            .addField("agi", int.class)
                            .addField("mrl", int.class)
                            .addField("mov", int.class)
                            .addRealmListField("passiveIds", realmIntegerSchema)
                            .addRealmListField("passiveVals", realmStringSchema)
                            .addField("description", String.class)
                            .addRealmListField("unitId", realmIntegerSchema)
                            .addRealmListField("unitTypeIds", realmIntegerSchema)
                            .addRealmObjectField("category", artifactsCateSchema)
                            .addField("nameWithoutBlank", String.class)
                            .addIndex("nameWithoutBlank")
                            .addRealmListField("passives", passivesSchema)
                            .addRealmObjectField("unit", unitsSchema)
                            .addRealmListField("unitTypes", unitTypesSchema);
                }

                schema.create("Reinforcement")
                        .addField("grade", int.class)
                        .addField("type", String.class)
                        .addRealmListField("vals", realmIntegerSchema);

            }


            Log.d(TAG, "realm migration v" + oldVersion + "to v" + (oldVersion + 1));
            oldVersion++;
        }

        if( oldVersion == 2) {
            /*migrate from v2 to v3
            - Property 'Building.unlocksList' has been added.
             */

            final RealmObjectSchema buildingSchema = schema.get("Building");
            final RealmObjectSchema realmStringSchema = schema.get("RealmString");
            if (buildingSchema != null && realmStringSchema != null ) {
                buildingSchema.addRealmListField("unlocksList", realmStringSchema);
            }

            final RealmObjectSchema civilSchema = schema.get("Civilization");
            final RealmObjectSchema commanderSchema = schema.get("Commander");
            if( civilSchema != null && commanderSchema != null) {
                civilSchema.addRealmObjectField("initCommander", commanderSchema );
            }

            Log.d(TAG, "realm migration v" + oldVersion + "to v" + (oldVersion + 1));
            oldVersion++;
        }

        if( oldVersion == 3) {
            /*migrate from v3 to v4
            - Class 'Land' has been added.
            - Class 'RokName' has been added.
            - Property 'Vertex.server' has been added.
            - Property 'Vertex.landIds' has been added.
            - Property 'Vertex.nameId' has been added.
            - Property 'Vertex.name' has been added.
            - Property 'Vertex.lands' has been added.
            - Property 'Vertex.deadline' has been added.
            - Property 'Vertex.timeLimit' has been added.
            * */
            final RealmObjectSchema realmIntegerPairSchema = schema.get("RealmIntegerPair");
            final RealmObjectSchema vertexSchema = schema.get("Vertex");
            final RealmObjectSchema realmIntegerSchema = schema.get("RealmInteger");
            if( vertexSchema != null && realmIntegerSchema != null && realmIntegerPairSchema != null ) {
                final RealmObjectSchema landSchema = schema.createWithPrimaryKeyField("Land", "id", int.class);
                final RealmObjectSchema rokNameSchema = schema.createWithPrimaryKeyField("RokName","id",int.class);
                landSchema.addField("zone", int.class);
                landSchema.addField("nameId", int.class);
                landSchema.addField("server", int.class);
                landSchema.addRealmObjectField("center", realmIntegerPairSchema);
                landSchema.addRealmListField("boundary", realmIntegerPairSchema);
                landSchema.addRealmObjectField("name", rokNameSchema);

                rokNameSchema.addField("eng", String.class);
                rokNameSchema.addField("kor", String.class);

                vertexSchema.addField( "server", int.class);
                vertexSchema.addField( "nameId", int.class);
                vertexSchema.addField( "deadline", long.class);
                vertexSchema.addField( "timeLimit", long.class);
                vertexSchema.addRealmObjectField("name", rokNameSchema );
                vertexSchema.addRealmListField("landIds", realmIntegerSchema);
                vertexSchema.addRealmListField("lands", landSchema);
            }

            Log.d(TAG, "realm migration v" + oldVersion + "to v" + (oldVersion + 1));
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
