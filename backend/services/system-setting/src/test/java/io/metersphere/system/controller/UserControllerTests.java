package io.metersphere.system.controller;

import com.jayway.jsonpath.JsonPath;
import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.controller.handler.ResultHolder;
import io.metersphere.sdk.dto.BasePageRequest;
import io.metersphere.sdk.dto.UserDTO;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Pager;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.UserBatchCreateDTO;
import io.metersphere.system.dto.response.UserInfo;
import io.metersphere.system.utils.UserTestUtils;
import io.metersphere.utils.JsonUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerTests {
    @Resource
    private MockMvc mockMvc;
    private static String sessionId;
    private static String csrfToken;

    private static final String URL_USER_CREATE = "/user/add";
    private static final String URL_USER_GET = "/user/get/";
    private static final String URL_USER_PAGE = "/user/page";

    private static final ResultMatcher BAD_REQUEST_MATCHER = status().isBadRequest();
    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();

    private static final List<User> USER_LIST = new ArrayList<>();

    //成功入库的用户保存内存中，其他用例会使用到
    private void addUser2List(MvcResult mvcResult) throws Exception {
        String returnData = mvcResult.getResponse().getContentAsString();
        ResultHolder resultHolder = JsonUtils.parseObject(returnData, ResultHolder.class);

        //返回请求正常
        Assertions.assertNotNull(resultHolder);

        UserBatchCreateDTO userMaintainRequest = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), UserBatchCreateDTO.class);

        //返回值不为空
        Assertions.assertNotNull(userMaintainRequest);

        USER_LIST.addAll(userMaintainRequest.getUserInfoList());
    }

    private void checkUserList() throws Exception {
        if (CollectionUtils.isEmpty(USER_LIST)) {
            //测试数据初始化入库
            this.testAddSuccess();
        }
    }

    @BeforeEach
    public void login() throws Exception {
        if (StringUtils.isAnyBlank(sessionId, csrfToken)) {
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/login")
                            .content("{\"username\":\"admin\",\"password\":\"metersphere\"}")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
            sessionId = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.sessionId");
            csrfToken = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.csrfToken");
        }
    }

    private void requestPost(String url, Object param, ResultMatcher resultMatcher) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(param))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher).andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    private MvcResult responsePost(String url, Object param) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(param))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    private MvcResult responseGet(String url) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Test
    @Order(1)
    public void testAddSuccess() throws Exception {
        //模拟前台批量添加用户
        UserBatchCreateDTO userMaintainRequest = UserTestUtils.getSimpleUserCreateDTO();
        MvcResult mvcResult = this.responsePost(URL_USER_CREATE, userMaintainRequest);
        this.addUser2List(mvcResult);

        //含有重复的用户名称
        userMaintainRequest = UserTestUtils.getSimpleUserCreateDTO();
        userMaintainRequest.setUserInfoList(
                new ArrayList<>() {{
                    add(new User() {{
                        setName("tianyang.repeat");
                        setEmail("tianyang.repeat.name.1@126.com");
                        setSource("LOCAL");
                    }});

                    add(new User() {{
                        setName("tianyang.repeat");
                        setEmail("tianyang.repeat.name.2@126.com");
                        setSource("LOCAL");
                    }});
                }}
        );
        mvcResult = this.responsePost(URL_USER_CREATE, userMaintainRequest);
        this.addUser2List(mvcResult);
    }

    @Test
    @Order(2)
    public void testAddError() throws Exception {
        UserBatchCreateDTO userMaintainRequest;
        boolean roleIsEmpty = true;
        boolean organizationIsEmpty = true;
        boolean userIsEmpty = true;
        /*
         * 校验参数不合法的反例
         * 每一次校验，使用getErrorUserCreateDTO方法重新获取参数，避免上一步的参数干扰
         */
        //所有参数都为空
        userMaintainRequest = UserTestUtils.getErrorUserCreateDTO(organizationIsEmpty, roleIsEmpty, userIsEmpty);
        this.requestPost(URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //组织ID为空
        userMaintainRequest = UserTestUtils.getErrorUserCreateDTO(organizationIsEmpty, !roleIsEmpty, !userIsEmpty);
        this.requestPost(URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //用户组ID为空
        userMaintainRequest = UserTestUtils.getErrorUserCreateDTO(!organizationIsEmpty, roleIsEmpty, !userIsEmpty);
        this.requestPost(URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //没有用户
        userMaintainRequest = UserTestUtils.getErrorUserCreateDTO(!organizationIsEmpty, !roleIsEmpty, userIsEmpty);
        this.requestPost(URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //含有不存在的组织
        userMaintainRequest = UserTestUtils.getErrorUserCreateDTO(!organizationIsEmpty, !roleIsEmpty, !userIsEmpty);
        userMaintainRequest.getOrganizationIdList().add(null);
        userMaintainRequest.getOrganizationIdList().add("");
        this.requestPost(URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //含有不存在的用户组
        userMaintainRequest = UserTestUtils.getErrorUserCreateDTO(!organizationIsEmpty, !roleIsEmpty, !userIsEmpty);
        userMaintainRequest.getUserRoleIdList().add(null);
        userMaintainRequest.getUserRoleIdList().add("");
        this.requestPost(URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //含有用户名称为空的数据
        userMaintainRequest = UserTestUtils.getSimpleUserCreateDTO();
        userMaintainRequest.getUserInfoList().add(new User() {{
            setEmail("tianyang.name.empty@126.com");
            setSource("LOCAL");
        }});
        this.requestPost(URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //含有用户邮箱为空的数据
        userMaintainRequest = UserTestUtils.getSimpleUserCreateDTO();
        userMaintainRequest.getUserInfoList().add(new User() {{
            setName("tianyang.email.empty");
            setSource("LOCAL");
        }});
        this.requestPost(URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //用户邮箱不符合标准
        userMaintainRequest = UserTestUtils.getSimpleUserCreateDTO();
        userMaintainRequest.getUserInfoList().add(new User() {{
            setName("用户邮箱放飞自我");
            setEmail("用户邮箱放飞自我");
            setSource("LOCAL");
        }});
        this.requestPost(URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);
        //用户来源为空
        userMaintainRequest = UserTestUtils.getSimpleUserCreateDTO();
        userMaintainRequest.getUserInfoList().add(new User() {{
            setName("tianyang.source.empty");
            setEmail("tianyang.source.empty@126.com");
        }});
        this.requestPost(URL_USER_CREATE, userMaintainRequest, BAD_REQUEST_MATCHER);

        /*
         * 校验业务判断出错的反例 （500 error)
         * 需要保证数据库有正常数据
         */
        this.checkUserList();
        //含有重复的用户邮箱
        userMaintainRequest = UserTestUtils.getErrorUserCreateDTO(!organizationIsEmpty, !roleIsEmpty, !userIsEmpty);
        String firstUserEmail = userMaintainRequest.getUserInfoList().get(0).getEmail();
        userMaintainRequest.getUserInfoList().add(new User() {{
            setName("tianyang.no.error4");
            setEmail(firstUserEmail);
            setSource("LOCAL");
        }});
        this.requestPost(URL_USER_CREATE, userMaintainRequest, ERROR_REQUEST_MATCHER);
        //测试请求参数中含有数据库中已存在的邮箱情况
        userMaintainRequest = UserTestUtils.getErrorUserCreateDTO(!organizationIsEmpty, !roleIsEmpty, userIsEmpty);
        userMaintainRequest.setUserInfoList(
                new ArrayList<>() {{
                    add(new User() {{
                        setName("tianyang.repeat.email.db");
                        setEmail(UserTestUtils.USER_DEFAULT_EMAIL);
                        setSource("LOCAL");
                    }});
                }}
        );
        this.requestPost(URL_USER_CREATE, userMaintainRequest, ERROR_REQUEST_MATCHER);
    }

    @Test
    @Order(3)
    public void testGetByEmailSuccess() throws Exception {
        this.checkUserList();
        String url = URL_USER_GET + UserTestUtils.USER_DEFAULT_EMAIL;
        MvcResult mvcResult = this.responseGet(url);

        String returnData = mvcResult.getResponse().getContentAsString();
        ResultHolder resultHolder = JsonUtils.parseObject(returnData, ResultHolder.class);
        //返回请求正常
        Assertions.assertNotNull(resultHolder);

        UserDTO userDTO = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), UserDTO.class);

        //返回值不为空
        Assertions.assertNotNull(userDTO);

        //返回邮箱等于参数邮箱，且用户名合法
        Assertions.assertEquals(userDTO.getEmail(), UserTestUtils.USER_DEFAULT_EMAIL);
        Assertions.assertNotNull(userDTO.getName());
    }

    @Test
    @Order(3)
    public void testGetByEmailError() throws Exception {
        //测试使用任意参数，不能获取到任何用户信息
        this.checkUserList();
        String url = URL_USER_GET + UUID.randomUUID();
        MvcResult mvcResult = this.responseGet(url);

        String returnData = mvcResult.getResponse().getContentAsString();
        ResultHolder resultHolder = JsonUtils.parseObject(returnData, ResultHolder.class);
        //返回请求正常
        Assertions.assertNotNull(resultHolder);
        //返回值为空
        Assertions.assertNull(resultHolder.getData());
    }

    @Test
    @Order(4)
    public void testPageSuccess() throws Exception {
        BasePageRequest basePageRequest = UserTestUtils.getDefaultPageRequest();
        MvcResult mvcResult = this.responsePost(URL_USER_PAGE, basePageRequest);
        String returnData = mvcResult.getResponse().getContentAsString();
        ResultHolder resultHolder = JsonUtils.parseObject(returnData, ResultHolder.class);
        //返回请求正常
        Assertions.assertNotNull(resultHolder);
        Pager<UserInfo> returnPager = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), basePageRequest.getCurrent());
        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(((List<ApiDefinition>) returnPager.getList()).size() <= basePageRequest.getPageSize());
    }

    @Test
    @Order(4)
    public void testPageError() throws Exception {
        //当前页码不大于0
        BasePageRequest basePageRequest = new BasePageRequest();
        basePageRequest.setPageSize(5);
        this.requestPost(URL_USER_PAGE, basePageRequest, BAD_REQUEST_MATCHER);
        //当前页数不大于5
        basePageRequest = new BasePageRequest();
        basePageRequest.setCurrent(1);
        this.requestPost(URL_USER_PAGE, basePageRequest, BAD_REQUEST_MATCHER);
        //排序字段不合法
        basePageRequest = new BasePageRequest();
        basePageRequest.setCurrent(1);
        basePageRequest.setPageSize(5);
        basePageRequest.setSort(new HashMap<>() {{
            put("SELECT * FROM user", "asc");
        }});
        this.requestPost(URL_USER_PAGE, basePageRequest, BAD_REQUEST_MATCHER);
    }
}