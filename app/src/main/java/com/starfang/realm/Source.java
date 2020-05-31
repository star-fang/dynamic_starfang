package com.starfang.realm;

public interface Source {

    String FIELD_ID = "id";
    String FIELD_NAME ="name";
    String FIELD_NAME2 = "name2";
    String FIELD_NAME_WITHOUT_BLANK = "nameWithoutBlank";
    String FIELD_REMARK = "remark";
    String FIELD_DESCRIPTION = "desc";

    int getId();
    String getString( String field );
    int getInt( String field );
}
