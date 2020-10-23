package com.starfang.realm.source.rok;

import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;

public class RokUser extends RealmObject {

    public static final String FIELD_ID = "id";
    public static final String FIELD_SENDCAT = "sendCat";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_FORUM_ID = "forumId";

    public static final String FIELD_VIP = "vip";
    public static final String FIELD_CIVIL = "civil";

    public static final String FIELD_TECHS = "techs";
    public static final String FIELD_BUILDINGS = "buildings";

    @PrimaryKey
    private long id;

    private String sendCat;
    private String name;
    private long forumId;

    private int vip;
    private Civilization civil;


    private RealmList<Technology> techs;
    private RealmList<Building> buildings;

    public RokUser() throws RealmPrimaryKeyConstraintException {
        this.id = UUID.randomUUID().getMostSignificantBits();
        this.techs = new RealmList<>();
        this.buildings = new RealmList<>();
    }


    public RokUser( String sendCat, String name, long forumId ) throws RealmPrimaryKeyConstraintException {
        this.id = UUID.randomUUID().getMostSignificantBits();
        this.sendCat = sendCat;
        this.name = name == null ? sendCat : name;
        this.forumId = forumId;
        this.techs = new RealmList<>();
        this.buildings = new RealmList<>();
    }

    public long getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setVip(int vip) {
        this.vip = vip;
    }

    public int getVip() {
        return vip;
    }

    public void setCivil(Civilization civil) {
        this.civil = civil;
    }

    public Civilization getCivil() {
        return civil;
    }

    public RealmList<Technology> getTechs() {
        return techs;
    }

    public void setTechs(RealmList<Technology> techs) {
        this.techs = techs;
    }

    public RealmList<Building> getBuildings() {
        return buildings;
    }

    public void setBuildings(RealmList<Building> buildings) {
        this.buildings = buildings;
    }

    public boolean addTech( Technology tech ) {
        if( this.techs != null ) {
            if( !this.techs.contains( tech ) ) {
                this.techs.add(tech);
                return true;
            }
        }
        return false;
    }

    public boolean deleteTech( Technology tech ) {
        if( this.techs != null ) {
            if( this.techs.contains( tech ) ) {
                this.techs.remove(tech);
                return true;
            }
        }
        return false;
    }

    public boolean addBuilding( Building building ) {
        if( this.buildings != null ) {
            if( !this.buildings.contains( building ) ) {
                this.buildings.add(building);
                return true;
            }
        }
        return false;
    }

    public boolean deleteBuilding( Building building ) {
        if( this.buildings != null ) {
            if( this.buildings.contains( building ) ) {
                this.buildings.remove(building);
                return true;
            }
        }
        return false;
    }

    public long getForumId() {
        return forumId;
    }
}
