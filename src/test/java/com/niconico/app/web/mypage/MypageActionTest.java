package com.niconico.app.web.mypage;

import org.dbflute.utflute.lastaflute.mock.TestingHtmlData;
import com.niconico.unit.UnitNiconicoTestCase;
import org.lastaflute.web.response.HtmlResponse;

/**
 * @author jflute
 */
public class MypageActionTest extends UnitNiconicoTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mockLogin();
    }

    public void test_index() {
        // ## Arrange ##
        MypageAction action = new MypageAction();
        inject(action);

        // ## Act ##
        HtmlResponse response = action.index();

        // ## Assert ##
        TestingHtmlData data = validateHtmlData(response);
        log("Recent:");
        data.requiredList("recentProducts", MypageProductBean.class).forEach(bean -> {
            log(bean);
        });
        log("High-Price:");
        data.requiredList("highPriceProducts", MypageProductBean.class).forEach(bean -> {
            log(bean);
        });
    }
}
