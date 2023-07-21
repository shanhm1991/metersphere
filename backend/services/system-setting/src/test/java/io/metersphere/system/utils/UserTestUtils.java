package io.metersphere.system.utils;

import io.metersphere.sdk.controller.handler.ResultHolder;
import io.metersphere.sdk.dto.BasePageRequest;
import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.sdk.service.BaseUserRoleService;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.UserRole;
import io.metersphere.system.dto.UserBatchCreateDTO;
import io.metersphere.system.dto.UserCreateInfo;
import io.metersphere.system.dto.UserRoleOption;
import io.metersphere.system.dto.request.UserEditRequest;
import io.metersphere.system.dto.response.UserImportResponse;
import io.metersphere.system.service.GlobalUserRoleService;
import io.metersphere.utils.JsonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class UserTestUtils {

    //用户管理URL
    public static final String URL_USER_CREATE = "/system/user/add";
    public static final String URL_USER_UPDATE = "/system/user/update";
    public static final String URL_USER_GET = "/system/user/get/%s";
    public static final String URL_USER_PAGE = "/system/user/page";
    public static final String URL_GET_GLOBAL_SYSTEM = "/system/user/get/global/system/role";
    public static final String URL_USER_UPDATE_ENABLE = "/system/user/update/enable";
    public static final String URL_USER_IMPORT = "/system/user/import";
    public static final String URL_USER_DELETE = "/system/user/delete";


    public static <T> T parseObjectFromMvcResult(MvcResult mvcResult, Class<T> parseClass) {
        try {
            String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
            ResultHolder resultHolder = JsonUtils.parseObject(returnData, ResultHolder.class);
            //返回请求正常
            Assertions.assertNotNull(resultHolder);
            return JSON.parseObject(JSON.toJSONString(resultHolder.getData()), parseClass);
        } catch (Exception ignore) {
        }
        return null;
    }

    public static UserBatchCreateDTO getUserCreateDTO(
            List<UserRoleOption> userRoleList,
            List<UserCreateInfo> userInfoList) {
        UserBatchCreateDTO userMaintainRequest = new UserBatchCreateDTO();
        if (CollectionUtils.isNotEmpty(userRoleList)) {
            userMaintainRequest.setUserRoleIdList(
                    userRoleList.stream().map(UserRoleOption::getId).collect(Collectors.toList()));
        }
        userMaintainRequest.setUserInfoList(userInfoList);
        return userMaintainRequest;
    }

    public static UserEditRequest getUserUpdateDTO(UserCreateInfo user, List<UserRoleOption> userRoleList) {
        UserEditRequest returnDTO = new UserEditRequest();
        if (user.getPhone() == null) {
            user.setPhone("");
        }
        BeanUtils.copyBean(returnDTO, user);
        if (CollectionUtils.isNotEmpty(userRoleList)) {
            returnDTO.setUserRoleIdList(
                    userRoleList.stream().map(UserRoleOption::getId).collect(Collectors.toList()));
        }
        return returnDTO;
    }

    public static BasePageRequest getDefaultPageRequest() {
        return new BasePageRequest() {{
            this.setCurrent(1);
            this.setPageSize(10);
        }};
    }

    public static byte[] getFileBytes(String filePath) {
        File file = new File(filePath);
        byte[] buffer = new byte[0];
        FileInputStream fi = null;
        try {
            fi = new FileInputStream(file);
            buffer = new byte[(int) file.length()];
            int offset = 0;
            int numRead = 0;
            while (offset < buffer.length
                    && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
                offset += numRead;
            }
        } catch (Exception ignore) {
        } finally {
            try {
                fi.close();
            } catch (Exception ignore) {
            }

        }
        return buffer;
    }

    public static void compareUserDTO(UserEditRequest editRequest, UserDTO selectUserDTO) {
        Assertions.assertNotNull(editRequest);
        Assertions.assertNotNull(selectUserDTO);
        //判断ID是否一样
        Assertions.assertTrue(StringUtils.equals(editRequest.getId(), selectUserDTO.getId()));
        //判断名称是否一样
        Assertions.assertTrue(StringUtils.equals(editRequest.getName(), selectUserDTO.getName()));
        //判断邮箱是否一样
        Assertions.assertTrue(StringUtils.equals(editRequest.getEmail(), selectUserDTO.getEmail()));
        //判断电话号码是否一样
        Assertions.assertTrue(StringUtils.equals(editRequest.getPhone(), selectUserDTO.getPhone()));
        //判断系统权限是否一样
        List<String> selectUserSystemRoleId
                = selectUserDTO.getUserRoles().stream()
                .filter(item -> (StringUtils.equals(item.getType(), BaseUserRoleService.SYSTEM_TYPE)
                        && StringUtils.equals(item.getScopeId(), GlobalUserRoleService.GLOBAL_SCOPE)))
                .map(UserRole::getId).collect(Collectors.toList());
        Assertions.assertTrue(
                editRequest.getUserRoleIdList().containsAll(selectUserSystemRoleId)
                        && selectUserSystemRoleId.containsAll(editRequest.getUserRoleIdList()));
    }

    public static void checkImportResponse(UserImportResponse responsePost, int successCount, int[] errorDataIndex) {
        //导入总数据是否一致
        Assertions.assertTrue(responsePost.getImportCount() == successCount + errorDataIndex.length);
        //导入成功数据是否一致
        Assertions.assertTrue(responsePost.getSuccessCount() == successCount);
        //报错数据数量是否一致
        Assertions.assertTrue(responsePost.getErrorMessages().size() == errorDataIndex.length);
        //报错数据行编码是否一致
        for (int index : errorDataIndex) {
            Assertions.assertTrue(responsePost.getErrorMessages().containsKey(index));
        }
    }
}