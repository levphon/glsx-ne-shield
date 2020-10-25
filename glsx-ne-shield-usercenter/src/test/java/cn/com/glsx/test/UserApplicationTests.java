package cn.com.glsx.test;

import cn.com.glsx.Application;
import cn.com.glsx.auth.utils.ShieldContextHolder;
import cn.com.glsx.neshield.modules.entity.Department;
import cn.com.glsx.neshield.modules.entity.Role;
import cn.com.glsx.neshield.modules.model.param.OrgTreeSearch;
import cn.com.glsx.neshield.modules.model.param.UserBO;
import cn.com.glsx.neshield.modules.service.MenuService;
import cn.com.glsx.neshield.modules.service.OrganizationService;
import cn.com.glsx.neshield.modules.service.RoleService;
import cn.com.glsx.neshield.modules.service.UserService;
import cn.com.glsx.neshield.modules.service.permissionStrategy.PermissionStrategy;
import com.alibaba.fastjson.JSON;
import com.glsx.plat.common.utils.StringUtils;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.com.glsx.admin.common.constant.UserConstants.RolePermitCastType.getBeanNameByCode;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserApplicationTests {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private MenuService menuService;

    @Resource
    private HashedCredentialsMatcher hcm;

    @Autowired
    private Map<String, PermissionStrategy> permissionStrategyMap;

    @Test
    public void testOrg() {
        OrgTreeSearch search = new OrgTreeSearch();
        search.setOrgName("部");

//        List<Long> superiorIds = organizationService.getSuperiorIdsByName(search.getOrgName());
//        search.setOrgIds(superiorIds);

        List list1 = organizationService.fullOrgTree(search);
        System.out.println(JSON.toJSONString(list1));

        List list2 = organizationService.orgTree(search);
        System.out.println(JSON.toJSONString(list2));
    }

    @Test
    public void testRole() {
        List<Role> list = roleService.getUserRoleList(1L);
        System.out.println(JSON.toJSONString(list));
    }

    @Test
    public void testMenu() {
        List<Long> ids = new ArrayList<>();
        ids.add(1L);
        List list = menuService.getMenuTree(ids);
        System.out.println(JSON.toJSONString(list));
    }

    @Test
    public void testMenuNo() {
        Long no = menuService.generateMenuNo(null);
        System.out.println(no);
    }

    @Test
    public void testUser() {
//        SyntheticUser user = userService.getSyntheticUser();
//        System.out.println(JSON.toJSONString(user));
//        R r = userService.suitableSuperUsers(5L);
//        System.out.println(JSON.toJSON(r));
        userService.addUser(new UserBO().setAccount("rrew").setUsername("桃桃桃").setDepartmentId(1L).setRoleId(1L).setPassword("123456"));
    }

    @Test
    public void testPassword() {
        String password = "123456";
        String salt = StringUtils.generateRandomCode(false, 4);
        SimpleHash hash = new SimpleHash(hcm.getHashAlgorithmName(), password, salt, hcm.getHashIterations());

        //加密入库的密码
        String epassword = hash.toString();

        System.out.println(salt);
        System.out.println(epassword);

        String account = "admin";

        UsernamePasswordToken token = new UsernamePasswordToken(account, password);

        SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(account, epassword, ByteSource.Util.bytes(salt), account);

        boolean matchFlag = hcm.doCredentialsMatch(token, authenticationInfo);

        System.out.println(matchFlag);
    }

}