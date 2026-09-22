package com.own.user.party.address.repository;
import com.own.user.party.address.domain.BuyerAddress; import java.util.List; import org.springframework.data.jpa.repository.JpaRepository;
public interface BuyerAddressRepository extends JpaRepository<BuyerAddress,Long> { List<BuyerAddress> findByBuyerIdOrderByDefaultAddressDescIdDesc(Long buyerId); BuyerAddress findByIdAndBuyerId(Long id, Long buyerId); }
