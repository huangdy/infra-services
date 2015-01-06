package com.saic.uicds.core.infrastructure.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.oasisOpen.docs.wsn.b2.FilterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uicds.directoryServiceData.WorkProductTypeListType;
import org.uicds.resourceProfileService.Interest;
import org.uicds.resourceProfileService.ResourceProfile;
import org.uicds.resourceProfileService.ResourceProfileListType;

import com.saic.precis.x2009.x06.base.CodespaceValueType;
import com.saic.precis.x2009.x06.base.IdentifierType;
import com.saic.uicds.core.infrastructure.dao.ResourceProfileDAO;
import com.saic.uicds.core.infrastructure.model.CodeSpaceValueType;
import com.saic.uicds.core.infrastructure.model.ResourceProfileModel;
import com.saic.uicds.core.infrastructure.service.DirectoryService;
import com.saic.uicds.core.infrastructure.service.ResourceProfileService;
import com.saic.uicds.core.infrastructure.service.WorkProductService;
import com.saic.uicds.core.infrastructure.util.ResourceProfileUtil;
import com.saic.uicds.core.infrastructure.util.ServiceNamespaces;

/**
 * Implements the ResourceProfileService interface.
 * 
 * @see com.saic.uicds.core.infrastructure.model.ResourceProfileModel ResourceProfile Data Model
 * @author Andre Bonner
 * @ssdd
 */
public class ResourceProfileServiceImpl
    implements ResourceProfileService, ServiceNamespaces {

    Logger log = LoggerFactory.getLogger(this.getClass());

    private WorkProductService workProductService;

    private ResourceProfileDAO resourceProfileDAO;

    public void setWorkProductService(WorkProductService workProductService) {

        this.workProductService = workProductService;
    }

    public WorkProductService getWorkProductService() {

        return this.workProductService;
    }

    public void setResourceProfileDAO(ResourceProfileDAO p) {

        this.resourceProfileDAO = p;
    }

    private DirectoryService directoryService;

    public void setDirectoryService(DirectoryService directoryService) {

        this.directoryService = directoryService;
    }

    /**
     * Creates the profile with subscriptions.
     * 
     * @param profile the profile
     * 
     * @return the resource profile model
     * @ssdd
     */
    @Override
    public ResourceProfileModel createProfile(ResourceProfile profile) {

        if (profile == null) {
            log.error("Problem with profile in the request: no profile");
            return null;
        }
        // Create ResourceProfile model and create subscriptions
        ResourceProfileModel profileModel = ResourceProfileUtil.copyProperties(profile);

        // Persist profile to database
        profileModel = resourceProfileDAO.makePersistent(profileModel);

        return profileModel;
    }

    /**
     * Update profile (description and resource typing)
     * 
     * @param request the request
     * 
     * @return the resource profile model
     * @ssdd
     */
    @Override
    public ResourceProfileModel updateProfile(ResourceProfile request) {

        ResourceProfileModel currentModel = resourceProfileDAO.findByIdentifier(request.getID().getStringValue());
        ResourceProfileModel updatedModel = null;
        if (currentModel != null) {
            // currently only allow user to update Description and Resource Typing
            currentModel.setDescription(request.getDescription());

            // resource typing
            if (request.getResourceTyping() != null
                && request.getResourceTyping().sizeOfTypeArray() > 0) {
             //   Map<String, String> mapTyping = new HashMap<String, String>();
            	Set<CodeSpaceValueType> cvts = new HashSet<CodeSpaceValueType>();
            	
                for (CodespaceValueType type : request.getResourceTyping().getTypeArray()) 
                {
                	CodeSpaceValueType newType=new CodeSpaceValueType();
                	newType.setCodeSpace(type.getCodespace());
                	newType.setLabel(type.getLabel());
                	newType.setValue(type.getStringValue());
               //     mapTyping.put(type.getCodespace() + "," + type.getLabel(),
               //         type.getStringValue());
                	cvts.add(newType);
                }
                
               // currentModel.setResourceTyping(mapTyping);
                currentModel.setCvts(cvts);
            }

            // Update a new copy of a profile model with the new profile information
            updatedModel = resourceProfileDAO.makePersistent(currentModel);
        }
        return updatedModel;
    }

    private FilterType getFilterFromInterest(Interest interest) throws XmlException {

        XmlOptions xo = new XmlOptions();
        xo.setSaveInner();
        XmlCursor ic = interest.newCursor();
        FilterType filter = FilterType.Factory.parse(ic.xmlText(xo));
        ic.dispose();
        return filter;
    }

    /**
     * Delete profile.
     * 
     * @param profileID the profile id
     * @ssdd
     */
    @Override
    public void deleteProfile(IdentifierType profileID) {

        // Find the right object
        ResourceProfileModel currentProfile = resourceProfileDAO.findByIdentifier(profileID.getStringValue());
        if (currentProfile == null) {
            log.error("ERROR: profile: " + profileID + " doesn't exist in database");
        } else {
            resourceProfileDAO.makeTransient(currentProfile);
        }
    }

    /**
     * Gets the profile.
     * 
     * @param profileID the profile id
     * 
     * @return the profile
     * @ssdd
     */
    @Override
    public ResourceProfileModel getProfile(IdentifierType profileID) {

        log.debug("Get Profile: " + profileID);
        ResourceProfileModel profileModel = resourceProfileDAO.findByIdentifier(profileID.getStringValue());

        return profileModel;
    }

    /**
     * Adds an interest to a profile
     * 
     * @param profileID the profile id
     * @param interest the interest
     * 
     * @return true, if successful
     * @ssdd
     */
    @Override
    public boolean addInterest(IdentifierType profileID, Interest interest) {

        boolean added = false;
        // Get the profile
        ResourceProfileModel profile = resourceProfileDAO.findByIdentifier(profileID.getStringValue());
        if (profile != null) {
            // if this returns true, then persist the profile
            if (profile.addInterest(ResourceProfileUtil.copyProperties(interest))) {
                added = true;
                resourceProfileDAO.makePersistent(profile);
            }
        }

        return added;
    }

    /**
     * Removes an interest from a profile.
     * 
     * @param profileID the profile id
     * @param it the it
     * 
     * @return true, if successful
     * @ssdd
     */
    @Override
    public boolean removeInterest(IdentifierType profileID, Interest it) {

        boolean removed = false;
        // Get profile
        ResourceProfileModel profile = resourceProfileDAO.findByIdentifier(profileID.getStringValue());
        if (profile != null) {
            if (profile.removeInterest(ResourceProfileUtil.copyProperties(it))) {
                log.debug("Interest removed from profile: " + it.getTopicExpression());
                removed = true;
                resourceProfileDAO.makePersistent(profile);
            }
            // Interests interests = Interests.Factory.newInstance();
            // interests.getInterestArray()[0] = it;
            // Set<InterestElement> list = new HashSet<InterestElement>();
            // ResourceProfileUtil.merge(interests, list);
            // if (profile.removeInterest((InterestElement) list.toArray()[0])) {
            // removed = true;
            // resourceProfileDAO.makePersistent(profile);
            // }
        }
        return removed;
    }

    /**
     * Gets the profile list.
     * 
     * @param queryString the query string
     * 
     * @return the profile list
     * @ssdd
     */
    @Override
    public ResourceProfileListType getProfileList(String queryString) {

        // TODO: use the queryString for finding profiles
        // ignore the queryString and get all the profiles
        ResourceProfileListType response = ResourceProfileListType.Factory.newInstance();

        // List<String> profileIDList = getAllProfileNames();
        List<ResourceProfileModel> profileList = resourceProfileDAO.findAll();
        if (profileList != null)
            // log.debug("profileList size: " + profileList.size());

            if (profileList != null && profileList.size() > 0) {
                ResourceProfile[] profiles = new ResourceProfile[profileList.size()];
                int i = 0;
                for (ResourceProfileModel profileModel : profileList) {
                    ResourceProfile profile = ResourceProfileUtil.copyProperties(profileModel);
                    if (profile != null) {
                        // log.debug("getProfileList, ProfileName: "
                        // + profile.getID().getStringValue());
                        profiles[i++] = profile;
                    }
                }
                response.setResourceProfileArray(profiles);
            }
        return response;

    }

    // private void createSubscription(Interest interest, EndpointReferenceType endpoint) {
    // // Are we interested
    // try {
    // FilterType filter = getFilterFromInterest(interest);
    // } catch (XmlException e) {
    // e.printStackTrace();
    // }
    // }

    public void systemInitializedHandler(String messgae) {

        WorkProductTypeListType publishedProducts = WorkProductTypeListType.Factory.newInstance();
        WorkProductTypeListType subscribedProducts = WorkProductTypeListType.Factory.newInstance();
        directoryService.registerUICDSService(NS_ResourceProfileService,
            RESOURCEPROFILE_SERVICE_NAME, publishedProducts, subscribedProducts);
        init();
    }

    private void init() {

        List<ResourceProfileModel> profiles = resourceProfileDAO.findAll();
        if (profiles != null && profiles.size() > 0) {
            for (ResourceProfileModel profile : profiles) {
                ResourceProfileUtil.copyProperties(profile);
                log.debug("init: send [ " + profile.getId() + " ] to directory service");
            }
        }
    }

    private List<String> getAllProfileNames() {

        List<ResourceProfileModel> profiles = resourceProfileDAO.findAll();
        List<String> ids = new ArrayList<String>();
        if (profiles != null && profiles.size() > 0) {
            for (ResourceProfileModel profile : profiles) {
                ids.add(profile.getIdentifier());
            }
        }
        return ids;
    }
}
