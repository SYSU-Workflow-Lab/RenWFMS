import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.sysu.renCommon.entity.RenServiceInfo;
import org.sysu.renNameService.RenNameServiceApplication;
import org.sysu.renNameService.dao.RenServiceInfoDAO;

import java.util.List;

/**
 * Created by Skye on 2019/1/5.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RenNameServiceApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class DAOTest {

    @Autowired
    private RenServiceInfoDAO renServiceInfoDAO;

    @Before
    public void initData() {
        for (int i = 1; i < 10; i++) {
            renServiceInfoDAO.saveOrUpdate(new RenServiceInfo(String.valueOf(i), i + "." + i + "." +i + "." + i));
        }
    }

    @Test
    public void test1() {
        List<String> allLocations = renServiceInfoDAO.findAllLocation();
        int[] index = { 1 };
        allLocations.forEach((i) -> {
            String temp = index[0] + "." + index[0] + "." + index[0] + "." + index[0]++;
            Assert.assertEquals(temp, i);
        });
    }

}
