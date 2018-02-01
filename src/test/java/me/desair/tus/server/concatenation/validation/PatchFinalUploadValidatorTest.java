package me.desair.tus.server.concatenation.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.UUID;

import me.desair.tus.server.HttpMethod;
import me.desair.tus.server.exception.PatchOnFinalUploadNotAllowedException;
import me.desair.tus.server.upload.UploadInfo;
import me.desair.tus.server.upload.UploadStorageService;
import me.desair.tus.server.upload.UploadType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

@RunWith(MockitoJUnitRunner.class)
public class PatchFinalUploadValidatorTest {

    private PatchFinalUploadValidator validator;

    private MockHttpServletRequest servletRequest;

    @Mock
    private UploadStorageService uploadStorageService;

    @Before
    public void setUp() {
        servletRequest = new MockHttpServletRequest();
        validator = new PatchFinalUploadValidator();
    }

    @Test
    public void supports() throws Exception {
        assertThat(validator.supports(HttpMethod.GET), is(false));
        assertThat(validator.supports(HttpMethod.POST), is(false));
        assertThat(validator.supports(HttpMethod.PUT), is(false));
        assertThat(validator.supports(HttpMethod.DELETE), is(false));
        assertThat(validator.supports(HttpMethod.HEAD), is(false));
        assertThat(validator.supports(HttpMethod.OPTIONS), is(false));
        assertThat(validator.supports(HttpMethod.PATCH), is(true));
        assertThat(validator.supports(null), is(false));
    }

    @Test
    public void testValid() throws Exception {
        UploadInfo info1 = new UploadInfo();
        info1.setId(UUID.randomUUID());
        info1.setUploadType(UploadType.REGULAR);

        UploadInfo info2 = new UploadInfo();
        info2.setId(UUID.randomUUID());
        info2.setUploadType(UploadType.PARTIAL);

        UploadInfo info3 = new UploadInfo();
        info3.setId(UUID.randomUUID());
        info3.setUploadType(null);

        when(uploadStorageService.getUploadInfo(eq(info1.getId().toString()), anyString())).thenReturn(info1);
        when(uploadStorageService.getUploadInfo(eq(info2.getId().toString()), anyString())).thenReturn(info2);
        when(uploadStorageService.getUploadInfo(eq(info3.getId().toString()), anyString())).thenReturn(info3);

        //When we validate the request
        servletRequest.setRequestURI(info1.getId().toString());
        validator.validate(HttpMethod.PATCH, servletRequest, uploadStorageService, null);

        servletRequest.setRequestURI(info2.getId().toString());
        validator.validate(HttpMethod.PATCH, servletRequest, uploadStorageService, null);

        servletRequest.setRequestURI(info3.getId().toString());
        validator.validate(HttpMethod.PATCH, servletRequest, uploadStorageService, null);
    }

    @Test
    public void testValidNotFound() throws Exception {
        //When we validate the request
        servletRequest.setRequestURI("/upload/test");
        validator.validate(HttpMethod.PATCH, servletRequest, uploadStorageService, null);
    }

    @Test(expected = PatchOnFinalUploadNotAllowedException.class)
    public void testInvalidFinal() throws Exception {
        UploadInfo info1 = new UploadInfo();
        info1.setId(UUID.randomUUID());
        info1.setUploadType(UploadType.CONCATENATED);

        when(uploadStorageService.getUploadInfo(eq(info1.getId().toString()), anyString())).thenReturn(info1);

        //When we validate the request
        servletRequest.setRequestURI(info1.getId().toString());
        validator.validate(HttpMethod.PATCH, servletRequest, uploadStorageService, null);
    }
}