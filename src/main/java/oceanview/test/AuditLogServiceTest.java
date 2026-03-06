package oceanview.test;

import oceanview.dao.AuditLogDAO;
import oceanview.model.AuditLog;
import oceanview.service.AuditLogService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AuditLogServiceTest {

    private AuditLogDAO mockDao;
    private AuditLogService service;
    private AuditLog sampleLog;

    @Before
    public void setUp() {
        mockDao = Mockito.mock(AuditLogDAO.class);
        service = new AuditLogService(mockDao);

        sampleLog = new AuditLog();
        sampleLog.setLogId(1);
        sampleLog.setAction("CREATE");
        sampleLog.setTableName("rooms");
        sampleLog.setRecordId(101);
        sampleLog.setPerformedBy("admin");
        sampleLog.setIpAddress("127.0.0.1");
        sampleLog.setDescription("Created room #101");
        sampleLog.setCreatedAt(LocalDateTime.now());
    }

    // -----------------------------------------------------------------------
    // getLogs
    // -----------------------------------------------------------------------

    @Test
    public void testGetLogs_success_returnsList() throws Exception {
        when(mockDao.findFiltered("CREATE", "admin", null, null, null, 1, AuditLogService.PAGE_SIZE))
            .thenReturn(Arrays.asList(sampleLog));

        List<AuditLog> result = service.getLogs("CREATE", "admin", null, null, null, 1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CREATE", result.get(0).getAction());
        verify(mockDao, times(1)).findFiltered("CREATE", "admin", null, null, null, 1, AuditLogService.PAGE_SIZE);
    }

    @Test
    public void testGetLogs_noFilters_returnsAllLogs() throws Exception {
        when(mockDao.findFiltered(null, null, null, null, null, 1, AuditLogService.PAGE_SIZE))
            .thenReturn(Arrays.asList(sampleLog));

        List<AuditLog> result = service.getLogs(null, null, null, null, null, 1);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testGetLogs_emptyResult_returnsEmptyList() throws Exception {
        when(mockDao.findFiltered(null, null, null, null, null, 1, AuditLogService.PAGE_SIZE))
            .thenReturn(Collections.emptyList());

        List<AuditLog> result = service.getLogs(null, null, null, null, null, 1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test(expected = SQLException.class)
    public void testGetLogs_sqlException_throwsSQLException() throws Exception {
        when(mockDao.findFiltered(null, null, null, null, null, 1, AuditLogService.PAGE_SIZE))
            .thenThrow(new SQLException("DB error"));

        service.getLogs(null, null, null, null, null, 1);
    }

    @Test
    public void testGetLogs_withDateRange_returnsFilteredLogs() throws Exception {
        when(mockDao.findFiltered(null, null, "2025-01-01", "2025-12-31", null, 1, AuditLogService.PAGE_SIZE))
            .thenReturn(Arrays.asList(sampleLog));

        List<AuditLog> result = service.getLogs(null, null, "2025-01-01", "2025-12-31", null, 1);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetLogs_withSearch_returnsMatchingLogs() throws Exception {
        when(mockDao.findFiltered(null, null, null, null, "room", 1, AuditLogService.PAGE_SIZE))
            .thenReturn(Arrays.asList(sampleLog));

        List<AuditLog> result = service.getLogs(null, null, null, null, "room", 1);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // -----------------------------------------------------------------------
    // getTotalPages
    // -----------------------------------------------------------------------

    @Test
    public void testGetTotalPages_exactPage_returnsCorrectCount() throws Exception {
        when(mockDao.countFiltered(null, null, null, null, null)).thenReturn(40);

        int pages = service.getTotalPages(null, null, null, null, null);

        assertEquals(2, pages);
    }

    @Test
    public void testGetTotalPages_withRemainder_roundsUp() throws Exception {
        when(mockDao.countFiltered(null, null, null, null, null)).thenReturn(41);

        int pages = service.getTotalPages(null, null, null, null, null);

        assertEquals(3, pages);
    }

    @Test
    public void testGetTotalPages_zeroRecords_returnsZeroPages() throws Exception {
        when(mockDao.countFiltered(null, null, null, null, null)).thenReturn(0);

        int pages = service.getTotalPages(null, null, null, null, null);

        assertEquals(0, pages);
    }

    @Test
    public void testGetTotalPages_oneRecord_returnsOnePage() throws Exception {
        when(mockDao.countFiltered(null, null, null, null, null)).thenReturn(1);

        int pages = service.getTotalPages(null, null, null, null, null);

        assertEquals(1, pages);
    }

    @Test
    public void testGetTotalPages_pageSizeRecords_returnsOnePage() throws Exception {
        when(mockDao.countFiltered(null, null, null, null, null)).thenReturn(AuditLogService.PAGE_SIZE);

        int pages = service.getTotalPages(null, null, null, null, null);

        assertEquals(1, pages);
    }

    @Test(expected = SQLException.class)
    public void testGetTotalPages_sqlException_throwsSQLException() throws Exception {
        when(mockDao.countFiltered(null, null, null, null, null))
            .thenThrow(new SQLException("DB error"));

        service.getTotalPages(null, null, null, null, null);
    }

    // -----------------------------------------------------------------------
    // getTotalCount
    // -----------------------------------------------------------------------

    @Test
    public void testGetTotalCount_success_returnsCount() throws Exception {
        when(mockDao.countFiltered(null, null, null, null, null)).thenReturn(55);

        int count = service.getTotalCount(null, null, null, null, null);

        assertEquals(55, count);
    }

    @Test
    public void testGetTotalCount_withFilters_returnsFilteredCount() throws Exception {
        when(mockDao.countFiltered("DELETE", "admin", null, null, null)).thenReturn(5);

        int count = service.getTotalCount("DELETE", "admin", null, null, null);

        assertEquals(5, count);
    }

    @Test
    public void testGetTotalCount_zeroRecords_returnsZero() throws Exception {
        when(mockDao.countFiltered(null, null, null, null, null)).thenReturn(0);

        int count = service.getTotalCount(null, null, null, null, null);

        assertEquals(0, count);
    }

    @Test(expected = SQLException.class)
    public void testGetTotalCount_sqlException_throwsSQLException() throws Exception {
        when(mockDao.countFiltered(null, null, null, null, null))
            .thenThrow(new SQLException("DB error"));

        service.getTotalCount(null, null, null, null, null);
    }

    // -----------------------------------------------------------------------
    // getDistinctActions
    // -----------------------------------------------------------------------

    @Test
    public void testGetDistinctActions_success_returnsList() throws Exception {
        when(mockDao.getDistinctActions()).thenReturn(Arrays.asList("CREATE", "UPDATE", "DELETE"));

        List<String> actions = service.getDistinctActions();

        assertNotNull(actions);
        assertEquals(3, actions.size());
        assertTrue(actions.contains("CREATE"));
        assertTrue(actions.contains("DELETE"));
    }

    @Test
    public void testGetDistinctActions_emptyResult_returnsEmptyList() throws Exception {
        when(mockDao.getDistinctActions()).thenReturn(Collections.emptyList());

        List<String> actions = service.getDistinctActions();

        assertNotNull(actions);
        assertTrue(actions.isEmpty());
    }

    @Test(expected = SQLException.class)
    public void testGetDistinctActions_sqlException_throwsSQLException() throws Exception {
        when(mockDao.getDistinctActions()).thenThrow(new SQLException("DB error"));

        service.getDistinctActions();
    }

    // -----------------------------------------------------------------------
    // getDistinctPerformedBy
    // -----------------------------------------------------------------------

    @Test
    public void testGetDistinctPerformedBy_success_returnsList() throws Exception {
        when(mockDao.getDistinctPerformedBy()).thenReturn(Arrays.asList("admin", "staff1"));

        List<String> users = service.getDistinctPerformedBy();

        assertNotNull(users);
        assertEquals(2, users.size());
        assertTrue(users.contains("admin"));
    }

    @Test
    public void testGetDistinctPerformedBy_emptyResult_returnsEmptyList() throws Exception {
        when(mockDao.getDistinctPerformedBy()).thenReturn(Collections.emptyList());

        List<String> users = service.getDistinctPerformedBy();

        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test(expected = SQLException.class)
    public void testGetDistinctPerformedBy_sqlException_throwsSQLException() throws Exception {
        when(mockDao.getDistinctPerformedBy()).thenThrow(new SQLException("DB error"));

        service.getDistinctPerformedBy();
    }

    // -----------------------------------------------------------------------
    // PAGE_SIZE constant
    // -----------------------------------------------------------------------

    @Test
    public void testPageSize_isTwenty() {
        assertEquals(20, AuditLogService.PAGE_SIZE);
    }
}
