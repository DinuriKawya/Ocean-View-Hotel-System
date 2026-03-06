package oceanview.test;

import oceanview.dao.BillingDAO;
import oceanview.dao.ExtraChargeDAO;
import oceanview.dao.PaymentDAO;
import oceanview.dao.ReservationDAO;
import oceanview.model.*;
import oceanview.service.BillingService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BillingServiceTest {

    private BillingDAO     mockBillingDAO;
    private ReservationDAO mockReservationDAO;
    private PaymentDAO     mockPaymentDAO;
    private ExtraChargeDAO mockExtraChargeDAO;
    private BillingService service;

    private Reservation    sampleReservation;
    private Payment        samplePayment;
    private ExtraCharge    sampleExtraCharge;
    private BillingRow     sampleBillingRow;

    @Before
    public void setUp() {
        mockBillingDAO     = Mockito.mock(BillingDAO.class);
        mockReservationDAO = Mockito.mock(ReservationDAO.class);
        mockPaymentDAO     = Mockito.mock(PaymentDAO.class);
        mockExtraChargeDAO = Mockito.mock(ExtraChargeDAO.class);

        service = new BillingService(mockBillingDAO, mockReservationDAO,
                                     mockPaymentDAO, mockExtraChargeDAO);

        // sample reservation
        sampleReservation = new Reservation();
        sampleReservation.setReservationId(1);
        sampleReservation.setGuestName("John Doe");
        sampleReservation.setGuestEmail("john@example.com");
        sampleReservation.setRoomType(RoomType.DELUXE);
        sampleReservation.setRoomNumber(101);
        sampleReservation.setCheckInDate(LocalDate.now().minusDays(3));
        sampleReservation.setCheckOutDate(LocalDate.now());
        sampleReservation.setTotalAmount(300.0);
        sampleReservation.setStatus(ReservationStatus.CHECKED_OUT);

        // sample payment
        samplePayment = new Payment();
        samplePayment.setPaymentId(1);
        samplePayment.setReservationId(1);
        samplePayment.setAmount(350.0);
        samplePayment.setMethod(PaymentMethod.CASH);

        // sample extra charge
        sampleExtraCharge = new ExtraCharge();
        sampleExtraCharge.setReservationId(1);
        sampleExtraCharge.setChargeType("Mini Bar");
        sampleExtraCharge.setAmount(50.0);

        // sample billing row
        sampleBillingRow = new BillingRow();
        sampleBillingRow.setReservationId(1);
        sampleBillingRow.setGuestName("John Doe");
        sampleBillingRow.setTotalDue(350.0);
        sampleBillingRow.setTotalPaid(350.0);
        sampleBillingRow.setBalance(0.0);
        sampleBillingRow.setBillingStatus("PAID");
    }

    // ------------------------------------------------------------------
    // getBillingList
    // ------------------------------------------------------------------

    @Test
    public void testGetBillingList_success_returnsList() throws Exception {
        when(mockBillingDAO.findBillingRows(null, null, null, null))
            .thenReturn(Arrays.asList(sampleBillingRow));

        List<BillingRow> result = service.getBillingList(null, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getGuestName());
        verify(mockBillingDAO, times(1)).findBillingRows(null, null, null, null);
    }

    @Test
    public void testGetBillingList_withFilters_returnsFilteredList() throws Exception {
        when(mockBillingDAO.findBillingRows("John", "2025-01-01", "2025-12-31", "CHECKED_OUT"))
            .thenReturn(Arrays.asList(sampleBillingRow));

        List<BillingRow> result = service.getBillingList("John", "2025-01-01", "2025-12-31", "CHECKED_OUT");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetBillingList_empty_returnsEmptyList() throws Exception {
        when(mockBillingDAO.findBillingRows(null, null, null, null))
            .thenReturn(Collections.emptyList());

        List<BillingRow> result = service.getBillingList(null, null, null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test(expected = BillingService.BillingException.class)
    public void testGetBillingList_sqlException_throwsBillingException() throws Exception {
        when(mockBillingDAO.findBillingRows(null, null, null, null))
            .thenThrow(new SQLException("DB error"));

        service.getBillingList(null, null, null, null);
    }

    @Test
    public void testGetBillingList_sqlException_correctMessage() throws Exception {
        when(mockBillingDAO.findBillingRows(null, null, null, null))
            .thenThrow(new SQLException("connection failed"));

        try {
            service.getBillingList(null, null, null, null);
            fail("Expected BillingException");
        } catch (BillingService.BillingException e) {
            assertTrue(e.getMessage().contains("Database error loading billing list"));
        }
    }

    // ------------------------------------------------------------------
    // getDashboardStats
    // ------------------------------------------------------------------

    @Test
    public void testGetDashboardStats_success_returnsMap() throws Exception {
        when(mockBillingDAO.getTotalRevenue("today")).thenReturn(500.0);
        when(mockBillingDAO.getTotalRevenue("month")).thenReturn(5000.0);
        when(mockBillingDAO.getTotalRevenue("year")).thenReturn(50000.0);
        when(mockBillingDAO.getCheckoutCount("month")).thenReturn(20);
        when(mockBillingDAO.getRevenueByMethod()).thenReturn(new ArrayList<>());
        when(mockBillingDAO.getRevenueByRoomType()).thenReturn(new ArrayList<>());
        when(mockBillingDAO.getDailyRevenue(30)).thenReturn(new ArrayList<>());
        when(mockBillingDAO.getRecentPayments(10)).thenReturn(new ArrayList<>());

        Map<String, Object> stats = service.getDashboardStats();

        assertNotNull(stats);
        assertEquals(8, stats.size());
        assertEquals(500.0, (Double) stats.get("revenueToday"), 0.01);
        assertEquals(5000.0, (Double) stats.get("revenueMonth"), 0.01);
        assertEquals(50000.0, (Double) stats.get("revenueYear"), 0.01);
        assertEquals(20, stats.get("checkoutsMonth"));
    }

    @Test
    public void testGetDashboardStats_containsAllKeys() throws Exception {
        when(mockBillingDAO.getTotalRevenue(anyString())).thenReturn(0.0);
        when(mockBillingDAO.getCheckoutCount(anyString())).thenReturn(0);
        when(mockBillingDAO.getRevenueByMethod()).thenReturn(new ArrayList<>());
        when(mockBillingDAO.getRevenueByRoomType()).thenReturn(new ArrayList<>());
        when(mockBillingDAO.getDailyRevenue(30)).thenReturn(new ArrayList<>());
        when(mockBillingDAO.getRecentPayments(10)).thenReturn(new ArrayList<>());

        Map<String, Object> stats = service.getDashboardStats();

        assertTrue(stats.containsKey("revenueToday"));
        assertTrue(stats.containsKey("revenueMonth"));
        assertTrue(stats.containsKey("revenueYear"));
        assertTrue(stats.containsKey("checkoutsMonth"));
        assertTrue(stats.containsKey("revenueByMethod"));
        assertTrue(stats.containsKey("revenueByType"));
        assertTrue(stats.containsKey("dailyRevenue"));
        assertTrue(stats.containsKey("recentPayments"));
    }

    @Test(expected = BillingService.BillingException.class)
    public void testGetDashboardStats_sqlException_throwsBillingException() throws Exception {
        when(mockBillingDAO.getTotalRevenue("today")).thenThrow(new SQLException("DB error"));

        service.getDashboardStats();
    }

    @Test
    public void testGetDashboardStats_sqlException_correctMessage() throws Exception {
        when(mockBillingDAO.getTotalRevenue("today")).thenThrow(new SQLException("connection failed"));

        try {
            service.getDashboardStats();
            fail("Expected BillingException");
        } catch (BillingService.BillingException e) {
            assertTrue(e.getMessage().contains("Database error loading dashboard"));
        }
    }

    // ------------------------------------------------------------------
    // getInvoice
    // ------------------------------------------------------------------

    @Test
    public void testGetInvoice_success_returnsMap() throws Exception {
        when(mockReservationDAO.findById(1)).thenReturn(sampleReservation);
        when(mockPaymentDAO.findByReservationId(1)).thenReturn(Arrays.asList(samplePayment));
        when(mockExtraChargeDAO.findByReservationId(1)).thenReturn(Arrays.asList(sampleExtraCharge));

        Map<String, Object> result = service.getInvoice(1);

        assertNotNull(result);
        assertEquals(sampleReservation, result.get("reservation"));
        assertEquals(50.0,  (Double) result.get("extraTotal"), 0.01);
        assertEquals(350.0, (Double) result.get("totalDue"),   0.01);
        assertEquals(350.0, (Double) result.get("totalPaid"),  0.01);
        assertEquals(0.0,   (Double) result.get("balance"),    0.01);
    }

    @Test
    public void testGetInvoice_noExtraCharges_correctTotals() throws Exception {
        when(mockReservationDAO.findById(1)).thenReturn(sampleReservation);
        when(mockPaymentDAO.findByReservationId(1)).thenReturn(Arrays.asList(samplePayment));
        when(mockExtraChargeDAO.findByReservationId(1)).thenReturn(Collections.emptyList());

        Map<String, Object> result = service.getInvoice(1);

        assertEquals(0.0,   (Double) result.get("extraTotal"), 0.01);
        assertEquals(300.0, (Double) result.get("totalDue"),   0.01);
    }

    @Test
    public void testGetInvoice_noPayments_balanceEqualsTotal() throws Exception {
        when(mockReservationDAO.findById(1)).thenReturn(sampleReservation);
        when(mockPaymentDAO.findByReservationId(1)).thenReturn(Collections.emptyList());
        when(mockExtraChargeDAO.findByReservationId(1)).thenReturn(Collections.emptyList());

        Map<String, Object> result = service.getInvoice(1);

        assertEquals(0.0,   (Double) result.get("totalPaid"), 0.01);
        assertEquals(300.0, (Double) result.get("balance"),   0.01);
    }

    @Test
    public void testGetInvoice_containsAllKeys() throws Exception {
        when(mockReservationDAO.findById(1)).thenReturn(sampleReservation);
        when(mockPaymentDAO.findByReservationId(1)).thenReturn(Collections.emptyList());
        when(mockExtraChargeDAO.findByReservationId(1)).thenReturn(Collections.emptyList());

        Map<String, Object> result = service.getInvoice(1);

        assertTrue(result.containsKey("reservation"));
        assertTrue(result.containsKey("payments"));
        assertTrue(result.containsKey("extraCharges"));
        assertTrue(result.containsKey("extraTotal"));
        assertTrue(result.containsKey("totalDue"));
        assertTrue(result.containsKey("totalPaid"));
        assertTrue(result.containsKey("balance"));
    }

    @Test(expected = BillingService.BillingException.class)
    public void testGetInvoice_reservationNotFound_throwsBillingException() throws Exception {
        when(mockReservationDAO.findById(99)).thenReturn(null);

        service.getInvoice(99);
    }

    @Test
    public void testGetInvoice_reservationNotFound_correctMessage() throws Exception {
        when(mockReservationDAO.findById(99)).thenReturn(null);

        try {
            service.getInvoice(99);
            fail("Expected BillingException");
        } catch (BillingService.BillingException e) {
            assertEquals("Reservation #99 not found.", e.getMessage());
        }
    }

    @Test(expected = BillingService.BillingException.class)
    public void testGetInvoice_sqlException_throwsBillingException() throws Exception {
        when(mockReservationDAO.findById(1)).thenThrow(new SQLException("DB error"));

        service.getInvoice(1);
    }

    @Test
    public void testGetInvoice_sqlException_correctMessage() throws Exception {
        when(mockReservationDAO.findById(1)).thenThrow(new SQLException("connection failed"));

        try {
            service.getInvoice(1);
            fail("Expected BillingException");
        } catch (BillingService.BillingException e) {
            assertTrue(e.getMessage().contains("Database error loading invoice"));
        }
    }

    // ------------------------------------------------------------------
    // getFolio
    // ------------------------------------------------------------------

    @Test
    public void testGetFolio_success_returnsSameAsInvoice() throws Exception {
        when(mockReservationDAO.findById(1)).thenReturn(sampleReservation);
        when(mockPaymentDAO.findByReservationId(1)).thenReturn(Arrays.asList(samplePayment));
        when(mockExtraChargeDAO.findByReservationId(1)).thenReturn(Arrays.asList(sampleExtraCharge));

        Map<String, Object> folio   = service.getFolio(1);
        Map<String, Object> invoice = service.getInvoice(1);

        assertNotNull(folio);
        assertEquals(invoice.get("totalDue"),  folio.get("totalDue"));
        assertEquals(invoice.get("totalPaid"), folio.get("totalPaid"));
        assertEquals(invoice.get("balance"),   folio.get("balance"));
    }

    @Test(expected = BillingService.BillingException.class)
    public void testGetFolio_reservationNotFound_throwsBillingException() throws Exception {
        when(mockReservationDAO.findById(99)).thenReturn(null);

        service.getFolio(99);
    }

    // ------------------------------------------------------------------
    // BillingException
    // ------------------------------------------------------------------

    @Test
    public void testBillingException_message() {
        BillingService.BillingException ex =
            new BillingService.BillingException("Test error");
        assertEquals("Test error", ex.getMessage());
    }

    @Test
    public void testBillingException_isException() {
        BillingService.BillingException ex =
            new BillingService.BillingException("error");
        assertTrue(ex instanceof Exception);
    }
}
