package me.hypherionmc.sdlinklib.database;

import me.hypherionmc.jqlite.annotations.SQLCOLUMN;
import me.hypherionmc.jqlite.data.SQLiteTable;

public class UserTable extends SQLiteTable {

    @SQLCOLUMN(type = SQLCOLUMN.Type.PRIMARY)
    private int ID;

    @SQLCOLUMN(type = SQLCOLUMN.Type.TEXT, maxSize = 100)
    public String username;

    @SQLCOLUMN(type = SQLCOLUMN.Type.TEXT, maxSize = 100)
    public String UUID;

    @SQLCOLUMN(type = SQLCOLUMN.Type.BIGINT)
    public long discordID;

}
