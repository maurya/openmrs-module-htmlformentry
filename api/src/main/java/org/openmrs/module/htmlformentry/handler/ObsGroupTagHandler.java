package org.openmrs.module.htmlformentry.handler;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.htmlformentry.FormEntryContext.Mode;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.htmlformentry.HtmlFormEntryUtil;
import org.openmrs.module.htmlformentry.ObsGroupComponent;
import org.openmrs.module.htmlformentry.action.ObsGroupAction;
import org.openmrs.module.htmlformentry.matching.ObsGroupEntity;
import org.openmrs.module.htmlformentry.schema.ObsGroup;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Handles the {@code <obsGroup>} tag
 */
public class ObsGroupTagHandler extends AbstractTagHandler {
   
	boolean unmatchedInd = false;
	
	@Override
    protected List<AttributeDescriptor> createAttributeDescriptors() {
		List<AttributeDescriptor> attributeDescriptors = new ArrayList<AttributeDescriptor>();	
		attributeDescriptors.add(new AttributeDescriptor("groupingConceptId", Concept.class));
		return Collections.unmodifiableList(attributeDescriptors);
	}
	
    /**
     * @see org.openmrs.module.htmlformentry.handler.TagHandler#doStartTag(org.openmrs.module.htmlformentry.FormEntrySession, java.io.PrintWriter, org.w3c.dom.Node, org.w3c.dom.Node)
     */
    @Override
    public boolean doStartTag(FormEntrySession session, PrintWriter out,
            Node parent, Node node) {
        
        Map<String, String> attributes = new HashMap<String, String>();        
        NamedNodeMap map = node.getAttributes();
        for (int i = 0; i < map.getLength(); ++i) {
            Node attribute = map.item(i);
            attributes.put(attribute.getNodeName(), attribute.getNodeValue());
        }
        if (attributes.get("groupingConceptId") == null) {
            throw new NullPointerException("obsgroup tag requires a groupingConceptId attribute");
        }
        Concept groupingConcept = HtmlFormEntryUtil.getConcept(attributes.get("groupingConceptId"));
        if (groupingConcept == null) {
            throw new NullPointerException("could not find concept " + attributes.get("groupingConceptId") + " as grouping obs for an obsgroup tag");
        }
                    
        // avoid lazy init exception
        groupingConcept.getDatatype().getHl7Abbreviation();
               
        String name = attributes.get("label");
        // find relevant obs group to display for this element
        Obs thisGroup = findObsGroup(session, node, attributes.get("groupingConceptId"));
        
        boolean digDeeper = true;
        
        if (thisGroup == null && (session.getContext().getMode() == Mode.EDIT || session.getContext().getMode() == Mode.VIEW)) {
        	if (!session.getContext().isUnmatchedMode()) {
            	unmatchedInd = true;

            	ObsGroupEntity obsGroupEntity = new ObsGroupEntity();
            	obsGroupEntity.setPath(ObsGroupComponent.getObsGroupPath(node));
            	obsGroupEntity.setQuestionsAndAnswers(ObsGroupComponent.findQuestionsAndAnswersForGroup(attributes.get("groupingConceptId"), node));
            	obsGroupEntity.setXmlObsGroupConcept(attributes.get("groupingConceptId"));
            	obsGroupEntity.setGroupingConcept(groupingConcept);
            	obsGroupEntity.setNode(node);
            	int unmatchedObsGroupId = session.getContext().addUnmatchedObsGroupEntities(obsGroupEntity);
                out.print(String.format("<unmatched id=\"%s\" />", unmatchedObsGroupId));        	
                digDeeper = false;
        	}
        } else {
        	unmatchedInd = false;
        }
        
        // sets up the obs group stack, sets current obs group to this one
        ObsGroup ogSchemaObj = new ObsGroup(groupingConcept, name);
        session.getContext().beginObsGroup(groupingConcept, thisGroup, ogSchemaObj);
        //adds the obsgroup action to the controller stack
        session.getSubmissionController().addAction(ObsGroupAction.start(groupingConcept, thisGroup, ogSchemaObj));
        return digDeeper;
    }

    private Obs findObsGroup(FormEntrySession session, Node node, String parentGroupingConceptId) {
        String path = ObsGroupComponent.getObsGroupPath(node);
    	
        if (session.getContext().isUnmatchedMode()) {
            return session.getContext().getNextUnmatchedObsGroup(path);
        } else {
            List<ObsGroupComponent> questionsAndAnswers = ObsGroupComponent.findQuestionsAndAnswersForGroup(parentGroupingConceptId, node);
            return session.getContext().findBestMatchingObsGroup(questionsAndAnswers, parentGroupingConceptId, path);
        }

    }

    @Override
    public void doEndTag(FormEntrySession session, PrintWriter out, Node parent, Node node) {
//                Concept question = null;
//                if (parent != null){
//                    NamedNodeMap attrs = parent.getAttributes();
//                    try {
//                        question = HtmlFormEntryUtil.getConcept(attrs.getNamedItem("groupingConceptId").getNodeValue());
//                    } catch (Exception ex){}    
//                }
                 session.getContext().endObsGroup();
                 session.getSubmissionController().addAction(ObsGroupAction.end());
    }
    
}
