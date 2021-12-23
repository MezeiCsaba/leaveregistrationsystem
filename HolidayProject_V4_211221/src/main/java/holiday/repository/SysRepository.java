package holiday.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import holiday.entity.SystemParams;

@Repository
public interface SysRepository extends JpaRepository<SystemParams, Long> {

	SystemParams findFirstById(Long Id);

}
