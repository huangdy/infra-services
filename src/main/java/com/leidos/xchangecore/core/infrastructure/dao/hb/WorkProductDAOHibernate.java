package com.leidos.xchangecore.core.infrastructure.dao.hb;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.leidos.xchangecore.core.dao.hb.GenericHibernateDAO;
import com.leidos.xchangecore.core.infrastructure.dao.UserInterestGroupDAO;
import com.leidos.xchangecore.core.infrastructure.dao.WorkProductDAO;
import com.leidos.xchangecore.core.infrastructure.model.WorkProduct;
import com.leidos.xchangecore.core.infrastructure.util.DigestHelper;
import com.leidos.xchangecore.core.infrastructure.util.GeometryUtil;
import com.leidos.xchangecore.core.infrastructure.util.WorkProductConstants;
import com.leidos.xchangecore.core.infrastructure.util.WorkProductHelper;
import com.leidos.xchangecore.core.infrastructure.util.WorkProductQueryBuilder;
import com.saic.precis.x2009.x06.base.IdentificationType;
import com.saic.precis.x2009.x06.structures.WorkProductDocument;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

@Transactional
public class WorkProductDAOHibernate
    extends GenericHibernateDAO<WorkProduct, Integer>
    implements WorkProductDAO, WorkProductConstants {

    private final static Logger logger = LoggerFactory.getLogger(WorkProductDAOHibernate.class);

    private UserInterestGroupDAO userInterestGroupDAO;
    private final static String N_WorkProductList = "WorkProductList";
    private final static Order SortByVersion_Desc = Order.desc(C_ProductVersion);
    private final static Order SortByVersion_Asc = Order.asc(C_ProductVersion);
    private final static Order SortByLastUpdated_Desc = Order.desc(C_UpdatedDate);
    private final static Order SortByLastUpdated_Asc = Order.asc(C_UpdatedDate);
    private final static Order SortByProductID_Desc = Order.desc(C_ProductID);
    private final static Order SortByProductID_Asc = Order.asc(C_ProductID);
    private final static Criterion Criterion_State_Active = Restrictions.eq(C_State, State_Active);

    /*
    @Override
    public void delete(Integer id) {

        WorkProduct product = findById(id);
        if (product != null) {
            makeTransient(product);
        }

    }
    */

    @Override
    public void deleteProduct(Integer id) {

        WorkProduct product = findById(id);
        if (product != null) {
            makeTransient(product);
        }
    }

    @Override
    public List<WorkProduct> findAll() {

        return findAll(false);
    }

    @Override
    public List<WorkProduct> findAll(boolean isAscending) {

        List<WorkProduct> productList = findUniquProductList(isAscending ? SortByLastUpdated_Asc
                                                                        : SortByLastUpdated_Desc);
        logger.debug("findAll: " + (isAscending ? Order_Asc
                                               : Order_Desc) + ", found " +
                     (productList == null ? 0
                                         : productList.size()) + " entries");
        return productList == null ? new ArrayList<WorkProduct>()
                                  : productList;
    }

    @Override
    public List<WorkProduct> findAllClosedVersionOfProduct(String productID) {

        logger.debug("findAllClosedVersionOfProduct: " + productID);
        List<Criterion> criterionList = new ArrayList<Criterion>();
        criterionList.add(Restrictions.eq(C_ProductID, productID));
        criterionList.add(Restrictions.eq(C_State, State_Inactive));
        List<Order> orderList = new ArrayList<Order>();
        orderList.add(SortByVersion_Desc);

        List<WorkProduct> productList = findByCriteriaAndOrder(0, orderList, criterionList);
        return productList;
    }

    @Override
    public List<WorkProduct> findAllVersionOfProduct(String productID) {

        logger.debug("findAllVersionOfProduct: " + productID);

        List<Criterion> criterionList = new ArrayList<Criterion>();
        criterionList.add(Restrictions.eq(C_ProductID, productID));
        List<Order> orderList = new ArrayList<Order>();

        List<WorkProduct> productList = findByCriteriaAndOrder(0, orderList, criterionList);
        return productList;
    }

    @Override
    public List<WorkProduct> findByInterestGroup(String interestGroupID) {

        logger.debug("findByInterestGroup: IGID: " + interestGroupID);
        List<Criterion> criterionList = new ArrayList<Criterion>();
        criterionList.add(Restrictions.like(C_AssociatedInterestGroupIDs, "%" + interestGroupID +
                                                                          "%"));
        List<Order> orderList = new ArrayList<Order>();
        orderList.add(SortByProductID_Desc);
        orderList.add(SortByVersion_Desc);
        List<WorkProduct> productList = findByCriteriaAndOrder(0, orderList, criterionList);
        if (productList != null && productList.size() > 0) {
            Hashtable<String, WorkProduct> productHash = new Hashtable<String, WorkProduct>();
            for (WorkProduct product : productList) {
                WorkProduct p = productHash.get(product.getProductID());
                if (p != null) {
                    continue;
                }
                productHash.put(product.getProductID(), product);
            }

            List<WorkProduct> products = new ArrayList<WorkProduct>(productHash.values());
            logger.debug("findByInterestGroup: found " + products.size() + " entries");
            return products;
        } else {
            return productList;
        }
    }

    @Override
    public List<WorkProduct> findByInterestGroupAndType(String interestGroupID, String productType) {

        logger.debug("findByInterestGroupAndType: IGID: " + interestGroupID + ", ProductType: " +
                     productType);
        List<WorkProduct> productList = findUniquProductList(null, Restrictions.eq(C_ProductType,
                                                                                   productType));
        List<WorkProduct> products = new ArrayList<WorkProduct>();
        for (WorkProduct product : productList) {
            if (product.getAssociatedInterestGroupIDs().contains(interestGroupID)) {
                products.add(product);
            }
        }

        return products;
    }

    @Override
    public WorkProduct findByProductID(String productID) {

        logger.debug("findByProductID: ProductID: " + productID);

        List<Order> orders = new ArrayList<Order>();
        orders.add(SortByVersion_Desc);
        List<Criterion> criterions = new ArrayList<Criterion>();
        criterions.add(Restrictions.eq(C_ProductID, productID));

        List<WorkProduct> productList = findByCriteriaAndOrder(0, orders, criterions);

        logger.debug("findByProductID: " + productID + ", found " +
                     (productList == null ? 0
                                         : productList.size()) + " entries");
        if (productList == null || productList.size() == 0) {
            return null;
        }

        return productList.get(0);
    }

    @Override
    public WorkProduct findByProductIDAndVersion(String productID, Integer productVersion) {

        logger.debug("findByProductID: ProductID: " + productID + ", ProductVersion: " +
                     productVersion);

        List<Criterion> criterionList = new ArrayList<Criterion>();
        criterionList.add(Restrictions.eq(C_ProductID, productID));
        criterionList.add(Restrictions.eq(C_ProductVersion, productVersion));
        List<Order> orderList = new ArrayList<Order>();

        List<WorkProduct> productList = findByCriteriaAndOrder(0, orderList, criterionList);
        if (productList == null || productList.size() == 0) {
            return null;
        }

        return productList.get(0);
    }

    @Override
    public List<WorkProduct> findByProductType(String productType) {

        logger.debug("findByProductType: ProductType: " + productType);

        List<WorkProduct> productList = findUniquProductList(null, Restrictions.eq(C_ProductType,
                                                                                   productType));
        logger.debug("findByProductType: " + productType + ", found " +
                     (productList != null ? productList.size()
                                         : 0) + " entries");

        return productList;
    }

    @Override
    public List<Object> findBySearchCritia(Map<String, String[]> params) {

        logger.debug("findBySearchCritia: not implemented");

        return null;
    }

    @Override
    public WorkProduct findByWorkProductIdentification(IdentificationType pkgId) {

        logger.debug("findByWorkProductIdentification: productID: " +
                     pkgId.getIdentifier().getStringValue() + ", productType: " +
                     pkgId.getType().getStringValue() + ", productVersion: " +
                     pkgId.getVersion().getStringValue() + ", checksum: " +
                     pkgId.getChecksum().getStringValue() + ", state: " +
                     pkgId.getState().toString());
        Criterion c1 = Restrictions.eq(C_ProductID, pkgId.getIdentifier().getStringValue());
        Criterion c2 = Restrictions.eq(C_Checksum, pkgId.getChecksum().getStringValue());
        Criterion c3 = Restrictions.eq(C_ProductType, pkgId.getType().getStringValue());
        Criterion c4 = Restrictions.eq(C_State, pkgId.getState().toString());
        Criterion c5 = Restrictions.eq(C_ProductVersion,
                                       Integer.parseInt(pkgId.getVersion().getStringValue()));
        List<WorkProduct> productList = findUniquProductList(null, c1, c2, c3, c4, c5);
        logger.debug("findByWorkProductIdentification found: " + productList.size() + " entries");
        return productList != null ? productList.get(0)
                                  : null;
    }

    @Override
    public Document findDocsBySearchCriteria(Map<String, String[]> params) {

        WorkProductQueryBuilder queryBuilder = new WorkProductQueryBuilder(params);
        String username = queryBuilder.getUsername();
        if (username == null) {
            logger.error("No user specfied");
            return null;
        }

        logger.debug("findDocsBySearchCriteria: for " + username);

        // only find active product
        //c.add(Criterion_State_Active);
        //dsh
        List<Order> orderList = new ArrayList<Order>();
        orderList.add(SortByVersion_Desc);
        orderList.add(SortByProductID_Desc);
        orderList.add(queryBuilder.getOrder());

        List<WorkProduct> products = findByCriteriaAndOrder(queryBuilder.getStartIndex(),
                                                            orderList,
                                                            queryBuilder.getCriterionList());

        Hashtable<String, WorkProduct> productSet = new Hashtable<String, WorkProduct>();
        List<WorkProduct> productList = new ArrayList<WorkProduct>();
        // only save the lastest version either active or not
        for (WorkProduct p : products) {
            if (productSet.containsKey(p.getProductID())) {
                continue;
            }
            productSet.put(p.getProductID(), p);
        }
        productList = new ArrayList<WorkProduct>(productSet.values());

        logger.debug("findDocsBySearchCriteria: found " + productList.size() + " entries");
        if (productList.size() == 0) {
            return null;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            Element results = doc.createElement(N_WorkProductList);
            doc.appendChild(results);

            int cnt = 0;
            for (WorkProduct product : productList) {
                // logger.debug("findDocsBySearchCriteria: found: " + product.getMetadata());
                // filtered by access permission
                String IGID = product.getFirstAssociatedInterestGroupID();
                if (IGID == null && queryBuilder.getIGIDSet() != null) {
                    continue;
                }
                if (IGID != null && queryBuilder.getIGIDSet() != null &&
                    queryBuilder.getIGIDSet().contains(IGID) == false) {
                    // not matching the query IGs
                    // logger.debug("mismatch InterestGroupID: " + queryBuilder.getIGID());
                    continue;
                }
                // if the product has IGID and query string contains IGID the we need to match for eligibilty
                if (IGID != null && getUserInterestGroupDAO().isEligible(username, IGID) == false) {
                    // logger.warn(username + " cannot access " + IGID + " skip product: " + product.getProductID());
                    continue;
                }
                WorkProductDocument.WorkProduct productDocument = null;
                if (queryBuilder.isFull()) {
                    productDocument = WorkProductHelper.toWorkProductDocument(product).getWorkProduct();
                } else {
                    productDocument = WorkProductHelper.toWorkProductSummary(product);
                }
                // logger.debug("insert product: " + productDocument.xmlText());
                // filtered by the bounding box
                if (queryBuilder.getBoundingBox() != null &&
                    intersects(queryBuilder.getBoundingBox(),
                               DigestHelper.getFirstGeometry(WorkProductHelper.getDigestElement(productDocument))) == false) {
                    continue;
                }
                Node node = doc.importNode(productDocument.getDomNode(), true);
                results.appendChild(node);

                if (queryBuilder.getCount() != -1 && queryBuilder.getCount() == ++cnt) {
                    break;
                }
            }

            return doc;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<WorkProduct> findUniquProductList(Order order, Criterion... criterions) {

        // Criteria criteria = getSession().createCriteria(WorkProduct.class);
        List<Criterion> criterionList = new ArrayList<Criterion>();
        for (Criterion c : criterions) {
            criterionList.add(c); // criteria.add(c);
        }

        List<Order> orderList = new ArrayList<Order>();
        if (order != null) {
            if (!order.equals(SortByVersion_Desc) && !order.equals(SortByVersion_Asc)) {
                orderList.add(SortByVersion_Desc); // criteria.addOrder(SortByVersion_Desc);
            }
            if (!order.equals(SortByProductID_Desc) && !order.equals(SortByProductID_Asc)) {
                orderList.add(SortByProductID_Desc); // criteria.addOrder(SortByProductID_Desc);
            }
            orderList.add(order); // criteria.addOrder(order);
        } else {
            orderList.add(SortByVersion_Desc); // criteria.addOrder(SortByVersion_Desc);
            orderList.add(SortByProductID_Desc); // criteria.addOrder(SortByProductID_Desc);
        }

        List<WorkProduct> productList = findByCriteriaAndOrder(0, orderList, criterionList);

        List<WorkProduct> products = new ArrayList<WorkProduct>();
        String productID = null;
        for (WorkProduct p : productList) {
            if (!p.getProductID().equals(productID)) {
                productID = p.getProductID();
                products.add(p);
            }
        }
        return products;
    }

    public UserInterestGroupDAO getUserInterestGroupDAO() {

        return userInterestGroupDAO;
    }

    private boolean intersects(Double[][] boundingBox, Geometry geom) {

        if (geom instanceof Polygon) {
            return GeometryUtil.intersects(boundingBox, (Polygon) geom);
        } else if (geom instanceof Point) {
            return GeometryUtil.contains(boundingBox, (Point) geom);
        }
        return true;
    }

    public void setUserInterestGroupDAO(UserInterestGroupDAO interestGroupDAO) {

        userInterestGroupDAO = interestGroupDAO;
    }
}
