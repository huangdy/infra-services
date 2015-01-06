package com.saic.uicds.core.infrastructure.controller;

import java.util.HashMap;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;

import com.saic.uicds.core.infrastructure.service.ISearchService;

public class QueryControllerTest
    extends Assert {

    ISearchService searchService;
    QueryController endpoint = new QueryController();;

    @Before
    public void setup() {

        searchService = org.easymock.EasyMock.createMock(ISearchService.class);
        endpoint.setService(searchService);
        endpoint.setServiceName("exist");
    }

    @Test
    public void testService() throws Exception {

        HashMap<String, String[]> params = new HashMap<String, String[]>();
        String[] format = new String[1];
        format[0] = "xml";
        params.put("format", format);

        Document products = null;
        EasyMock.expect(searchService.searchAsDocument(params)).andReturn(products);
        EasyMock.replay(searchService);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "api/search");
        request.setParameter("format", format);

        MockHttpServletResponse response = new MockHttpServletResponse();
        ModelAndView mav = endpoint.handleRequest(request, response);
        assertNotNull(mav);
        // assertNotNull(mav.getModelMap().get("output"));
    }
}
