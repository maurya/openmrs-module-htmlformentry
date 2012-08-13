package org.openmrs.module.htmlformentry.handler;

import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.module.htmlformentry.BadFormDesignException;
import org.openmrs.module.htmlformentry.FormEntryContext;
import org.openmrs.module.htmlformentry.FormEntryContext.Mode;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.htmlformentry.action.FormSubmissionControllerAction;
import org.openmrs.module.htmlformentry.action.RepeatControllerAction;
import org.openmrs.module.htmlformentry.widget.HiddenFieldWidget;
import org.w3c.dom.Node;


public class DynamicRepeatTagHandler extends RepeatControllerAction implements TagHandler, FormSubmissionControllerAction{
	
	HiddenFieldWidget numberOfRepeatsWidget;

	/**
	 * @see org.openmrs.module.htmlformentry.handler.TagHandler#getAttributeDescriptors()
	 */
	@Override
	public List<AttributeDescriptor> getAttributeDescriptors() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.openmrs.module.htmlformentry.handler.TagHandler#doStartTag(org.openmrs.module.htmlformentry.FormEntrySession,
	 *      java.io.PrintWriter, org.w3c.dom.Node, org.w3c.dom.Node)
	 */
	@Override
	public boolean doStartTag(FormEntrySession session, PrintWriter out, Node parent, Node node)
	    throws BadFormDesignException {
		if (session.getContext().getMode() == Mode.VIEW)
			return false;
		numberOfRepeatsWidget = new HiddenFieldWidget();
		numberOfRepeatsWidget.setInitialValue("0");
		session.getContext().registerWidget(numberOfRepeatsWidget);

		session.getContext().beginDynamicRepeat();
		out.println("<div class=\"dynamic-repeat-container\">");
		out.println(numberOfRepeatsWidget.generateHtml(session.getContext()));
		out.println("<div class=\"dynamic-repeat-template\">");
		this.repeatingActions.clear(); //to clear any existing data
		session.getSubmissionController().startRepeat(this);
		return true; // yes, do the children
	}

	/**
	 * @see org.openmrs.module.htmlformentry.handler.TagHandler#doEndTag(org.openmrs.module.htmlformentry.FormEntrySession,
	 *      java.io.PrintWriter, org.w3c.dom.Node, org.w3c.dom.Node)
	 */
	@Override
	public void doEndTag(FormEntrySession session, PrintWriter out, Node parent, Node node) throws BadFormDesignException {
		if (session.getContext().getMode() != Mode.VIEW) {
			out.println("<input type=\"button\" class=\"dynamicRepeat\" value=\"Add\" onClick=\"duplicateTemplate($j(this).parent());\"/></div> <!-- End of Dynamic Repeat --></div> <!-- End of Dynamic Repeat Container -->");
			session.getContext().endDynamicRepeat();
			session.getSubmissionController().endRepeat();
		}
	}

	/**
	 * @see org.openmrs.module.htmlformentry.action.RepeatControllerAction#getNumberOfIterations(org.openmrs.module.htmlformentry.FormEntryContext,
	 *      javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected int getNumberOfIterations(FormEntryContext context, HttpServletRequest submission) {
		return Integer.valueOf(numberOfRepeatsWidget.getValue(context, submission).toString());
	}
	
}
