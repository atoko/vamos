package org.atoko.call4code.entrado.service.meta;

import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;
import oshi.SystemInfo;

@Component
public class DeviceService {
    private SystemInfo systemInfo = new SystemInfo();
    private String serial = systemInfo.getHardware().getComputerSystem().getBaseboard().getSerialNumber().replace(" ", "_");

    public DeviceService() {

    }

    {
        if (StringUtils.isEmpty(serial)) {
            serial = systemInfo.getHardware().getNetworkIFs()[0].getMacaddr();
        }
    }

    public String getDeviceId() {
        return serial;
    }
}
