package org.job.services;

import org.apache.commons.io.IOUtils;
import org.job.services.storage.ZipException;
import org.job.services.storage.ZipService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.RandomAccessFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ZipIntegrationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private ZipService zipService;

    @LocalServerPort
    private int port;


    @Test
    public void shouldDownloadFileWith200Code() throws Exception {
        ClassPathResource resource = new ClassPathResource("txt.txt");
        Mockito.doNothing().when(this.zipService).validateFile(Mockito.any());
        given(this.zipService.getMd5SumForFile(Mockito.any())).willReturn("sum");
        given(this.zipService.isContainedInCache(Mockito.any())).willReturn(false);
        given(this.zipService.zip(Mockito.any(), Mockito.any())).willReturn(resource);
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("file", resource);
        ResponseEntity<Resource> response = this.restTemplate
                .postForEntity("/", map, Resource.class);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        byte[] bytesActual = IOUtils.toByteArray(response.getBody().getInputStream());
        byte[] bytesExpected = IOUtils.toByteArray(resource.getInputStream());
        assertThat(bytesActual).isEqualTo(bytesExpected);
    }

    @Test
    public void shouldDownloadFileWith304Code() throws Exception {
        ClassPathResource resource = new ClassPathResource("txt.txt");
        Mockito.doNothing().when(this.zipService).validateFile(Mockito.any());
        given(this.zipService.getMd5SumForFile(Mockito.any())).willReturn("sum");
        given(this.zipService.isContainedInCache(Mockito.any())).willReturn(true);
        given(this.zipService.getResourceFromCache(Mockito.any())).willReturn(resource);
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("file", resource);
        ResponseEntity<Resource> response = this.restTemplate
                .postForEntity("/", map, Resource.class);
        assertThat(response.getStatusCodeValue()).isEqualTo(304);
    }

    @Test
    public void shouldDownloadFileWith413Code() throws Exception {
        ClassPathResource resource = new ClassPathResource("big.txt");
        Mockito.doNothing().when(this.zipService).validateFile(Mockito.any());
        given(this.zipService.getMd5SumForFile(Mockito.any())).willReturn("sum");
        given(this.zipService.isContainedInCache(Mockito.any())).willReturn(false);
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("file", resource);
        ResponseEntity<Resource> response = this.restTemplate
                .postForEntity("/", map, Resource.class);
        assertThat(response.getStatusCodeValue()).isEqualTo(413);
        byte[] bytesActual = IOUtils.toByteArray(response.getBody().getInputStream());
    }

    @Test
    public void shouldReturned404Code() {
        Mockito.doThrow(new ZipException("error")).when(this.zipService).validateFile(Mockito.any());
        ClassPathResource resource = new ClassPathResource("txt.txt");
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("file", resource);
        ResponseEntity<Resource> response = this.restTemplate
                .postForEntity("/", map, Resource.class);
        assertThat(response.getStatusCodeValue()).isEqualTo(404);
    }
}
