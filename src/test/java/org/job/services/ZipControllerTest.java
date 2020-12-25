package org.job.services;


import org.job.services.storage.ZipException;
import org.job.services.storage.ZipService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.ByteArrayInputStream;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@AutoConfigureMockMvc
@SpringBootTest
public class ZipControllerTest {

    private final MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt",
            "text/plain", "Spring Framework".getBytes());
    @Autowired
    private MockMvc mvc;

    @MockBean
    private ZipService zipService;

    @Test
    public void shouldZipFile() throws Exception {
        Mockito.doNothing().when(this.zipService).validateFile(Mockito.any());
        given(this.zipService.getMd5SumForFile(Mockito.any()))
                .willReturn("sum");
        given(this.zipService.isContainedInCache(Mockito.any()))
                .willReturn(false);
        Resource resource = Mockito.mock(Resource.class);
        Mockito.when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        given(this.zipService.zip(Mockito.any(), Mockito.any())).willReturn(resource);
        this.mvc.perform(multipart("/").file(multipartFile)).andExpect(MockMvcResultMatchers.status()
                .isOk()).andExpect(header().string("Etag", "sum"));
    }

    @Test
    public void shouldReturnedFileCache() throws Exception {
        Mockito.doNothing().when(this.zipService).validateFile(Mockito.any());
        given(this.zipService.getMd5SumForFile(Mockito.any())).willReturn("sum");
        given(this.zipService.isContainedInCache(Mockito.any())).willReturn(true);
        Resource resource = Mockito.mock(Resource.class);
        Mockito.when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        given(this.zipService.getResourceFromCache(Mockito.any())).willReturn(resource);
        this.mvc.perform(multipart("/").file(multipartFile)).andExpect(MockMvcResultMatchers.status()
                .is(HttpStatus.SEE_OTHER.value())).andExpect(header().string("Etag", "sum"));
    }

    @Test
    public void shouldReturnedNotFound() throws Exception {
        Mockito.doThrow(new ZipException("error")).when(this.zipService).validateFile(Mockito.any());
        this.mvc.perform(multipart("/").file(multipartFile)).andExpect(MockMvcResultMatchers.status()
                .isNotFound());
    }


}
