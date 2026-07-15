package com.admin.base.infrastructure.controller;

import com.admin.base.infrastructure.config.common.SysConfig;
import com.admin.base.shared.constant.DownloadType;
import com.admin.base.shared.exception.BusinessException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommonControllerTest {

    @TempDir
    Path tempDir;

    private CommonController controller;

    @BeforeEach
    void setUp() {
        SysConfig config = new SysConfig();
        config.setUploadPath(tempDir.resolve("upload").toString());
        config.setDownloadPath(tempDir.resolve("download").toString());
        config.setLocalStore(tempDir.resolve("local").toString());
        controller = new CommonController();
    }

    @Test
    void fileEndpointsRequireExplicitAuthorities() throws Exception {
        assertAuthority(
                "fileDownload",
                "hasAuthority('sys:file:download') and (!#delete or hasAuthority('sys:file:delete'))",
                String.class, Integer.class, Boolean.class, HttpServletResponse.class);
        assertAuthority("uploadFile", "hasAuthority('sys:file:upload')", MultipartFile.class);
        assertAuthority(
                "resourceDownloadTest",
                "hasAuthority('sys:file:download')",
                String.class, HttpServletResponse.class);
    }

    @Test
    void missingDownloadIsReportedAsBusinessError() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> controller.fileDownload(
                "missing.txt", DownloadType.DOWNLOAD, false, response))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("下载文件失败");
    }

    private void assertAuthority(String methodName, String expression, Class<?>... parameterTypes) throws Exception {
        Method method = CommonController.class.getMethod(methodName, parameterTypes);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo(expression);
    }
}
