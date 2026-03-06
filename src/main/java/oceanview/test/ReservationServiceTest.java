package oceanview.test;

import oceanview.dao.ReservationDAO;
import oceanview.model.Reservation;
import oceanview.model.ReservationStatus;
import oceanview.model.RoomType;
import oceanview.service.ReservationService;

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

public class ReservationServiceTest {

    private ReservationDAO mockDao;
    private ReservationService service;
    private Reservation validReservation;

    @Before
    public void setUp() {
        mockDao = Mockito.mock(ReservationDAO.class);
        service = new ReservationService(mockDao);

        validReservation = new Reservation();
        validReservation.setReservationId(1);
        validReservation.setGuestName("John Doe");
        validReservation.setGuestEmail("john@example.com");
        validReservation.setGuestPhone("0771234567");
        validReservation.setRoomType(RoomType.DELUXE);
        validReservation.setRoomNumber(101);
        validReservation.setCheckInDate(LocalDate.now().plusDays(1));
        validReservation.setCheckOutDate(LocalDate.now().plusDays(3));
        validReservation.setNumberOfGuests(2);
        validReservation.setTotalAmount(300.0);
        validReservation.setStatus(ReservationStatus.PENDING);
    }

    // -----------------------------------------------------------------------
    // createReservation
    // -----------------------------------------------------------------------

    @Test
    public void testCreateReservation_success() throws Exception {
        when(mockDao.insert(validReservation)).thenReturn(1);
        Reservation result = service.createReservation(validReservation, "admin");
        assertNotNull(result);
        assertEquals(1, result.getReservationId());
        assertEquals(ReservationStatus.PENDING, result.getStatus());
        assertEquals("admin", result.getCreatedBy());
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testCreateReservation_insertFails_throwsException() throws Exception {
        when(mockDao.insert(validReservation)).thenReturn(-1);
        service.createReservation(validReservation, "admin");
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testCreateReservation_sqlException_throwsException() throws Exception {
        when(mockDao.insert(validReservation)).thenThrow(new SQLException("DB error"));
        service.createReservation(validReservation, "admin");
    }

    // -----------------------------------------------------------------------
    // createReservation — validation
    // -----------------------------------------------------------------------

    @Test(expected = ReservationService.ReservationException.class)
    public void testCreateReservation_nullGuestName_throwsException() throws Exception {
        validReservation.setGuestName(null);
        service.createReservation(validReservation, "admin");
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testCreateReservation_blankGuestName_throwsException() throws Exception {
        validReservation.setGuestName("   ");
        service.createReservation(validReservation, "admin");
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testCreateReservation_invalidEmail_throwsException() throws Exception {
        validReservation.setGuestEmail("invalidemail");
        service.createReservation(validReservation, "admin");
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testCreateReservation_nullEmail_throwsException() throws Exception {
        validReservation.setGuestEmail(null);
        service.createReservation(validReservation, "admin");
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testCreateReservation_nullRoomType_throwsException() throws Exception {
        validReservation.setRoomType(null);
        service.createReservation(validReservation, "admin");
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testCreateReservation_nullCheckInDate_throwsException() throws Exception {
        validReservation.setCheckInDate(null);
        service.createReservation(validReservation, "admin");
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testCreateReservation_nullCheckOutDate_throwsException() throws Exception {
        validReservation.setCheckOutDate(null);
        service.createReservation(validReservation, "admin");
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testCreateReservation_checkOutBeforeCheckIn_throwsException() throws Exception {
        validReservation.setCheckOutDate(LocalDate.now().plusDays(1));
        validReservation.setCheckInDate(LocalDate.now().plusDays(3));
        service.createReservation(validReservation, "admin");
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testCreateReservation_checkInSameDayAsCheckOut_throwsException() throws Exception {
        LocalDate date = LocalDate.now().plusDays(2);
        validReservation.setCheckInDate(date);
        validReservation.setCheckOutDate(date);
        service.createReservation(validReservation, "admin");
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testCreateReservation_checkInInPast_throwsException() throws Exception {
        validReservation.setCheckInDate(LocalDate.now().minusDays(1));
        service.createReservation(validReservation, "admin");
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testCreateReservation_zeroGuests_throwsException() throws Exception {
        validReservation.setNumberOfGuests(0);
        service.createReservation(validReservation, "admin");
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testCreateReservation_guestsExceedRoomMax_throwsException() throws Exception {
        // DELUXE max is 3, set 10 guests
        validReservation.setNumberOfGuests(10);
        service.createReservation(validReservation, "admin");
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testCreateReservation_negativeTotalAmount_throwsException() throws Exception {
        validReservation.setTotalAmount(-100.0);
        service.createReservation(validReservation, "admin");
    }

    // -----------------------------------------------------------------------
    // getById
    // -----------------------------------------------------------------------

    @Test
    public void testGetById_success() throws Exception {
        when(mockDao.findById(1)).thenReturn(validReservation);
        Reservation result = service.getById(1);
        assertNotNull(result);
        assertEquals(1, result.getReservationId());
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testGetById_notFound_throwsException() throws Exception {
        when(mockDao.findById(99)).thenReturn(null);
        service.getById(99);
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testGetById_sqlException_throwsException() throws Exception {
        when(mockDao.findById(1)).thenThrow(new SQLException("DB error"));
        service.getById(1);
    }

    // -----------------------------------------------------------------------
    // getAllReservations
    // -----------------------------------------------------------------------

    @Test
    public void testGetAllReservations_success() throws Exception {
        when(mockDao.findAll()).thenReturn(Arrays.asList(validReservation));
        List<Reservation> result = service.getAllReservations();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetAllReservations_empty_returnsEmptyList() throws Exception {
        when(mockDao.findAll()).thenReturn(Collections.emptyList());
        List<Reservation> result = service.getAllReservations();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testGetAllReservations_sqlException_throwsException() throws Exception {
        when(mockDao.findAll()).thenThrow(new SQLException("DB error"));
        service.getAllReservations();
    }

    // -----------------------------------------------------------------------
    // searchByGuestName
    // -----------------------------------------------------------------------

    @Test
    public void testSearchByGuestName_success_returnsMatches() throws Exception {
        when(mockDao.findAll()).thenReturn(Arrays.asList(validReservation));
        List<Reservation> result = service.searchByGuestName("John");
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testSearchByGuestName_nullName_throwsException() throws Exception {
        service.searchByGuestName(null);
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testSearchByGuestName_blankName_throwsException() throws Exception {
        service.searchByGuestName("   ");
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testSearchByGuestName_sqlException_throwsException() throws Exception {
        when(mockDao.findAll()).thenThrow(new SQLException("DB error"));
        service.searchByGuestName("John");
    }

    // -----------------------------------------------------------------------
    // getByStatus
    // -----------------------------------------------------------------------

    @Test
    public void testGetByStatus_success_returnsMatchingReservations() throws Exception {
        when(mockDao.findAll()).thenReturn(Arrays.asList(validReservation));
        List<Reservation> result = service.getByStatus(ReservationStatus.PENDING);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testGetByStatus_sqlException_throwsException() throws Exception {
        when(mockDao.findAll()).thenThrow(new SQLException("DB error"));
        service.getByStatus(ReservationStatus.PENDING);
    }

    // -----------------------------------------------------------------------
    // searchActiveByGuestName
    // -----------------------------------------------------------------------

    @Test
    public void testSearchActiveByGuestName_success_returnsNonCancelled() throws Exception {
        when(mockDao.findAll()).thenReturn(Arrays.asList(validReservation));
        List<Reservation> result = service.searchActiveByGuestName("John");
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testSearchActiveByGuestName_nullName_throwsException() throws Exception {
        service.searchActiveByGuestName(null);
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testSearchActiveByGuestName_blankName_throwsException() throws Exception {
        service.searchActiveByGuestName("");
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testSearchActiveByGuestName_sqlException_throwsException() throws Exception {
        when(mockDao.findAll()).thenThrow(new SQLException("DB error"));
        service.searchActiveByGuestName("John");
    }

    // -----------------------------------------------------------------------
    // updateReservation
    // -----------------------------------------------------------------------

    @Test
    public void testUpdateReservation_success() throws Exception {
        when(mockDao.update(validReservation)).thenReturn(true);
        Reservation result = service.updateReservation(validReservation);
        assertNotNull(result);
        verify(mockDao, times(1)).update(validReservation);
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testUpdateReservation_notFound_throwsException() throws Exception {
        when(mockDao.update(validReservation)).thenReturn(false);
        service.updateReservation(validReservation);
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testUpdateReservation_cancelledStatus_throwsException() throws Exception {
        validReservation.setStatus(ReservationStatus.CANCELLED);
        service.updateReservation(validReservation);
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testUpdateReservation_checkedOutStatus_throwsException() throws Exception {
        validReservation.setStatus(ReservationStatus.CHECKED_OUT);
        service.updateReservation(validReservation);
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testUpdateReservation_sqlException_throwsException() throws Exception {
        when(mockDao.update(validReservation)).thenThrow(new SQLException("DB error"));
        service.updateReservation(validReservation);
    }

    // -----------------------------------------------------------------------
    // changeStatus
    // -----------------------------------------------------------------------

    @Test
    public void testChangeStatus_success() throws Exception {
        when(mockDao.updateStatus(1, ReservationStatus.CONFIRMED)).thenReturn(true);
        service.changeStatus(1, ReservationStatus.CONFIRMED);
        verify(mockDao, times(1)).updateStatus(1, ReservationStatus.CONFIRMED);
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testChangeStatus_notFound_throwsException() throws Exception {
        when(mockDao.updateStatus(99, ReservationStatus.CONFIRMED)).thenReturn(false);
        service.changeStatus(99, ReservationStatus.CONFIRMED);
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testChangeStatus_sqlException_throwsException() throws Exception {
        when(mockDao.updateStatus(1, ReservationStatus.CONFIRMED)).thenThrow(new SQLException("DB error"));
        service.changeStatus(1, ReservationStatus.CONFIRMED);
    }

    // -----------------------------------------------------------------------
    // cancelReservation
    // -----------------------------------------------------------------------

    @Test
    public void testCancelReservation_success() throws Exception {
        when(mockDao.updateStatus(1, ReservationStatus.CANCELLED)).thenReturn(true);
        service.cancelReservation(1);
        verify(mockDao, times(1)).updateStatus(1, ReservationStatus.CANCELLED);
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testCancelReservation_notFound_throwsException() throws Exception {
        when(mockDao.updateStatus(99, ReservationStatus.CANCELLED)).thenReturn(false);
        service.cancelReservation(99);
    }

    // -----------------------------------------------------------------------
    // deleteReservation
    // -----------------------------------------------------------------------

    @Test
    public void testDeleteReservation_success() throws Exception {
        when(mockDao.delete(1)).thenReturn(true);
        service.deleteReservation(1);
        verify(mockDao, times(1)).delete(1);
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testDeleteReservation_notFound_throwsException() throws Exception {
        when(mockDao.delete(99)).thenReturn(false);
        service.deleteReservation(99);
    }

    @Test(expected = ReservationService.ReservationException.class)
    public void testDeleteReservation_sqlException_throwsException() throws Exception {
        when(mockDao.delete(1)).thenThrow(new SQLException("DB error"));
        service.deleteReservation(1);
    }

    // -----------------------------------------------------------------------
    // ReservationException
    // -----------------------------------------------------------------------

    @Test
    public void testReservationException_message() {
        ReservationService.ReservationException ex =
            new ReservationService.ReservationException("Test error");
        assertEquals("Test error", ex.getMessage());
    }
}
