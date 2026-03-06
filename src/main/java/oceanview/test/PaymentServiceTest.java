package oceanview.test;

import oceanview.dao.BankDAO;
import oceanview.dao.ExtraChargeDAO;
import oceanview.dao.PaymentDAO;
import oceanview.dao.ReservationDAO;
import oceanview.model.*;
import oceanview.service.PaymentService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PaymentServiceTest {

    private PaymentDAO     mockPaymentDAO;
    private ReservationDAO mockReservationDAO;
    private BankDAO        mockBankDAO;
    private ExtraChargeDAO mockExtraChargeDAO;
    private PaymentService service;

    private Reservation confirmedReservation;
    private Reservation checkedInReservation;
    private Payment     cashPayment;
    private Payment     cardPayment;
    private Payment     transferPayment;

    @Before
    public void setUp() {
        mockPaymentDAO     = Mockito.mock(PaymentDAO.class);
        mockReservationDAO = Mockito.mock(ReservationDAO.class);
        mockBankDAO        = Mockito.mock(BankDAO.class);
        mockExtraChargeDAO = Mockito.mock(ExtraChargeDAO.class);

        service = new PaymentService(mockPaymentDAO, mockReservationDAO,
                                     mockBankDAO, mockExtraChargeDAO);

        // confirmed reservation
        confirmedReservation = new Reservation();
        confirmedReservation.setReservationId(1);
        confirmedReservation.setGuestName("John Doe");
        confirmedReservation.setStatus(ReservationStatus.CONFIRMED);
        confirmedReservation.setTotalAmount(300.0);
        confirmedReservation.setCheckInDate(LocalDate.now().plusDays(1));
        confirmedReservation.setCheckOutDate(LocalDate.now().plusDays(3));

        // checked-in reservation
        checkedInReservation = new Reservation();
        checkedInReservation.setReservationId(2);
        checkedInReservation.setGuestName("Jane Doe");
        checkedInReservation.setStatus(ReservationStatus.CHECKED_IN);
        checkedInReservation.setTotalAmount(300.0);
        checkedInReservation.setCheckInDate(LocalDate.now().plusDays(1));
        checkedInReservation.setCheckOutDate(LocalDate.now().plusDays(3));

        // cash payment
        cashPayment = new Payment();
        cashPayment.setReservationId(2);
        cashPayment.setAmount(300.0);
        cashPayment.setMethod(PaymentMethod.CASH);
        cashPayment.setCreatedBy("admin");

        // card payment
        cardPayment = new Payment();
        cardPayment.setReservationId(2);
        cardPayment.setAmount(300.0);
        cardPayment.setMethod(PaymentMethod.CARD);
        cardPayment.setBankId(1);
        cardPayment.setCardLast4("1234");
        cardPayment.setCreatedBy("admin");

        // transfer payment
        transferPayment = new Payment();
        transferPayment.setReservationId(2);
        transferPayment.setAmount(300.0);
        transferPayment.setMethod(PaymentMethod.TRANSFER);
        transferPayment.setBankId(1);
        transferPayment.setReferenceNo("REF123456");
        transferPayment.setCreatedBy("admin");
    }

    // -----------------------------------------------------------------------
    // checkIn
    // -----------------------------------------------------------------------

    @Test
    public void testCheckIn_success() throws Exception {
        when(mockReservationDAO.findById(1)).thenReturn(confirmedReservation);

        service.checkIn(1, "admin");

        verify(mockReservationDAO, times(1)).updateStatus(1, ReservationStatus.CHECKED_IN);
    }

    @Test(expected = PaymentService.PaymentException.class)
    public void testCheckIn_reservationNotFound_throwsException() throws Exception {
        when(mockReservationDAO.findById(99)).thenReturn(null);

        service.checkIn(99, "admin");
    }

    @Test(expected = PaymentService.PaymentException.class)
    public void testCheckIn_notConfirmedStatus_throwsException() throws Exception {
        confirmedReservation.setStatus(ReservationStatus.PENDING);
        when(mockReservationDAO.findById(1)).thenReturn(confirmedReservation);

        service.checkIn(1, "admin");
    }

    @Test
    public void testCheckIn_notConfirmed_correctMessage() throws Exception {
        confirmedReservation.setStatus(ReservationStatus.PENDING);
        when(mockReservationDAO.findById(1)).thenReturn(confirmedReservation);

        try {
            service.checkIn(1, "admin");
            fail("Expected PaymentException");
        } catch (PaymentService.PaymentException e) {
            assertTrue(e.getMessage().contains("CONFIRMED"));
        }
    }

    @Test(expected = PaymentService.PaymentException.class)
    public void testCheckIn_sqlException_throwsException() throws Exception {
        when(mockReservationDAO.findById(1)).thenThrow(new SQLException("DB error"));

        service.checkIn(1, "admin");
    }

    // -----------------------------------------------------------------------
    // saveExtraCharges
    // -----------------------------------------------------------------------

    @Test
    public void testSaveExtraCharges_nullList_doesNothing() throws Exception {
        service.saveExtraCharges(1, null, "admin");

        verify(mockExtraChargeDAO, never()).insert(any());
    }

    @Test
    public void testSaveExtraCharges_emptyList_doesNothing() throws Exception {
        service.saveExtraCharges(1, Collections.emptyList(), "admin");

        verify(mockExtraChargeDAO, never()).insert(any());
    }

    @Test
    public void testSaveExtraCharges_success() throws Exception {
        ExtraCharge ec = new ExtraCharge();
        ec.setChargeType("Mini Bar");
        ec.setAmount(50.0);

        service.saveExtraCharges(1, Arrays.asList(ec), "admin");

        verify(mockExtraChargeDAO, times(1)).insert(ec);
        assertEquals(1, ec.getReservationId());
        assertEquals("admin", ec.getAddedBy());
    }

    @Test(expected = PaymentService.PaymentException.class)
    public void testSaveExtraCharges_nullChargeType_throwsException() throws Exception {
        ExtraCharge ec = new ExtraCharge();
        ec.setChargeType(null);
        ec.setAmount(50.0);

        service.saveExtraCharges(1, Arrays.asList(ec), "admin");
    }

    @Test(expected = PaymentService.PaymentException.class)
    public void testSaveExtraCharges_blankChargeType_throwsException() throws Exception {
        ExtraCharge ec = new ExtraCharge();
        ec.setChargeType("   ");
        ec.setAmount(50.0);

        service.saveExtraCharges(1, Arrays.asList(ec), "admin");
    }

    @Test(expected = PaymentService.PaymentException.class)
    public void testSaveExtraCharges_zeroAmount_throwsException() throws Exception {
        ExtraCharge ec = new ExtraCharge();
        ec.setChargeType("Mini Bar");
        ec.setAmount(0);

        service.saveExtraCharges(1, Arrays.asList(ec), "admin");
    }

    @Test(expected = PaymentService.PaymentException.class)
    public void testSaveExtraCharges_negativeAmount_throwsException() throws Exception {
        ExtraCharge ec = new ExtraCharge();
        ec.setChargeType("Mini Bar");
        ec.setAmount(-10.0);

        service.saveExtraCharges(1, Arrays.asList(ec), "admin");
    }

    @Test(expected = PaymentService.PaymentException.class)
    public void testSaveExtraCharges_sqlException_throwsException() throws Exception {
        ExtraCharge ec = new ExtraCharge();
        ec.setChargeType("Mini Bar");
        ec.setAmount(50.0);

        doThrow(new SQLException("DB error")).when(mockExtraChargeDAO).insert(ec);

        service.saveExtraCharges(1, Arrays.asList(ec), "admin");
    }

    // -----------------------------------------------------------------------
    // getPaymentsByReservation
    // -----------------------------------------------------------------------

    @Test
    public void testGetPaymentsByReservation_success() throws Exception {
        when(mockPaymentDAO.findByReservationId(1)).thenReturn(Arrays.asList(cashPayment));

        List<Payment> result = service.getPaymentsByReservation(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(mockPaymentDAO, times(1)).findByReservationId(1);
    }

    @Test
    public void testGetPaymentsByReservation_empty_returnsEmptyList() throws Exception {
        when(mockPaymentDAO.findByReservationId(1)).thenReturn(Collections.emptyList());

        List<Payment> result = service.getPaymentsByReservation(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test(expected = PaymentService.PaymentException.class)
    public void testGetPaymentsByReservation_sqlException_throwsException() throws Exception {
        when(mockPaymentDAO.findByReservationId(1)).thenThrow(new SQLException("DB error"));

        service.getPaymentsByReservation(1);
    }

    // -----------------------------------------------------------------------
    // getTotalPaid
    // -----------------------------------------------------------------------

    @Test
    public void testGetTotalPaid_success() throws Exception {
        when(mockPaymentDAO.sumByReservationId(1)).thenReturn(300.0);

        double result = service.getTotalPaid(1);

        assertEquals(300.0, result, 0.01);
    }

    @Test
    public void testGetTotalPaid_zero_returnsZero() throws Exception {
        when(mockPaymentDAO.sumByReservationId(1)).thenReturn(0.0);

        double result = service.getTotalPaid(1);

        assertEquals(0.0, result, 0.01);
    }

    @Test(expected = PaymentService.PaymentException.class)
    public void testGetTotalPaid_sqlException_throwsException() throws Exception {
        when(mockPaymentDAO.sumByReservationId(1)).thenThrow(new SQLException("DB error"));

        service.getTotalPaid(1);
    }

    // -----------------------------------------------------------------------
    // getExtraChargesByReservation
    // -----------------------------------------------------------------------

    @Test
    public void testGetExtraChargesByReservation_success() throws Exception {
        ExtraCharge ec = new ExtraCharge();
        ec.setChargeType("Mini Bar");
        ec.setAmount(50.0);

        when(mockExtraChargeDAO.findByReservationId(1)).thenReturn(Arrays.asList(ec));

        List<ExtraCharge> result = service.getExtraChargesByReservation(1);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test(expected = PaymentService.PaymentException.class)
    public void testGetExtraChargesByReservation_sqlException_throwsException() throws Exception {
        when(mockExtraChargeDAO.findByReservationId(1)).thenThrow(new SQLException("DB error"));

        service.getExtraChargesByReservation(1);
    }

    // -----------------------------------------------------------------------
    // getTotalExtraCharges
    // -----------------------------------------------------------------------

    @Test
    public void testGetTotalExtraCharges_success() throws Exception {
        when(mockExtraChargeDAO.sumByReservationId(1)).thenReturn(75.0);

        double result = service.getTotalExtraCharges(1);

        assertEquals(75.0, result, 0.01);
    }

    @Test(expected = PaymentService.PaymentException.class)
    public void testGetTotalExtraCharges_sqlException_throwsException() throws Exception {
        when(mockExtraChargeDAO.sumByReservationId(1)).thenThrow(new SQLException("DB error"));

        service.getTotalExtraCharges(1);
    }

    // -----------------------------------------------------------------------
    // PaymentException
    // -----------------------------------------------------------------------

    @Test
    public void testPaymentException_message() {
        PaymentService.PaymentException ex =
            new PaymentService.PaymentException("Test error");
        assertEquals("Test error", ex.getMessage());
    }

    @Test
    public void testPaymentException_isException() {
        PaymentService.PaymentException ex =
            new PaymentService.PaymentException("error");
        assertTrue(ex instanceof Exception);
    }
}
