package com.example.siddharthgautam.csc301;

import android.provider.BaseColumns;

/**
 * Created by siddharthgautam on 19/11/15.
 */
public class TableData {

    public TableData()
    {


    }
    /*
    * Will be used for creating the database.
     */
    public static abstract class TableInfo implements BaseColumns
    {
        public static final String DATABASE_NAME = "MessageData";
        public static final String TABLE_NAME = "Messages";
        public static final String DATETIME = "DateTime";
        public static final String SENDERID = "SenderId";
        public static final String RECID = "ReceiverId";
        public static final String MSG = "Message";

    }
}
