package org.sysu.workflow;

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
import org.sysu.workflow.dao.RenServiceInfoDAO;

/**
 * Created by Skye on 2019/1/5.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BOEngineApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class DAOTest {

    @Autowired
    private RenServiceInfoDAO renServiceInfoDAO;

    @Before
    public void initData() {
        renServiceInfoDAO.saveOrUpdate(new RenServiceInfo("WFMSComponent_RS_" + 1, "1.1.1.1"));
    }

    @Test
    public void test1() {
        String rsLocation = renServiceInfoDAO.findRSLocation();
        Assert.assertEquals("1.1.1.1", rsLocation);
    }

}
