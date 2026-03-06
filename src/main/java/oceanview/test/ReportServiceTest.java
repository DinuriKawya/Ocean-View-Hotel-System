package oceanview.test;

import oceanview.dao.ReportDAO;
import oceanview.service.ReportService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ReportServiceTest {

    private ReportDAO mockDao;
    private ReportService service;

    private List<String[]> sampleStaffRows;
    private List<String[]> sampleRoomRows;
    private List<String[]> samplePaymentRows;

    @Before
    public void setUp() {
        mockDao = Mockito.mock(ReportDAO.class);
        service = new ReportService(mockDao);

        sampleStaffRows   = new ArrayList<>();
        sampleStaffRows.add(new String[]{"admin", "5", "8", "1200.00"});

        sampleRoomRows    = new ArrayList<>();
        sampleRoomRows.add(new String[]{"DELUXE", "3", "9", "900.00", "50.00", "950.00"});

        samplePaymentRows = new ArrayList<>();
        samplePaymentRows.add(new String[]{"CASH", "4", "6", "800.00"});
    }

    // -----------------------------------------------------------------------
    // getReport — staff
    // -----------------------------------------------------------------------

    @Test
    public void testGetReport_staff_success() throws Exception {
        when(mockDao.staffReport("2025-01-01", "2025-12-31")).thenReturn(sampleStaffRows);

        List<String[]> result = service.getReport("staff", "2025-01-01", "2025-12-31");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("admin", result.get(0)[0]);
        verify(mockDao, times(1)).staffReport("2025-01-01", "2025-12-31");
    }

    @Test
    public void testGetReport_staff_emptyResult() throws Exception {
        when(mockDao.staffReport("2025-01-01", "2025-12-31")).thenReturn(Collections.emptyList());

        List<String[]> result = service.getReport("staff", "2025-01-01", "2025-12-31");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test(expected = SQLException.class)
    public void testGetReport_staff_sqlException() throws Exception {
        when(mockDao.staffReport("2025-01-01", "2025-12-31")).thenThrow(new SQLException("DB error"));

        service.getReport("staff", "2025-01-01", "2025-12-31");
    }

    // -----------------------------------------------------------------------
    // getReport — room
    // -----------------------------------------------------------------------

    @Test
    public void testGetReport_room_success() throws Exception {
        when(mockDao.roomCategoryReport("2025-01-01", "2025-12-31")).thenReturn(sampleRoomRows);

        List<String[]> result = service.getReport("room", "2025-01-01", "2025-12-31");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("DELUXE", result.get(0)[0]);
        verify(mockDao, times(1)).roomCategoryReport("2025-01-01", "2025-12-31");
    }

    @Test
    public void testGetReport_room_emptyResult() throws Exception {
        when(mockDao.roomCategoryReport("2025-01-01", "2025-12-31")).thenReturn(Collections.emptyList());

        List<String[]> result = service.getReport("room", "2025-01-01", "2025-12-31");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test(expected = SQLException.class)
    public void testGetReport_room_sqlException() throws Exception {
        when(mockDao.roomCategoryReport("2025-01-01", "2025-12-31")).thenThrow(new SQLException("DB error"));

        service.getReport("room", "2025-01-01", "2025-12-31");
    }

    // -----------------------------------------------------------------------
    // getReport — payment
    // -----------------------------------------------------------------------

    @Test
    public void testGetReport_payment_success() throws Exception {
        when(mockDao.paymentMethodReport("2025-01-01", "2025-12-31")).thenReturn(samplePaymentRows);

        List<String[]> result = service.getReport("payment", "2025-01-01", "2025-12-31");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CASH", result.get(0)[0]);
        verify(mockDao, times(1)).paymentMethodReport("2025-01-01", "2025-12-31");
    }

    @Test
    public void testGetReport_payment_emptyResult() throws Exception {
        when(mockDao.paymentMethodReport("2025-01-01", "2025-12-31")).thenReturn(Collections.emptyList());

        List<String[]> result = service.getReport("payment", "2025-01-01", "2025-12-31");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test(expected = SQLException.class)
    public void testGetReport_payment_sqlException() throws Exception {
        when(mockDao.paymentMethodReport("2025-01-01", "2025-12-31")).thenThrow(new SQLException("DB error"));

        service.getReport("payment", "2025-01-01", "2025-12-31");
    }

    // -----------------------------------------------------------------------
    // getReport — unknown type
    // -----------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void testGetReport_unknownType_throwsIllegalArgumentException() throws Exception {
        service.getReport("unknown", "2025-01-01", "2025-12-31");
    }

    @Test
    public void testGetReport_unknownType_correctMessage() throws Exception {
        try {
            service.getReport("xyz", "2025-01-01", "2025-12-31");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Unknown report type"));
        }
    }

    // -----------------------------------------------------------------------
    // getHeaders
    // -----------------------------------------------------------------------

    @Test
    public void testGetHeaders_staff_returnsCorrectLength() {
        String[] headers = service.getHeaders("staff");
        assertNotNull(headers);
        assertEquals(4, headers.length);
    }

    @Test
    public void testGetHeaders_room_returnsCorrectLength() {
        String[] headers = service.getHeaders("room");
        assertNotNull(headers);
        assertEquals(6, headers.length);
    }

    @Test
    public void testGetHeaders_payment_returnsCorrectLength() {
        String[] headers = service.getHeaders("payment");
        assertNotNull(headers);
        assertEquals(4, headers.length);
    }

    @Test
    public void testGetHeaders_unknown_returnsEmptyArray() {
        String[] headers = service.getHeaders("unknown");
        assertNotNull(headers);
        assertEquals(0, headers.length);
    }

    @Test
    public void testGetHeaders_staff_firstColumnIsStaffName() {
        String[] headers = service.getHeaders("staff");
        assertEquals("Staff Name", headers[0]);
    }

    @Test
    public void testGetHeaders_room_firstColumnIsRoomCategory() {
        String[] headers = service.getHeaders("room");
        assertEquals("Room Category", headers[0]);
    }

    @Test
    public void testGetHeaders_payment_firstColumnIsPaymentMethod() {
        String[] headers = service.getHeaders("payment");
        assertEquals("Payment Method", headers[0]);
    }

    // -----------------------------------------------------------------------
    // getTitle
    // -----------------------------------------------------------------------

    @Test
    public void testGetTitle_staff_returnsCorrectTitle() {
        assertEquals("Staff-Wise Revenue Report", service.getTitle("staff"));
    }

    @Test
    public void testGetTitle_room_returnsCorrectTitle() {
        assertEquals("Room Category Revenue Report", service.getTitle("room"));
    }

    @Test
    public void testGetTitle_payment_returnsCorrectTitle() {
        assertEquals("Payment Method Report", service.getTitle("payment"));
    }

    @Test
    public void testGetTitle_unknown_returnsDefaultTitle() {
        assertEquals("Report", service.getTitle("unknown"));
    }

    // -----------------------------------------------------------------------
    // getMoneyCols
    // -----------------------------------------------------------------------

    @Test
    public void testGetMoneyCols_staff_returnsCorrectCols() {
        int[] cols = service.getMoneyCols("staff");
        assertArrayEquals(new int[]{3}, cols);
    }

    @Test
    public void testGetMoneyCols_room_returnsCorrectCols() {
        int[] cols = service.getMoneyCols("room");
        assertArrayEquals(new int[]{3, 4, 5}, cols);
    }

    @Test
    public void testGetMoneyCols_payment_returnsCorrectCols() {
        int[] cols = service.getMoneyCols("payment");
        assertArrayEquals(new int[]{3}, cols);
    }

    @Test
    public void testGetMoneyCols_unknown_returnsEmptyArray() {
        int[] cols = service.getMoneyCols("unknown");
        assertNotNull(cols);
        assertEquals(0, cols.length);
    }

    // -----------------------------------------------------------------------
    // Static constants
    // -----------------------------------------------------------------------

    @Test
    public void testStaffHeaders_staticConstant_correctLength() {
        assertEquals(4, ReportService.STAFF_HEADERS.length);
    }

    @Test
    public void testRoomHeaders_staticConstant_correctLength() {
        assertEquals(6, ReportService.ROOM_HEADERS.length);
    }

    @Test
    public void testPaymentHeaders_staticConstant_correctLength() {
        assertEquals(4, ReportService.PAYMENT_HEADERS.length);
    }

    @Test
    public void testStaffMoneyCols_staticConstant_correctValues() {
        assertArrayEquals(new int[]{3}, ReportService.STAFF_MONEY_COLS);
    }

    @Test
    public void testRoomMoneyCols_staticConstant_correctValues() {
        assertArrayEquals(new int[]{3, 4, 5}, ReportService.ROOM_MONEY_COLS);
    }

    @Test
    public void testPaymentMoneyCols_staticConstant_correctValues() {
        assertArrayEquals(new int[]{3}, ReportService.PAYMENT_MONEY_COLS);
    }
}
