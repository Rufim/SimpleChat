package com.simplechat.client.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.simplechat.client.domain.ChatMessage;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Rufim on 25.01.2015.
 */
public class HistoryDAO extends BaseDaoImpl<ChatMessage, String> {

        public HistoryDAO(ConnectionSource connectionSource, Class<ChatMessage> dataClass) throws SQLException {
            super(connectionSource, dataClass);
        }

        public List<ChatMessage> getHistory() throws SQLException {
            return this.queryForAll();
        }

}
