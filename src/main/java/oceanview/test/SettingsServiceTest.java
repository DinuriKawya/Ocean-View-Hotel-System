package oceanview.test;

import oceanview.dao.SettingsDAO;
import oceanview.service.SettingsService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SettingsServiceTest {

    private SettingsDAO mockDao;
    private SettingsService service;
    private Map<String, String> sampleSettings;

    @Before
    public void setUp() {
        mockDao  = Mockito.mock(SettingsDAO.class);
        service  = new SettingsService(mockDao);

        sampleSettings = new LinkedHashMap<>();
        sampleSettings.put("hotel_name",    "Ocean View Hotel");
        sampleSettings.put("hotel_email",   "info@oceanview.com");
        sampleSettings.put("hotel_phone",   "+94771234567");
        sampleSettings.put("currency",      "USD");
        sampleSettings.put("check_in_time", "14:00");
    }

    // -----------------------------------------------------------------------
    // getAll
    // -----------------------------------------------------------------------

    @Test
    public void testGetAll_success_returnsMap() throws Exception {
        when(mockDao.findAll()).thenReturn(sampleSettings);

        Map<String, String> result = service.getAll();

        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals("Ocean View Hotel", result.get("hotel_name"));
        verify(mockDao, times(1)).findAll();
    }

    @Test
    public void testGetAll_emptyMap_returnsEmptyMap() throws Exception {
        when(mockDao.findAll()).thenReturn(Collections.emptyMap());

        Map<String, String> result = service.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAll_returnsCorrectKeys() throws Exception {
        when(mockDao.findAll()).thenReturn(sampleSettings);

        Map<String, String> result = service.getAll();

        assertTrue(result.containsKey("hotel_name"));
        assertTrue(result.containsKey("hotel_email"));
        assertTrue(result.containsKey("currency"));
    }

    @Test(expected = SettingsService.SettingsException.class)
    public void testGetAll_sqlException_throwsSettingsException() throws Exception {
        when(mockDao.findAll()).thenThrow(new SQLException("DB error"));

        service.getAll();
    }

    @Test
    public void testGetAll_sqlException_correctMessage() throws Exception {
        when(mockDao.findAll()).thenThrow(new SQLException("connection failed"));

        try {
            service.getAll();
            fail("Expected SettingsException");
        } catch (SettingsService.SettingsException e) {
            assertTrue(e.getMessage().contains("Database error"));
        }
    }

    // -----------------------------------------------------------------------
    // save
    // -----------------------------------------------------------------------

    @Test
    public void testSave_success_callsUpdateForEachEntry() throws Exception {
        service.save(sampleSettings);

        verify(mockDao, times(1)).update("hotel_name",    "Ocean View Hotel");
        verify(mockDao, times(1)).update("hotel_email",   "info@oceanview.com");
        verify(mockDao, times(1)).update("hotel_phone",   "+94771234567");
        verify(mockDao, times(1)).update("currency",      "USD");
        verify(mockDao, times(1)).update("check_in_time", "14:00");
    }

    @Test
    public void testSave_singleEntry_callsUpdateOnce() throws Exception {
        Map<String, String> single = new LinkedHashMap<>();
        single.put("hotel_name", "New Name");

        service.save(single);

        verify(mockDao, times(1)).update("hotel_name", "New Name");
    }

    @Test
    public void testSave_emptyMap_doesNotCallUpdate() throws Exception {
        service.save(Collections.emptyMap());

        verify(mockDao, never()).update(anyString(), anyString());
    }

    @Test(expected = SettingsService.SettingsException.class)
    public void testSave_sqlException_throwsSettingsException() throws Exception {
        doThrow(new SQLException("DB error")).when(mockDao).update(anyString(), anyString());

        service.save(sampleSettings);
    }

    @Test
    public void testSave_sqlException_correctMessage() throws Exception {
        doThrow(new SQLException("update failed")).when(mockDao).update(anyString(), anyString());

        try {
            service.save(sampleSettings);
            fail("Expected SettingsException");
        } catch (SettingsService.SettingsException e) {
            assertTrue(e.getMessage().contains("Database error"));
        }
    }

    @Test
    public void testSave_multipleEntries_callsUpdateCorrectNumberOfTimes() throws Exception {
        service.save(sampleSettings);

        verify(mockDao, times(sampleSettings.size())).update(anyString(), anyString());
    }

    // -----------------------------------------------------------------------
    // SettingsException
    // -----------------------------------------------------------------------

    @Test
    public void testSettingsException_message() {
        SettingsService.SettingsException ex =
            new SettingsService.SettingsException("Test error");
        assertEquals("Test error", ex.getMessage());
    }

    @Test
    public void testSettingsException_isException() {
        SettingsService.SettingsException ex =
            new SettingsService.SettingsException("error");
        assertTrue(ex instanceof Exception);
    }
}
