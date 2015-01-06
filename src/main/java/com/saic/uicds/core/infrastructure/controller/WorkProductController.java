package com.saic.uicds.core.infrastructure.controller;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.support.MarshallingView;
import org.springframework.oxm.xmlbeans.XmlBeansMarshaller;
import org.springframework.web.servlet.ModelAndView;

import com.usersmarts.cx.search.Query;
import com.usersmarts.cx.search.Results;
import com.usersmarts.cx.search.SimpleResults;
import com.usersmarts.cx.service.GenericService;
import com.usersmarts.cx.web.RestfulController;
import com.usersmarts.xmf2.Configuration;

import com.saic.uicds.core.infrastructure.model.WorkProduct;
import com.saic.uicds.core.infrastructure.service.WorkProductService;
import com.saic.uicds.core.infrastructure.service.impl.ProductPublicationStatus;
import com.saic.uicds.core.infrastructure.util.WorkProductHelper;

/**
 * IncidentsFeedController
 * 
 * @author Christopher Lakey
 * 
 * @created Dec 11, 2008
 * 
 */
public class WorkProductController extends RestfulController<WorkProduct> {

    @Autowired
    WorkProductService workProductService;

    @PersistenceContext
    EntityManager entityManager;

    public void setEntityManager(EntityManager entityManager) {

        this.entityManager = entityManager;
    }

    // TODO: Add WorkProductModule.class
    Configuration cfg = new Configuration(WorkProductModule.class);

    public WorkProductController() {

    }

    @SuppressWarnings("unchecked")
    @Override
    protected void setView(HttpServletRequest request, HttpServletResponse response,
        ModelAndView modelAndView) throws Exception {

        // this works for any object that maps
        if ("digest".equals(request.getParameter("view"))) {
            MarshallingView view = new MarshallingView(new XmlBeansMarshaller());
            WorkProduct workProduct = (WorkProduct) modelAndView.getModel().get("output");
            modelAndView.getModel().put("digest", workProduct.getDigest());
            view.setModelKey("digest");
            modelAndView.setView(view);
        } else {
            // need to generate the proper work product XmlBean
        }
    }

    @Override
    protected WorkProduct adaptObject(HttpServletRequest request, HttpServletResponse response,
        Object object) throws Exception {

        WorkProduct WorkProduct = (WorkProduct) object;
        return WorkProduct;
    }

    @PostConstruct
    protected void initialize() {

    }

    public void setWorkProductService(WorkProductService wps) {

        setService(new WorkProductGenericService(wps));
    }

    class WorkProductGenericService implements GenericService<WorkProduct> {

        WorkProductService wps;

        public WorkProductGenericService(WorkProductService wps) {

            this.wps = wps;
        }

        @Override
        public int count(Query query) {

            Results<WorkProduct> results = search(query);
            int result = results.getResultSize();
            return result;
        }

        @Override
        public WorkProduct deleteResource(String id) {

            WorkProduct result = wps.getProduct(id);
            if (result == null)
                return null;

            ProductPublicationStatus status;
            // if the product is active then we need to close the product first
            if (result.isActive() == true) {
                status = workProductService.closeProduct(WorkProductHelper.getWorkProductIdentification(result));
                if (status.getStatus().equals(ProductPublicationStatus.SuccessStatus))
                    return null;
            }
            status = workProductService.archiveProduct(WorkProductHelper.getWorkProductIdentification(result));
            if (status.getStatus().equals(ProductPublicationStatus.SuccessStatus))
                return result;
            else
                return null;
        }

        @Override
        public WorkProduct getResource(String id) {

            WorkProduct result = wps.getProduct(id);
            return result;
        }

        @Override
        public WorkProduct postResource(Object representation) {

            WorkProduct workProduct = (WorkProduct) representation;
            wps.publishProduct(workProduct);
            return workProduct;
        }

        @Override
        public WorkProduct putResource(String id, Object representation) {

            // will this associate it with the original work product?
            WorkProduct update = (WorkProduct) representation;
            wps.publishProduct(update);
            return update;
        }

        @Override
        public Results<WorkProduct> search(Query q) {

            return new SimpleResults<WorkProduct>(q);
        }
    }
}
