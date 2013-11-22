package org.feuyeux.jaxrs2.atup.device.resource;

import org.apache.log4j.Logger;
import org.feuyeux.jaxrs2.atup.core.constant.AtupApi;
import org.feuyeux.jaxrs2.atup.core.domain.AtupDevice;
import org.feuyeux.jaxrs2.atup.core.rest.AtupRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.core.MediaType;

@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class TSDeviceResource {
    private final static Logger LOGGER = Logger.getLogger(TSDeviceResource.class);
    public static final String BASE_URI = "http://localhost:8080/atup-device/restapi/";

    @Test
    public void testFindAll() {
        AtupRequest<AtupDevice> request = new AtupRequest<AtupDevice>();
        AtupDevice device = request.rest("GET", BASE_URI + AtupApi.DEVICE_PATH + "/1", null, null, MediaType.APPLICATION_JSON_TYPE, AtupDevice.class);
        LOGGER.info(device);
    }
}
