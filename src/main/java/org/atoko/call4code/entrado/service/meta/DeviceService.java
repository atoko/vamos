package org.atoko.call4code.entrado.service.meta;

import org.springframework.stereotype.Component;
import oshi.SystemInfo;

@Component
public class DeviceService {
    private SystemInfo systemInfo = new SystemInfo();
    private String serial = systemInfo.getHardware().getComputerSystem().getBaseboard().getSerialNumber().replace(" ", "_");

    public DeviceService() {

    }

    public String getDeviceId() {
        return serial;
    }
}
