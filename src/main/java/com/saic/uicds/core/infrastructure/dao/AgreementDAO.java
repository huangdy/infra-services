/**
 * 
 */
package com.saic.uicds.core.infrastructure.dao;

import java.util.List;

import com.saic.uicds.core.dao.GenericDAO;
import com.saic.uicds.core.infrastructure.model.Agreement;

/**
 * @author summersw
 * 
 */
public interface AgreementDAO
    extends GenericDAO<Agreement, Integer> {

    public Agreement findByRemoteCoreName(String coreName);

    public List<Agreement> getAgreementsWithEnabledRules();

    public boolean isRemoteCoreMutuallyAgreed(String remoteJID);

    void setRemoteCoreMutuallyAgreed(String remoteJID, boolean isMutuallyAgreed);
}
