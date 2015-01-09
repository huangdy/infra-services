package com.leidos.xchangecore.core.infrastructure.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkProductQueryBuilder
    implements WorkProductConstants {

    private final Logger logger = LoggerFactory.getLogger(WorkProductQueryBuilder.class);

    private static final String Key_InterestGroup = "interestGroup";
    private static final String Key_Username = "req.remoteUser";
    private static final String Key_Full = "full";
    private static final String Key_BBox = "bbox";
    private static final String Key_StartIndex = "startIndex";
    private static final String Key_Count = "count";
    private static final String OP_EQUAL = "=";
    private static final String OP_LE = "<=";
    private static final String OP_GE = ">=";
    private static final String OP_LIKE = "like";

    public static HashMap<String, String[]> paramMap;

    private Double[][] boundingBox = null;
    private final List<Criterion> criterionList = new ArrayList<Criterion>();
    private Order order = Order.desc(C_UpdatedDate);
    private String username = null;
    private int startIndex = 0;
    private int count = -1;
    private boolean full = false;
    private Set<String> iGIDSet = null;

    static {
        paramMap = new HashMap<String, String[]>();
        paramMap.put("productid", new String[] {
            C_ProductID, OP_EQUAL
        });
        paramMap.put("producttypeversion", new String[] {
            C_ProductTypeVersion, OP_LIKE
        });
        paramMap.put("productversion", new String[] {
            C_ProductVersion, OP_EQUAL
        });
        paramMap.put("producttype", new String[] {
            C_ProductType, OP_LIKE
        });
        paramMap.put("productstate", new String[] {
            C_State, OP_LIKE
        });
        paramMap.put("createdbegin", new String[] {
            C_CreatedDate, OP_GE
        });
        paramMap.put("createdend", new String[] {
            C_CreatedDate, OP_LE
        });
        paramMap.put("updatedbegin", new String[] {
            C_UpdatedDate, OP_GE
        });
        paramMap.put("updatedend", new String[] {
            C_UpdatedDate, OP_LE
        });
        paramMap.put("mimetype", new String[] {
            C_Mimetype, OP_LIKE
        });
        /*
        paramMap.put("interestgroup", new String[] {
            C_InterestGroupID, OP_EQUAL
        });
        */
    }

    public WorkProductQueryBuilder() {

    }

    public WorkProductQueryBuilder(Map<String, String[]> params) {

        Set<String> keys = params.keySet();
        for (String key : keys) {
            String[] values = params.get(key);
            if (key.equalsIgnoreCase(Key_InterestGroup)) {
                setIGIDSet(values);
                logger.debug("\tsetInterestGroupID: " + values.length + " entries");
            } else if (key.equalsIgnoreCase(Key_Username)) {
                setUsername(values[0]);
                logger.debug("\tsetUsername: " + getUsername());
            } else if (key.equalsIgnoreCase(Key_Full)) {
                if (values != null && values[0].equalsIgnoreCase("true")) {
                    setFull(true);
                    logger.debug("\tfull: true");
                }
            } else if (key.equalsIgnoreCase(Key_StartIndex)) {
                setStartIndex(getIntegerValue(values[0]));
                logger.debug("\tstartIndex: " + getStartIndex());
            } else if (key.equalsIgnoreCase(Key_Count)) {
                setCount(getIntegerValue(values[0]));
                logger.debug("\tcount: " + getCount());
            } else if (key.equalsIgnoreCase(Key_BBox)) {
                // the coordinates are lon/lat or x/y based on the JTS
                String[] coordArray = values[0].split(",", -1);
                Double[][] coords = new Double[5][2];
                Double west = Double.valueOf(coordArray[0]);
                Double south = Double.valueOf(coordArray[1]);
                Double east = Double.valueOf(coordArray[2]);
                Double north = Double.valueOf(coordArray[3]);

                coords[0][0] = west;
                coords[0][1] = north;
                coords[1][0] = east;
                coords[1][1] = north;
                coords[2][0] = east;
                coords[2][1] = south;
                coords[3][0] = west;
                coords[3][1] = south;
                coords[4][0] = west;
                coords[4][1] = north;

                setBoundingBox(coords);
                logger.debug("\tBoundingBox: " + values[0]);
            } else if (key.endsWith(Key_OrderBy) && values.length == 2) {
                setOrder(values[1].equalsIgnoreCase(Order_Desc) ? Order.desc(values[0]) : Order.asc(values[0]));
                logger.debug("\t" + values[0] + "orderBy: " + values[1]);
            } else if (paramMap.containsKey(key.toLowerCase())) {
                List<Criterion> cList = createCriterionList(key, values);
                if (cList.size() == 1) {
                    criterionList.add(cList.get(0));
                } else {
                    Disjunction or = Restrictions.disjunction();
                    for (Criterion c : cList) {
                        or.add(c);
                    }
                    criterionList.add(or);
                }
            } else {
                logger.warn("Unresolved parameter: [key/value]: [" + key + "/" + values[0] + "]");
            }
        }
    }

    private List<Criterion> createCriterionList(String key, String[] values) {

        String[] ops = paramMap.get(key.toLowerCase());
        List<Criterion> cList = new ArrayList<Criterion>();
        for (String value : values) {
            if (value.length() == 0) {
                continue;
            }
            if (ops[1].equalsIgnoreCase(OP_EQUAL)) {
                cList.add(Restrictions.eq(ops[0],
                    ops[0].equalsIgnoreCase(C_ProductVersion) ? new Integer(value) : value));
                logger.debug("\t" + ops[0] + " = " + value);
            } else if (ops[1].equalsIgnoreCase(OP_LIKE)) {
                cList.add(Restrictions.like(ops[0], value));
                logger.debug("\t" + ops[0] + " like " + value);
            } else if (ops[1].equalsIgnoreCase(OP_LE)) {
                cList.add(Restrictions.le(ops[0], value));
                logger.debug("\t" + ops[0] + " <= " + value);
            } else if (ops[1].equalsIgnoreCase(OP_GE)) {
                cList.add(Restrictions.ge(ops[0], value));
                logger.debug("\t" + ops[0] + " >= " + value);
            } else {
                logger.error("non-processed operation: " + ops[1] + " for " + ops[0]);
            }
        }

        return cList;
    }

    public Double[][] getBoundingBox() {

        return boundingBox;
    }

    public int getCount() {

        return count;
    }

    public List<Criterion> getCriterionList() {

        return criterionList;
    }

    public Set<String> getIGIDSet() {

        return iGIDSet;
    }

    private Integer getIntegerValue(String intString) {

        try {
            return new Integer(Integer.parseInt(intString));
        } catch (Exception e) {
            logger.error(intString + " is invalide integer");
            return new Integer(1);
        }
    }

    public Order getOrder() {

        return order;
    }

    public int getStartIndex() {

        return startIndex;
    }

    public String getUsername() {

        return username;
    }

    public boolean isFull() {

        return full;
    }

    public void setBoundingBox(Double[][] boundingBox) {

        this.boundingBox = boundingBox;
    }

    public void setCount(int count) {

        this.count = count;
    }

    public void setFull(boolean full) {

        this.full = full;
    }

    public void setIGIDSet(String[] iGIDs) {

        iGIDSet = new HashSet<String>();
        for (String iGID : iGIDs) {
            iGIDSet.add(iGID);
        }
    }

    public void setOrder(Order order) {

        this.order = order;
    }

    public void setStartIndex(int startIndex) {

        this.startIndex = startIndex;
    }

    public void setUsername(String username) {

        this.username = username;
    }
}
