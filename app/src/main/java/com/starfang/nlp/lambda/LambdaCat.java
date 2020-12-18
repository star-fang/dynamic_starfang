package com.starfang.nlp.lambda;

import android.util.Log;

import com.starfang.realm.primitive.RealmString;
import com.starfang.realm.source.Source;
import com.starfang.realm.source.cat.Artifacts;
import com.starfang.realm.source.cat.Friendships;
import com.starfang.realm.source.cat.PassiveList;
import com.starfang.realm.source.cat.Passives;
import com.starfang.realm.source.cat.Units;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;


public class LambdaCat {

    private static final String TAG = "FANG_MOD_CAT";

    private enum CMD_ENUM {
        CMD_DESC, CMD_UNIT, CMD_TACTIC, CMD_PASSIVE, CMD_FRIEND, CMD_ARTIFACT, CMD_DEFAULT
    }

    private static final String[] CMD_CERTAIN = {
            "설명", "유닛"
            , "책략", "패시브"
            , "인연", "보물", "멍"};


    private static CMD_ENUM findCMD(RealmString rString) {
        String req = rString.toString().trim();
        CMD_ENUM cmd = CMD_ENUM.CMD_DEFAULT;
        for (CMD_ENUM certain : CMD_ENUM.values()) {
            try {
                int certainIndex = certain.ordinal();
                if (certainIndex < CMD_CERTAIN.length && req != null) {
                    String probKey = CMD_CERTAIN[certainIndex];
                    int reqLength = req.length();
                    int keyLength = probKey.length();

                    if (reqLength >= keyLength) {

                        try {
                            if (req.substring(reqLength - keyLength).equals(probKey)) {
                                cmd = certain;
                                req = (reqLength == keyLength) ? null : req.substring(0, reqLength - keyLength).trim();
                                rString.setValue(req);
                                break;
                            }
                        } catch (StringIndexOutOfBoundsException e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        }
                    }

                }
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
        return cmd;
    }

    public static List<String> processReq(String req) {
        Log.d(TAG, req);

        try (Realm realm = Realm.getDefaultInstance()) {
            RealmString rString = new RealmString(req);
            CMD_ENUM cmd = findCMD(rString);
            req = rString.toString(); //empty -> null

            Command unitsByProperty = (l, q) -> {
                if (q == null) {
                    return;
                }

                String[] qSplit = q.trim().split("\\s+");
                List<Integer> bases = new ArrayList<>();
                List<Passives[]> pasvList = new ArrayList<>();
                for (String pasvName : qSplit) {
                    if (pasvName.length() > 1) {
                        Passives[] passives = realm.where(Passives.class).contains(Passives.FIELD_NAME_WITHOUT_BLANK, pasvName).findAll().toArray(new Passives[0]);
                        if (passives.length == 0) {
                            return;
                        }
                        bases.add(passives.length);
                        pasvList.add(passives);
                    } else {
                        return;
                    }
                }

                MultiBaseNotation mbn = new MultiBaseNotation(bases);
                List<int[]> iCombs = mbn.getPositiveBaseDigitsCombination(99999);
                for (int[] indexes : iCombs) {
                    Integer[] pasvIds = new Integer[bases.size()];
                    String[] pasvNames = new String[bases.size()];
                    for (int i = 0; i < indexes.length; i++) {
                        Passives pasv = pasvList.get(i)[indexes[i]];
                        pasvIds[i] = pasv.getId();
                        pasvNames[i] = pasv.getString(Source.FIELD_NAME);
                    }
                    RealmQuery<Units> unitsRealmQuery = realm.where(Units.class).alwaysTrue();

                    for (int pasvId : pasvIds) {
                        unitsRealmQuery.and().equalTo(Units.FIELD_PASSIVE_LISTS + "." + PassiveList.FIELD_PASV_ID, pasvId);
                    }
                    RealmResults<Units> unitsByPropertyList = unitsRealmQuery.findAll();
                    if (unitsByPropertyList.size() > 0) {
                        StringBuilder unitsByPropertyBuilder = new StringBuilder();
                        for (String pasvName : pasvNames) {
                            unitsByPropertyBuilder.append("*").append(pasvName).append("\r\n");
                        }
                        unitsByPropertyBuilder.append("검색 결과: ").append(unitsByPropertyList.size()).append("개\r\n");
                        unitsByPropertyBuilder.append("---------------------");
                        for (Units unit : unitsByPropertyList.sort(Source.FIELD_NAME).sort(Units.FIELD_COST).sort(Units.FIELD_TYPE_ID)) {
                            unitsByPropertyBuilder.append(unit.getInfo(false, null));
                        }

                        l.add(unitsByPropertyBuilder.toString());
                    }
                }

            };

            Command unitsByName = (l, q) -> {

                if (q == null) {
                    return;
                }

                String[] unitNames = q.trim().split("\\s+");

                List<Units> unitList = new ArrayList<>();
                for (String unitName : unitNames) {
                    RealmResults<Units> units = realm.where(Units.class).equalTo(Source.FIELD_NAME, unitName)
                            .or().equalTo(Source.FIELD_NAME2, unitName).findAll();
                    if (units.size() == 0) {
                        return;
                    }
                    for (Units unit : units) {
                        if (!unitList.contains(unit)) {
                            unitList.add(unit);
                        }
                    }
                }

                if (unitNames.length == 1) {
                    for (Units unit : unitList) {
                        l.add(unit.getInfo(true, null));
                    }
                } else {
                    StringBuilder infoBuilder = new StringBuilder();
                    infoBuilder.append("  병종　　 이름　　 COST\r\n---------------------");
                    Units.UnitAccumulation accu = new Units.UnitAccumulation();
                    for (Units unit : unitList) {
                        infoBuilder.append(unit.getInfo(false, accu));
                    }
                    infoBuilder.append("\r\n---------------------\r\nTOTAL COST: [")
                            .append(accu.getCost()).append("]");
                    List<Friendships> activatedFriendships = accu.getActivatedFriendships();
                    for (Friendships friendship : activatedFriendships) {
                        infoBuilder.append("\r\n---------------------\r\n")
                                .append(friendship.getInfo(accu.getFriendshipUnitCount(friendship)));
                    }
                    l.add(infoBuilder.toString());
                }
            };

            Command artifactsByName = (l, q) -> {

                if (q == null) {
                    return;
                }

                String levelStr = q.replaceAll("[^0-9]", "");
                int level = NumberUtils.toInt(levelStr, 0);

                q = q.replaceAll("\\s+|[0-9]", "").trim();

                if(StringUtils.isEmpty(q)) {
                    return;
                }

                RealmResults<Artifacts> artifacts =
                        realm.where(Artifacts.class).contains(Artifacts.FIELD_NAME_WITHOUT_BLANK, q).findAll();

                List<Artifacts> artifactList = new ArrayList<>();
                if (artifacts.size() > 1) {
                    Artifacts artifact = artifacts.where().equalTo(Artifacts.FIELD_NAME_WITHOUT_BLANK, q).findFirst();
                    if (artifact != null) {
                        artifactList.add(artifact);
                    }
                }

                if (artifactList.size() == 0) {
                    artifactList.addAll(artifacts);
                }

                for (Artifacts artifact : artifactList) {
                    String info = artifact.getSubjectInfo(level) +
                            artifact.getStatInfo(level, realm) +
                            artifact.getPassivesInfo() +
                            artifact.getWearRestrictionInfo();
                    l.add(info);
                }


            };


            List<String> result = new ArrayList<>();

            unitsByName.search(result, req);
            if (result.size() == 0) {
                artifactsByName.search(result, req);
            }
            unitsByProperty.search(result, req);

            return result;
        } catch (RuntimeException e) {
            Log.e(TAG, Log.getStackTraceString(e));

        }
        return null;
    }

    private interface Command {
        void search(List<String> list, String req);
    }


}
