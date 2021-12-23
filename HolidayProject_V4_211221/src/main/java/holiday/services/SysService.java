package holiday.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import holiday.entity.SystemParams;
import holiday.repository.SysRepository;

@Service
public class SysService {

	private SysRepository sysRepo;

	@Autowired
	public void setSysRepo(SysRepository sysRepo) {
		this.sysRepo = sysRepo;
	}

	public SystemParams getLastSysParam() {
		final List<SystemParams> sysList = sysRepo.findAll();
		return sysList.isEmpty() ? new SystemParams() : sysRepo.findFirstById(sysList.get(sysList.size() - 1).getId());
	}

	public void save(SystemParams sysparams) {
		sysRepo.save(sysparams);
	}

}
