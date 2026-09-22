package com.own.user.party.address.service;

import com.own.user.party.address.domain.BuyerAddress;
import com.own.user.party.address.domain.BuyerAddressGuard;
import com.own.user.party.address.dto.AddressCommand;
import com.own.user.party.address.repository.BuyerAddressGuardRepository;
import com.own.user.party.address.repository.BuyerAddressRepository;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class BuyerAddressServiceTest {
    @Test public void creatingDefaultAddressClearsThePreviousDefault() throws Exception {
        BuyerAddressRepository repository = mock(BuyerAddressRepository.class);
        BuyerAddressGuardRepository guards = guards();
        BuyerAddress old = address(1L, true);
        when(repository.findByBuyerIdOrderByDefaultAddressDescIdDesc(7L)).thenReturn(Collections.singletonList(old));
        when(repository.save(any(BuyerAddress.class))).thenAnswer(invocation -> invocation.getArguments()[0]);
        BuyerAddressService service = new BuyerAddressService(repository, guards);

        service.create(7L, command(true));

        assertFalse(old.getDefaultAddress());
        ArgumentCaptor<BuyerAddress> saved = ArgumentCaptor.forClass(BuyerAddress.class);
        verify(repository, atLeast(2)).save(saved.capture());
        assertTrue(saved.getAllValues().get(saved.getAllValues().size() - 1).getDefaultAddress());
        verify(guards).ensure(7L);
        verify(guards).findByBuyerIdForUpdate(7L);
    }

    @Test public void deletingDefaultPromotesTheMostRecentRemainingAddress() throws Exception {
        BuyerAddressRepository repository = mock(BuyerAddressRepository.class);
        BuyerAddressGuardRepository guards = guards();
        BuyerAddress primary = address(1L, true), next = address(2L, false);
        when(repository.findByIdAndBuyerId(1L, 7L)).thenReturn(primary);
        when(repository.findByBuyerIdOrderByDefaultAddressDescIdDesc(7L)).thenReturn(Arrays.asList(primary, next));
        BuyerAddressService service = new BuyerAddressService(repository, guards);

        service.delete(7L, 1L);

        assertTrue(next.getDefaultAddress());
        verify(repository).delete(primary);
        verify(repository).save(next);
        verify(guards).ensure(7L);
        verify(guards).findByBuyerIdForUpdate(7L);
    }

    @Test public void settingDefaultAcquiresBuyerGuardBeforeChangingAddress() throws Exception {
        BuyerAddressRepository repository = mock(BuyerAddressRepository.class);
        BuyerAddressGuardRepository guards = guards();
        BuyerAddress address = address(3L, false);
        when(repository.findByIdAndBuyerId(3L, 7L)).thenReturn(address);
        when(repository.findByBuyerIdOrderByDefaultAddressDescIdDesc(7L)).thenReturn(Collections.singletonList(address));
        when(repository.save(address)).thenReturn(address);
        BuyerAddressService service = new BuyerAddressService(repository, guards);

        service.setDefault(7L, 3L);

        assertTrue(address.getDefaultAddress());
        verify(guards).ensure(7L);
        verify(guards).findByBuyerIdForUpdate(7L);
    }

    private BuyerAddress address(Long id, boolean primary) throws Exception {
        BuyerAddress address = new BuyerAddress(7L, "张三", "13800138000", "上海", "上海", "浦东", "示例路", primary);
        Field field = BuyerAddress.class.getDeclaredField("id"); field.setAccessible(true); field.set(address, id); return address;
    }
    private BuyerAddressGuardRepository guards() {
        BuyerAddressGuardRepository guards = mock(BuyerAddressGuardRepository.class);
        when(guards.findByBuyerIdForUpdate(7L)).thenReturn(mock(BuyerAddressGuard.class));
        return guards;
    }
    private AddressCommand command(boolean primary) { AddressCommand value = new AddressCommand(); value.setRecipientName("李四"); value.setMobile("13900139000"); value.setProvince("北京"); value.setCity("北京"); value.setDistrict("朝阳"); value.setDetail("示例街"); value.setDefaultAddress(primary); return value; }
}
