package com.aefamily.support.db;

import java.util.List;

import com.aefamily.support.SupportRequest;

public interface SupportRequestDAO {
    int create(SupportRequest req) throws Exception;
    SupportRequest findById(int id) throws Exception;
    List<SupportRequest> findAll() throws Exception;
    List<SupportRequest> findByPlayer(String player) throws Exception;
    boolean update(SupportRequest req) throws Exception;
    boolean updateStatus(int id, String status) throws Exception;
    boolean delete(int id) throws Exception;
}
