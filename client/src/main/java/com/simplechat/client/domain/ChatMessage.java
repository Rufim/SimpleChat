package com.simplechat.client.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created by Rufim on 24.01.2015.
 */

@DatabaseTable(tableName = "history")
public class ChatMessage implements Serializable {

    @DatabaseField(canBeNull = false)
    public boolean left;
    @DatabaseField(canBeNull = true)
    public String user;
    @DatabaseField(canBeNull = false)
    public String message;

    public ChatMessage() {
    }

    public ChatMessage(String user, boolean left, String message) {
        super();
        this.left = left;
        this.user = user;
        this.message = message;
    }


    public boolean isLeft() {
        return left;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
