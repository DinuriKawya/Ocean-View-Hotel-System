package oceanview.test;

import oceanview.dao.BankDAO;
import oceanview.model.Bank;
import oceanview.service.BankService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BankServiceTest {

    private BankDAO     mockDao;
    private BankService service;
    private Bank        validBank;

    @Before
    public void setUp() {
        mockDao  = Mockito.mock(BankDAO.class);
        service  = new BankService(mockDao);

        validBank = new Bank(1, "Bank of Ceylon", true);
    }

    // -----------------------------------------------------------------------
    // getAllBanks
    // -----------------------------------------------------------------------

    @Test
    public void testGetAllBanks_success_returnsList() throws Exception {
        when(mockDao.findAll()).thenReturn(Arrays.asList(validBank));

        List<Bank> result = service.getAllBanks();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Bank of Ceylon", result.get(0).getName());
        verify(mockDao, times(1)).findAll();
    }

    @Test
    public void testGetAllBanks_empty_returnsEmptyList() throws Exception {
        when(mockDao.findAll()).thenReturn(Collections.emptyList());

        List<Bank> result = service.getAllBanks();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test(expected = BankService.BankException.class)
    public void testGetAllBanks_sqlException_throwsBankException() throws Exception {
        when(mockDao.findAll()).thenThrow(new SQLException("DB error"));

        service.getAllBanks();
    }

    @Test
    public void testGetAllBanks_sqlException_correctMessage() throws Exception {
        when(mockDao.findAll()).thenThrow(new SQLException("connection failed"));

        try {
            service.getAllBanks();
            fail("Expected BankException");
        } catch (BankService.BankException e) {
            assertTrue(e.getMessage().contains("DB error"));
        }
    }

    // -----------------------------------------------------------------------
    // getActiveBanks
    // -----------------------------------------------------------------------

    @Test
    public void testGetActiveBanks_success_returnsList() throws Exception {
        when(mockDao.findActive()).thenReturn(Arrays.asList(validBank));

        List<Bank> result = service.getActiveBanks();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());
        verify(mockDao, times(1)).findActive();
    }

    @Test
    public void testGetActiveBanks_empty_returnsEmptyList() throws Exception {
        when(mockDao.findActive()).thenReturn(Collections.emptyList());

        List<Bank> result = service.getActiveBanks();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test(expected = BankService.BankException.class)
    public void testGetActiveBanks_sqlException_throwsBankException() throws Exception {
        when(mockDao.findActive()).thenThrow(new SQLException("DB error"));

        service.getActiveBanks();
    }

    // -----------------------------------------------------------------------
    // getById
    // -----------------------------------------------------------------------

    @Test
    public void testGetById_success_returnsBank() throws Exception {
        when(mockDao.findById(1)).thenReturn(validBank);

        Bank result = service.getById(1);

        assertNotNull(result);
        assertEquals(1, result.getBankId());
        assertEquals("Bank of Ceylon", result.getName());
    }

    @Test(expected = BankService.BankException.class)
    public void testGetById_notFound_throwsBankException() throws Exception {
        when(mockDao.findById(99)).thenReturn(null);

        service.getById(99);
    }

    @Test
    public void testGetById_notFound_correctMessage() throws Exception {
        when(mockDao.findById(99)).thenReturn(null);

        try {
            service.getById(99);
            fail("Expected BankException");
        } catch (BankService.BankException e) {
            assertEquals("Bank #99 not found.", e.getMessage());
        }
    }

    @Test(expected = BankService.BankException.class)
    public void testGetById_sqlException_throwsBankException() throws Exception {
        when(mockDao.findById(1)).thenThrow(new SQLException("DB error"));

        service.getById(1);
    }

    // -----------------------------------------------------------------------
    // createBank
    // -----------------------------------------------------------------------

    @Test
    public void testCreateBank_success_returnsBank() throws Exception {
        when(mockDao.insert(validBank)).thenReturn(1);

        Bank result = service.createBank(validBank);

        assertNotNull(result);
        assertEquals(1, result.getBankId());
        verify(mockDao, times(1)).insert(validBank);
    }

    @Test(expected = BankService.BankException.class)
    public void testCreateBank_nullName_throwsBankException() throws Exception {
        Bank b = new Bank(0, null, true);

        service.createBank(b);
    }

    @Test(expected = BankService.BankException.class)
    public void testCreateBank_blankName_throwsBankException() throws Exception {
        Bank b = new Bank(0, "   ", true);

        service.createBank(b);
    }

    @Test
    public void testCreateBank_nullName_correctMessage() {
        Bank b = new Bank(0, null, true);

        try {
            service.createBank(b);
            fail("Expected BankException");
        } catch (BankService.BankException e) {
            assertEquals("Bank name is required.", e.getMessage());
        }
    }

    @Test(expected = BankService.BankException.class)
    public void testCreateBank_sqlException_throwsBankException() throws Exception {
        when(mockDao.insert(validBank)).thenThrow(new SQLException("DB error"));

        service.createBank(validBank);
    }

    // -----------------------------------------------------------------------
    // updateBank
    // -----------------------------------------------------------------------

    @Test
    public void testUpdateBank_success_returnsBank() throws Exception {
        when(mockDao.update(validBank)).thenReturn(true);

        Bank result = service.updateBank(validBank);

        assertNotNull(result);
        verify(mockDao, times(1)).update(validBank);
    }

    @Test(expected = BankService.BankException.class)
    public void testUpdateBank_nullName_throwsBankException() throws Exception {
        Bank b = new Bank(1, null, true);

        service.updateBank(b);
    }

    @Test(expected = BankService.BankException.class)
    public void testUpdateBank_blankName_throwsBankException() throws Exception {
        Bank b = new Bank(1, "   ", true);

        service.updateBank(b);
    }

    @Test(expected = BankService.BankException.class)
    public void testUpdateBank_sqlException_throwsBankException() throws Exception {
        when(mockDao.update(validBank)).thenThrow(new SQLException("DB error"));

        service.updateBank(validBank);
    }

    // -----------------------------------------------------------------------
    // deleteBank
    // -----------------------------------------------------------------------

    @Test
    public void testDeleteBank_success() throws Exception {
        when(mockDao.delete(1)).thenReturn(true);

        service.deleteBank(1);

        verify(mockDao, times(1)).delete(1);
    }

    @Test(expected = BankService.BankException.class)
    public void testDeleteBank_notFound_throwsBankException() throws Exception {
        when(mockDao.delete(99)).thenReturn(false);

        service.deleteBank(99);
    }

    @Test
    public void testDeleteBank_notFound_correctMessage() throws Exception {
        when(mockDao.delete(99)).thenReturn(false);

        try {
            service.deleteBank(99);
            fail("Expected BankException");
        } catch (BankService.BankException e) {
            assertEquals("Bank #99 not found.", e.getMessage());
        }
    }

    @Test(expected = BankService.BankException.class)
    public void testDeleteBank_sqlException_throwsBankException() throws Exception {
        when(mockDao.delete(1)).thenThrow(new SQLException("DB error"));

        service.deleteBank(1);
    }

    // -----------------------------------------------------------------------
    // BankException
    // -----------------------------------------------------------------------

    @Test
    public void testBankException_message() {
        BankService.BankException ex = new BankService.BankException("Test error");
        assertEquals("Test error", ex.getMessage());
    }

    @Test
    public void testBankException_isException() {
        BankService.BankException ex = new BankService.BankException("error");
        assertTrue(ex instanceof Exception);
    }
}
