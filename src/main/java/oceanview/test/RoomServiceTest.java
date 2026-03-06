package oceanview.test;

import oceanview.dao.RoomDAO;
import oceanview.model.Room;
import oceanview.model.RoomStatus;
import oceanview.model.RoomType;
import oceanview.service.RoomService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RoomServiceTest {

    private RoomDAO mockDao;
    private RoomService service;
    private Room validRoom;

    @Before
    public void setUp() throws Exception {
        mockDao  = Mockito.mock(RoomDAO.class);
        service  = new RoomService(mockDao);

        validRoom = new Room();
        validRoom.setRoomId(1);
        validRoom.setRoomNumber(101);
        validRoom.setRoomType(RoomType.DELUXE);
        validRoom.setPricePerNight(150.0);
        validRoom.setFloor(1);
        validRoom.setStatus(RoomStatus.AVAILABLE);
    }

    // -----------------------------------------------------------------------
    // getAllRooms
    // -----------------------------------------------------------------------

    @Test
    public void testGetAllRooms_success() throws Exception {
        when(mockDao.findAll()).thenReturn(Arrays.asList(validRoom));
        List<Room> rooms = service.getAllRooms();
        assertNotNull(rooms);
        assertEquals(1, rooms.size());
    }

    @Test(expected = RoomService.RoomException.class)
    public void testGetAllRooms_sqlException() throws Exception {
        when(mockDao.findAll()).thenThrow(new SQLException("DB error"));
        service.getAllRooms();
    }

    // -----------------------------------------------------------------------
    // getById
    // -----------------------------------------------------------------------

    @Test
    public void testGetById_success() throws Exception {
        when(mockDao.findById(1)).thenReturn(validRoom);
        Room result = service.getById(1);
        assertNotNull(result);
        assertEquals(1, result.getRoomId());
    }

    @Test(expected = RoomService.RoomException.class)
    public void testGetById_notFound() throws Exception {
        when(mockDao.findById(99)).thenReturn(null);
        service.getById(99);
    }

    @Test(expected = RoomService.RoomException.class)
    public void testGetById_sqlException() throws Exception {
        when(mockDao.findById(1)).thenThrow(new SQLException("DB error"));
        service.getById(1);
    }

    // -----------------------------------------------------------------------
    // getByRoomNumber
    // -----------------------------------------------------------------------

    @Test
    public void testGetByRoomNumber_success() throws Exception {
        when(mockDao.findByRoomNumber(101)).thenReturn(validRoom);
        Room result = service.getByRoomNumber(101);
        assertNotNull(result);
        assertEquals(101, result.getRoomNumber());
    }

    @Test(expected = RoomService.RoomException.class)
    public void testGetByRoomNumber_notFound() throws Exception {
        when(mockDao.findByRoomNumber(999)).thenReturn(null);
        service.getByRoomNumber(999);
    }

    @Test(expected = RoomService.RoomException.class)
    public void testGetByRoomNumber_sqlException() throws Exception {
        when(mockDao.findByRoomNumber(101)).thenThrow(new SQLException("DB error"));
        service.getByRoomNumber(101);
    }

    // -----------------------------------------------------------------------
    // getAvailableRooms
    // -----------------------------------------------------------------------

    @Test
    public void testGetAvailableRooms_success() throws Exception {
        when(mockDao.findAvailable()).thenReturn(Arrays.asList(validRoom));
        List<Room> rooms = service.getAvailableRooms();
        assertNotNull(rooms);
        assertFalse(rooms.isEmpty());
    }

    @Test(expected = RoomService.RoomException.class)
    public void testGetAvailableRooms_sqlException() throws Exception {
        when(mockDao.findAvailable()).thenThrow(new SQLException("DB error"));
        service.getAvailableRooms();
    }

    // -----------------------------------------------------------------------
    // getAvailableRoomsForDates
    // -----------------------------------------------------------------------

    @Test
    public void testGetAvailableRoomsForDates_success() throws Exception {
        LocalDate checkIn  = LocalDate.of(2025, 6, 1);
        LocalDate checkOut = LocalDate.of(2025, 6, 5);
        when(mockDao.findAvailableForDates(checkIn, checkOut)).thenReturn(Arrays.asList(validRoom));
        List<Room> rooms = service.getAvailableRoomsForDates(checkIn, checkOut);
        assertNotNull(rooms);
        assertEquals(1, rooms.size());
    }

    @Test(expected = RoomService.RoomException.class)
    public void testGetAvailableRoomsForDates_sqlException() throws Exception {
        LocalDate checkIn  = LocalDate.of(2025, 6, 1);
        LocalDate checkOut = LocalDate.of(2025, 6, 5);
        when(mockDao.findAvailableForDates(checkIn, checkOut)).thenThrow(new SQLException("DB error"));
        service.getAvailableRoomsForDates(checkIn, checkOut);
    }

    // -----------------------------------------------------------------------
    // createRoom
    // -----------------------------------------------------------------------

    @Test
    public void testCreateRoom_success() throws Exception {
        when(mockDao.insert(validRoom)).thenReturn(1);
        Room result = service.createRoom(validRoom);
        assertNotNull(result);
        assertEquals(1, result.getRoomId());
    }

    @Test(expected = RoomService.RoomException.class)
    public void testCreateRoom_insertFails() throws Exception {
        when(mockDao.insert(validRoom)).thenReturn(-1);
        service.createRoom(validRoom);
    }

    @Test(expected = RoomService.RoomException.class)
    public void testCreateRoom_sqlException() throws Exception {
        when(mockDao.insert(validRoom)).thenThrow(new SQLException("DB error"));
        service.createRoom(validRoom);
    }

    @Test(expected = RoomService.RoomException.class)
    public void testCreateRoom_zeroRoomNumber() throws Exception {
        validRoom.setRoomNumber(0);
        service.createRoom(validRoom);
    }

    @Test(expected = RoomService.RoomException.class)
    public void testCreateRoom_negativeRoomNumber() throws Exception {
        validRoom.setRoomNumber(-5);
        service.createRoom(validRoom);
    }

    @Test(expected = RoomService.RoomException.class)
    public void testCreateRoom_nullRoomType() throws Exception {
        validRoom.setRoomType(null);
        service.createRoom(validRoom);
    }

    @Test(expected = RoomService.RoomException.class)
    public void testCreateRoom_zeroPricePerNight() throws Exception {
        validRoom.setPricePerNight(0);
        service.createRoom(validRoom);
    }

    @Test(expected = RoomService.RoomException.class)
    public void testCreateRoom_negativePricePerNight() throws Exception {
        validRoom.setPricePerNight(-99.9);
        service.createRoom(validRoom);
    }

    @Test(expected = RoomService.RoomException.class)
    public void testCreateRoom_zeroFloor() throws Exception {
        validRoom.setFloor(0);
        service.createRoom(validRoom);
    }

    @Test(expected = RoomService.RoomException.class)
    public void testCreateRoom_negativeFloor() throws Exception {
        validRoom.setFloor(-1);
        service.createRoom(validRoom);
    }

    @Test(expected = RoomService.RoomException.class)
    public void testCreateRoom_nullStatus() throws Exception {
        validRoom.setStatus(null);
        service.createRoom(validRoom);
    }

    // -----------------------------------------------------------------------
    // updateRoom
    // -----------------------------------------------------------------------

    @Test
    public void testUpdateRoom_success() throws Exception {
        when(mockDao.update(validRoom)).thenReturn(true);
        Room result = service.updateRoom(validRoom);
        assertNotNull(result);
    }

    @Test(expected = RoomService.RoomException.class)
    public void testUpdateRoom_notFound() throws Exception {
        when(mockDao.update(validRoom)).thenReturn(false);
        service.updateRoom(validRoom);
    }

    @Test(expected = RoomService.RoomException.class)
    public void testUpdateRoom_sqlException() throws Exception {
        when(mockDao.update(validRoom)).thenThrow(new SQLException("DB error"));
        service.updateRoom(validRoom);
    }

    @Test(expected = RoomService.RoomException.class)
    public void testUpdateRoom_invalidRoomNumber() throws Exception {
        validRoom.setRoomNumber(0);
        service.updateRoom(validRoom);
    }

    @Test(expected = RoomService.RoomException.class)
    public void testUpdateRoom_nullRoomType() throws Exception {
        validRoom.setRoomType(null);
        service.updateRoom(validRoom);
    }

    @Test(expected = RoomService.RoomException.class)
    public void testUpdateRoom_invalidPrice() throws Exception {
        validRoom.setPricePerNight(0);
        service.updateRoom(validRoom);
    }

    @Test(expected = RoomService.RoomException.class)
    public void testUpdateRoom_invalidFloor() throws Exception {
        validRoom.setFloor(0);
        service.updateRoom(validRoom);
    }

    @Test(expected = RoomService.RoomException.class)
    public void testUpdateRoom_nullStatus() throws Exception {
        validRoom.setStatus(null);
        service.updateRoom(validRoom);
    }

    // -----------------------------------------------------------------------
    // changeStatus
    // -----------------------------------------------------------------------

    @Test
    public void testChangeStatus_success() throws Exception {
        when(mockDao.updateStatus(1, RoomStatus.OCCUPIED)).thenReturn(true);
        service.changeStatus(1, RoomStatus.OCCUPIED);
        verify(mockDao, times(1)).updateStatus(1, RoomStatus.OCCUPIED);
    }

    @Test(expected = RoomService.RoomException.class)
    public void testChangeStatus_notFound() throws Exception {
        when(mockDao.updateStatus(99, RoomStatus.OCCUPIED)).thenReturn(false);
        service.changeStatus(99, RoomStatus.OCCUPIED);
    }

    @Test(expected = RoomService.RoomException.class)
    public void testChangeStatus_sqlException() throws Exception {
        when(mockDao.updateStatus(1, RoomStatus.OCCUPIED)).thenThrow(new SQLException("DB error"));
        service.changeStatus(1, RoomStatus.OCCUPIED);
    }

    // -----------------------------------------------------------------------
    // deleteRoom
    // -----------------------------------------------------------------------

    @Test
    public void testDeleteRoom_success() throws Exception {
        when(mockDao.findById(1)).thenReturn(validRoom);
        when(mockDao.delete(1)).thenReturn(true);
        service.deleteRoom(1);
        verify(mockDao, times(1)).delete(1);
    }

    @Test(expected = RoomService.RoomException.class)
    public void testDeleteRoom_notFound() throws Exception {
        when(mockDao.findById(99)).thenReturn(null);
        service.deleteRoom(99);
    }

    @Test(expected = RoomService.RoomException.class)
    public void testDeleteRoom_occupiedRoom() throws Exception {
        validRoom.setStatus(RoomStatus.OCCUPIED);
        when(mockDao.findById(1)).thenReturn(validRoom);
        service.deleteRoom(1);
    }

    @Test(expected = RoomService.RoomException.class)
    public void testDeleteRoom_deleteFails() throws Exception {
        when(mockDao.findById(1)).thenReturn(validRoom);
        when(mockDao.delete(1)).thenReturn(false);
        service.deleteRoom(1);
    }

    @Test(expected = RoomService.RoomException.class)
    public void testDeleteRoom_sqlException() throws Exception {
        when(mockDao.findById(1)).thenThrow(new SQLException("DB error"));
        service.deleteRoom(1);
    }

    // -----------------------------------------------------------------------
    // RoomException message check
    // -----------------------------------------------------------------------

    @Test
    public void testRoomException_containsCorrectMessage() {
        RoomService.RoomException ex = new RoomService.RoomException("Test error");
        assertEquals("Test error", ex.getMessage());
    }
}